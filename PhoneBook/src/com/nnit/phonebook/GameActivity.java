package com.nnit.phonebook;


import java.lang.reflect.Field;

import com.nnit.phonebook.config.ConfigManager;
import com.nnit.phonebook.game.Block;
import com.nnit.phonebook.game.Game;
import com.nnit.phonebook.game.GameException;
import com.nnit.phonebook.game.IGameListener;
import com.nnit.phonebook.game.Path;
import com.nnit.phonebook.service.BGMusicService;
import com.nnit.phonebook.ui.GameMapView;
import com.nnit.phonebook.util.SoundPlayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class GameActivity extends Activity{
	public static final int SFX_PRESSKEY = 1;
	public static final int SFX_GAMEOVER = 2;
	public static final int SFX_GAMESTART = 3;
	public static final int SFX_GAMEFINISH = 4;
	public static final int SFX_HINT = 5;
	public static final int SFX_BLOCKREMOVE = 6;
	public static final int SFX_BLOCKSELECTED = 7;
	public static final int SFX_TIMEUP = 8;
	
	
	private Game game = null;
	private int rowNumber = 10;
	private int columnNumber = 6;
	private int sameImageCount = 4;
	private int maxHintNumber = 3;
	private int maxTime = 100;
	private int bonusTime = 3;
	
	private Resources resources = null;
	
	private GameMapView gameMapView = null;
	private ImageButton hintButton = null;
	private TextView hintNumberText = null;
	private ProgressBar counterProgress = null;
	private TextView timeValueText = null;
	private ImageButton controlButton = null;
	private TextView controlText = null;
	
	private SoundPlayer soundPlayer = null;
	
	private BGMusicService musicService = null;
	
	private boolean isSFXMute = true;
	private boolean isBGMusicMute = true;
	
	private boolean timeUPPlayed = false;
	
	private ServiceConnection conn = new ServiceConnection(){
		@Override
		public void onServiceDisconnected(ComponentName name){
			musicService = null;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder binder){
			musicService = ((BGMusicService.BGMusicBinder)binder).getService();
		}
	};
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        
		setContentView(R.layout.activity_game);
		
		resources = getResources();
		
		loadConfiguration();
		
		initSFXandMusic();
		
		
		gameMapView = (GameMapView)findViewById(R.id.game_map);
		
		controlButton = (ImageButton)findViewById(R.id.game_imagebtn_control);
		controlText = (TextView)findViewById(R.id.game_textview_control);
		
		hintButton = (ImageButton)findViewById(R.id.game_imagebtn_hint);
		hintNumberText = (TextView)findViewById(R.id.game_textview_hintnumber);
		
		counterProgress =(ProgressBar)findViewById(R.id.game_processbar_counter);
		timeValueText=(TextView)findViewById(R.id.game_textview_counter);
		
		controlText.setText(resources.getString(R.string.game_lable_pause));
		hintNumberText.setText(resources.getString(R.string.game_lable_hint_number) + "(" + (maxHintNumber == -1?"-":Integer.toString(maxHintNumber)) +")");
		
		controlButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				playSFX(SFX_PRESSKEY);
				
				if(game!=null && game.isStarted()){
					if(game.isPaused()){
						game.resume();
						
					}else{
						game.pause();
					}
				}
			}
			
		});
		
		hintButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if(game!=null && game.isRunning()){
					playSFX(SFX_PRESSKEY);
					
					int hintNumber = game.getHintNumber();
					if(hintNumber<=0 && hintNumber!=-1){
						Toast.makeText(GameActivity.this, "No available hint", Toast.LENGTH_SHORT).show();
						return;
					}
					game.getHint();
				}
				
			}
			
		});
		startGame();
	}
	
	private void initSFXandMusic() {
		soundPlayer = new SoundPlayer(this);
		soundPlayer.load(R.raw.key, SFX_PRESSKEY);
		soundPlayer.load(R.raw.gameover, SFX_GAMEOVER);
		soundPlayer.load(R.raw.gamestart, SFX_GAMESTART);
		soundPlayer.load(R.raw.gamefinish, SFX_GAMEFINISH);
		soundPlayer.load(R.raw.hint, SFX_HINT);
		soundPlayer.load(R.raw.blockremove, SFX_BLOCKREMOVE);
		soundPlayer.load(R.raw.blockselected, SFX_BLOCKSELECTED);
		soundPlayer.load(R.raw.timeup, SFX_TIMEUP);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu){
		if(!game.isPaused()){
			game.pause();
		}
		return true;
	}
	
	@Override
	public void onOptionsMenuClosed(Menu menu){
		super.onOptionsMenuClosed(menu);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.game, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.game_menuitem_restart:
			new AlertDialog.Builder(this)
			.setIcon(R.drawable.ic_launcher)
			.setTitle(resources.getString(R.string.game_info_restartconfirm))
			.setPositiveButton(resources.getString(R.string.game_lable_okbtn),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,	int which) {
							restartGame();
						}
					})
			.setNegativeButton(resources.getString(R.string.game_lable_cancelbtn),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							dialog.dismiss();
						}
					}).show();
			break;
		case R.id.game_menuitem_settings:
			showSettingsDialog();
			break;
		case R.id.game_menuitem_quit:
			new AlertDialog.Builder(this)
			.setIcon(R.drawable.ic_launcher)
			.setTitle(resources.getString(R.string.game_info_quitconfirm))
			.setPositiveButton(resources.getString(R.string.game_lable_okbtn),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,	int which) {
							GameActivity.this.finish();
						}
					})
			.setNegativeButton(resources.getString(R.string.game_lable_cancelbtn),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							dialog.dismiss();
						}
					}).show();
			break;
		}
		return true;
	}

	@Override
	protected void onDestroy(){
		if(musicService != null){
			stopPlayBGMusic();
		}
		super.onDestroy();
	}
	
	@Override
	protected void onPause(){
		if(musicService != null){
			pauseBGMusic();
		}
		super.onPause();
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		if(musicService != null){
			resumeBGMusic();
		}
	}

	private void showSettingsDialog() {
		final View dialogView = getLayoutInflater().inflate(R.layout.dialog_gamesettings, null);
    	
		EditText rowsET = (EditText)dialogView.findViewById(R.id.game_settings_rows);
		rowsET.setText(Integer.toString(rowNumber));
		
		EditText columnsET = (EditText)dialogView.findViewById(R.id.game_settings_columns);
		columnsET.setText(Integer.toString(columnNumber));
		
		EditText sameImageCountET = (EditText)dialogView.findViewById(R.id.game_settings_sameimagecount);
		sameImageCountET.setText(Integer.toString(sameImageCount));
		
		EditText hintNumberET = (EditText)dialogView.findViewById(R.id.game_settings_maxhintnumber);
		hintNumberET.setText(Integer.toString(maxHintNumber));
		
		EditText maxTimeET = (EditText)dialogView.findViewById(R.id.game_settings_maxtime);
		maxTimeET.setText(Integer.toString(maxTime));
		
		EditText bonusTimeET = (EditText)dialogView.findViewById(R.id.game_settings_bonustime);
		bonusTimeET.setText(Integer.toString(bonusTime));
		
		
		ToggleButton musicTB = (ToggleButton) dialogView.findViewById(R.id.game_settings_music);
		if(isBGMusicMute){
			musicTB.setChecked(false);
		}else{
			musicTB.setChecked(true);
		}
		
		ToggleButton sfxTB = (ToggleButton) dialogView.findViewById(R.id.game_settings_sfx);
		if(isSFXMute){
			sfxTB.setChecked(false);
		}else{
			sfxTB.setChecked(true);
		}
		
    	Dialog dialog = new AlertDialog.Builder(this)
        	.setIcon(R.drawable.ic_launcher)
        	.setTitle(resources.getString(R.string.game_title_settings))
        	.setView(dialogView)
        	.setPositiveButton(resources.getString(R.string.game_lable_okbtn),new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					try{
						Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
						field.setAccessible(true);  
			            field.set(dialog, false);
			        }catch(Exception e) {
			        	e.printStackTrace();  
			        }
					
					boolean needRestartEffect = false;
					
					EditText maxHintNumberET = (EditText)dialogView.findViewById(R.id.game_settings_maxhintnumber);
					int newMaxHintNumber = Integer.parseInt(maxHintNumberET.getText().toString());
					if(newMaxHintNumber !=-1 && newMaxHintNumber<=0){
						Toast.makeText(GameActivity.this, resources.getString(R.string.game_error_invalid_hintnumber),Toast.LENGTH_SHORT).show();
						return;
					}
					
					if(maxHintNumber != newMaxHintNumber){
						maxHintNumber = newMaxHintNumber;
						needRestartEffect = true;
					}
					
					EditText maxTimeET = (EditText)dialogView.findViewById(R.id.game_settings_maxtime);
					int newMaxTime = Integer.parseInt(maxTimeET.getText().toString());
					if(newMaxTime<=0){
						Toast.makeText(GameActivity.this, resources.getString(R.string.game_error_invalid_maxcountervalue),Toast.LENGTH_SHORT).show();
						return;
					}
					if(maxTime != newMaxTime){
						maxTime = newMaxTime;
						needRestartEffect = true;
					}
					
					EditText bonusTimeET = (EditText)dialogView.findViewById(R.id.game_settings_bonustime);
					int newBonusTime = Integer.parseInt(bonusTimeET.getText().toString());
					if(newBonusTime<=0){
						Toast.makeText(GameActivity.this, resources.getString(R.string.game_error_invalid_counterincreasevalue),Toast.LENGTH_SHORT).show();
						return;
					}
					if(bonusTime != newBonusTime){
						bonusTime = newBonusTime;
						needRestartEffect = true;
					}
					
					EditText rowsET = (EditText)dialogView.findViewById(R.id.game_settings_rows);
					int newRowNumber = Integer.parseInt(rowsET.getText().toString());
					if(rowNumber != newRowNumber){
						rowNumber = newRowNumber;
						needRestartEffect = true;
					}
					
					EditText columnsET = (EditText)dialogView.findViewById(R.id.game_settings_columns);
					int newColumnNumber = Integer.parseInt(columnsET.getText().toString());
					if(columnNumber != newColumnNumber){
						columnNumber = newColumnNumber;
						needRestartEffect = true;
					}
					
					
					EditText sameImageCountET = (EditText)dialogView.findViewById(R.id.game_settings_sameimagecount);
					int newSameImageCount = Integer.parseInt(sameImageCountET.getText().toString());
					if(sameImageCount != newSameImageCount){
						sameImageCount = newSameImageCount;
						needRestartEffect = true;
					}
					
					ToggleButton musicTB = (ToggleButton) dialogView.findViewById(R.id.game_settings_music);
					isBGMusicMute = !musicTB.isChecked();
					
					ToggleButton sfxTB = (ToggleButton) dialogView.findViewById(R.id.game_settings_sfx);
					isSFXMute = !sfxTB.isChecked();
					
					saveConfiguration();
					
					if(needRestartEffect){
					
						new AlertDialog.Builder(GameActivity.this)
							.setIcon(R.drawable.ic_launcher)
							.setTitle(resources.getString(R.string.game_info_settingschangeconfirm))
							.setPositiveButton(resources.getString(R.string.game_lable_yesbtn), new DialogInterface.OnClickListener(){
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									restartGame();
									
								}
							})
							.setNegativeButton(resources.getString(R.string.game_lable_nobtn), new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									
								}
							})
							.show();
					}
					
					try{
						Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
						field.setAccessible(true);  
			            field.set(dialog, true);
			        }catch(Exception e) {
			        	e.printStackTrace();  
			        }
				}

			})
        	.setNegativeButton(resources.getString(R.string.game_lable_exitbtn), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			})
        	.show();
	}

	protected void showSucceedDialog() {
		final View dialogView = getLayoutInflater().inflate(R.layout.dialog_gamesucceed, null);
    	
    	Dialog dialog = new AlertDialog.Builder(this)
        	.setIcon(R.drawable.ic_launcher)
        	.setTitle(resources.getString(R.string.game_title_dialog))
        	.setView(dialogView)
        	.setPositiveButton(resources.getString(R.string.game_lable_restartbtn),new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					restartGame();
				}
			})
        	.setNegativeButton(resources.getString(R.string.game_lable_exitbtn), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					GameActivity.this.finish();
				}
			})
        	.show();
	}
	
	protected void showGameOverDialog() {
		final View dialogView = getLayoutInflater().inflate(R.layout.dialog_gameover, null);
    	
    	Dialog dialog = new AlertDialog.Builder(this)
        	.setIcon(R.drawable.ic_launcher)
        	.setTitle(resources.getString(R.string.game_title_dialog))
        	.setView(dialogView)
        	.setPositiveButton(resources.getString(R.string.game_lable_restartbtn),new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					restartGame();
				}
			})
        	.setNegativeButton(resources.getString(R.string.game_lable_exitbtn), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					GameActivity.this.finish();
				}
			})
        	.show();
	}
	
	private void saveConfiguration() {
		ConfigManager.getInstance().saveConfigure(ConfigManager.CONFIG_GAME_ROWNUMBER, Integer.toString(rowNumber));
		ConfigManager.getInstance().saveConfigure(ConfigManager.CONFIG_GAME_COLUMNNUMBER, Integer.toString(columnNumber));
		ConfigManager.getInstance().saveConfigure(ConfigManager.CONFIG_GAME_SAMEIMAGECOUNT, Integer.toString(sameImageCount));
		ConfigManager.getInstance().saveConfigure(ConfigManager.CONFIG_GAME_MAXHINTNUMBER, Integer.toString(maxHintNumber));
		ConfigManager.getInstance().saveConfigure(ConfigManager.CONFIG_GAME_MAXTIME, Integer.toString(maxTime));
		ConfigManager.getInstance().saveConfigure(ConfigManager.CONFIG_GAME_BONUSTIME, Integer.toString(bonusTime));
		ConfigManager.getInstance().saveConfigure(ConfigManager.CONFIG_GAME_MUSIC_ON, isBGMusicMute?"0":"1");
		ConfigManager.getInstance().saveConfigure(ConfigManager.CONFIG_GAME_SFX_ON, isSFXMute?"0":"1");
	}
	

	private void loadConfiguration() {
		String rowNumberStr = ConfigManager.getInstance().getConfigure(ConfigManager.CONFIG_GAME_ROWNUMBER);
		if(rowNumberStr != null && !rowNumberStr.equals("")){
			rowNumber = Integer.parseInt(rowNumberStr);
		}
		String columnNumberStr = ConfigManager.getInstance().getConfigure(ConfigManager.CONFIG_GAME_COLUMNNUMBER);
		if(columnNumberStr != null && !columnNumberStr.equals("")){
			columnNumber = Integer.parseInt(columnNumberStr);
		}
		String sameImageCountStr = ConfigManager.getInstance().getConfigure(ConfigManager.CONFIG_GAME_SAMEIMAGECOUNT);
		if(sameImageCountStr != null && !sameImageCountStr.equals("")){
			sameImageCount = Integer.parseInt(sameImageCountStr);
		}
		String maxHintNumberStr = ConfigManager.getInstance().getConfigure(ConfigManager.CONFIG_GAME_MAXHINTNUMBER);
		if(maxHintNumberStr != null && !maxHintNumberStr.equals("")){
			maxHintNumber = Integer.parseInt(maxHintNumberStr);
		}
		String maxTimeStr = ConfigManager.getInstance().getConfigure(ConfigManager.CONFIG_GAME_MAXTIME);
		if(maxTimeStr != null && !maxTimeStr.equals("")){
			maxTime = Integer.parseInt(maxTimeStr);
		}
		String bonusTimeStr = ConfigManager.getInstance().getConfigure(ConfigManager.CONFIG_GAME_BONUSTIME);
		if(bonusTimeStr != null && !bonusTimeStr.equals("")){
			bonusTime = Integer.parseInt(bonusTimeStr);
		}
		
		String musicOnStr = ConfigManager.getInstance().getConfigure(ConfigManager.CONFIG_GAME_MUSIC_ON);
		if(musicOnStr != null && (musicOnStr.equals("1")||musicOnStr.equalsIgnoreCase("true"))){
			isBGMusicMute = false;
		}else{
			isBGMusicMute = true;
		}
		
		String sfxOnStr = ConfigManager.getInstance().getConfigure(ConfigManager.CONFIG_GAME_SFX_ON);
		if(sfxOnStr != null && (sfxOnStr.equals("1")||sfxOnStr.equalsIgnoreCase("true"))){
			isSFXMute = false;
		}else{
			isSFXMute = true;
		}
	}
	
	private boolean startGame(){
		
		game = new Game(rowNumber, columnNumber, sameImageCount,maxHintNumber, maxTime, bonusTime);
		
		try {
			game.initGame();
		} catch (GameException e) {
			e.printStackTrace();
			Toast.makeText(this, "Init Game Failed:" + e.getMessage(), Toast.LENGTH_SHORT).show();
			return false;
		}
		
		game.addListener(new IGameListener(){

			@Override
			public void onFinished(Game game) {
				stopPlayBGMusic();
				
				controlButton.setEnabled(false);
				hintButton.setEnabled(false);
				gameMapView.invalidate();
				showSucceedDialog();
				playSFX(SFX_GAMEFINISH);
			}

			@Override
			public void onDeadLock(Game game) {
				game.rerange();
			}

			@Override
			public void onPaused(Game game) {
				pauseBGMusic();
				controlButton.setBackgroundResource(R.layout.selector_btn_resume);
				controlText.setText(resources.getString(R.string.game_lable_resume));
				hintButton.setEnabled(false);
				gameMapView.invalidate();
			}

			@Override
			public void onResumed(Game game) {
				resumeBGMusic();
				if(game.getHintNumber() == -1 ||game.getHintNumber()>0){
					hintButton.setEnabled(true);
				}
				
				controlButton.setBackgroundResource(R.layout.selector_btn_pause);
				controlText.setText(resources.getString(R.string.game_lable_pause));
				gameMapView.invalidate();
			}

			@Override
			public void onStarted(Game game) {
				playSFX(SFX_GAMESTART);
				
				counterProgress.setMax(maxTime);
				timeValueText.setText(resources.getString(R.string.game_lable_counter) +":(" + maxTime +"s)");
				controlButton.setEnabled(true);
				controlButton.setBackgroundResource(R.layout.selector_btn_pause);
				controlText.setText(resources.getString(R.string.game_lable_pause));
				hintButton.setEnabled(true);
				hintNumberText.setText(resources.getString(R.string.game_lable_hint_number) + "(" + (maxHintNumber == -1?"-":Integer.toString(maxHintNumber)) +")");
				gameMapView.invalidate();
				
				startPlayBGMusic();
			}

			@Override
			public void onTimeLeftChanged(Game game, int timeLeft) {
				timeValueText.setText(resources.getString(R.string.game_lable_counter) +":(" + timeLeft +"s)");
				counterProgress.setProgress(timeLeft);
				
				if(!timeUPPlayed && (timeLeft <= maxTime * 0.25)){
					playSFX(SFX_TIMEUP);
					timeUPPlayed = true;
				}
				if(timeUPPlayed && (timeLeft > maxTime * 0.25)){
					timeUPPlayed = false;
				}
				
			}

			@Override
			public void onGameOver(Game game) {
				stopPlayBGMusic();
				controlButton.setEnabled(false);
				hintButton.setEnabled(false);
				gameMapView.invalidate();
				playSFX(SFX_GAMEOVER);
				showGameOverDialog();
			}

			@Override
			public void onStopped(Game game) {
				stopPlayBGMusic();
				controlButton.setEnabled(false);
				hintButton.setEnabled(false);
				gameMapView.invalidate();
				
			}

			@Override
			public void onBlockStateChanged(Game game, int event, Block block) {
				if(event == IGameListener.BLOCK_REMOVED){
					playSFX(SFX_BLOCKREMOVE);
				}else if(event == IGameListener.BLOCK_SELECTED){
					playSFX(SFX_BLOCKSELECTED);
				}
				gameMapView.invalidate();
			}

			@Override
			public void onNewHintPathFound(Game game, Path hintPath) {
				playSFX(SFX_HINT);
				int hintNumber = game.getHintNumber();
				if(hintNumber == 0){
					hintButton.setEnabled(false);
				}
				hintNumberText.setText(resources.getString(R.string.game_lable_hint_number) + "(" + (hintNumber == -1?"-":Integer.toString(hintNumber)) +")");
				gameMapView.invalidate();
			}

			@Override
			public void onGetHintPath(Game game, Path hintPath) {
				gameMapView.invalidate();
			}

		});
		
		
		gameMapView.setGame(game);
		
		timeUPPlayed = false;
		
		game.start();
		
		return true;
	}

	protected void restartGame() {
		if(game.isStarted()){
			game.stop();
		}
		startGame();
	}
	
	private void playSFX(int id){
		if(!isSFXMute){
			soundPlayer.play(id, 0);
		}
	}
	
	private void startPlayBGMusic(){
		if(!isBGMusicMute){
			Intent intent = new Intent();
			intent.setClass(this, BGMusicService.class);
			startService(intent);
			bindService(intent, conn, Context.BIND_AUTO_CREATE);
		}
	}
	
	private void stopPlayBGMusic(){
		if(musicService != null){
			Intent intent = new Intent();
			intent.setClass(this, BGMusicService.class);
			unbindService(conn);
			stopService(intent);
			musicService = null;
		}
	}
	
	private void pauseBGMusic(){
		if(musicService!=null){
			musicService.pauseMusic();
		}
	}
	
	private void resumeBGMusic(){
		if(!isBGMusicMute){
			if(musicService == null){
				startPlayBGMusic();
			}else{
				musicService.resumeMusic();
			}
		}else{
			if(musicService != null){
				stopPlayBGMusic();
			}
		}
	}
	
}

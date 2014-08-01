package com.nnit.phonebook.game;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.nnit.phonebook.GameActivity;
import com.nnit.phonebook.R;
import com.nnit.phonebook.data.DataPackageManager;
import com.nnit.phonebook.data.JSONPBDataSource;
import com.nnit.phonebook.data.PhoneBookItem;
import com.nnit.phonebook.data.PhotoManager;

public class Game {
	
	public static final int MSG_COUNTER = 1;
	
	public static final int GAME_NOTSTART = 0;
	public static final int GAME_FINISHED = 1;
	public static final int GAME_OVER=2;
	
	private static List<PhoneBookItem> phonebookList = null;
	
	
	private GameMap map = null;
	
	private boolean isPaused = false;
	
	private boolean isStarted = false;

	private int gameResult = GAME_NOTSTART;
	
	private List<IGameListener> listeners = new ArrayList<IGameListener>();
	
	private Handler handler = null;
	
	private Runnable counterRunnable = null;
	
	private Block selectedBlock = null;
	
	private Path currentPath = null;
	
	private Path hintPath = null;
	
	private boolean showHint = false;
	
	private int mapRowNumber;
	private int mapColumnNumber;
	private int mapSameImageCount;
	private int maxHintNumber;
	private int hintNumber;
	private int maxTime;
	private int timeLeft;
	private int bonusTime = 10;
	
	private Map<String, Bitmap> bitmapCache = new HashMap<String, Bitmap>();
	
	public Game(int rows, int cols, int sameImageCount, int maxHintNumber, int maxTime, int bonusTime){
		this.mapRowNumber = rows;
		this.mapColumnNumber = cols;
		this.mapSameImageCount = sameImageCount;
		this.maxHintNumber = maxHintNumber;
		this.maxTime = maxTime;
		this.bonusTime = bonusTime;
	}
	
	public int getMapRowNumber() {
		return mapRowNumber;
	}

	public int getMapColumnNumber() {
		return mapColumnNumber;
	}

	public int getMapSameImageCount() {
		return mapSameImageCount;
	}

	public int getMaxHintNumber() {
		return maxHintNumber;
	}

	public int getMaxTime() {
		return maxTime;
	}

	public int getHintNumber() {
		return hintNumber;
	}

	public void setHintNumber(int hintNumber) {
		this.hintNumber = hintNumber;
	}

	public int getTimeLeft() {
		return timeLeft;
	}

	public void setTimeLeft(int timeLeft) {
		this.timeLeft = timeLeft;
	}
	
	

	public GameMap getGameMap(){
		return map;
	}
	
	public Block getSelectedBlock() {
		return selectedBlock;
	}

	public Path getCurrentPath() {
		return currentPath;
	}

	public Path getHintPath() {
		return hintPath;
	}
	

	public void addListener(IGameListener listener){
		listeners.add(listener);
	}
	
	public void removeListener(IGameListener listener){
		listeners.remove(listener);
	}


	public void initGame() throws GameException{
		
		//first check parameter valid
		List<Integer> imageIds = new ArrayList<Integer>();
		
		int blockNumber = (mapRowNumber -2) * (mapColumnNumber - 2);
		
		if(blockNumber %2 != 0){
			throw new GameException("Invalid game settings: the rows x columns should be even");
		}
		
		if(mapSameImageCount %2 != 0){
			throw new GameException("Invalid game settings: the sameImageCount should be even");
		}
		
		map = new GameMap(this, mapRowNumber, mapColumnNumber);
		
		//prepare image ids
		int differentImageCount = blockNumber/mapSameImageCount;
		
		for(int i=0; i<differentImageCount; i++){
			for(int j = 0; j<mapSameImageCount; j++){
				imageIds.add(i);
			}
		}
		
		int imageIdSize = imageIds.size();
		int leftSize = blockNumber - imageIdSize;
		if(leftSize >0){
			for(int i=0; i< leftSize; i++){
				imageIds.add(differentImageCount);
			}
			differentImageCount++;
		}
		
		//get different initials list
		List<String> initialsList = getRandomInitialsList(differentImageCount);
		
		Random r = new Random();
		
		for(int i = 0; i < mapRowNumber; i++){
			for(int j = 0; j < mapColumnNumber; j++){
				Block b = map.getBlock(i, j);
				if(i == 0 || i == mapRowNumber-1 || j == 0|| j == mapColumnNumber-1){
					//for the invisible blocks
					b.setImageId(Block.EMPTY_IMAGEID);
					b.setEmpty(true);
				}else{
					
					int index = r.nextInt(imageIds.size());
					Bitmap bitmap = getBlockImage(initialsList.get(imageIds.get(index)));
					if(bitmap == null){
						throw new GameException("Game initial fail: get bitmap failed");
					}
					b.setBitmap(bitmap);
					b.setImageId(imageIds.get(index));
					b.setEmpty(false);
					
					imageIds.remove(index);
					
				}
			}
		}
		
		hintNumber = maxHintNumber;
		
		timeLeft = maxTime;
		
		
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg){
				switch (msg.what){
				case MSG_COUNTER:
					for(IGameListener listener: listeners){
						listener.onTimeLeftChanged(Game.this, timeLeft);
					}
					break;
				}
				super.handleMessage(msg);
			}
		};
		
		counterRunnable =new Runnable(){
			@Override
			public void run() {
				if(isStarted){
				
					if(!isPaused){
						handler.sendEmptyMessage(MSG_COUNTER);
						timeLeft --;
					}
					if(timeLeft == 0){
						gameOver();
					}else if(timeLeft >0){
						handler.postDelayed(this, 1000);
					}
				}
			}
		};
		
		isStarted = false;
		gameResult = GAME_NOTSTART;
		isPaused = false;
	}
	
	public boolean isStarted() {
		return isStarted;
	}
	
	public boolean isRunning() {
		return isStarted && !isPaused;
	}
	
	public boolean isPaused() {
		return isStarted && isPaused;
	}
	
	public boolean isShowHint(){
		return showHint;
	}
	
	public boolean isGameOver(){
		return !isStarted && (gameResult == GAME_OVER);
	}
	
	public boolean isGameSucceed(){
		return !isStarted && (gameResult == GAME_FINISHED);
	}
	
	public void gameChecking() {
		if(map.isAllBlocksEmpty()){
			gameWin();
		}else if(map.isDeadLock()){
			gameDeadLock();
		}
	}
	
	
	public void rerange() {
		List<Bitmap> bitmaps = new ArrayList<Bitmap>();
		List<Integer> imageIDs = new ArrayList<Integer>();
		
		List<Block> blocks = map.getNotEmptyBlocks();
		for(Block b: blocks){
			bitmaps.add(b.getBitmap());
			imageIDs.add(b.getImageId());
		}
		
		Random r = new Random();
		
		for(Block b2: blocks){
			int index = r.nextInt(bitmaps.size());
			b2.setBitmap(bitmaps.get(index));
			b2.setImageId(imageIDs.get(index));
			bitmaps.remove(index);
			imageIDs.remove(index);
		}
	}
	
	public void pause() {
		if(isStarted){
			isPaused = true;
			for(IGameListener listener: listeners){
				listener.onPaused(this);
			}
		}
	}
	
	public void resume(){
		if(isStarted){
			isPaused = false;
			for(IGameListener listener: listeners){
				listener.onResumed(this);
			}
		}
	}
	
	public void start(){
		isStarted = true;
		
		for(IGameListener listener: listeners){
			listener.onStarted(this);
		}
		
		handler.post(counterRunnable);
	}
	
	public void stop() {
		isStarted = false;
		gameResult = GAME_NOTSTART;
		handler.removeCallbacks(counterRunnable);
		counterRunnable = null;
		
		for(IGameListener listener: listeners){
			listener.onStopped(this);
		}
	}
	
	public void gameOver() {
		isStarted = false;
		gameResult = GAME_OVER;
		
		for(IGameListener listener: listeners){
			listener.onGameOver(this);
		}
	}
	
	public void gameWin(){
		isStarted = false;
		gameResult = GAME_FINISHED;
		for(IGameListener listener: listeners){
			listener.onFinished(this);
		}
	}
	
	public void gameDeadLock(){
		for(IGameListener listener: listeners){
			listener.onDeadLock(this);
		}
	}
	
	public void selectBlock(int row, int column) {
		
		Block newSelectedBlock = map.getBlock(row, column);
		Block oldSelectedBlock = selectedBlock;
		
		if(newSelectedBlock ==null || newSelectedBlock.isEmpty()){
			return;
		}
		showHint = false;
		
		if(selectedBlock == null){
			//select new block
			selectedBlock = newSelectedBlock;
			for(IGameListener l: listeners){
				l.onBlockStateChanged(this, IGameListener.BLOCK_SELECTED, newSelectedBlock);
			}
		}else{
			if(selectedBlock.sameAs(newSelectedBlock)){
				selectedBlock = null;
				for(IGameListener l: listeners){
					l.onBlockStateChanged(this, IGameListener.BLOCK_UNSELECTED, oldSelectedBlock);
				}
			}else{
				//first unselected old block
				selectedBlock = null;
				
				for(IGameListener l: listeners){
					l.onBlockStateChanged(this, IGameListener.BLOCK_UNSELECTED, oldSelectedBlock);
				}
				
				//calculate path
				if(newSelectedBlock.getImageId() == oldSelectedBlock.getImageId()){
					currentPath = map.findPath(oldSelectedBlock, newSelectedBlock);
				}
				
				
				//select new block
				selectedBlock = newSelectedBlock;
				for(IGameListener l: listeners){
					l.onBlockStateChanged(this, IGameListener.BLOCK_SELECTED, newSelectedBlock);
				}
				
			}
			
		}
	}

	public void handleRemoveBlocks() {
		if(currentPath != null){
			Block startBlock = currentPath.getStartBlock();
			Block endBlock = currentPath.getEndBlock();
			currentPath = null;
			selectedBlock = null;
			
			map.removeBlock(startBlock);
			for(IGameListener l: listeners){
				l.onBlockStateChanged(this, IGameListener.BLOCK_REMOVED, startBlock);
			}
			map.removeBlock(endBlock);
			for(IGameListener l: listeners){
				l.onBlockStateChanged(this, IGameListener.BLOCK_REMOVED, endBlock);
			}
			
			increaseTime();
			
			//check game finished
			gameChecking();
		}
	}
	
	public void testPath() {
		Block start = map.getBlock(2, 1);
		Block end = map.getBlock(1, 2);
		
		Log.d("Game","Start Block -- " + start.toString());
		Log.d("Game","End Block -- " + end.toString());
		List<Path> paths = map.findPaths(start, end);
		if(paths.size()>0){
			for(int i=0; i<paths.size(); i++){
				Log.d("Game","Path"+i +":");
				Log.d("Game",paths.get(i).toString());
			}
		}else{
			Log.d("Game","find path failed");
		}
	}

	private Bitmap getBlockImage(String initials) {
		
		if(!bitmapCache.containsKey(initials.toUpperCase())){
			FileInputStream fis = null;
			try{
				String photoFilename = PhotoManager.getInstance().getPhotoFilenameByInitials(initials);
				if(photoFilename == null){
					return null;
				}
				File f = new File(photoFilename);
			
				if(f.exists() && f.isFile()){
					fis = new FileInputStream(f);
					Bitmap bitmap = BitmapFactory.decodeStream(fis);
					bitmapCache.put(initials.toUpperCase(), bitmap);
				}		
				
			}catch(Exception exp){
				exp.printStackTrace();
				return null;
			}finally{
				if(fis != null){
					try {
						fis.close();
					} catch (IOException e) {
					}
				}
			}
		}
		return bitmapCache.get(initials.toUpperCase());
	}

	private List<String> getRandomInitialsList(int size) throws GameException{
		List<String> result = new ArrayList<String>();
		
		loadPhoneBookList();
		
		if(phonebookList.size()<size){
			throw new GameException("Not enough image types");
		}
		
		Random r = new Random();
		int loopCount = size;
		while(loopCount != 0){
			int index = r.nextInt(phonebookList.size());
			String initials = phonebookList.get(index).getInitials();
			if(!result.contains(initials) && hasImage(initials)){
				result.add(initials);
				loopCount--;
			}
		}
		
		return result;
	}

	private void loadPhoneBookList() throws GameException {
		if(phonebookList == null){
			JSONPBDataSource ds =  new JSONPBDataSource();
			ds.setJsonFilePath(DataPackageManager.getInstance().getPhoneBookDataFileAbsolutePath());
		
			try {
				phonebookList = ds.getDataSet().getPBItems();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new GameException(e);
			}
		}
	}

	private boolean hasImage(String initials) {
		if(PhotoManager.getInstance().getPhotoFilenameByInitials(initials) != null){
			return true;
		}else{
			return false;
		}
	}
	
	private void increaseTime() {
		timeLeft += bonusTime;
		
		if(timeLeft > maxTime){
			timeLeft = maxTime;
		}
		
		for(IGameListener listener: listeners){
			listener.onTimeLeftChanged(this, timeLeft);
		}
		
	}

	
	public void getHint() {
		showHint = true;
		
		if(hintNumber == -1){
			//no limited
			selectedBlock = null;
			if(hintPath==null || hintPath.getStartBlock().isEmpty() || hintPath.getEndBlock().isEmpty()){
				hintPath = map.findHintPath();
				for(IGameListener listener: listeners){
					listener.onNewHintPathFound(this, hintPath);
				}
			}
		}else if(hintNumber>0){
			selectedBlock = null;
			if(hintPath==null || hintPath.getStartBlock().isEmpty() || hintPath.getEndBlock().isEmpty()){
				hintPath = map.findHintPath();
				hintNumber--;
				for(IGameListener listener: listeners){
					listener.onNewHintPathFound(this, hintPath);
				}
			}
		}
		
		for(IGameListener listener: listeners){
			listener.onGetHintPath(this, hintPath);
		}
	}

	

	
}

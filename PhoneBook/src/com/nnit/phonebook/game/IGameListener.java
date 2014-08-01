package com.nnit.phonebook.game;

public interface IGameListener {
	public static final int BLOCK_SELECTED = 1;
	public static final int BLOCK_UNSELECTED = 2;
	public static final int BLOCK_REMOVED = 3;
	
	public void onFinished(Game game);
	public void onDeadLock(Game game);
	public void onPaused(Game game);
	public void onResumed(Game game);
	public void onStarted(Game game);
	public void onStopped(Game game);
	public void onTimeLeftChanged(Game game, int timeLeft);
	public void onGameOver(Game game);
	public void onBlockStateChanged(Game game, int event, Block block);
	public void onNewHintPathFound(Game game, Path hintPath);
	public void onGetHintPath(Game game, Path hintPath);
	
	
}

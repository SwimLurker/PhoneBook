package com.nnit.phonebook.game;

public interface IGameMapChangeListener {
	public void blockSelected(Block block);
	public void blockUnSelected(Block block);
	public void blockRemoved(Block block);
}

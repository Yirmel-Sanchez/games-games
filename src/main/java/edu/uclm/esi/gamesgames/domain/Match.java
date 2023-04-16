package edu.uclm.esi.gamesgames.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Match {
	
	private String id;
	private boolean ready;
	private List<String> players;
	private HashMap<String, Board> boards;
	
	public Match() {
		this.id = UUID.randomUUID().toString();
		this.players = new ArrayList<String>();
		this.boards = new HashMap<>();
	}
	public String getId() {
		return this.id;
	}
	
	public boolean isReady() {
		return this.ready;
	}
	
	public void setReady(boolean b) {
		this.ready = ready;
		this.buildBoard();
	}
	
	private void buildBoard() {
		Board board = new Board();
		this.boards.put(this.players.get(0), board);
		this.boards.put(this.players.get(1), board.copy());
		
	}
	
	public void addPlayer(String player) {
		this.players.add(player);
		if(this.players.size() == 2) 
			this.setReady(true);
		
	}
	
	public List<String> getPlayer(){
		return this.players;
	}
	
	public List<Board> getBoards() {
		List<Board> list = new ArrayList<>();
		for (Map.Entry<String, Board> entry : this.boards.entrySet()) {
		    list.add(entry.getValue());
		}
		return list;
	}
}


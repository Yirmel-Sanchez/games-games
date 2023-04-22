package edu.uclm.esi.gamesgames.domain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.JSONObject;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import edu.uclm.esi.gamesgames.ws.Manager;

public class Match {

	private String id;
	private boolean ready;
	private boolean started;
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

	public void setReady(boolean ready) {
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
		if (this.players.size() == 2)
			this.setReady(true);
	}

	public List<String> getPlayer() {
		return this.players;
	}

	public List<Board> getBoards() {
		List<Board> list = new ArrayList<>();
		for (Map.Entry<String, Board> entry : this.boards.entrySet()) {
			list.add(entry.getValue());
		}
		return list;
	}

	public boolean isStarted() {
		return this.started;
	}

	public void setStarted() {
		this.started = true;
	}

	public void notifyStart() {
		for (String player : players) {
			WebSocketSession wsSession = Manager.get().getSessionByUserId(player);
			JSONObject jso = new JSONObject().put("type", "MATCH STARTED").put("matchId", this.id);
			TextMessage message = new TextMessage(jso.toString());
			try {
				wsSession.sendMessage(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void notifyWinner(String winner, String looser) {
		for (String player : players) {
			WebSocketSession wsSession = Manager.get().getSessionByUserId(player);
			JSONObject jso = new JSONObject().put("type", "MATCH FINISHED").put("winner", winner);
			TextMessage message = new TextMessage(jso.toString());
			try {
				wsSession.sendMessage(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void removePlayer(String player) {
		this.players.remove(player);
	}
	
	public String nameOther(String player) {
		if(players.get(0).equals(player)) {
			return players.get(1);
		}else {
			return players.get(0);
		}
	}
}

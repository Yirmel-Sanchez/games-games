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
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "matchs")
public class Match {
	// almacenar en la bd
	@Id @Column(length=36)
	private String id; 
	@Column(length=100)
	private String player1;
	@Column(length=100)
	private String player2;
	@Column(length=100)
	private String winner;
	@Column(length=80)
	private String idBoard1;
	@Column(length=80)
	private String idBoard2;
	
	@Transient
	private boolean ready;
	@Transient
	private boolean started;
	@Transient
	private HashMap<String, Board> boards;

	public Match() {
		this.id = UUID.randomUUID().toString();
		this.boards = new HashMap<>();
		this.player1 = "";
		this.player2 = "";
		this.winner = "";
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
		this.boards.put(this.player1, board);
		this.boards.put(this.player2, board.copy());

	}

	public void addPlayer(String player) {
		if(player1.equals("")) {
			player1 = player;
		} else if(player2.equals("")) {
			player2 = player;
		}
		
		if (!player1.equals("") && !player2.equals(""))
			this.setReady(true);
	}

	public List<String> getPlayer() {
		List<String> listPlayers = new ArrayList<String>();
		if (!player1.equals(""))
			listPlayers.add(player1);
		if (!player2.equals(""))
			listPlayers.add(player2);
		return listPlayers;
	}

	public List<Board> getBoards() {
		List<Board> list = new ArrayList<>();
		list.add(this.boards.get(player1));
		list.add(this.boards.get(player2));
		return list;
	}

	public boolean isStarted() {
		return this.started;
	}

	public void setStarted() {
		this.started = true;
	}

	public void notifyStart() {
		//notificar al jugador 1
		notifyWithBoard("MATCH STARTED", "matchId", this.id, player1);
		
		//notificar al jugador 2
		notifyWithBoard("MATCH STARTED", "matchId", this.id, player2);
	}
	
	private void notifyWithBoard(String typeMessage, String keyMessage, String value, String player) {
		WebSocketSession wsSession = Manager.get().getSessionByUserId(player);
		JSONObject jso = new JSONObject().put("type", typeMessage).put(keyMessage, value);
		
		JSONObject json = new JSONObject();
        for (String key : boards.keySet()) {
            json.put(key, boards.get(key));
        }
		jso.put("boards", json);

		TextMessage message = new TextMessage(jso.toString());
		try {
			wsSession.sendMessage(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void notifyWinner(String winner) {
		//notificar jugador1
		notifyWithBoard("MATCH FINISHED", "winner", winner, player1);
		
		//notificar jugador2
		notifyWithBoard("MATCH FINISHED", "winner", winner, player2);
		
		this.winner = winner;
	}
	
	public void notifyMove(String playerWithBoard) {
		//notificar al jugador 1
		notifyWithBoard("UPDATE BOARDS", "matchId", this.id, player1);
		
		//notificar al jugador 2
		notifyWithBoard("UPDATE BOARDS", "matchId", this.id, player2);
		
		Manager.get().NewMove(playerWithBoard, boards.get(playerWithBoard));
		
	}

	public void removePlayer(String player) {
		if(player1.equals(player)) {
			player1 = "";
		}else if(player2.equals(player)) {
			player2 = "";
		}
	}

	public String nameOther(String player) {
		if (player1.equals(player)) {
			return player2;
		} else {
			return player1;
		}
	}
	
	public void setBoard(String player, Board board) {
		boards.put(player, board);
	}
	
	public void setIdBoards() {
		for (Map.Entry<String, Board> entry : this.boards.entrySet()) {
			if(entry.getKey().equals(player1)) {
				idBoard1 = entry.getValue().getId();
			} else if(entry.getKey().equals(player2)) {
				idBoard2 = entry.getValue().getId();
			}
		}
	}

	public void setWinner(String winner) {
		this.winner = winner;
	}
	
	public String getWinner() {
		return this.winner;
	}

	public String getIdBoard1() {
		return idBoard1;
	}

	public void setIdBoard1(String idBoard1) {
		this.idBoard1 = idBoard1;
	}

	public String getIdBoard2() {
		return idBoard2;
	}

	public void setIdBoard2(String idBoard2) {
		this.idBoard2 = idBoard2;
	}
	
	

}

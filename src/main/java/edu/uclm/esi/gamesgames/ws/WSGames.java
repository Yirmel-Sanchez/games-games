package edu.uclm.esi.gamesgames.ws;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import edu.uclm.esi.gamesgames.domain.Match;
import edu.uclm.esi.gamesgames.services.GamesService;

@Component
public class WSGames extends TextWebSocketHandler {
	private ArrayList<WebSocketSession> sessions = new ArrayList<>();
	
	// Cada objeto representa el WS del cliente
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		this.sessions.add(session);
		String query = session.getUri().toString();
		query = query.substring("ws://localhost:8084/wsGames?".length());
		System.out.println(query);

		String userId = "";
		String idMatch = "";

		if (query != null) {
			String[] params = query.split("&");
			for (String param : params) {
				String[] keyVal = param.split("=");
				if (keyVal.length == 2) {
					String key = keyVal[0];
					String val = keyVal[1];
					if ("nameUser".equals(key)) {
						userId = val;
					} else if ("idMatch".equals(key)) {
						idMatch = val;
					}
				}
			}
		}
		System.out.println("idMatch:" + idMatch);
		System.out.println("nameUser:" + userId);
		Manager.get().addSessionByUserId(userId, session);

		System.out.println("Conexion ws establecida con " + session.getId());
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		String payload = message.getPayload();
		JSONObject jso = new JSONObject(payload);
		String type = jso.getString("type");
		if (type.equals("MOVEMENT")) {
			this.move(jso);
		} else if (type.equals("CHAT")) {
			this.chat(jso);
		} else if (type.equals("BROADCAST")) {
			this.broadcast(jso);
		} else if (type.equals("PLAYER READY")) {
			this.playerReady(jso);
		} else if (type.equals("LEAVE GAME")) {
			this.leaveGame(jso); 
		} else if (type.equals("ADD NUMBERS")) {
			this.addNumbers(jso); 
		} else {
			this.send(session, "type", "ERROR", "message", "Mensaje no reconocido");
		}
	}

	private void addNumbers(JSONObject jso) {
		String matchId = jso.getString("matchId");
		String nameUser = jso.getString("nameUser");
		
		Match match = Manager.get().getMatch(matchId);
		Manager.get().getGamesService().addNumber(match, nameUser); //realizar movimiento si es valido
		
		//comprobarTableroBloqueado
		if(Manager.get().getGamesService().checkBlock(match, nameUser)) { //tablero bloqueado
			String winner = match.nameOther(nameUser);
			Manager.get().finishMatch(matchId, winner); //terminar la partida
		}else { //hay movimientos posibles
			match.notifyMove(nameUser);
		}
		System.out.println("add number to user: "+nameUser);
	}

	private void leaveGame(JSONObject jso) {
		String userId = jso.getString("nameUser");
		String matchId = jso.getString("matchId");
		Match match = Manager.get().getMatch(matchId);
		String winner = match.nameOther(userId);
		Manager.get().leaveMatch(matchId, winner);
		
	}

	private void send(WebSocketSession session, String... tv) {
		JSONObject jso = new JSONObject();
		for (int i = 0; i < tv.length; i++) {
			jso.put(tv[i], tv[i + 1]);
		}
		TextMessage message = new TextMessage(jso.toString());
		try {
			session.sendMessage(message);
		} catch (IOException e) {
			this.sessions.remove(session);
		}
	}

	private void chat(JSONObject jso) {
		// TODO Auto-generated method stub
	}

	private void move(JSONObject jso) {
		String userId = jso.getString("nameUser");
		String matchId = jso.getString("matchId");
		String move = jso.getString("move");
		
		Match match = Manager.get().getMatch(matchId);
		Manager.get().getGamesService().isValidMove(match, userId, move); //realizar movimiento si es valido
		
		if(Manager.get().getGamesService().emptyBoard(match, userId)) {//tablero vacio
			Manager.get().finishMatch(matchId, userId); //terminar la partida
		}else {//tablero con numeros
			match.notifyMove(userId);
		}
		System.out.println("user: "+userId+", move: "+ move);

	}

	private void broadcast(JSONObject jso) {
		TextMessage message = new TextMessage(jso.getString("message"));
		for (WebSocketSession client : this.sessions) {
			Runnable r = new Runnable() {

				@Override
				public void run() {
					try {
						client.sendMessage(message);
					} catch (IOException e) {
						WSGames.this.sessions.remove(client);
					}
				}
			};
			new Thread(r).start();
		}
	}

	private void playerReady(JSONObject jso) {
		String matchId = jso.getString("idMatch");
		Match match = Manager.get().getMatch(matchId);
		if (match != null && !match.isStarted()) {
			match.setStarted();
			match.notifyStart();
			Manager.get().saveMatch(match, "");
		}
	}

	@Override
	protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {

	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {

	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		//Manager.get().closeSesion(session.getId());
		this.sessions.remove(session);
		JSONObject jso = new JSONObject();
		jso.put("type", "BYE");
		jso.put("message", "Un usuario se ha ido");
		this.broadcast(jso);
	}
}

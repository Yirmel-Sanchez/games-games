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
	
	/*********************************************************************
	*
	* Method name: afterConnectionEstablished
	* Description of the Method: Este método se encarga de establecer una conexión websocket, añade la sesión a la lista de 
	* sesiones y realiza el procesamiento de los parámetros que se reciben a través de la URI para obtener el id del usuario 
	* y el id del partido. A continuación, agrega la sesión a la lista de sesiones para el usuario correspondiente y muestra
	*  un mensaje de conexión establecida
	* 
	*********************************************************************/
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
	
	/*********************************************************************
	*
	* Method name: handleTextMessage
	* Description of the Method: Este método se encarga de procesar los mensajes de texto recibidos en la conexión websocket,
	* identifica el tipo de mensaje a través de un campo "type" en el JSON recibido y ejecuta el método correspondiente a ese
	* tipo de mensaje.
	* 
	*********************************************************************/
	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		String payload = message.getPayload();
		JSONObject jso = new JSONObject(payload);
		String type = jso.getString("type");
		if (type.equals("MOVEMENT")) {
			this.move(jso);
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
	
	/*********************************************************************
	*
	* Method name: addNumbers
	* Description of the Method: Este método se encarga de procesar el mensaje de tipo "ADD NUMBERS" que se recibe a través de
	* la conexión websocket. El mensaje contiene el id del partido y el nombre del usuario que está realizando el movimiento.
	* El método agrega el número a la lista de números del usuario en el partido y verifica si el tablero está bloqueado. 
	* Si está bloqueado, termina el partido y notifica al ganador. Si no está bloqueado, notifica al siguiente usuario que es su turno.
	* 
	*********************************************************************/
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
	
	/*********************************************************************
	*
	* Method name: leaveGame
	* Description of the Method: Este método se encarga de procesar el mensaje de tipo "LEAVE GAME" que se recibe a través de 
	* la conexión websocket. El mensaje contiene el id del partido y el nombre del usuario que está abandonando el partido. 
	* El método actualiza el estado del partido y notifica al ganador.
	* 
	*********************************************************************/
	private void leaveGame(JSONObject jso) {
		String userId = jso.getString("nameUser");
		String matchId = jso.getString("matchId");
		Match match = Manager.get().getMatch(matchId);
		String winner = match.nameOther(userId);
		Manager.get().leaveMatch(matchId, winner);
		
	}
	
	/*********************************************************************
	*
	* Method name: send
	* Description of the Method: Este método se encarga de enviar un mensaje a través de la conexión websocket. El mensaje se construye a partir de
	* una serie de pares de clave-valor que se pasan como argumentos al método. El mensaje se envía a la sesión correspondiente y si ocurre algún error
	* al enviar el mensaje, se elimina la sesión de la lista de sesiones.
	* 
	*********************************************************************/
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
	
	/*********************************************************************
	*
	* - Method name: move
	* - Description of the Method: Este método se encarga de realizar el movimiento de un jugador en una partida determinada, 
	* verificando primero si es un movimiento válido. Si el tablero queda vacío después del movimiento, se termina la partida, 
	* de lo contrario se notifica el movimiento al otro jugador.
	* 
	*********************************************************************/
	
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
	
	/*********************************************************************
	*
	* - Method name: broadcast
	* - Description of the Method: Este método se encarga de enviar un mensaje a todos los clientes conectados al servidor.
	*********************************************************************/
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
	
	/*********************************************************************
	*
	* - Method name: playerReady
	* - Description of the Method: Este método se encarga de iniciar una partida en el momento en que ambos jugadores están 
	* listos para jugar. Si ambos jugadores están listos y la partida aún no ha comenzado, se establece el estado de la partida
	* a "iniciada" y se notifica a ambos jugadores para comenzar.
	*  
	*********************************************************************/
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
	
	/*********************************************************************
	*
	* - Method name: afterConnectionClosed
	* - Description of the Method: Este método se llama después de que se haya cerrado una conexión WebSocket y se encarga de
	*  realizar las tareas necesarias cuando un usuario se desconecta. En concreto, cierra la sesión, elimina la sesión de la
	*  lista de sesiones y envía un mensaje de despedida a todos los usuarios conectados mediante el método broadcast.
	*
	*********************************************************************/
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

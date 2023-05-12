package edu.uclm.esi.gamesgames.ws;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import edu.uclm.esi.gamesgames.dao.BoardDAO;
import edu.uclm.esi.gamesgames.dao.MatchDAO;
import edu.uclm.esi.gamesgames.domain.Board;
import edu.uclm.esi.gamesgames.domain.Match;
import edu.uclm.esi.gamesgames.services.GamesService;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class Manager {

	@Autowired
	private GamesService gamesService;
	@Autowired
	private BoardDAO boardDAO;
	@Autowired
	private MatchDAO matchDAO;
	
	private ConcurrentHashMap<String, WebSocketSession> sessionsByUserId;
	private ConcurrentHashMap<String, WebSocketSession> sessionsByWsId;
	private ConcurrentHashMap<String, Match> matches;
	private ConcurrentHashMap<String, Board> boards;
	private ArrayList<String> playersInMatch;

	private Manager() {
		this.sessionsByUserId = new ConcurrentHashMap<>();
		this.sessionsByWsId = new ConcurrentHashMap<>();
		this.matches = new ConcurrentHashMap<>();
		this.boards = new ConcurrentHashMap<>();
		this.playersInMatch = new ArrayList<>();
	}

	private static class ManagerHolder {
		static Manager singleton = new Manager();
	}

	@Bean
	public static Manager get() {
		return ManagerHolder.singleton;
	}

	public GamesService getGamesService() {
		return gamesService;
	}
	
	public void addPlayerInMatch(String user) {
		this.playersInMatch.add(user);
	}
	
	public void removePlayerInMatch(String user) {
		this.playersInMatch.remove(user);
	}
	
	public boolean isPlayerInMatch(String user) {
		return this.playersInMatch.contains(user);
	}
	
	public void addSessionByUserId(String userId, WebSocketSession Session) {
		this.sessionsByUserId.put(userId, Session);
		System.out.println("user: "+userId+" wsId: "+this.sessionsByUserId.get(userId).getId());
		
	}
	
	public WebSocketSession getSessionByUserId(String userId) {
		//System.out.println("user: "+userId+"wsId: "+this.sessionsByUserId.get(userId));
		return this.sessionsByUserId.get(userId);
	}
	
	public WebSocketSession removeSessionByUserId(String userId) {
		WebSocketSession wsSession = this.sessionsByUserId.remove(userId);
		this.sessionsByWsId.remove(wsSession.getId());
		return wsSession;
	}
	
	public void addSessionByWsId(WebSocketSession httpSession) {
		this.sessionsByWsId.put(httpSession.getId(), httpSession);
	}
	
	public WebSocketSession getSessionByWsId(String wsId) {
		return this.sessionsByWsId.get(wsId);
	}
	
	/*********************************************************************
	*
	* - Nombre del método: addMatch
	* - Descripción del método: Este método añade un nuevo objeto de tipo Match al mapa de partidas (matches). Además, establece las relaciones entre el objeto Match y sus tableros correspondientes, actualiza el historial de tableros y los guarda en la base de datos.
	*
	*********************************************************************/
	public void addMatch(Match match) {
		this.matches.put(match.getId(), match);
		
		//añadir los board al historial de boards
		String idNodo0 = match.getId()+"-0-0";
		String idNodo1 = match.getId()+"-1-0";
		Board b0 = match.getBoards().get(0);
		Board b1 = match.getBoards().get(1);
		b0.setParentMove("", idNodo0);
		b1.setParentMove("", idNodo1);
		match.setBoard(match.getPlayer().get(0), b0);
		match.setBoard(match.getPlayer().get(1), b1);
		
		this.boards.put(match.getPlayer().get(0), b0);
		this.boards.put(match.getPlayer().get(1), b1);
		
		boardDAO.save(b0);
		boardDAO.save(b1);
		//guardar match inicial
	}
	
	/*********************************************************************
	*
	* - Nombre del método: getMatch
	* - Descripción del método: Este método recibe un String que es el id de una partida, busca la partida correspondiente en 
	* el mapa de partidas (matches) y devuelve el objeto Match correspondiente.
	* 
	*********************************************************************/
	public Match getMatch(String matchId) {
		return this.matches.get(matchId);
	}
	
	/*********************************************************************
	*
	* - Nombre del método: removeMatch
	* - Descripción del método: Este método recibe un String que es el id de una partida, busca la partida correspondiente en 
	* el mapa de partidas (matches), la elimina del mapa y devuelve el objeto Match correspondiente.
	* 
	*********************************************************************/
	public Match removeMatch(String matchId) {
		return this.matches.remove(matchId);
	}
	
	/*********************************************************************
	*
	* - Nombre del método: finishMatch
	* - Descripción del método: Este método recibe el id de una partida y el id del jugador ganador de la misma. A continuación,
	* elimina el objeto Match correspondiente, establece el tablero del ganador como "padre" del último movimiento y lo guarda 
	* en la base de datos, y finalmente actualiza el objeto Match y lo guarda en la base de datos.
	* 
	*********************************************************************/
	public void finishMatch(String idMatch, String winner) {
		String boardEmpty = 
				"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,"+
				"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,"+
				"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,"+
				"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,"+
				"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,"+
				"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,"+
				"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,"+
				"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,"+
				"0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0";
		
		Match match = removeMatch(idMatch);
		match.notifyWinner(winner);
		removerPlayersInMatch(match);
		
		//save x-1
		Board prev =  boards.get(winner);
		String idPrev= prev.getId();	
		int lastPos = idPrev.lastIndexOf('-')+1;
		int numMove = Integer.parseInt(idPrev.substring(lastPos, idPrev.length()));
		int parentPrevNum = numMove-1;
		String parentPrevId = idPrev.substring(0, lastPos)+parentPrevNum;
		prev.setParentMove(parentPrevId, idPrev);
		boardDAO.save(prev);//guardar paso x-1
		System.out.println("x-1:"+prev.getId());
		
		//save x
		prev.setParentMove(idPrev, idPrev.substring(0, lastPos)+(numMove+1));
		prev.setBoard_values(boardEmpty);
		boardDAO.save(prev);//guardar paso x-1
		System.out.println("x:"+prev.getId());
		
		match.setIdBoards();
		if(winner.equals(match.getPlayer().get(0))) {
			match.setIdBoard1(prev.getId());
		}else {
			match.setIdBoard2(prev.getId());
		}
		saveMatch(match, winner);
	}
	
	/*********************************************************************
	*
	* - Nombre del método: removerPlayersInMatch
	* - Descripción del método: Este método recibe un objeto Match y elimina a los jugadores que participaron en ella de la 
	* lista de jugadores en partida (playersInMatch), cierra las sesiones de WebSocket correspondientes a dichos jugadores y
	* elimina dichas sesiones de la lista de sesiones activas (sessions).
	* 
	*********************************************************************/
	private void removerPlayersInMatch(Match match) {
		for(String player: match.getPlayer()) {
			this.playersInMatch.remove(player);
			try {
				removeSessionByUserId(player).close(CloseStatus.NORMAL);
			}catch(IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
	
	/*********************************************************************
	*
	* - Nombre del método: NewMove
	* - Descripción del método: Este método recibe como parámetros el nombre del jugador y un objeto Board. A partir del nombre
	*  del jugador, recupera el board previo del mismo, obtiene su último movimiento, genera un nuevo id para el board nuevo a 
	*  partir del último movimiento incrementado en 1 y asigna los atributos correspondientes al nuevo board. Luego guarda el 
	*  board previo actualizado, asigna el nuevo board a los boards y lo guarda.
	*  
	*********************************************************************/
	public void NewMove(String player, Board boardNuevo) {
		Board boardPrevio = boards.get(player);
		String idPrevio= boardPrevio.getId();
		int lastPos = idPrevio.lastIndexOf('-')+1;
		int numMove = Integer.parseInt(idPrevio.substring(lastPos, idPrevio.length()));
		System.out.println("idprevio: "+idPrevio);
		String newId = idPrevio.substring(0, lastPos)+(numMove+1);
		System.out.println("idnuevo: "+newId);
		boardNuevo.setParentMove(idPrevio, newId);//asignar atributos al nuevo Board
		boardDAO.save(boardPrevio);// guardar el movimiento actual
		boards.put(player, boardNuevo);//asignar el nuevo board a los boards
	}

	/*********************************************************************
	*
	* - Nombre del método: saveMatch
	* - Descripción del método: Este método recibe como parámetros un objeto Match y un String con el nombre del ganador. 
	* Asigna un id a los boards del match, establece el ganador y guarda el match.
	* 
	*********************************************************************/
	public void saveMatch(Match match, String winner) {
		match.setIdBoards();
		match.setWinner(winner);
		System.out.println(match.toString());
		matchDAO.save(match);
	}

	/*********************************************************************
	*
	* - Nombre del método: leaveMatch
	* - Descripción del método: Este método recibe como parámetros un String con el id del match y un String con el nombre 
	* del ganador. Remueve el match del mapa de matches, notifica al ganador, remueve los jugadores del match, establece un 
	* nuevo id a los boards del match y los guarda.
	* 
	*********************************************************************/
	public void leaveMatch(String idMatch, String winner) {
		Match match = removeMatch(idMatch);
		match.notifyWinner(winner);
		removerPlayersInMatch(match);
		match.setIdBoards();
		for(Board board : match.getBoards()) {
			boardDAO.save(board);
		}
		
		saveMatch(match, winner);
	}
	
	/*********************************************************************
	*
	* - Nombre del método: closeMatch
	* - Descripción del método: Este método recibe como parámetro un String con el nombre del jugador. Busca el match en 
	* el que participa el jugador y obtiene su id y el nombre del ganador. Si encuentra un match, llama al método leaveMatch
	* con el id del match y el nombre del ganador.
	*********************************************************************/
	public void closeMatch(String player) {
		//match
		String matchID="";
		String winner="";
		for (String idMatch : matches.keySet()) {
	        Match match = matches.get(idMatch);
	        if(match.getPlayer().get(0).equals(player)) {
	        	matchID = idMatch;
	        	winner = match.getPlayer().get(1);
	        }else if(match.getPlayer().get(1).equals(player)) {
	        	matchID = idMatch;
	        	winner = match.getPlayer().get(0);
	        }
	    }
		
		if(!matchID.equals("")) {
			leaveMatch(matchID, winner);
		}
	}
}

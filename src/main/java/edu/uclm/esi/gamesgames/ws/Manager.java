package edu.uclm.esi.gamesgames.ws;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

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
	private ConcurrentHashMap<String, WebSocketSession> sessionsByUserId;
	private ConcurrentHashMap<String, WebSocketSession> sessionsByWsId;
	private ConcurrentHashMap<String, Match> matches;
	private ArrayList<String> playersInMatch;

	private Manager() {
		this.sessionsByUserId = new ConcurrentHashMap<>();
		this.sessionsByWsId = new ConcurrentHashMap<>();
		this.matches = new ConcurrentHashMap<>();
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
		System.out.println("user: "+userId+"wsId: "+this.sessionsByUserId.get(userId));
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

	/*public void invalidate(HWSession existingSession) {
		existingSession.getHttpSession().invalidate();
		try {
			existingSession.getWebsocketSession().close();
		} catch (IOException e) {
		}
		this.removeSessionByHttpId(existingSession.getHttpSession().getId());
	}*/

	public void addMatch(Match match) {
		this.matches.put(match.getId(), match);
		//enviar mensaje a los dos jugadores para empezar a jugar
	}

	public Match getMatch(String matchId) {
		return this.matches.get(matchId);
	}

	public Match removeMatch(String matchId) {
		return this.matches.remove(matchId);
	}
	
	public void finishMatch(String idMatch, String winner) {
		Match match = removeMatch(idMatch);
		match.notifyWinner(winner);
		removerPlayersInMatch(match);
		//guardarDBMatch(result);
	}

	private void removerPlayersInMatch(Match match) {
		for(String player: match.getPlayer()) {
			this.playersInMatch.remove(player);
		}
	}

}

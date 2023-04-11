package edu.uclm.esi.gamesgames.services;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import edu.uclm.esi.gamesgames.domain.Match;
import edu.uclm.esi.gamesgames.domain.WaitingRoom;

@Service
public class GamesService {
	
	private WaitingRoom waitingRoom;
	private ConcurrentHashMap<String, Match> matches;
	public GamesService() {
		this.waitingRoom = new WaitingRoom();
		this.matches = new ConcurrentHashMap<>();
	}
	public Match requestGame(String juego, String player) {
		Match match = this.waitingRoom.findMatch(juego, player);
		if (match.isReady())
			this.matches.put(match.getId(), match);
		return match;
	}

	
}
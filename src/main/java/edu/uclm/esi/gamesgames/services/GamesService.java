package edu.uclm.esi.gamesgames.services;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import edu.uclm.esi.gamesgames.domain.Match;
import edu.uclm.esi.gamesgames.domain.WaitingRoom;

@Service
public class GamesService {
	
	private WaitingRoom waitingRoom;
	private ConcurrentHashMap<String, Match> matches;
	private ArrayList<String> playersInMatch;
	
	public GamesService() {
		this.waitingRoom = new WaitingRoom();
		this.matches = new ConcurrentHashMap<>();
		this.playersInMatch = new ArrayList<>();
	}
	
	public Match requestGame(String juego, String player) throws Exception {
		System.out.println(player+":"+this.playersInMatch.contains(player));
		if(this.playersInMatch.contains(player)) //Jugador ya en espera
			throw new Exception();
			
		Match match = this.waitingRoom.findMatch(juego, player);
		this.playersInMatch.add(player);
		if (match.isReady())
			this.matches.put(match.getId(), match);
		return match;
	}

	public void leaveGame(String idMatch, String userName) {
		// Quitar el nombre del usuario de la lista de usuarios en partidas
		this.playersInMatch.remove(userName);
		
	}

	
}
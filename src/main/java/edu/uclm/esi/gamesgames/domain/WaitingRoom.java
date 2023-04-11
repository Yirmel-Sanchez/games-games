package edu.uclm.esi.gamesgames.domain;

import java.util.concurrent.ConcurrentHashMap;

public class WaitingRoom {
	
	private ConcurrentHashMap<String, Match> matches;
	
	public WaitingRoom() {
		this.matches = new ConcurrentHashMap<>();
	}
	public Match findMatch(String juego, String player) {
		Match match = this.matches.get(juego);
		if(match == null) {
			match = new Match();
			match.addPlayer(player);
			this.matches.put(juego, match);
		} else {
			match.addPlayer(player);
		}
		return match;
	}

}

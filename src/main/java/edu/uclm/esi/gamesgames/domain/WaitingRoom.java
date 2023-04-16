package edu.uclm.esi.gamesgames.domain;

import java.util.concurrent.ConcurrentHashMap;

public class WaitingRoom {
	
	private ConcurrentHashMap<String, Match> pendingMatches;
	
	
	public WaitingRoom() {
		this.pendingMatches = new ConcurrentHashMap<>();
	}
	
	public Match findMatch(String juego, String player) {
		Match match = this.pendingMatches.get(juego);
		if(match == null) {
			match = new Match();
			if(!match.getPlayer().contains(player)) {
				match.addPlayer(player);
				this.pendingMatches.put(juego, match);
			}
		} else {
			match.addPlayer(player);
			this.pendingMatches.remove(juego);
		}
		return match;
	}

}

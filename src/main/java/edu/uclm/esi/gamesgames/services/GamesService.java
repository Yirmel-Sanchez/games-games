package edu.uclm.esi.gamesgames.services;


import org.springframework.stereotype.Service;

import edu.uclm.esi.gamesgames.domain.Board;
import edu.uclm.esi.gamesgames.domain.Match;
import edu.uclm.esi.gamesgames.domain.WaitingRoom;
import edu.uclm.esi.gamesgames.ws.Manager;

@Service
public class GamesService {
	
	private WaitingRoom waitingRoom;
	
	public GamesService() {
		this.waitingRoom = new WaitingRoom();
	}
	
	public Match requestGame(String juego, String player) throws Exception {
		System.out.println(player+":"+Manager.get().isPlayerInMatch(player));//**************************************
		if(Manager.get().isPlayerInMatch(player)) //Jugador ya en espera
			throw new Exception();
		
		Manager.get().addPlayerInMatch(player);	
		Match match = this.waitingRoom.findMatch(juego, player);
		if (match.isReady())
			Manager.get().addMatch(match);
		return match;
	}

	public void leaveGame(String idMatch, String userName) {
		// Quitar el nombre del usuario de la lista de usuarios en partidas
		Manager.get().removePlayerInMatch(userName);
		this.waitingRoom.leaveMatch(idMatch, userName); //quitarlo de la lista de espera
	}

	public void isValidMove(Match match, String userId, String move) {
		//obtener tablero del usuario
		Board boardOriginal;
		if(match.getPlayer().get(0).equals(userId)) {
			boardOriginal = match.getBoards().get(0);
		}else {
			boardOriginal = match.getBoards().get(1);
		}
		checkMove(boardOriginal, move);
		
	}

	private void checkMove(Board boardOriginal, String move) {
		
	}

	
}
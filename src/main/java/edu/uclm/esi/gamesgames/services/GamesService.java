package edu.uclm.esi.gamesgames.services;

import java.util.ArrayList;
import java.util.Iterator;

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
		System.out.println(player + ":" + Manager.get().isPlayerInMatch(player));// **************************************
		if (Manager.get().isPlayerInMatch(player)) // Jugador ya en espera
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
		this.waitingRoom.leaveMatch(idMatch, userName); // quitarlo de la lista de espera
	}

	public void isValidMove(Match match, String userId, String move) {
		// obtener tablero del usuario
		Board userBoard; // obtener el board del usuario
		int posBoard;
		if (match.getPlayer().get(0).equals(userId)) {
			userBoard = match.getBoards().get(0);
			posBoard = 0;
		} else {
			userBoard = match.getBoards().get(1);
			posBoard = 1;
		}
		boolean validMove = checkMove(userBoard, move);// validar el movimiento
		if (validMove) {
			Board nuevoBoard = move(userBoard, move);
			// System.out.println(nuevoBoard);
			// aqui deberia estar la logica para relacionar el tablero previo y el nuevo y
			// almacenar el movimiento
			match.setBoard(userId, nuevoBoard);
			// System.out.println(match.getBoards().get(posBoard));
		}

	}

	private boolean checkMove(Board boardOriginal, String move) {
		int x1, y1, x2, y2;
		try {
			String[] positions = move.split(",");
			x1 = Integer.parseInt(positions[0]);
			y1 = Integer.parseInt(positions[1]);
			x2 = Integer.parseInt(positions[2]);
			y2 = Integer.parseInt(positions[3]);

			// obtener el tablero
			byte[][][] board = boardOriginal.getDigits();

			// misma posicion
			if (x1 == x2 && y1 == y2)
				return false;
			// ambos numeros activos
			if (board[x1][y1][1] != 1 && board[x2][y2][1] != 1)
				return false;
			// No son iguales ni suman 10
			if (board[x1][y1][0] != board[x2][y2][0] && board[x1][y1][0] + board[x2][y2][0] != 10)
				return false;

			// hallar la posicion mas pequeña
			int xm, xM, ym, yM;
			int pos1 = (x1 * 9 + y1);
			int pos2 = (x2 * 9 + y2);
			if (pos1 < pos2) {// posicion 1 va antes que la 2
				xm = x1;
				ym = y1;
				xM = x2;
				yM = y2;
			} else { // posicion 2 va antes que la 1
				xm = x2;
				ym = y2;
				xM = x1;
				yM = y1;
			}

			if (Math.abs(pos1 - pos2) == 1 || Math.abs(pos1 - pos2) == 9) // son vecinos
				return true;

			if (x1 == x2) { // misma fila
				for (int y = ym + 1; y < yM; y++) // recorrer todas las posiciones intermedias
					if (board[x1][y][1] == 1) // si hay un numero entre las posiciones
						return false;
			} else if (y1 == y2) { // misma columna
				for (int x = xm + 1; x < xM; x++) // recorrer todas las posiciones intermedias
					if (board[x][y1][1] == 1)// si hay un numero entre las posiciones
						return false;
			} else if (Math.abs(x1 - x2) == Math.abs(y1 - y2)) { // misma diagonal
				if (Math.abs(x1 - x2) != 1) { // no son vecinos
					if (ym < yM) // diagonal descendente
						for (int x = xm + 1; x < xM; x++) // recorrer todas las posiciones intermedias
							if (board[x][ym + (x - xm)][1] == 1)// si hay un numero entre las posiciones
								return false;
							else // diagonal ascendente
								for (int y = yM + 1; y < ym; y++) // recorrer todas las posiciones intermedias
									if (board[xM - (y - yM)][y][1] == 1) // si hay un numero entre las posiciones
										return false;
				}
			} else { // ninguna de las anteriores
				int ini = (xm * 9 + ym);
				int fin = (xM * 9 + yM);

				for (int i = ini + 1; i < fin; i++) // recorrer secuencialmente las posiciones intermedias
					if (board[(int) i / 9][i % 9][1] == 1) // si hay un numero entre las posiciones
						return false;
			}
			return true; // no hay ningun numero entre las posiciones
		} catch (Exception e) {
			return false; // si hay un error no permite el movimiento
		}
	}

	public Board move(Board board, String move) {
		Board boardCopy = board.copy();
		String[] positions = move.split(",");
		int x1 = Integer.parseInt(positions[0]);
		int y1 = Integer.parseInt(positions[1]);
		int x2 = Integer.parseInt(positions[2]);
		int y2 = Integer.parseInt(positions[3]);

		// obtener el tablero
		byte[][][] grid = boardCopy.getDigits();

		// inhabilitar las posiciones
		grid[x1][y1][1] = 0;
		grid[x2][y2][1] = 0;

		byte[][][] grid2 = new byte[9][9][2];

		// comprobar filas
		int filaPintar = 0;
		for (int i = 0; i < 9; i++) {// recorrer todas las filas
			boolean filaVacia = true;
			for (int j = 0; j < 9; j++) {// recorrer las columnas de la fila
				if (grid[i][j][1] == 1) {// si una columna esta llena
					filaVacia = false;// la fila no esta vacia
					break;// dejo de buscar esa columna
				}
			}
			if (!filaVacia) {// si la fila no esta vacia //la pinto en grid2
				for (int j = 0; j < 9; j++) {// recorrer las columnas de la fila
					// asignar el valor de la fila que estamos recorriendo en el primer hueco de
					// fila
					grid2[filaPintar][j][0] = grid[i][j][0];
					grid2[filaPintar][j][1] = grid[i][j][1];
				}
				filaPintar++;// siguiente pintada en la siguiente linea
			}
		}

		// comprobar si el tablero está vacio
		boolean tableroVacio = true;
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				if (grid2[i][j][1] == 1) {
					tableroVacio = false;
					break;
				}
			}
		}
		if (tableroVacio) {
			boardCopy.setBoardEmpty(true);// indicar que el tablero está vacio
		}

		boardCopy.setDigits(grid2);// asignar el nuevo tablero
		return boardCopy;
	}

	public void addNumber(Match match, String nameUser) {
		// obtener tablero del usuario
		Board userBoard; // obtener el board del usuario
		int posBoard;
		if (match.getPlayer().get(0).equals(nameUser)) {
			userBoard = match.getBoards().get(0);
			posBoard = 0;
		} else {
			userBoard = match.getBoards().get(1);
			posBoard = 1;
		}
		
		addNumbersToBoard(userBoard);
		System.out.println(userBoard);
		match.setBoard(nameUser, userBoard);

	}

	private void addNumbersToBoard(Board userBoard) {
		int lastPos=80;
		byte[][][] tablero = userBoard.getDigits();
		ArrayList<Byte> numsToAdd = new ArrayList<>();
		for(int i=0; i<81;i++) {//ver que numeros hay que añadir y guardar la ultima posicion de estos
			if(tablero[(int) i / 9][i % 9][1]==1) {
				numsToAdd.add(tablero[(int) i / 9][i % 9][0]);//añade el numero entre los que hay que añadir
			}
			if(tablero[(int) i / 9][i % 9][0]==0 && i<lastPos)
				lastPos = i; //actualiza la ultima posicion ocupada
		}
		for(int i=lastPos; i<81;i++) { //añadir los numeros al tablero
			if(!numsToAdd.isEmpty()) { // si la lista no esta vacia
				tablero[(int) i / 9][i % 9][0]=numsToAdd.remove(0);
				tablero[(int) i / 9][i % 9][1]=1;
			}
		}
	}

	public boolean checkBlock(Match match, String nameUser) {
		Board userBoard; // obtener el board del usuario
		int posBoard;
		if (match.getPlayer().get(0).equals(nameUser)) {
			userBoard = match.getBoards().get(0);
			posBoard = 0;
		} else {
			userBoard = match.getBoards().get(1);
			posBoard = 1;
		}
		
		for(int i=0; i<81;i++) {//posibles combinaciones de movimientos
			for(int j=0; j<81;j++) {
				int x1 = (int) i / 9;
				int y1 = i % 9;
				int x2 = (int) j / 9;
				int y2 = j % 9;
				
				if(checkMove(userBoard,""+x1+","+y1+","+x2+","+y2))
					return false;//hay un movimiento disponible
			}
		}
		return true;//hay un bloqueo en el tablero
	}

	public boolean emptyBoard(Match match, String userId) {
		if (match.getPlayer().get(0).equals(userId)) {
			if(match.getBoards().get(0).isBoardEmpty()) 
				return true;
		} else {
			if(match.getBoards().get(1).isBoardEmpty()) 
				return true;
		}
		return false;
	}

}
package edu.uclm.esi.gamesgames.services;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import edu.uclm.esi.gamesgames.domain.Board;
import edu.uclm.esi.gamesgames.domain.Match;
import edu.uclm.esi.gamesgames.domain.WaitingRoom;
import edu.uclm.esi.gamesgames.ws.Manager;

@Service
public class GamesService {

	private WaitingRoom waitingRoom;
	private ConcurrentHashMap<String, Board> matchesAlone;
	

	public GamesService() {
		this.waitingRoom = new WaitingRoom();
		this.matchesAlone = new ConcurrentHashMap<>();
	}
	/*********************************************************************
	*
	*  - Nombre del método: requestGame
	*  - Descripción del método: El método requestGame es llamado por un jugador que quiere unirse a una partida en espera o crear una nueva. 
	* una. Si el jugador ya está en una partida, cierra esa partida y añade al jugador a la lista de espera. A continuación, el método 
	* busca una partida con el nombre del juego y el nombre del jugador especificados. Si se encuentra una partida, y está lista, el método añade
	* la partida a la lista de partidas actuales y la devuelve.
	*
	*********************************************************************/
	public Match requestGame(String juego, String player) throws Exception {
		System.out.println(player + ":" + Manager.get().isPlayerInMatch(player));// **************************************
		if (Manager.get().isPlayerInMatch(player)) // Jugador ya en espera
			Manager.get().closeMatch(player);

		Manager.get().addPlayerInMatch(player);
		Match match = this.waitingRoom.findMatch(juego, player);
		if (match.isReady())
			Manager.get().addMatch(match);
		return match;
	}
	
	/*********************************************************************
	*
	* - Nombre del método: leaveGame 
	* - Descripción del método: El método leaveGame elimina a un jugador de una partida en espera o de una partida en curso.
	*
	*********************************************************************/
	public void leaveGame(String idMatch, String userName) {
		// Quitar el nombre del usuario de la lista de usuarios en partidas
		Manager.get().removePlayerInMatch(userName);
		this.waitingRoom.leaveMatch(idMatch, userName); // quitarlo de la lista de espera
	}
	
	/*********************************************************************
	*
	* - Nombre del método: 
	* - Descripción del método: El método isValidMove comprueba si un movimiento es válido para la coincidencia, ID de usuario, 
	* y jugada. Si el movimiento es válido, actualiza el tablero con el nuevo movimiento.
	*
	*********************************************************************/
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
		System.out.println("board original "+userId+": "+userBoard);
		boolean validMove = checkMove(userBoard, move);// validar el movimiento
		System.out.println("Valid move: "+validMove);
		if (validMove) {
			Board nuevoBoard = move(userBoard, move);
			
			match.setBoard(userId, nuevoBoard);
			// almacenar el movimiento
			System.out.println("board actualizado "+userId+": "+match.getBoards().get(posBoard));
		}

	}
	
	/*********************************************************************
	*
	* - Nombre del método: checkMove
	* - Descripción del método: El método checkMove comprueba si un movimiento es válido basándose en las reglas del juego. 
	* Devuelve true si la jugada es válida y false en caso contrario. El método comprueba si las posiciones son iguales, 
	* si ambos dígitos están activos, si los dígitos suman 10 o son iguales, si son vecinos, y si hay un
	* número intermedio. Si las posiciones son diagonales, el método comprueba si hay números entre ellas y si 
	* las posiciones son vecinas.
	*
	*********************************************************************/
	public boolean checkMove(Board boardOriginal, String move) {
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
	
	/*********************************************************************
	*
	* - Nombre del método: move
	* - Descripción del método: Este método toma un tablero de juego y un movimiento (representado como 
	* una cadena) y actualiza el tablero basándose en el movimiento. Se espera que el movimiento sea una cadena con cuatro 
	* números separados por * comas, que representan las coordenadas x e y de las dos celdas. comas, representando las 
	* coordenadas x e y de las dos celdas a rellenar. El método crea primero una copia del tablero, luego desactiva las dos
	* celdas que fueron rellenadas poniendo su valor de segundo byte a 0. Luego comprueba cada fila del tablero para ver 
	* cuáles siguen siendo válidas (es decir, no contienen números duplicados) y crea una nueva cuadrícula con sólo las filas
	* válidas. Finalmente, establece la nueva cuadrícula como los dígitos del tablero y devuelve el tablero actualizado.
	*
	*********************************************************************/
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
		System.out.println("tablero modificado en move:" + boardCopy);
		return boardCopy;
	}
	
	/*********************************************************************
	*
	* - Nombre del método: addNumber
	* - Descripción del método: addNumber(Partido partido, String nombreUsuario): Este método añade números al tablero de juego del
	* usuario especificado en la partida especificada. Primero recupera el tablero del usuario del objeto de la partida, luego llama al método 
	* addNumbersToBoard para añadir los números necesarios al tablero. Finalmente, actualiza el objeto partido con el 
	* tablero actualizado.
	*
	*********************************************************************/
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
		//Manager.get().setAddMove(match, nameUser, userBoard);
		match.setBoard(nameUser, userBoard);
	}
	
	/*********************************************************************
	*
	* - Nombre del método: addNumbersToBoard
	* - Descripción del método: Este método añade números al tablero. Primero encuentra la última posición ocupada en el tablero
	* y luego itera a través de las celdas restantes, añadiendo números según sea necesario.
	*
	*********************************************************************/
	private void addNumbersToBoard(Board userBoard) {
		int lastPos=90;
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
	
	/*********************************************************************
	*
	* - Nombre del método: checkBlock
	* - Descripción del método: Este método comprueba si el tablero del usuario especificado en la partida especificada 
	* está bloqueado (es decir, no quedan movimientos válidos). Primero recupera el tablero del usuario del objeto de la 
	* partida, luego llama al método checkBlock para comprobar si el tablero está bloqueado. Devuelve true si el tablero 
	* está bloqueado y false en caso contrario.
	*
	*********************************************************************/
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
		
		return checkBlock(userBoard);//hay un bloqueo en el tablero
	}
	/*********************************************************************
	*
	* - Method name: emptyBoard
	* - Description of the Method: Devuelve verdadero si el tablero correspondiente al usuario especificado está vacío en una partida dada.
	*
	*********************************************************************/
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
	
	/*********************************************************************
	*
	* - Method name: requestGameAlone
	* - Description of the Method: Crea una partida en solitario para un usuario especificado y devuelve la representación en 
	* cadena de la placa creada.
	*
	*********************************************************************/
	public String requestGameAlone(String userName) {
		Board board = new Board();
		matchesAlone.put(userName, board);
		return board.toString();
	}
	/*********************************************************************
	*
	*- Method name: moveAlone
	* - Description of the Method: Realiza un movimiento en la placa correspondiente al usuario especificado en una partida en
	* solitario dada, y devuelve la representación en cadena de la nueva placa. Si el movimiento no se puede realizar, devuelve
	* la representación en cadena de la placa actual.
	*
	*********************************************************************/
	public String moveAlone(String move, String userName) throws Exception {
		if(!matchesAlone.containsKey(userName))
			throw new Exception();
		
		Board userBoard = matchesAlone.get(userName);
		Board newBoard = null;
		if(checkMove(userBoard, move)) { //se puede realizar el movimiento
			newBoard = move(userBoard, move);
			matchesAlone.put(userName, newBoard);
			return newBoard.toString();
		}else {//no se puede realizar el movimiento
			return userBoard.toString();
		}
	}
	
	/*********************************************************************
	*
	*- Method name: addNumbersAlone
	* - Description of the Method: Agrega números aleatorios a la placa correspondiente al usuario especificado en una partida
	* en solitario dada. Si esto resulta en un bloqueo en el tablero, se crea una nueva placa y se devuelve su representación
	*  en cadena. Si no hay bloqueo, devuelve la representación en cadena de la placa actual.
	*
	*********************************************************************/
	
	public String addNumbersAlone(String userName) throws Exception {
		if(!matchesAlone.containsKey(userName))
			throw new Exception();
		
		Board userBoard = matchesAlone.get(userName);
		
		addNumbersToBoard(userBoard);
		if(checkBlock(userBoard)) {
			Board newBoard = new Board();
			matchesAlone.put(userName, newBoard);
			return newBoard.toString();
		}

		return userBoard.toString();
	}

	/*********************************************************************
	*
	*- Method name: checkBlock
	*- Description of the Method: Verifica si hay un bloqueo en la placa dada, es decir, si no hay más movimientos posibles. 
	* Si hay un movimiento disponible, devuelve falso, de lo contrario devuelve verdadero.
	*
	*********************************************************************/
	public boolean checkBlock(Board userBoard){
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
}
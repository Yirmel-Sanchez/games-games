package edu.uclm.esi.gamesgames;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import edu.uclm.esi.gamesgames.domain.Board;
import edu.uclm.esi.gamesgames.domain.Match;
import edu.uclm.esi.gamesgames.domain.WaitingRoom;
import edu.uclm.esi.gamesgames.services.GamesService;
import edu.uclm.esi.gamesgames.ws.Manager;

@ExtendWith(MockitoExtension.class)
public class TestGamesServices {

	@InjectMocks
	private GamesService gamesService;

	// @Mock(name = "myManager")
	// private Manager manager;

	@Mock
	private WaitingRoom waitingRoom;

	@Test
	public void testRequestGame() throws Exception {
		// Preparar datos de prueba
		String juego = "nm";
		String player = "Player1";
		Match match = new Match();

		// manager = Mockito.mock(Manager.class);

		// Configurar el comportamiento esperado de los mocks
		// when(manager.isPlayerInMatch(player)).thenReturn(false);
		when(waitingRoom.findMatch(juego, player)).thenReturn(match);

		// Ejecutar el método a probar
		Match result = gamesService.requestGame(juego, player);
		verify(waitingRoom).findMatch(juego, player);

		// Verificar el resultado esperado
		assertEquals(match.getId(), result.getId());

	}

	@Test
	public void testLeaveGame() throws Exception {
		// Preparar datos de prueba
		String juego = "nm";
		String player = "Player1";
		Match match = new Match();

		// manager = Mockito.mock(Manager.class);

		// Configurar el comportamiento esperado de los mocks
		// when(manager.isPlayerInMatch(player)).thenReturn(false);
		doAnswer(invocation -> { 
	 		return true;
	 	}).when(waitingRoom).leaveMatch(juego, player);

		// Ejecutar el método a probar
		gamesService.leaveGame(juego, player);
		verify(waitingRoom).leaveMatch(juego, player);

	}
	
	@Test
	public void testIsValidMoveNeighbors() throws Exception {
		// Preparar datos de prueba
		String userId = "Player1";
		String move = "0,0,0,1";
		
		byte[][][] matriz = new byte[9][9][2];
		for (int i = 0; i < 9; i++) {
		    for (int j = 0; j < 9; j++) {
		        for (int k = 0; k < 2; k++) {
		            matriz[i][j][k] = 0;
		        }
		    }
		}
		matriz[0][0][0] = 1;
		matriz[0][0][1] = 1;
		matriz[0][1][0] = 1;
		matriz[0][1][1] = 1;
		
		Board board1 = new Board();
		board1.setDigits(matriz);
		Board board2 = board1.copy();
		
		Match match = new Match();
		match.addPlayer("Player1");
		match.addPlayer("Player2");
		match.setBoard("Player1", board1);
		match.setBoard("Player2", board2);

		// Ejecutar el método a probar
		gamesService.isValidMove(match, userId, move);
		assertEquals(match.getBoards().get(0).getDigits()[0][0][0], 0);
		assertEquals(match.getBoards().get(0).getDigits()[0][0][1], 0);
		assertEquals(match.getBoards().get(0).getDigits()[0][1][0], 0);
		assertEquals(match.getBoards().get(0).getDigits()[0][1][1], 0);

	}
	
	@Test
	public void testIsValidMoveRow() throws Exception {
		// Preparar datos de prueba
		String userId = "Player2";
		String move = "0,3,0,0";
		
		byte[][][] matriz = new byte[9][9][2];
		for (int i = 0; i < 9; i++) {
		    for (int j = 0; j < 9; j++) {
		        for (int k = 0; k < 2; k++) {
		            matriz[i][j][k] = 0;
		        }
		    }
		}
		matriz[0][0][0] = 1;
		matriz[0][0][1] = 1;
		matriz[0][3][0] = 9;
		matriz[0][3][1] = 1;
		
		Board board1 = new Board();
		board1.setDigits(matriz);
		Board board2 = board1.copy();
		
		Match match = new Match();
		match.addPlayer("Player1");
		match.addPlayer("Player2");
		match.setBoard("Player1", board1);
		match.setBoard("Player2", board2);

		// Ejecutar el método a probar
		gamesService.isValidMove(match, userId, move);
		assertEquals(match.getBoards().get(1).getDigits()[0][0][0], 0);
		assertEquals(match.getBoards().get(1).getDigits()[0][0][1], 0);
		assertEquals(match.getBoards().get(1).getDigits()[0][3][0], 0);
		assertEquals(match.getBoards().get(1).getDigits()[0][3][1], 0);

	}
	
	@Test
	public void testIsValidMoveColumn() throws Exception {
		// Preparar datos de prueba
		String userId = "Player2";
		String move = "0,0,3,0";
		
		byte[][][] matriz = new byte[9][9][2];
		for (int i = 0; i < 9; i++) {
		    for (int j = 0; j < 9; j++) {
		        for (int k = 0; k < 2; k++) {
		            matriz[i][j][k] = 0;
		        }
		    }
		}
		matriz[0][0][0] = 1;
		matriz[0][0][1] = 1;
		matriz[3][0][0] = 9;
		matriz[3][0][1] = 1;
		
		Board board1 = new Board();
		board1.setDigits(matriz);
		Board board2 = board1.copy();
		
		Match match = new Match();
		match.addPlayer("Player1");
		match.addPlayer("Player2");
		match.setBoard("Player1", board1);
		match.setBoard("Player2", board2);

		// Ejecutar el método a probar
		gamesService.isValidMove(match, userId, move);
		assertEquals(match.getBoards().get(1).getDigits()[0][0][0], 0);
		assertEquals(match.getBoards().get(1).getDigits()[0][0][1], 0);
		assertEquals(match.getBoards().get(1).getDigits()[3][0][0], 0);
		assertEquals(match.getBoards().get(1).getDigits()[3][0][1], 0);

	}
	
	@Test
	public void testIsValidMoveInvalid() throws Exception {
		// Preparar datos de prueba
		String userId = "Player2";
		String move = "0,0,0,0";
		
		byte[][][] matriz = new byte[9][9][2];
		for (int i = 0; i < 9; i++) {
		    for (int j = 0; j < 9; j++) {
		        for (int k = 0; k < 2; k++) {
		            matriz[i][j][k] = 0;
		        }
		    }
		}
		matriz[0][0][0] = 1;
		matriz[0][0][1] = 1;
		matriz[3][0][0] = 9;
		matriz[3][0][1] = 1;
		
		Board board1 = new Board();
		board1.setDigits(matriz);
		Board board2 = board1.copy();
		
		Match match = new Match();
		match.addPlayer("Player1");
		match.addPlayer("Player2");
		match.setBoard("Player1", board1);
		match.setBoard("Player2", board2);

		// Ejecutar el método a probar
		gamesService.isValidMove(match, userId, move);
		assertEquals(match.getBoards().get(1).getDigits()[0][0][0], 1);
		assertEquals(match.getBoards().get(1).getDigits()[0][0][1], 1);
		assertEquals(match.getBoards().get(1).getDigits()[3][0][0], 9);
		assertEquals(match.getBoards().get(1).getDigits()[3][0][1], 1);

	}
	
	@Test
	public void testIsValidMoveDiagonal() throws Exception {
		// Preparar datos de prueba
		String userId = "Player2";
		String move = "0,0,3,3";
		
		byte[][][] matriz = new byte[9][9][2];
		for (int i = 0; i < 9; i++) {
		    for (int j = 0; j < 9; j++) {
		        for (int k = 0; k < 2; k++) {
		            matriz[i][j][k] = 0;
		        }
		    }
		}
		matriz[0][0][0] = 1;
		matriz[0][0][1] = 1;
		matriz[3][3][0] = 9;
		matriz[3][3][1] = 1;
		
		Board board1 = new Board();
		board1.setDigits(matriz);
		Board board2 = board1.copy();
		
		Match match = new Match();
		match.addPlayer("Player1");
		match.addPlayer("Player2");
		match.setBoard("Player1", board1);
		match.setBoard("Player2", board2);

		// Ejecutar el método a probar
		gamesService.isValidMove(match, userId, move);
		assertEquals(match.getBoards().get(1).getDigits()[0][0][0], 0);
		assertEquals(match.getBoards().get(1).getDigits()[0][0][1], 0);
		assertEquals(match.getBoards().get(1).getDigits()[3][3][0], 0);
		assertEquals(match.getBoards().get(1).getDigits()[3][3][1], 0);

	}
	
	@Test
	public void testIsValidMoveSecuencial() throws Exception {
		// Preparar datos de prueba
		String userId = "Player2";
		String move = "0,0,3,4";
		
		byte[][][] matriz = new byte[9][9][2];
		for (int i = 0; i < 9; i++) {
		    for (int j = 0; j < 9; j++) {
		        for (int k = 0; k < 2; k++) {
		            matriz[i][j][k] = 0;
		        }
		    }
		}
		matriz[0][0][0] = 1;
		matriz[0][0][1] = 1;
		matriz[3][4][0] = 9;
		matriz[3][4][1] = 1;
		
		Board board1 = new Board();
		board1.setDigits(matriz);
		Board board2 = board1.copy();
		
		Match match = new Match();
		match.addPlayer("Player1");
		match.addPlayer("Player2");
		match.setBoard("Player1", board1);
		match.setBoard("Player2", board2);

		// Ejecutar el método a probar
		gamesService.isValidMove(match, userId, move);
		assertEquals(match.getBoards().get(1).getDigits()[0][0][0], 0);
		assertEquals(match.getBoards().get(1).getDigits()[0][0][1], 0);
		assertEquals(match.getBoards().get(1).getDigits()[3][4][0], 0);
		assertEquals(match.getBoards().get(1).getDigits()[3][4][1], 0);

	}
	
	@Test
	public void testIsValidMoveNotSame() throws Exception {
		// Preparar datos de prueba
		String userId = "Player2";
		String move = "0,0,3,4";
		
		byte[][][] matriz = new byte[9][9][2];
		for (int i = 0; i < 9; i++) {
		    for (int j = 0; j < 9; j++) {
		        for (int k = 0; k < 2; k++) {
		            matriz[i][j][k] = 0;
		        }
		    }
		}
		matriz[0][0][0] = 1;
		matriz[0][0][1] = 1;
		matriz[3][4][0] = 7;
		matriz[3][4][1] = 1;
		
		Board board1 = new Board();
		board1.setDigits(matriz);
		Board board2 = board1.copy();
		
		Match match = new Match();
		match.addPlayer("Player1");
		match.addPlayer("Player2");
		match.setBoard("Player1", board1);
		match.setBoard("Player2", board2);

		// Ejecutar el método a probar
		gamesService.isValidMove(match, userId, move);
		assertEquals(match.getBoards().get(1).getDigits()[0][0][0], 1);
		assertEquals(match.getBoards().get(1).getDigits()[0][0][1], 1);
		assertEquals(match.getBoards().get(1).getDigits()[3][4][0], 7);
		assertEquals(match.getBoards().get(1).getDigits()[3][4][1], 1);

	}
	
	@Test
	public void testIsValidMoveRowDeleted() throws Exception {
		// Preparar datos de prueba
		String userId = "Player2";
		String move = "0,3,0,0";
		
		byte[][][] matriz = new byte[9][9][2];
		for (int i = 0; i < 9; i++) {
		    for (int j = 0; j < 9; j++) {
		        for (int k = 0; k < 2; k++) {
		            matriz[i][j][k] = 0;
		        }
		    }
		}
		matriz[0][0][0] = 1;
		matriz[0][0][1] = 1;
		matriz[0][3][0] = 9;
		matriz[0][3][1] = 1;
		matriz[1][3][0] = 1;
		matriz[1][3][1] = 1;
		
		Board board1 = new Board();
		board1.setDigits(matriz);
		Board board2 = board1.copy();
		
		Match match = new Match();
		match.addPlayer("Player1");
		match.addPlayer("Player2");
		match.setBoard("Player1", board1);
		match.setBoard("Player2", board2);

		// Ejecutar el método a probar
		gamesService.isValidMove(match, userId, move);
		assertEquals(match.getBoards().get(1).getDigits()[0][0][0], 0);
		assertEquals(match.getBoards().get(1).getDigits()[0][0][1], 0);
		assertEquals(match.getBoards().get(1).getDigits()[0][3][0], 1);
		assertEquals(match.getBoards().get(1).getDigits()[0][3][1], 1);

	}
	
	@Test
	public void testaddNumber() throws Exception {
		// Preparar datos de prueba
		String userId = "Player1";
		
		byte[][][] matriz = new byte[9][9][2];
		for (int i = 0; i < 9; i++) {
		    for (int j = 0; j < 9; j++) {
		        for (int k = 0; k < 2; k++) {
		            matriz[i][j][k] = 0;
		        }
		    }
		}
		matriz[0][0][0] = 1;
		matriz[0][0][1] = 1;
		matriz[0][1][0] = 9;
		matriz[0][1][1] = 0;
		matriz[0][2][0] = 7;
		matriz[0][2][1] = 1;
		
		Board board1 = new Board();
		board1.setDigits(matriz);
		Board board2 = board1.copy();
		
		Match match = new Match();
		match.addPlayer("Player1");
		match.addPlayer("Player2");
		match.setBoard("Player1", board1);
		match.setBoard("Player2", board2);

		// Ejecutar el método a probar
		gamesService.addNumber(match, userId);
		assertEquals(match.getBoards().get(0).getDigits()[0][3][0], 1);
		assertEquals(match.getBoards().get(0).getDigits()[0][3][1], 1);
		assertEquals(match.getBoards().get(0).getDigits()[0][4][0], 7);
		assertEquals(match.getBoards().get(0).getDigits()[0][4][1], 1);

	}
	
	@Test
	public void testCheckBlockFalse() throws Exception {
		// Preparar datos de prueba
		String userId = "Player1";
		
		byte[][][] matriz = new byte[9][9][2];
		for (int i = 0; i < 9; i++) {
		    for (int j = 0; j < 9; j++) {
		        for (int k = 0; k < 2; k++) {
		            matriz[i][j][k] = 0;
		        }
		    }
		}
		matriz[0][0][0] = 1;
		matriz[0][0][1] = 1;
		matriz[0][1][0] = 9;
		matriz[0][1][1] = 0;
		matriz[0][2][0] = 7;
		matriz[0][2][1] = 1;
		
		Board board1 = new Board();
		board1.setDigits(matriz);
		Board board2 = board1.copy();
		
		Match match = new Match();
		match.addPlayer("Player1");
		match.addPlayer("Player2");
		match.setBoard("Player1", board1);
		match.setBoard("Player2", board2);

		// Ejecutar el método a probar
		boolean result = gamesService.checkBlock(match, userId);
		assertFalse(result);

	}
	
	@Test
	public void testCheckBlockTrue() throws Exception {
		// Preparar datos de prueba
		String userId = "Player1";
		
		byte[][][] matriz = new byte[9][9][2];
		for (int i = 0; i < 9; i++) {
		    for (int j = 0; j < 9; j++) {
		        for (int k = 0; k < 2; k++) {
		            matriz[i][j][k] = 0;
		        }
		    }
		}
		
		Board board1 = new Board();
		board1.setDigits(matriz);
		Board board2 = board1.copy();
		
		Match match = new Match();
		match.addPlayer("Player1");
		match.addPlayer("Player2");
		match.setBoard("Player1", board1);
		match.setBoard("Player2", board2);

		// Ejecutar el método a probar
		boolean result = gamesService.checkBlock(match, userId);
		assertTrue(result);

	}
	
	@Test
	public void testEmptyBoard() throws Exception {
		// Preparar datos de prueba
		String userId = "Player1";
		
		byte[][][] matriz = new byte[9][9][2];
		for (int i = 0; i < 9; i++) {
		    for (int j = 0; j < 9; j++) {
		        for (int k = 0; k < 2; k++) {
		            matriz[i][j][k] = 0;
		        }
		    }
		}
		
		Board board1 = new Board();
		board1.setDigits(matriz);
		Board board2 = board1.copy();
		board1.setBoardEmpty(true);
		board2.setBoardEmpty(true);
		
		Match match = new Match();
		match.addPlayer("Player1");
		match.addPlayer("Player2");
		match.setBoard("Player1", board1);
		match.setBoard("Player2", board2);

		// Ejecutar el método a probar
		boolean result = gamesService.emptyBoard(match, userId);
		assertTrue(result);
		
		userId = "Player2";
		result = gamesService.emptyBoard(match, userId);
		assertTrue(result);

		board2.setBoardEmpty(false);
		match.setBoard("Player2", board2);
		result = gamesService.emptyBoard(match, userId);
		assertFalse(result);
	}
	
	@Test
    public void testRequestGameAlone() throws Exception {
        String userName = "Player1";
        String expectedBoard = "0,0,0"; // el resultado esperado de la función

        String actualBoard = gamesService.requestGameAlone(userName);
        assertTrue(actualBoard.contains(expectedBoard)); // verificar que el resultado sea el esperado
        
        actualBoard = gamesService.moveAlone("0,0,0,1", userName);
        assertTrue(actualBoard.contains(expectedBoard)); // verificar que el resultado sea el esperado
        
        actualBoard = gamesService.addNumbersAlone(userName);
        assertTrue(actualBoard.contains(expectedBoard)); // verificar que el resultado sea el esperado
    }
}

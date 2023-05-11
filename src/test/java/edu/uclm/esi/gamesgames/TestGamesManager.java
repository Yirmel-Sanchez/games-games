package edu.uclm.esi.gamesgames;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.aspectj.lang.annotation.Before;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import edu.uclm.esi.gamesgames.dao.BoardDAO;
import edu.uclm.esi.gamesgames.dao.MatchDAO;
import edu.uclm.esi.gamesgames.domain.Board;
import edu.uclm.esi.gamesgames.domain.Match;
import edu.uclm.esi.gamesgames.services.GamesService;
import edu.uclm.esi.gamesgames.ws.Manager;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation.class)
public class TestGamesManager {
	
	@MockBean
    private BoardDAO boardDAO;
	
	@MockBean
    private MatchDAO matchDAO;
	
	@MockBean
    private GamesService gamesService;

    private Manager manager;
    
    private String idMatch;

    @Test
    public void testAddPlayerInMatch() {
    	manager = Manager.get();
        manager.addPlayerInMatch("user1");
        assertTrue(manager.isPlayerInMatch("user1"));
    }
    
    @Test @Order(1)
	public void testAddMatch() throws Exception {
    	manager = Manager.get();
    	
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
		
		idMatch = match.getId();
		
		when(boardDAO.save(board1)).thenReturn(board1);
		when(boardDAO.save(board2)).thenReturn(board2);
		when(matchDAO.save(match)).thenReturn(match);
		/*doAnswer(invocation -> {
			return true;
		}).when(match).notifyWinner("Player1");*/
    	
		Manager.get().addMatch(match);
		Match result = manager.getMatch(match.getId());
		assertEquals(result.getId(), match.getId());
		verify(boardDAO).save(board1);
	}
    
    @Test @Order(2)
    public void testNewMove() {
    	manager = Manager.get();
    	
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

		String player = "Player1";
		when(boardDAO.save(board1)).thenReturn(board1);
		
		manager.NewMove(player, board1);
		verify(boardDAO).save(any(Board.class));
		
    }
    
    @Test @Order(3)
    public void testRemovePlayerInMatch() {
    	manager = Manager.get();
    	
		manager.removePlayerInMatch("Player1");
		assertFalse(manager.isPlayerInMatch("Player1"));
		
    }
    
    @Test
    public void testCloseMatch() {
    	manager = Manager.get();
    	
		manager.closeMatch("Player3");
		assertFalse(manager.isPlayerInMatch("Player3"));
		
    }
}

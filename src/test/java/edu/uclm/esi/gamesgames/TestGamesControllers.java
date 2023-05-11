package edu.uclm.esi.gamesgames;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import edu.uclm.esi.gamesgames.services.GamesService;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
public class TestGamesControllers {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private GamesService gamesService;

	@Test
	public void testRequestGameAlone() throws Exception {
		// Configurar el comportamiento del servicio para devolver un tablero
		String board = "0,0,0";
		doAnswer(invocation -> {
			return board;
		}).when(gamesService).requestGameAlone(anyString());

		// Realizar la petición
		MvcResult result = mockMvc.perform(get("/games/requestGameAlone").param("userName", "user123"))
				.andExpect(status().isOk()).andReturn();

		// Comprobar la respuesta
		JSONObject jsonResponse = new JSONObject(result.getResponse().getContentAsString());
		assertEquals(board, jsonResponse.getString("board"));

		// Realizar la petición
		result = mockMvc.perform(get("/games/requestGameAlone").param("userName", "")).andExpect(status().isConflict())
				.andReturn();

		// Comprobar la respuesta
		int statusCode = result.getResponse().getStatus();
		assertEquals(HttpStatus.CONFLICT.value(), statusCode);

		// Configurar el comportamiento del servicio para devolver una excepcion
		doAnswer(invocation -> {
			throw new Exception();
		}).when(gamesService).requestGameAlone(anyString());

		// Realizar la petición
		result = mockMvc.perform(get("/games/requestGameAlone").param("userName", "user123"))
				.andExpect(status().isInternalServerError()).andReturn();

		// Comprobar la respuesta
		statusCode = result.getResponse().getStatus();
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), statusCode);

	}

	@Test
	public void testMoveAlone() throws Exception {
		// Movimiento exitoso
		String userName = "user123";
		String move = "A1";
		String board = "0,0,0,0";
		doReturn(board).when(gamesService).moveAlone(move, userName);

		MvcResult result = mockMvc.perform(get("/games/moveAlone").param("move", move).param("userName", userName))
				.andExpect(status().isOk()).andReturn();

		JSONObject jsonResponse = new JSONObject(result.getResponse().getContentAsString());
		assertEquals(board, jsonResponse.getString("board"));

		// Error
		move = "";
		doThrow(new Exception()).when(gamesService).moveAlone(move, userName);
		result = mockMvc.perform(get("/games/moveAlone").param("move", move).param("userName", userName))
				.andExpect(status().isInternalServerError()).andReturn();

		//comprobar la respuesta
		int statusCode = result.getResponse().getStatus();
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), statusCode);

	}
	
	@Test
	public void testAddNumbersAloneException() throws Exception {
		//exito
		String userName = "user123";
	    String board = "0,0,0,0";
	    doReturn(board).when(gamesService).addNumbersAlone(userName);

	    MvcResult result = mockMvc.perform(get("/games/addNumbersAlone").param("userName", userName))
	            .andExpect(status().isOk()).andReturn();

	    JSONObject jsonResponse = new JSONObject(result.getResponse().getContentAsString());
	    assertEquals(board, jsonResponse.getString("board"));

		
		//error
	    doThrow(new Exception()).when(gamesService).addNumbersAlone(userName);

	    result = mockMvc.perform(get("/games/addNumbersAlone").param("userName", userName))
	            .andExpect(status().isInternalServerError()).andReturn();

	    int statusCode = result.getResponse().getStatus();
	    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), statusCode);
	}
}

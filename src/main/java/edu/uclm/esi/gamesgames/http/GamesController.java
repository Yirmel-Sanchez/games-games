package edu.uclm.esi.gamesgames.http;

import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import edu.uclm.esi.gamesgames.domain.Match;
import edu.uclm.esi.gamesgames.services.GamesService;

@RestController
@RequestMapping("games")
@CrossOrigin("*")
public class GamesController {

	final double precioJuego = 2.99;

	@Autowired
	private GamesService gamesService;

	@GetMapping("/requestGame")
	public Match requestGame(@RequestParam String juego, @RequestParam String idPlayer) {
		if (!juego.equals("nm"))
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encuentra ese juego");

		// Hacer una llamada HTTP GET al servidor externo para comprobar si el usuario
		// tiene saldo suficiente
		RestTemplate restTemplate = new RestTemplate();
		String url = "http://localhost:8082/users/balance/" + idPlayer;
		ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY,
				String.class);
		String responseBody = responseEntity.getBody();

		// Parsear la respuesta JSON para obtener los valores de userBalance y userName
		JSONObject responseJson = new JSONObject(responseBody);
		double userBalanceNum = responseJson.getDouble("userBalance");
		String userName = responseJson.getString("userName");

		int statusCode = responseEntity.getStatusCode().value();

		if (statusCode != 200)
			throw new ResponseStatusException(HttpStatus.CONFLICT, "No se ha podido consultar el saldo del usuario");

		if (userBalanceNum < precioJuego)
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Saldo insuficiente");

		Match match = null;
		try {
			match = this.gamesService.requestGame(juego, userName);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al asignar la partida");
		}

		// peticion para restar el saldo
		// Construir el objeto JSON para enviar en la petición POST
		JSONObject requestBody = new JSONObject();
		requestBody.put("idPlayer", idPlayer);
		requestBody.put("amount", precioJuego);

		// Hacer la petición HTTP POST al servidor externo para restar el saldo
		restTemplate = new RestTemplate();
		url = "http://localhost:8082/users/payGame";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> requestEntity = new HttpEntity<String>(requestBody.toString(), headers);
		responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

		// Verificar si la petición se realizó con éxito
		if (responseEntity.getStatusCode() != HttpStatus.OK) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al restar el saldo");
		}

		return match;
	}

	@PostMapping("/leaveGame")
	public ResponseEntity<String> leaveGame(@RequestBody Map<String, Object> requestBody) {
		String idPlayer = (String) requestBody.get("idPlayer");
		String idMatch = (String) requestBody.get("idMatch");
		
		try {
			// Hacer una llamada HTTP GET al servidor externo para comprobar el nombre del
			// usuario
			RestTemplate restTemplate = new RestTemplate();
			String url = "http://localhost:8082/users/balance/" + idPlayer;
			ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY,
					String.class);
			String responseBody = responseEntity.getBody();

			// Parsear la respuesta JSON para obtener el userName
			JSONObject responseJson = new JSONObject(responseBody);
			String userName = responseJson.getString("userName");

			int statusCode = responseEntity.getStatusCode().value();

			if (statusCode != 200)
				throw new Exception();

			this.gamesService.leaveGame(idMatch, userName);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al abandonar la partida");
		}
		
		// Devolver la respuesta JSON con la operación realizada con éxito
	    JSONObject responseBody = new JSONObject();
	    responseBody.put("message", "Partida abandonada con éxito");
	    return ResponseEntity.ok(responseBody.toString());
	}
}

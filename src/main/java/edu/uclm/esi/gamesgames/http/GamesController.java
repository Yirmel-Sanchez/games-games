package edu.uclm.esi.gamesgames.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import edu.uclm.esi.gamesgames.domain.Match;
import edu.uclm.esi.gamesgames.services.GamesService;

@RestController
@RequestMapping("games")
@CrossOrigin("*")
public class GamesController {
	@Autowired
	private GamesService gamesService;
	
	@GetMapping("/requestGame")
	public Match requestGame(@RequestParam String juego, @RequestParam String player) {
		if (!juego.equals("nm"))
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encuentra ese juego");
		
		return this.gamesService.requestGame(juego, player);
	}
}

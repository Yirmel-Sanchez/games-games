package edu.uclm.esi.gamesgames.ws;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;


@Configuration
@EnableWebSocket
public class WSConfigure implements WebSocketConfigurer {
	
	/*********************************************************************
	*
	* - Method name: registerWebSocketHandlers
	* - Description of the Method: Registers the WebSocketHandler for the WebSocket URL "/wsGames", sets the allowed origins to
	*  "*", and adds a HttpSessionHandshakeInterceptor as an interceptor.
	*
	*********************************************************************/
	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.
		addHandler(new WSGames(), "/wsGames").
		setAllowedOrigins("*").
		addInterceptors(new HttpSessionHandshakeInterceptor());
	}
}


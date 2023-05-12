package edu.uclm.esi.gamesgames;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.*;

@SpringBootApplication
@ServletComponentScan
public class GamesGamesApplication 
{
    public static void main( String[] args )
    {
        SpringApplication.run(GamesGamesApplication.class, args);
    }
}

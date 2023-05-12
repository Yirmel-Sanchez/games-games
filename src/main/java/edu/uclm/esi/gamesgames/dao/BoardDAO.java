package edu.uclm.esi.gamesgames.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.uclm.esi.gamesgames.domain.Board;


public interface BoardDAO  extends JpaRepository<Board, String>{
	
}

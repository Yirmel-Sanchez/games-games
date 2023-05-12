package edu.uclm.esi.gamesgames.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.uclm.esi.gamesgames.domain.Match;

public interface MatchDAO extends JpaRepository<Match, String> {

}

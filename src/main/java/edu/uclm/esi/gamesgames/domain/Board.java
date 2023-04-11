package edu.uclm.esi.gamesgames.domain;

import java.security.SecureRandom;

public class Board {
	
	private byte [][] digits;
	
	public Board() {
		SecureRandom dado = new SecureRandom();
		this.digits = new byte[9][9];
		for (int i = 0; i<3; i++) {
			for(int j = 0; j<9; j++) {
				this.digits[i][j] = (byte) dado.nextInt(1,10);
			}
		}		
	}

	public Board copy() {
		Board result = new Board();
		for (int i = 0; i<3; i++) {
			for(int j = 0; j<9; j++) {
				result.digits[i][j] = this.digits[i][j];
			}
		}
		return result;
	}
}

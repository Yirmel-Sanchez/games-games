package edu.uclm.esi.gamesgames.domain;

import java.security.SecureRandom;

public class Board {
	
	private byte [][][] digits;
	
	public Board() {
		SecureRandom dado = new SecureRandom();
		this.digits = new byte[9][9][2];
		for (int i = 0; i<3; i++) {
			for(int j = 0; j<9; j++) {
				this.digits[i][j][0] = (byte) dado.nextInt(1,10);
				this.digits[i][j][1] = (byte) 1;
			}
		}		
	}

	public Board copy() {
		Board result = new Board();
		for (int i = 0; i<9; i++) {
			for(int j = 0; j<9; j++) {
				result.digits[i][j][0] = this.digits[i][j][0];
				result.digits[i][j][1] = this.digits[i][j][1];
			}
		}
		return result;
	}
	
	public String toString() {
	    String result = "";
	    for (int i = 0; i < 9; i++) {
	    	for (int j = 0; j < 9; j++) {
	            result += digits[i][j][0] + "," + digits[i][j][1] + ",";
	        }
	    }
	    return result.substring(0, result.length()-1);
	}
}

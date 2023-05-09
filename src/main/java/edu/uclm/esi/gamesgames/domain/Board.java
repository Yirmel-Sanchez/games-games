package edu.uclm.esi.gamesgames.domain;

import java.security.SecureRandom;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "boards")
public class Board {
	@Id @Column(length=80)
	private String id;
	@Column(length=80)
	private String parent;
	@Column(length=323)
	private String board_values;
	@Transient
	private byte [][][] digits;
	@Transient
	private boolean boardEmpty;
	
	
	public Board() {
		SecureRandom dado = new SecureRandom();
		this.digits = new byte[9][9][2];
		for (int i = 0; i<3; i++) {
			for(int j = 0; j<9; j++) {
				this.digits[i][j][0] = (byte) dado.nextInt(1,10);
				this.digits[i][j][1] = (byte) 1;
			}
		}
		this.boardEmpty = false;
	}

	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public String getBoard_values() {
		return board_values;
	}

	public void setBoard_values(String board_values) {
		this.board_values = board_values;
	}
	
	public void setDigits(byte[][][] digits) {
		this.digits = digits;
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
	
	public byte [][][] getDigits(){
		return digits;
	}

	public boolean isBoardEmpty() {
		return boardEmpty;
	}

	public void setBoardEmpty(boolean boardEmpty) {
		this.boardEmpty = boardEmpty;
	}
	
	public void setParentMove(String idParent, String id){
		this.id = id;
		this.board_values = toString();
		this.parent = idParent;
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

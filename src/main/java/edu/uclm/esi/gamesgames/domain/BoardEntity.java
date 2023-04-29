package edu.uclm.esi.gamesgames.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "board")
public class BoardEntity {
	@Id
	@Column(length = 80)
	private String id;

	@Column(length = 80)
	private String parent;

	@Column(name = "board_values", length = 323)
	private String boardValues;

	public BoardEntity() {
	}

	public BoardEntity(Board board) {
		this.id = "";
		this.parent = null;
		this.boardValues = board.toString();
	}

	public Board toBoard() {
		Board board = new Board();
		// Parse the board values string back into the 3D array
		String[] values = this.boardValues.split(",");
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				int index = (i * 9 + j) * 2;
				board.getDigits()[i][j][0] = Byte.parseByte(values[index]);
				board.getDigits()[i][j][1] = Byte.parseByte(values[index + 1]);
			}
		}
		return board;
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

	public String getBoardValues() {
		return boardValues;
	}

	public void setBoardValues(String boardValues) {
		this.boardValues = boardValues;
	}
}

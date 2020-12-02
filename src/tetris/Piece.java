package tetris;

import java.awt.Color;
import java.awt.Rectangle;

public class Piece {
	private Rectangle square;
	private int type, offsetX, offsetY;
	
	public Piece(Rectangle square, int type, int offsetX, int offsetY) {
		this.square = square;
		this.type = type;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
	}
	
	public void move(int x, int y) {
		square.setLocation(square.x + x * Tetris.GRID, square.y + y * Tetris.GRID);
	}
	
	public static Color getColor(int type) {
		switch(type) {
		case 0: return new Color(0, 229, 249); //light blue
		case 1: return new Color(35, 7, 225); //blue
		case 2: return new Color(255, 110, 30); //orange
		case 3: return new Color(250, 235, 40); //yellow
		case 4: return new Color(100, 255, 14); //lime green
		case 5: return new Color(148, 0, 218); //purple
		default: return new Color(221, 12, 51); //red
		}
	}
	
	public Rectangle getSquare() {
		return square;
	}
	public int getType() {
		return type;
	}
	public int getOffsetX() {
		return offsetX;
	}
	public int getOffsetY() {
		return offsetY;
	}

	public void setSquare(Rectangle square) {
		this.square = square;
	}
	public void setOffsetX(int offsetX) {
		this.offsetX = offsetX;
	}
	public void setOffsetY(int offsetY) {
		this.offsetY = offsetY;
	}
}
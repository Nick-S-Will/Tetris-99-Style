package tetris;

import java.awt.Color;
import java.awt.Rectangle;

public class Piece {
	private Rectangle square;
	private final int type;
	private int offsetX;
	private int offsetY;
	
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
		return switch (type) {
			case 0 -> new Color(0, 229, 249); //light blue
			case 1 -> new Color(35, 7, 225); //blue
			case 2 -> new Color(255, 110, 30); //orange
			case 3 -> new Color(250, 235, 40); //yellow
			case 4 -> new Color(100, 255, 14); //lime green
			case 5 -> new Color(148, 0, 218); //purple
			default -> new Color(221, 12, 51); //red
		};
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
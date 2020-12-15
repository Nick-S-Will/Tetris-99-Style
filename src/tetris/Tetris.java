package tetris;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import javax.swing.*;

public class Tetris extends JPanel implements ActionListener, MouseListener, KeyListener {
	// Pixel Size of Tiles, Pixel Size of Tiles Borders
	public static final int GRID = 35, BORDER = 4, NEW_PIECE_INTERVAL = 100;

	JFrame F = new JFrame();
	public static Tetris tet;
	Timer T = new Timer(1, this);

	// Tiles to Show Where Your Piece Will Land
	ArrayList<Rectangle> ghost = new ArrayList<>();
	// Tiles in Your Piece
	ArrayList<Piece> inFall = new ArrayList<>(), landed = new ArrayList<>();
	// Tiles to Display the Type of Next Piece
	ArrayList<Piece> nextDisplay = new ArrayList<>(), holdDisplay = new ArrayList<>();
	// Array of Integers that Decides the Order of the Piece Types
	ArrayList<Integer> nextTypes = new ArrayList<>();

	// Time Handling Integers
	int ticks, timeToNextPiece, currentFallInt = 200, fallInt = 200, currentMoveInt = 80,
			moveStartTick, speedUpAmount = 7, clearStartTick;
	// Piece Handling Integers
	int currentType, nextType, heldType = -1, landCount = 1;
	// Clear Handling Integers
	int lines, singles, doubles, triples, tetrises, clearLevel, clearCount;

	// Array Holding the State of Each Space in the Grid
	static boolean[][] spaces = new boolean[10][20];
	boolean gameOver = false, falling = false, left = false, right = false, paused = false,
			canMove = true, canHold = true, canDrop = true;

	public Tetris() {
		// Makes Frame
		F.add(this);
		F.setTitle("Tetris");
		F.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		F.setSize(15 * GRID + 14, 20 * GRID + 37);
		F.setLocationRelativeTo(null);
		F.setResizable(false);
		F.addKeyListener(this);
		F.addMouseListener(this);
		F.setVisible(true);

		// Adds Piece Types to List
		for (int i = 0;i < 7;i++) nextTypes.add(i);
		Collections.shuffle(nextTypes);
		nextType = nextTypes.get(0);

		// Start Clock
		T.start();
	}

	public static void main(String[] args) {
		tet = new Tetris();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// Try Move Piece
		if ((ticks - moveStartTick) % currentMoveInt == 0 && canMove && falling && !gameOver) {
			if (left ^ right) {
				int direction = right ? 1 : -1, wall = right ? 9 : 0;
				boolean canMove = true;

				// Loops If Each Tile Can Move
				for (Piece i: inFall) {
					Rectangle rec = i.getSquare();
					// If Next to Wall or Landed Piece
					if (rec.x == wall * GRID || spaces[rec.x / GRID + direction][rec.y / GRID]) {
						canMove = false;
						break;
					}
				}

				if (canMove) for (Piece i: inFall) i.move(direction, 0);
			}
		}

		// Applies Gravity
		if (ticks % currentFallInt == 0 && !gameOver) {
			// Loops If Space Under Piece
			for (Piece i: inFall) {
				Rectangle rec = i.getSquare();
				// If Above Landed Piece
				if (rec.y == 19 * GRID || spaces[rec.x / GRID][rec.y / GRID + 1]) {
					falling = false;
					break;
				}
			}

			if (falling) for (Piece i : inFall) i.move(0, 1);
			else if (timeToNextPiece == -1) timeToNextPiece = NEW_PIECE_INTERVAL;
		}

		// Spawns Next Piece
		if (timeToNextPiece == 0) {
			// Moves Piece to Landed Pieces
			landed.addAll(inFall);
			updateSpaces(true);

			setNextDisplay();

			// Sets Conditions to Normal States
			canMove = true;
			canHold = true;
			falling = true;
			currentFallInt = fallInt;
		}
		if (timeToNextPiece > -1) timeToNextPiece--;

		// Applies Gravity on Clear With an Interval
		if ((clearStartTick - ticks) % 50 == 0 && clearCount > 0) {
			for (Piece i : landed) if (i.getSquare().y < clearLevel) i.move(0, 1);
			updateSpaces(false);
			clearCount--;
			if (clearCount == 0) canDrop = true;
		}

		// Makes Time Pass
		if (!paused && !gameOver) ticks++;

		// Draws Graphics
		repaint();
	}

	public void paintComponent(Graphics g) {
		// Draw Backgrounds
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, 15 * GRID, 20 * GRID);
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, 10 * GRID, 20 * GRID);

		// Draw Next and Hold Bounds
		g.drawRect(105 * GRID / 10, GRID, 5 * GRID, 3 * GRID);
		g.drawRect(105 * GRID / 10, 5 * GRID, 5 * GRID, 3 * GRID);

		// Draws Text
		g.setFont(new Font("Impact", Font.PLAIN, 30));
		g.drawString("NEXT", 105 * GRID / 10, GRID);
		g.drawString("HOLD", 105 * GRID / 10, 5 * GRID);
		g.drawString("LINES " + lines, 105 * GRID / 10, 10 * GRID);
		g.drawString("SINGLES " + singles, 105 * GRID / 10, 12 * GRID);
		g.drawString("DOUBLES " + doubles, 105 * GRID / 10, 13 * GRID);
		g.drawString("TRIPLES " + triples, 105 * GRID / 10, 14 * GRID);
		g.drawString("TETRIS " + tetrises, 105 * GRID / 10, 15 * GRID);

		// Draws All Pieces
		drawPieces(g, inFall);
		Color c = g.getColor();
		drawPieces(g, landed);
		drawPieces(g, nextDisplay);
		drawPieces(g, holdDisplay);

		// Makes Ghost Piece
		g.setColor(c);
		ghost.clear();
		// ghost Deep Copies inFall
		for (Piece i : inFall) {
			Rectangle rec = i.getSquare();
			ghost.add(new Rectangle(rec.x, rec.y, rec.width, rec.height));
		}
		// Moves Ghost Piece Down as Far as Possible
		int moveCount = -1;
		for (int i = 1;i < 20;i++) {
			int count = 0;

			// Counts If All Pieces Can Move Down
			for (Rectangle j : ghost)
				if (j.y / GRID + i < 20 && !spaces[j.x / GRID][j.y / GRID + i]) count++;

			if (count == 4) moveCount++;
			else break;
		}
		for (Rectangle i : ghost) i.setLocation(i.x, i.y + moveCount * GRID);
		// Draws Ghost Piece
		if (!inFall.isEmpty())
			if (inFall.get(0).getSquare().y - GRID != ghost.get(0).y)
				for (Rectangle i : ghost) g.drawRect(i.x, i.y + GRID, GRID - 1, GRID - 1);

		// Draws Messages
		g.setFont(new Font("Impact", Font.PLAIN, 70));
		if (paused) g.drawString("PAUSED", 70, F.getHeight() / 2 - 20);
		if (gameOver) g.drawString("GAME OVER", 20, F.getHeight() / 2 - 20);
	}

	void drawPieces(Graphics g, ArrayList<Piece> pieces) {
		int pieceInnerSize = GRID - 2 * BORDER;

		for (Piece i : pieces) {
			Rectangle rec = i.getSquare();
			g.setColor(Piece.getColor(i.getType()).darker());
			g.fillRect(rec.x, rec.y, GRID, GRID);
			g.setColor(Piece.getColor(i.getType()));
			g.fillRect(rec.x + BORDER, rec.y + BORDER, pieceInnerSize, pieceInnerSize);
		}
	}

	void tryRotate(int dir) {
		if (inFall.get(0).getType() != 3 && !paused && falling) {
			var rotatePoints = new Point[4];
			int clearCount = 0;

			// Calculates Rotate Points
			for (int i = 0;i < 4;i++){
				rotatePoints[i] = new Point(
						dir * -1 * inFall.get(i).getOffsetY() + inFall.get(0).getSquare().x,
						dir * inFall.get(i).getOffsetX() + inFall.get(0).getSquare().y);
			}

			// Counts the Amount that Can Rotate
			for (int i = 0;i < 4;i++) {
				try {
					if (!spaces[rotatePoints[i].x / GRID][rotatePoints[i].y / GRID]) clearCount += 1;
				} catch (Exception e) {break;}
			}

			// Moves Pieces to Rotate Points
			if (clearCount == 4) for (Piece i : inFall) {
				int ind = inFall.indexOf(i);

				i.setSquare(new Rectangle(rotatePoints[ind].x, rotatePoints[ind].y, GRID, GRID));
				i.setOffsetX(rotatePoints[ind].x - inFall.get(0).getSquare().x);
				i.setOffsetY(rotatePoints[ind].y - inFall.get(0).getSquare().y);
			}
		}
	}

	void setNextDisplay(){
		// Makes New Piece
		inFall = setPiece(nextType);
		nextType = nextTypes.get(landCount);
		heldType = currentType;

		// If All Types Have Been Used
		if (landCount == 6) {
			landCount = 0;
			Collections.shuffle(nextTypes);
		}
		else landCount++;

		// Makes New Next Piece Display
		nextDisplay = setPiece(nextType);
		for (Piece i: nextDisplay) i.getSquare().setLocation(
				i.getSquare().x + 80 * GRID / 10, i.getSquare().y + 15 * GRID / 10);
	}

	void holdPiece() {
		canHold = false;

		// First Hold
		if (heldType == -1)  setNextDisplay();
		else {
			// Make Held Piece
			inFall = setPiece(heldType);
			// Swap Held and Current Types
			var temp = currentType;
			currentType = heldType;
			heldType = temp;
		}

		// Display Hold Piece
		holdDisplay = setPiece(heldType);
		for (Piece i: holdDisplay) i.getSquare().setLocation(
				i.getSquare().x + 80 * GRID / 10, i.getSquare().y + 55 * GRID / 10);
	}

	public void updateSpaces(boolean checkClear) {
		// Sets all spaces to false
		for (int x = 0;x < 10;x++) for (int y = 0;y < 20;y++) spaces[x][y] = false;

		// Loops Setting spaces to True
		for (Piece i : landed) {
			int x = i.getSquare().x / GRID;
			int y = i.getSquare().y / GRID;
			// If 2 Pieces in the Same Position
			if (spaces[x][y]) {
				checkClear = false;
				gameOver = true; // GG
				break;
			}
			spaces[x][y] = true;
		}

		if (checkClear) {
			// Checks All 20 Rows for Clears
			for (int y = 0;y < 20;y++) {
				int count = 0;
				// Counts Pieces in Row
				for (int x = 0;x < 10;x++) if (spaces[x][y]) count++;
				else break;

				// If Full Row
				if (count == 10) {
					canDrop = false;
					// Removes Pieces in Full Row
					for (Iterator<Piece> i = landed.iterator();i.hasNext();) {
						Piece j = i.next();
						if (j.getSquare().y == y * GRID) i.remove();
					}

					// Sets Lowest Clear Row
					clearLevel = y * GRID;
					clearCount++;
					lines++;

					if (lines % 5 == 0) {
						// Speeds Up Game
						fallInt -= speedUpAmount - (lines / 50);
						fallInt = Math.min(Math.max(fallInt, 30), 500);
						currentFallInt = fallInt;

						if (lines % 25 == 0) {
							currentMoveInt--;
							currentMoveInt = Math.min(Math.max(currentMoveInt, 60), 80);
						}
					}
				}
			}
			// Increases Stats
			switch (clearCount) {
				case 1 -> singles++;
				case 2 -> doubles++;
				case 3 -> triples++;
				case 4 -> tetrises++;
			}
			if (clearCount > 0) clearStartTick = ticks;
		}
	}

	// Gets Piece of Type type's Default Values
	ArrayList<Piece> setPiece(int type) {
		var temp = new ArrayList<Piece>();

		switch(type) {
			case 0: //I block
				temp.add(new Piece(new Rectangle(5 * GRID, 0, GRID, GRID), type, 0, 0));
				temp.add(new Piece(new Rectangle(3 * GRID, 0, GRID, GRID), type, -2 * GRID, 0));
				temp.add(new Piece(new Rectangle(4 * GRID, 0, GRID, GRID), type, -GRID, 0));
				temp.add(new Piece(new Rectangle(6 * GRID, 0, GRID, GRID), type, GRID, 0));
				break;
			case 1: //J block
				temp.add(new Piece(new Rectangle(4 * GRID, GRID, GRID, GRID), type, 0, 0));
				temp.add(new Piece(new Rectangle(3 * GRID, 0, GRID, GRID), type, -GRID, -GRID));
				temp.add(new Piece(new Rectangle(3 * GRID, GRID, GRID, GRID), type, -GRID, 0));
				temp.add(new Piece(new Rectangle(5 * GRID, GRID, GRID, GRID), type, GRID, 0));
				break;
			case 2: //L block
				temp.add(new Piece(new Rectangle(4 * GRID, GRID, GRID, GRID), type, 0, 0));
				temp.add(new Piece(new Rectangle(5 * GRID, 0, GRID, GRID), type, GRID, -GRID));
				temp.add(new Piece(new Rectangle(3 * GRID, GRID, GRID, GRID), type, -GRID, 0));
				temp.add(new Piece(new Rectangle(5 * GRID, GRID, GRID, GRID), type, GRID, 0));
				break;
			case 3: //O block
				temp.add(new Piece(new Rectangle(4 * GRID, 0, GRID, GRID), type, 0, 0));
				temp.add(new Piece(new Rectangle(5 * GRID, 0, GRID, GRID), type, 0, 0));
				temp.add(new Piece(new Rectangle(4 * GRID, GRID, GRID, GRID), type, 0, 0));
				temp.add(new Piece(new Rectangle(5 * GRID, GRID, GRID, GRID), type, 0, 0));
				break;
			case 4: //S block
				temp.add(new Piece(new Rectangle(4 * GRID, GRID, GRID, GRID), type, 0, 0));
				temp.add(new Piece(new Rectangle(4 * GRID, 0, GRID, GRID), type, 0, -GRID));
				temp.add(new Piece(new Rectangle(5 * GRID, 0, GRID, GRID), type, GRID, -GRID));
				temp.add(new Piece(new Rectangle(3 * GRID, GRID, GRID, GRID), type, -GRID, 0));
				break;
			case 5: //T block
				temp.add(new Piece(new Rectangle(4 * GRID, GRID, GRID, GRID), type, 0, 0));
				temp.add(new Piece(new Rectangle(4 * GRID, 0, GRID, GRID), type, 0, -GRID));
				temp.add(new Piece(new Rectangle(3 * GRID, GRID, GRID, GRID), type, -GRID, 0));
				temp.add(new Piece(new Rectangle(5 * GRID, GRID, GRID, GRID), type, GRID, 0));
				break;
			case 6: //Z block
				temp.add(new Piece(new Rectangle(4 * GRID, GRID, GRID, GRID), type, 0, 0));
				temp.add(new Piece(new Rectangle(3 * GRID, 0, GRID, GRID), type, -GRID, -GRID));
				temp.add(new Piece(new Rectangle(4 * GRID, 0, GRID, GRID), type, 0, -GRID));
				temp.add(new Piece(new Rectangle(5 * GRID, GRID, GRID, GRID), type, GRID, 0));
				break;
		}
		return temp;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) tryRotate(-1); // Rotate left
		if (e.getButton() == MouseEvent.BUTTON3) tryRotate(1); // Rotate right
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_P && !gameOver) paused = !paused;

		if (!paused && !gameOver) {
			// Move Left
			if (e.getKeyCode() == KeyEvent.VK_LEFT) {
				moveStartTick = ticks;
				left = true;
			}
			// Move Right
			else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
				moveStartTick = ticks;
				right = true;
			}
			// Hard Drop
			else if (e.getKeyCode() == KeyEvent.VK_UP) {
				if (canDrop) {
					currentFallInt = 1;
					canMove = false;
					canHold = false;
				}
			}
			// Soft Drop
			else if (e.getKeyCode() == KeyEvent.VK_DOWN) currentFallInt = fallInt / 5;
			// Hold
			else if (e.getKeyCode() == KeyEvent.VK_CONTROL && canHold) holdPiece();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_LEFT) left = false;
		if (e.getKeyCode() == KeyEvent.VK_RIGHT) right = false;
		if (e.getKeyCode() == KeyEvent.VK_DOWN) currentFallInt = fallInt;
	}

	// Extra Implemented Methods from MouseListener and KeyListener
	@Override
	public void mouseClicked(MouseEvent e) {}
	@Override
	public void mouseReleased(MouseEvent e) {}
	@Override
	public void mouseEntered(MouseEvent e) {}
	@Override
	public void mouseExited(MouseEvent e) {}
	@Override
	public void keyTyped(KeyEvent e) {}
}
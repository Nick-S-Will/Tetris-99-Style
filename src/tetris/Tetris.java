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

import javax.swing.JFrame;
import javax.swing.Timer;

public class Tetris implements ActionListener, KeyListener, MouseListener {
	public static final int GRID = 35, BORDER = 4, NEXTPIECEINT = 75;
	static JFrame F = new JFrame();
	static Rend rend = new Rend();
	static Tetris tet = new Tetris();
	static Timer T;
	ArrayList<Rectangle> ghost = new ArrayList<Rectangle>();
	ArrayList<Piece> inFall = new ArrayList<Piece>(), landed = new ArrayList<Piece>(), nextDisplay = new ArrayList<Piece>(), holdDisplay = new ArrayList<Piece>();
	ArrayList<Integer> next = new ArrayList<Integer>();
	int ticks = -1, timeToNextPiece, currentFallInt = 200, fallInt = 200, currentMoveInt = 80, moveStartTick, currentType, nextType, holdType = -1, clearCount, clearLevel, lines, landCount = 1;
	int nextOffX = 80, nextOffY = 15, holdOffX = 80, holdOffY = 55, speedUpAmount = 7, singles, doubles, triples, tetrises;
	static boolean[][] spaces = new boolean[10][20];
	boolean gameOver = false, falling = false, left = false, right = false, paused = false, canMove = true, canHold = true, canDrop = true;

	public Tetris() {
		F.add(rend);
		F.setTitle("Tetris");	
		F.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		F.setSize(15 * GRID + 14, 20 * GRID + 37);
		F.setLocationRelativeTo(null);
		F.setResizable(false);
		F.addKeyListener(this);
		F.addMouseListener(this);
		F.setVisible(true);
		T = new Timer(1, this);
		
		for (int i = 0;i < 7;i++) next.add(i);
		Collections.shuffle(next);
		nextType = next.get(0);
	}

	public static void main(String[] args) {
		T.start();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (!paused && !gameOver) ticks++;

		//move current piece
		if ((ticks - moveStartTick) % currentMoveInt == 0 && canMove && !gameOver && falling) {
			if (left ^ right) {
				int direction = 1, wall = 9;
				boolean canMove = true;

				if (left) {
					direction = -1;
					wall = 0;
				}

				for (Piece i: inFall) {
					Rectangle rec = i.getSquare();
					if (rec.x == wall * GRID || spaces[rec.x / GRID + direction][rec.y / GRID]) canMove = false;
				}

				if (canMove) for (Piece i: inFall) i.move(direction, 0);
			}
		}

		//current piece gravity
		if (ticks % currentFallInt == 0 && !gameOver) {
			for (Piece i: inFall) {
				Rectangle rec = i.getSquare();
				if (rec.y == 19 * GRID || spaces[rec.x / GRID][rec.y / GRID + 1]) falling = false;
			}
			if (falling) for (Piece i : inFall) i.move(0, 1);
			else if (timeToNextPiece == -1) {
				timeToNextPiece = NEXTPIECEINT;
			}
		}
		
		if (timeToNextPiece == 0) {
			for (Piece i : inFall) landed.add(i);
			updateSpaces(true);
			inFall = setPiece(nextType);
			currentType = nextType;
			nextType = next.get(landCount);
			
			nextDisplay = setPiece(nextType);
			for (Piece i: nextDisplay) i.getSquare().setLocation(i.getSquare().x + nextOffX * GRID / 10, i.getSquare().y + nextOffY * GRID / 10);
			
			canMove = true;
			canHold = true;
			falling = true;
			currentFallInt = fallInt;
			if (landCount == 6) {
				landCount = 0;
				Collections.shuffle(next);
			}
			else landCount++;
		}
		if (timeToNextPiece > -1) timeToNextPiece--;

		//moving lines above cleared lines
		if (ticks % 50 == 0 && clearCount > 0) {
			for (Piece i : landed) if (i.getSquare().y < clearLevel) i.move(0, 1);
			updateSpaces(false);
			clearCount--;
			if (clearCount == 0) canDrop = true;
		}

		rend.repaint();
	}

	public void repaint(Graphics g) {
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, 15 * GRID, 20 * GRID);
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, 10 * GRID, 20 * GRID);

		g.drawRect(105 * GRID / 10, 1 * GRID, 5 * GRID, 3 * GRID);
		g.drawRect(105 * GRID / 10, 5 * GRID, 5 * GRID, 3 * GRID);

		g.setFont(new Font("Impact", Font.PLAIN, 30));
		g.drawString("NEXT", 105 * GRID / 10, 1 * GRID);
		g.drawString("HOLD", 105 * GRID / 10, 5 * GRID);
		g.drawString("LINES " + lines, 105 * GRID / 10, 10 * GRID);
		g.drawString("SINGLES " + singles, 105 * GRID / 10, 12 * GRID);
		g.drawString("DOUBLES " + doubles, 105 * GRID / 10, 13 * GRID);
		g.drawString("TRIPLES " + triples, 105 * GRID / 10, 14 * GRID);
		g.drawString("TETRIS " + tetrises, 105 * GRID / 10, 15 * GRID);

		drawPieces(g, inFall);
		Color c = g.getColor();
		drawPieces(g, landed);
		drawPieces(g, nextDisplay);
		drawPieces(g, holdDisplay);

		g.setColor(c);
		ghost.clear();
		for (Piece i : inFall) {
			Rectangle rec = i.getSquare();
			ghost.add(new Rectangle(rec.x, rec.y, rec.width, rec.height));
		}
		for (int i = 0;i < 20;i++) {
			int count = 0;
			for (Rectangle j : ghost) {
				if (j.y / GRID < 18) {
					if (spaces[j.x / GRID][j.y / GRID + 2]) break;
					else count++;
				}
			}
			if (count < 4) break;
			else for (Rectangle k : ghost) k.setLocation(k.x, k.y + GRID);
		}
		if (!ghost.isEmpty()) if (!(inFall.get(0).getSquare().y == ghost.get(0).y)) for (Rectangle i : ghost) g.drawRect(i.x, i.y + GRID, GRID - 1, GRID - 1);

		g.setFont(new Font("Impact", Font.PLAIN, 70));
		if (paused) g.drawString("PAUSED", 70, F.getHeight() / 2 - 20);
		if (gameOver) g.drawString("GAME OVER", 20, F.getHeight() / 2 - 20);
	}

	void tryRotate(int dir) {
		if (inFall.get(0).getType() != 3 && !paused && falling) {
			int clearCount = 0;

			for (Piece i : inFall) {
				Rectangle rec = inFall.get(0).getSquare();
				Point tar = new Point(dir * -1 * i.getOffsetY() + rec.x, dir * i.getOffsetX() + rec.y);

				try {
					if (!spaces[tar.x / GRID][tar.y / GRID]) clearCount += 1;
				} catch (Exception e) {
					break;
				}
			}

			if (clearCount == 4) for (Piece i : inFall) {
				Rectangle rec = inFall.get(0).getSquare();
				Point tar = new Point(dir * -1 * i.getOffsetY() + rec.x, dir * i.getOffsetX() + rec.y);

				i.setSquare(new Rectangle(tar.x, tar.y, GRID, GRID));
				i.setOffsetX(tar.x - rec.x);
				i.setOffsetY(tar.y - rec.y);
			}
		}
	}

	public void updateSpaces(boolean checkClear) {
		for (int x = 0;x < 10;x++) for (int y = 0;y < 20;y++) spaces[x][y] = false;

		for (Piece i : landed) {
			int x = i.getSquare().x / GRID;
			int y = i.getSquare().y / GRID;
			if (spaces[x][y]) {
				checkClear = false;
				gameOver = true;
				break;
			}
			spaces[x][y] = true;
		}
		
		if (checkClear) {
			for (int y = 0;y < 20;y++) {
				int count = 0;
				for (int x = 0;x < 10;x++) if (spaces[x][y]) count++;
				else break;
				if (count == 10) {
					canDrop = false;
					for (Iterator<Piece> i = landed.iterator();i.hasNext();) {
						Piece j = i.next();
						if (j.getSquare().y == y * GRID) {
							i.remove();
						}
					}

					clearLevel = y * GRID;
					clearCount++;
					lines++;
					if (lines % 5 == 0) {
						fallInt -= speedUpAmount - (lines / 50);
						fallInt = Clamp(fallInt, 30, 200);
						currentFallInt = fallInt;
						if (lines % 25 == 0) {
							currentMoveInt--;
							currentMoveInt = Clamp(currentMoveInt, 70, 80);
						}
					}
				}
			}
			switch(clearCount) {
			case 1: singles++; break;
			case 2: doubles++; break;
			case 3: triples++; break;
			case 4: tetrises++;
			}
		}
	}

	ArrayList<Piece> setPiece(int type) {
		ArrayList<Piece> temp = new ArrayList<Piece>();

		switch(type) {
		case 0: //I block
			temp.add(new Piece(new Rectangle(5 * GRID, 0 * GRID, GRID, GRID), type, 0, 0));
			temp.add(new Piece(new Rectangle(3 * GRID, 0 * GRID, GRID, GRID), type, -2 * GRID, 0 * GRID));
			temp.add(new Piece(new Rectangle(4 * GRID, 0 * GRID, GRID, GRID), type, -1 * GRID, 0 * GRID));
			temp.add(new Piece(new Rectangle(6 * GRID, 0 * GRID, GRID, GRID), type, 1 * GRID, 0 * GRID));
			break;
		case 1: //J block
			temp.add(new Piece(new Rectangle(4 * GRID, 1 * GRID, GRID, GRID), type, 0, 0));
			temp.add(new Piece(new Rectangle(3 * GRID, 0 * GRID, GRID, GRID), type, -1 * GRID, -1 * GRID));
			temp.add(new Piece(new Rectangle(3 * GRID, 1 * GRID, GRID, GRID), type, -1 * GRID, 0 * GRID));
			temp.add(new Piece(new Rectangle(5 * GRID, 1 * GRID, GRID, GRID), type, 1 * GRID, 0 * GRID));
			break;
		case 2: //L block
			temp.add(new Piece(new Rectangle(4 * GRID, 1 * GRID, GRID, GRID), type, 0, 0));
			temp.add(new Piece(new Rectangle(5 * GRID, 0 * GRID, GRID, GRID), type, 1 * GRID, -1 * GRID));
			temp.add(new Piece(new Rectangle(3 * GRID, 1 * GRID, GRID, GRID), type, -1 * GRID, 0 * GRID));
			temp.add(new Piece(new Rectangle(5 * GRID, 1 * GRID, GRID, GRID), type, 1 * GRID, 0 * GRID));
			break;
		case 3: //O block
			temp.add(new Piece(new Rectangle(4 * GRID, 0 * GRID, GRID, GRID), type, 0, 0));
			temp.add(new Piece(new Rectangle(5 * GRID, 0 * GRID, GRID, GRID), type, 0, 0));
			temp.add(new Piece(new Rectangle(4 * GRID, 1 * GRID, GRID, GRID), type, 0, 0));
			temp.add(new Piece(new Rectangle(5 * GRID, 1 * GRID, GRID, GRID), type, 0, 0));
			break;
		case 4: //S block
			temp.add(new Piece(new Rectangle(4 * GRID, 1 * GRID, GRID, GRID), type, 0, 0));
			temp.add(new Piece(new Rectangle(4 * GRID, 0 * GRID, GRID, GRID), type, 0 * GRID, -1 * GRID));
			temp.add(new Piece(new Rectangle(5 * GRID, 0 * GRID, GRID, GRID), type, 1 * GRID, -1 * GRID));
			temp.add(new Piece(new Rectangle(3 * GRID, 1 * GRID, GRID, GRID), type, -1 * GRID, 0 * GRID));
			break;
		case 5: //T block
			temp.add(new Piece(new Rectangle(4 * GRID, 1 * GRID, GRID, GRID), type, 0, 0));
			temp.add(new Piece(new Rectangle(4 * GRID, 0 * GRID, GRID, GRID), type, 0 * GRID, -1 * GRID));
			temp.add(new Piece(new Rectangle(3 * GRID, 1 * GRID, GRID, GRID), type, -1 * GRID, 0 * GRID));
			temp.add(new Piece(new Rectangle(5 * GRID, 1 * GRID, GRID, GRID), type, 1 * GRID, 0 * GRID));
			break;
		case 6: //Z block
			temp.add(new Piece(new Rectangle(4 * GRID, 1 * GRID, GRID, GRID), type, 0, 0));
			temp.add(new Piece(new Rectangle(3 * GRID, 0 * GRID, GRID, GRID), type, -1 * GRID, -1 * GRID));
			temp.add(new Piece(new Rectangle(4 * GRID, 0 * GRID, GRID, GRID), type, 0 * GRID, -1 * GRID));
			temp.add(new Piece(new Rectangle(5 * GRID, 1 * GRID, GRID, GRID), type, 1 * GRID, 0 * GRID));
			break;
		}
		return temp;
	}

	void holdPiece() {
		canHold = false;
		if (holdType == -1) {
			inFall = setPiece(nextType);
			nextType = next.get(landCount);
			holdType = currentType;
			
			if (landCount == 6) {
				landCount = 0;
				Collections.shuffle(next);
			}
			else landCount++;
			
			nextDisplay = setPiece(nextType);
			for (Piece i: nextDisplay) i.getSquare().setLocation(i.getSquare().x + nextOffX * GRID / 10, i.getSquare().y + nextOffY * GRID / 10);
		} 
		else {
			inFall = setPiece(holdType);
			int holder = currentType;
			currentType = holdType;
			holdType = holder;
		}

		holdDisplay = setPiece(holdType);
		for (Piece i: holdDisplay) i.getSquare().setLocation(i.getSquare().x + holdOffX * GRID / 10, i.getSquare().y + holdOffY * GRID / 10);
	}

	void drawPieces(Graphics g, ArrayList<Piece> a) {
		for (Piece i : a) {
			Rectangle rec = i.getSquare();
			g.setColor(Piece.getColor(i.getType()).darker());
			g.fillRect(rec.x, rec.y, GRID, GRID);
			g.setColor(Piece.getColor(i.getType()));
			g.fillRect(rec.x + BORDER, rec.y + BORDER, GRID - 2 * BORDER, GRID - 2 * BORDER);
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) tryRotate(-1); //rotate left
		if (e.getButton() == MouseEvent.BUTTON3) tryRotate(1); //rotate right
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_P && !gameOver) paused = !paused;
		
		if (!paused && !gameOver) {
			if (e.getKeyCode() == KeyEvent.VK_LEFT) {
				moveStartTick = ticks + 1;
				left = true;
			}
			if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
				moveStartTick = ticks +1;
				right = true;
			}
			if (e.getKeyCode() == KeyEvent.VK_UP) {
				if (canDrop) {
					currentFallInt = 1;
					canMove = false;
					canHold = false;
				}
			}
			if (e.getKeyCode() == KeyEvent.VK_DOWN) currentFallInt = fallInt / 5;
			if (e.getKeyCode() == KeyEvent.VK_CONTROL && canHold) holdPiece();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_LEFT) left = false;
		if (e.getKeyCode() == KeyEvent.VK_RIGHT) right = false;
		if (e.getKeyCode() == KeyEvent.VK_DOWN) currentFallInt = fallInt;
	}

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
	
	int Clamp(int value, int min, int max) {
		if (value < min) value = min;
		else if (value > max) value = max;
		return value;
	}
}
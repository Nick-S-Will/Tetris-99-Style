package tetris;

import java.awt.Graphics;

import javax.swing.JPanel;

public class Rend extends JPanel {
	private static final long serialVersionUID = 1L;

	protected void paintComponent(Graphics g) {
		Tetris.tet.repaint(g);
	}
}
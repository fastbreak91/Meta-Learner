package handwriting.gui;

import handwriting.core.Drawing;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings("serial")
public class DrawingPanel extends JPanel {
	private Drawing d;
	
	public DrawingPanel(Drawing d) {
		super();
		this.d = d;
		setBackground(Color.white);
	}
	
	public Drawing getDrawing() {return d;}
	
	public void resetDrawing(Drawing newDrawing) {
		d = newDrawing;
		repaint();
	}
	
	public int xCell() {
		return getWidth() / d.getWidth();
	}
	
	public int yCell() {
		return getHeight() / d.getHeight();
	}
	
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		for (int x = 0; x < d.getWidth(); ++x) {
			for (int y = 0; y < d.getHeight(); ++y) {
				if (d.isSet(x, y)) {
					g.fillRect(x * xCell(), y * yCell(), xCell(), yCell());
				}
			}
		}
	}
}

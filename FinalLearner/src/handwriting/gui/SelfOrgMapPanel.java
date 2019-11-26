package handwriting.gui;

import java.awt.Color;
import java.awt.Graphics;

import handwriting.learners.FloatDrawing;
import handwriting.learners.SelfOrgMap;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class SelfOrgMapPanel extends JPanel {
	private SelfOrgMap som;
	
	public SelfOrgMapPanel(SelfOrgMap som) {
		super();
		this.som = som;
		setBackground(Color.white);
	}
	
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		double xScale = (getWidth() / som.getGridSide());
		double yScale = (getHeight() / som.getGridSide());
		
		for (int x = 0; x < som.getGridSide(); ++x) {
			for (int y = 0; y < som.getGridSide(); ++y) {
				FloatDrawing fd = som.at(x, y).getIdealInput();
				int xStart = (int)(x * xScale);
				int yStart = (int)(y * yScale);
				int xEnd = (int)((x + 1) * xScale);
				int yEnd = (int)((y + 1) * yScale);
				double xDrawScale = (double)(xEnd - xStart) / fd.getWidth();
				double yDrawScale = (double)(yEnd - yStart) / fd.getHeight();
				for (int xSub = xStart; xSub < xEnd; ++xSub) {
					for (int ySub = yStart; ySub < yEnd; ++ySub) {
						int xRef = (int)((xSub - xStart) / xDrawScale);
						int yRef = (int)((ySub - yStart) / yDrawScale);
						int gray = (int)((1 - fd.at(xRef, yRef)) * 255);
						g.setColor(new Color(gray,gray,gray));
						g.fillRect(xSub, ySub, 1, 1);
					}
				}
			}
		}
	}
}

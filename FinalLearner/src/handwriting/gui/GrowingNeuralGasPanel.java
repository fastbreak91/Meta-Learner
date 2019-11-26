package handwriting.gui;

import java.awt.Color;
import java.awt.Graphics;

import handwriting.learners.FloatDrawing;
import handwriting.learners.GrowingNeuralGas;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class GrowingNeuralGasPanel extends JPanel {
	private GrowingNeuralGas gng;
	
	public GrowingNeuralGasPanel(GrowingNeuralGas gng) {
		super();
		this.gng = gng;
		setBackground(Color.white);
	}
	
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		int gridSide = (int)Math.ceil(Math.sqrt(gng.numNodes()));
		double xScale = (getWidth() / gridSide);
		double yScale = (getHeight() / gridSide);

		int x = 0, y = 0;
		for (GrowingNeuralGas.Neuron node: gng) {
			FloatDrawing fd = node.getIdealInput();
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
			
			x += 1;
			if (x == gridSide) {
				x = 0;
				y += 1;
			}
		}
	}
}

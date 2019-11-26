package handwriting.gui;

import handwriting.core.Drawing;

import java.awt.event.*;

public class MousePencil implements MouseMotionListener {
	private DrawingPanel v;
	private boolean isDrawing;
	
	public MousePencil(DrawingPanel v) {
		v.addMouseMotionListener(this);
		this.v = v;
		isDrawing = true;
	}
	
	private Drawing d() {return v.getDrawing();}
	
	public boolean isDrawing() {
		return isDrawing;
	}
	
	public void draw() {
		isDrawing = true;
	}
	
	public void erase() {
		isDrawing = false;
	}
	
	public void mouseDragged(MouseEvent e) {
		int x = e.getX() / v.xCell();
		int y = e.getY() / v.yCell();
		d().set(x, y, isDrawing());
		v.repaint();
	}

	public void mouseMoved(MouseEvent e) {}
}

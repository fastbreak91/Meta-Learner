package handwriting.gui;

import java.awt.BorderLayout;

import handwriting.core.SampleData;
import handwriting.learners.GrowingNeuralGas;

import javax.swing.*;
import java.awt.event.*;

public class AnimatedGNGPanel extends JPanel {
	private SampleData data;
	private GrowingNeuralGasPanel gngPanel;
	private GrowingNeuralGas gng;
	private JButton iterate;
	private JTextField iterations;
	
	public AnimatedGNGPanel(SampleData data, double maxTol, double k) {
		this.data = data;
		gng = new GrowingNeuralGas(data, maxTol, k);
		setLayout(new BorderLayout());
		
		gngPanel = new GrowingNeuralGasPanel(gng);
		add(gngPanel, BorderLayout.CENTER);
		
		JPanel top = new JPanel();
		iterate = new JButton("Iterate");
		iterate.addActionListener(new Trainer());
		top.add(iterate);
		top.add(new JLabel("Iteration:"));
		iterations = new JTextField("    0");
		top.add(iterations);
		add(top, BorderLayout.NORTH);
	}
	
	private class Trainer implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			iterations.setText(Integer.toString(1 + Integer.parseInt(iterations.getText().trim())));
			gng.trainOnce(AnimatedGNGPanel.this.data);
			gngPanel.repaint();
		}
		
	}
}

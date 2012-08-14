package x.mvmn.misc.thue;

import java.awt.BorderLayout;

import javax.swing.JFrame;

import x.mvmn.misc.thue.gui.ThueSwing;


public class Thue {
	public static void main(String[] args) {
		JFrame mainWindow = new JFrame("Thue in Java 1.1");
		mainWindow.getContentPane().setLayout(new BorderLayout());
		mainWindow.getContentPane().add(new ThueSwing(), BorderLayout.CENTER);
		mainWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		mainWindow.pack();
		mainWindow.setVisible(true);
	}
}

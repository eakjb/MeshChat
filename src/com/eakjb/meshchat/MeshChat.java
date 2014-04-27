package com.eakjb.meshchat;

import javax.swing.UIManager;

public class MeshChat {

	public static void main(String[] args) {
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			ErrorHandler.handle(e);
		}
		
		try {
			ChatSystem sys = new ChatSystem();
			sys.run();
		} catch (Exception e) {
			ErrorHandler.handle(e);
		}
	}

}

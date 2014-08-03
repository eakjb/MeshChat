package com.eakjb.meshchat;

import javax.swing.UIManager;

public class MeshChat {

	public static void main(String[] args) {
		int port = ChatConstants.DEFAULTPORT;
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			if (args[0] != null && args[0] == "") {
				port=Integer.parseInt(args[0]);
			}
		} catch (Exception e) {
			ErrorHandler.handle(e);
		}
		
		try {
			ChatSystem sys = new ChatSystem(port);
			sys.run();
		} catch (Exception e) {
			ErrorHandler.handle(e,ErrorHandler.HandlerMode.CRITICAL);
		}
	}

}

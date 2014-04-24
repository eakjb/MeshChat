package com.eakjb.meshchat;

import java.io.IOException;

public class MeshChat {

	public static void main(String[] args) {
		System.out.println("Running...");
		try {
			ChatSystem sys = new ChatSystem();
			System.out.println("Updating adresses");
			sys.updateAddresses("BILLINGSJM");
			System.out.println("Addresses updated.");
			sys.run();
		} catch (IOException e) {
			ErrorHandler.handle(e);
		}
	}

}

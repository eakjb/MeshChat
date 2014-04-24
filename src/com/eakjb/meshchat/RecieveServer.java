package com.eakjb.meshchat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class RecieveServer implements Runnable, ChatConstants {
	private final ChatSystem chatSystem;
	
	public RecieveServer(ChatSystem chatSystem) {
		this.chatSystem=chatSystem;
	}
	
	private void loop(ServerSocket server) throws IOException {
		Socket sock = server.accept();
		Thread t = new Thread(new RequestHandler(sock));
		t.start();
	}
	
	private class RequestHandler implements Runnable {
		private Socket sock;
		
		RequestHandler(Socket sock) {
			this.sock=sock;
		}

		@Override
		public void run() {
			try {
				System.out.println(sock.getRemoteSocketAddress().toString());
				chatSystem.addClient(sock.getRemoteSocketAddress().toString().split(":")[0].replace("/", ""));
				
				BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				StringBuffer b = new StringBuffer();
				while(in.ready()) {
					b.append(in.readLine());
				}
				
				//Split into metadata and data then individual datum and passes it to DA system
				String[] req = b.toString().split(METASEPARATOR);
				String[] meta = req[0].split(ADDRSEPARATOR);
				
				int reqType = Integer.parseInt(meta[0]);
				
				if (reqType==0) {
					//Chat
					chatSystem.addChat(req[1]);
				} else {
					//1 - Address request
					BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
					StringBuffer b1 = new StringBuffer();
					for (String a : chatSystem.getAddresses()) {
						b1.append(a);
						b1.append(ADDRSEPARATOR);
					}
					
					//Add local machine
					b1.append(chatSystem.getLocalHostName());
					b1.append(ADDRSEPARATOR);					
					
					//Needed for a readLine()
					b1.append("\n");
					
					out.write(b1.toString());
					out.flush();
					out.close();
				}
				
				in.close();
				sock.close();
			} catch (IOException e) {
				System.err.println("Error while processing incoming request.");
				ErrorHandler.handle(e);
			}
		}
		
	}

	@Override
	public void run() {
		ServerSocket sock;
		try {
			sock = new ServerSocket(PORT);
			while(chatSystem.isRunning()) {
				try {
					loop(sock);
				} catch (IOException e) {
					System.err.println("Error processing incoming request.");
					ErrorHandler.handle(e);
				}
			}
		} catch (IOException e) {
			System.err.println("Error binding server socket!");
			ErrorHandler.handle(e);
			throw new RuntimeException("Error binding server socket.", e);
		}
	}
	
	public Thread start() {
		Thread t = new Thread(this);
		t.start();
		return t;
	}

	public ChatSystem getChatSystem() {
		return chatSystem;
	}
}

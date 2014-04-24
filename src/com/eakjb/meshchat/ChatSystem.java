package com.eakjb.meshchat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import com.eakjb.meshchat.gui.MeshChatWindow;

public class ChatSystem implements Runnable, ChatConstants {
	private final ArrayList<String> addresses = new ArrayList<String>();
	private final ArrayList<String> chats = new ArrayList<String>();
	private final RecieveServer recieveServer = new RecieveServer(this);
	private final MeshChatWindow frame = new MeshChatWindow(this);
	
	private String username = "NoName";
	
	private boolean running = true;

	public void run() {
		recieveServer.start();
		frame.setVisible(true);
	}
	
	public void updateUI() {
		StringBuffer b = new StringBuffer();
		
		for (String chat : chats) {
			b.append(chat);
			b.append("\n");
		}
		
		frame.getTextArea().setText(b.toString());
	}
	
	public void updateAddresses(String address) throws IOException {
		Socket sock = new Socket(address,PORT);
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
		BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		
		out.write("1"+METASEPARATOR);
		out.flush();
		
		StringBuffer b = new StringBuffer();
		String line;
		while((line=in.readLine())!=null) {
			b.append(line);
		}
		
		addClients(b.toString().split(ADDRSEPARATOR));
		
		in.close();
		out.close();
		sock.close();
	}
	
	public void sendChat(String chat) {
		for (String addr : addresses) {
			Thread t = new Thread(new SendHandler(addr,chat));
			t.start();
		}
		addChat(chat);
	}
	
	private class SendHandler implements Runnable {
		private final String addr;
		private final String chat;
		
		SendHandler(String addr,String chat) {
			this.addr=addr;
			this.chat=chat;
		}
		
		@Override
		public void run() {
			try {
				Socket sock = new Socket(addr,PORT);
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
				out.write("0"+METASEPARATOR+CHATBRACKETL+username+CHATBRACKETR+chat);
				out.flush();
				out.close();
				sock.close();
			} catch (IOException e) {
				ErrorHandler.handle(e);
			}
		}
	}
	
	public void addChat(String chat) {
		chats.add(chat);
		System.out.println("Chat Added: "+chat);
		updateUI();
	}
	
	public void addClients(Collection<String> addrs) {
		addresses.addAll(addrs);
	}
	public void addClients(String[] addrs) {
		addClients(Arrays.asList(addrs));
	}
	
	public ArrayList<String> getAddresses() {
		return addresses;
	}

	public RecieveServer getRecieveServer() {
		return recieveServer;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public ArrayList<String> getChats() {
		return chats;
	}
}

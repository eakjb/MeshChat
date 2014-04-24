package com.eakjb.meshchat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import com.eakjb.meshchat.gui.MeshChatWindow;

public class ChatSystem implements Runnable, ChatConstants {
	private final ArrayList<String> addresses = new ArrayList<String>();
	private final ArrayList<String> chats = new ArrayList<String>();
	private final RecieveServer recieveServer = new RecieveServer(this);
	private final MeshChatWindow frame = new MeshChatWindow(this);
	private String localHostName;
	
	private String username = DEFAULTUSERNAME;
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	private boolean running = true;
	
	public ChatSystem() throws UnknownHostException {
		this(InetAddress.getLocalHost().getHostName());
	}
	
	public ChatSystem(String localHostName) throws UnknownHostException {
		this.setLocalHostName(localHostName);
	}

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
		frame.getMainAreaScroller().getVerticalScrollBar().setValue(frame.getMainAreaScroller().getVerticalScrollBar().getMaximum());
	}
	
	public void updateAddresses(String address) throws IOException {
		Socket sock = new Socket(address,PORT);
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
		BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		
		System.out.println("Writing message...");
		out.write("1"+METASEPARATOR+"\n");
		out.flush();

		System.out.println("Waiting for response...");
		
		StringBuffer b = new StringBuffer();
		String line;
		while((line=in.readLine())!=null) {
			b.append(line);
			System.out.println(b.toString());
		}

		addClients(b.toString().split(ADDRSEPARATOR));
		
		in.close();
		out.close();
		sock.close();
	}
	
	public void sendChat(String chat) {
		String msg = CHATBRACKETL+username+CHATBRACKETR+" "+chat;
		for (String addr : addresses) {
			Thread t = new Thread(new SendHandler(addr,msg));
			t.start();
		}
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
				out.write("0"+METASEPARATOR+chat);
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
	
	public void addClient(String client) throws UnknownHostException {
		String ip = InetAddress.getByName(client).getHostAddress();
		if (!addresses.contains(ip)) {
			addresses.add(ip);
		}
	}
	
	public void addClients(Collection<String> addrs) throws UnknownHostException {
		for (String c : addrs) {
			addClient(c);
		}
	}
	public void addClients(String[] addrs) throws UnknownHostException {
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

	public String getLocalHostName() {
		return localHostName;
	}

	public void setLocalHostName(String localHostName) throws UnknownHostException {
		this.getAddresses().remove(this.localHostName);
		this.addClient(localHostName);
		this.localHostName = localHostName;
	}
}

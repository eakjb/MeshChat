package com.eakjb.meshchat;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;

import com.eakjb.meshchat.gui.MeshChatWindow;

public class ChatSystem implements Runnable, ChatConstants {
	private final ArrayList<String> addresses = new ArrayList<String>();
	private final ArrayList<String> chats = new ArrayList<String>();
	private final RecieveServer recieveServer = new RecieveServer(this);
	private final MeshChatWindow frame = new MeshChatWindow(this);
	private String localHostName;

	private String username = DEFAULTUSERNAME;
	private ArrayList<String> localHosts = new ArrayList<String>();

	private boolean running = true;

	public ChatSystem() throws UnknownHostException, SocketException {
		this(InetAddress.getLocalHost().getHostName());
	}

	public ChatSystem(String localHostName) throws SocketException, UnknownHostException {
		this.setLocalHostName(localHostName);
		try {
			Enumeration<NetworkInterface> n = NetworkInterface.getNetworkInterfaces();
			for (; n.hasMoreElements();) {
				NetworkInterface e = n.nextElement();
				Enumeration<InetAddress> as  =e.getInetAddresses();
				while (as.hasMoreElements()) {
					localHosts.add(as.nextElement().getHostAddress());
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Network Connection Error",e);
		}
	}

	public void run() {
		recieveServer.start();
		frame.setVisible(true);
	}

	public void updateAddresses(String address) throws IOException {
		Socket sock = new Socket(address,PORT);
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
		BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));

		//System.out.println("Writing message...");
		out.write("1"+METASEPARATOR+"\n");
		out.flush();

		//System.out.println("Waiting for response...");

		StringBuffer b = new StringBuffer();
		String line;
		while((line=in.readLine())!=null) {
			b.append(line);
			//System.out.println(b.toString());
		}

		addClients(b.toString().split(ADDRSEPARATOR));

		in.close();
		out.close();
		sock.close();

		sendChat(username+CONNECTMESSAGE);
	}

	public void sendChat(String chat) {
		String parsed=CHATBRACKETL+username+CHATBRACKETR+" "+chat;
		for (String s : ILLEGALPHRASES) {
			parsed=parsed.replace(s, "");
		}
		sendStr(parsed);
	}

	public void sendStr(String msg) {
		for (String addr : addresses) {
			Thread t = new Thread(new SendHandler(addr,msg));
			t.start();
			//System.out.println("Sending to: "+addr);
		}
		addChat(msg);
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
				out.write("0"+METASEPARATOR+chat+"\n");
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
		System.out.println(chat);

		//Render chat
		try {
			ArrayList<String> URLs = new ArrayList<String>();

			Pattern pattern = Pattern.compile(URLREGEX);
			Matcher matcher = pattern.matcher(chat);
			while (matcher.find()) {
				URLs.add(matcher.group());
			}

			for (String s : chat.split(URLSPLITREGEX)) {
				//System.out.println(s);
				if (URLs.contains(s)) {
					if (s.contains(IMGTAGLEFT)) {
						BufferedImage img = loadImageFromURL(s.replace(IMGTAGLEFT, "").replace(IMGTAGRIGHT, ""));
						if (img != null) {
							ImageIcon icon;

							if (img.getWidth()>IMGWIDTH) {
								icon=new ImageIcon(img.getScaledInstance(IMGWIDTH, IMGWIDTH*img.getHeight()/img.getWidth(),Image.SCALE_SMOOTH));
							} else {
								icon=new ImageIcon(img);
							}

							frame.getTextArea().getDocument().insertString(
									frame.getTextArea().getDocument().getEndPosition()
									.getOffset()," ", null);
							frame.getTextArea().setCaretPosition(frame.getTextArea().getDocument().getEndPosition().getOffset()-1);
							frame.getTextArea().insertIcon(icon);
						}
					} else {
						frame.getTextArea().getDocument().insertString(frame.getTextArea().getDocument().getEndPosition().getOffset(), s+" ", null);
					}
				} else {
					frame.getTextArea().getDocument().insertString(frame.getTextArea().getDocument().getEndPosition().getOffset(), s+" ", null);
				}
			}
			frame.getTextArea().getDocument().insertString(frame.getTextArea().getDocument().getEndPosition().getOffset(), "\n", null);
		} catch (BadLocationException e) {
			ErrorHandler.handle(e);
		}
	}

	public static BufferedImage loadImageFromURL(String url) {
		BufferedImage img = null;
		try {
			img = ImageIO.read(new URL(url));
		} catch (Exception e) {
			ErrorHandler.handle(e);
			try {
				//System.out.println(ChatSystem.class.getResource("/img/question_mark.png").getPath());
				img = ImageIO.read(ChatSystem.class.getResource(UNKNOWNIMAGEPATH));
			} catch (Exception e1) {
				ErrorHandler.handle(e1);
			}
		}
		return img;
	}

	public void addClient(String client) throws UnknownHostException {
		String ip = InetAddress.getByName(client).getHostAddress();
		if (!addresses.contains(ip)&&!localHosts.contains(ip)) {
			addresses.add(ip);
			System.out.println("Added CLient: "+ip);
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
		//this.getAddresses().remove(this.localHostName);
		//this.addClient(localHostName);
		this.localHostName = localHostName;
	}

	public ArrayList<String> getLocalHosts() {
		return localHosts;
	}


	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}

package com.eakjb.meshchat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;

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
				String remoteIp = sock.getRemoteSocketAddress().toString().split(":")[0].replace("/", "");
				chatSystem.addClient(remoteIp);

				BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				//Helps with recieving all information
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
				StringBuffer b = new StringBuffer();
				while(in.ready()) {
					b.append(in.readLine());
				}
				
				System.out.println(b.toString());

				//Split into metadata and data then individual datum and passes it to DA system
				String[] req = b.toString().split(METASEPARATOR);

				if (req.length>1) {
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
					try {
						Enumeration<NetworkInterface> n = NetworkInterface.getNetworkInterfaces();
						for (; n.hasMoreElements();) {
							NetworkInterface e = n.nextElement();
							Enumeration<InetAddress> as  =e.getInetAddresses();
							while (as.hasMoreElements()) {
								InetAddress hostAddr = as.nextElement();
								if ((!hostAddr.getHostAddress().contains(":"))&&sameNetwork(remoteIp,hostAddr.getHostAddress(),
										getIPv4LocalNetMask(hostAddr,e.getInterfaceAddresses().get(0)
												.getNetworkPrefixLength()))) {
									b1.append(hostAddr.getHostAddress());
									b1.append(ADDRSEPARATOR);
								}
							}
						}
					} catch (Exception e) {
						throw new RuntimeException("Network Connection Error",e);
					}			

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

	private static InetAddress getIPv4LocalNetMask(InetAddress ip, int netPrefix) {

		try {
			// Since this is for IPv4, it's 32 bits, so set the sign value of
			// the int to "negative"...
			int shiftby = (1<<31);
			// For the number of bits of the prefix -1 (we already set the sign bit)
			for (int i=netPrefix-1; i>0; i--) {
				// Shift the sign right... Java makes the sign bit sticky on a shift...
				// So no need to "set it back up"...
				shiftby = (shiftby >> 1);
			}
			// Transform the resulting value in xxx.xxx.xxx.xxx format, like if
			/// it was a standard address...
			String maskString = Integer.toString((shiftby >> 24) & 255) + "." + Integer.toString((shiftby >> 16) & 255) + "." + Integer.toString((shiftby >> 8) & 255) + "." + Integer.toString(shiftby & 255);
			// Return the address thus created...
			return InetAddress.getByName(maskString);
		}
		catch(Exception e){e.printStackTrace();
		}
		// Something went wrong here...
		return null;
	}

	private static boolean sameNetwork(String ip1, String ip2, InetAddress mask) 
			throws Exception {

		byte[] a1 = InetAddress.getByName(ip1).getAddress();
		byte[] a2 = InetAddress.getByName(ip2).getAddress();
		byte[] m = mask.getAddress();

		for (int i = 0; i < a1.length; i++)
			if ((a1[i] & m[i]) != (a2[i] & m[i]))
				return false;

		return true;

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
			ErrorHandler.handle(e,ErrorHandler.HandlerMode.CRITICAL);
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

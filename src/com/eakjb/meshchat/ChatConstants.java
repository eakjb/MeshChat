package com.eakjb.meshchat;

public interface ChatConstants {
	public static final int PORT = 6969;
	public static final String METASEPARATOR = ";";
	public static final String ADDRSEPARATOR = ",";
	public static final String CHATBRACKETL = "[";
	public static final String CHATBRACKETR = "]";
	public static final String DEFAULTUSERNAME = "Guest";
	public static final String CONNECTMESSAGE = " connected.";
	public static final String WELCOMEMESSAGE = "Welcome to the system.";
	public static final String IMGTAGLEFT = "<";
	public static final String IMGTAGRIGHT = ">";
	public static final String URLREGEX = IMGTAGLEFT+"?https?://(([A-Za-z0-9]*)\\.?)*/.*"+IMGTAGRIGHT+"?";
	public static final String URLSPLITREGEX = " ";//"[\\"+IMGTAGLEFT+"|\\"+IMGTAGRIGHT+"]";
	public static final String UNKNOWNIMAGEPATH = "/img/question_mark.png";
	public static final String BUGREPORTURI = "http://github.com/eakjb/MeshChat/issues";
	
	public static final String[] ILLEGALPHRASES = {";",","};
	
	public static final int IMGWIDTH = 250; 
}

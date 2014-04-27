package com.eakjb.meshchat.gui;

import java.awt.BorderLayout;
import java.awt.Event;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;

import com.eakjb.meshchat.ChatConstants;
import com.eakjb.meshchat.ChatSystem;
import com.eakjb.meshchat.ErrorHandler;

public class MeshChatWindow extends JFrame implements ChatConstants {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6459930518540642477L;
	
	private final ChatSystem chatSystem;
	
	private JPanel contentPane;
	private JTextField textField;
	private JTextPane textArea;
	private JScrollPane mainAreaScroller;
	
	public JScrollPane getMainAreaScroller() {
		return mainAreaScroller;
	}

	private JMenuBar menuBar;

	public JTextPane getTextArea() {
		return textArea;
	}
	public MeshChatWindow(final ChatSystem chatSystem) {
		this(chatSystem,WELCOMEMESSAGE);
	}
	/**
	 * Create the frame.
	 */
	public MeshChatWindow(final ChatSystem chatSystem,String welcomeMessage) {
		this.chatSystem=chatSystem;
		
		//Establish JFrame
		setTitle("LemurChat");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		
		//Create Menu Bar
		menuBar = new JMenuBar();
		
		JMenu fileMenu = new JMenu("File");
		JMenuItem exitButton = new JMenuItem("Exit");
		exitButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
			
		});
		exitButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,Event.CTRL_MASK));
		fileMenu.add(exitButton);
		menuBar.add(fileMenu);
		
		JMenu userMenu = new JMenu("User");
		
		JMenuItem connectButton = new JMenuItem("Connect Network");
		connectButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					chatSystem.updateAddresses(JOptionPane.showInputDialog("Foreign Address: "));
				} catch (HeadlessException e) {
					ErrorHandler.handle(e);
				} catch (IOException e) {
					ErrorHandler.handle(e);
				}
				
			}
			
		});
		connectButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,Event.CTRL_MASK));
		
		JMenuItem changeNameButton = new JMenuItem("Change Username");
		changeNameButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				chatSystem.setUsername(JOptionPane.showInputDialog("New Username: ") );
			}
			
		});
		changeNameButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U,Event.CTRL_MASK));
		
		userMenu.add(changeNameButton);
		userMenu.add(connectButton);
		menuBar.add(userMenu);
		
		setJMenuBar(menuBar);
		
		
		//Main GUI
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		textArea = new JTextPane();
		textArea.setEditable(false);
		//textArea.setLineWrap(true);
		textArea.setFont(new Font("Dialog", Font.PLAIN, 11));
		textArea.setText(welcomeMessage);
		
		mainAreaScroller = new JScrollPane(textArea,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		contentPane.add(mainAreaScroller, BorderLayout.CENTER);
		
		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.SOUTH);
		panel.setLayout(new BorderLayout(0, 0));
		
		textField = new JTextField();
		textField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				send();
			}
		});
		textField.setFont(new Font("Dialog", Font.PLAIN, 11));
		panel.add(textField,BorderLayout.CENTER);
		//textField.setColumns(10);
		
		JButton btnSend = new JButton("Send");
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				send();
			}
		});
		panel.add(btnSend,BorderLayout.EAST);
		
		//Configure some specifics
		this.addWindowListener( new WindowAdapter() {
		    public void windowOpened( WindowEvent e ){
		        textField.requestFocus();
		    }
		}); 
	}
	
	protected void send() {
		String msg = textField.getText();
		textField.setText("");
		chatSystem.sendChat(msg);
	}

	public ChatSystem getChatSystem() {
		return chatSystem;
	}

}

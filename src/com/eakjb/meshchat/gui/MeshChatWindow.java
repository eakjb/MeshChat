package com.eakjb.meshchat.gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.eakjb.meshchat.ChatSystem;

public class MeshChatWindow extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6459930518540642477L;
	
	private final ChatSystem chatSystem;
	
	
	private JPanel contentPane;
	private JTextField textField;
	private JTextArea textArea;

	public JTextArea getTextArea() {
		return textArea;
	}

	/**
	 * Create the frame.
	 */
	public MeshChatWindow(ChatSystem chatSystem) {
		this.chatSystem=chatSystem;
		
		setTitle("LemurChat");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		textArea.setFont(new Font("Dialog", Font.PLAIN, 11));
		contentPane.add(textArea, BorderLayout.CENTER);
		
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

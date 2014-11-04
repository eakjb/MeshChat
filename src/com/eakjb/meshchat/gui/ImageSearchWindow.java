package com.eakjb.meshchat.gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.json.JSONObject;

import com.eakjb.meshchat.ChatConstants;
import com.eakjb.meshchat.ChatSystem;
import com.eakjb.meshchat.ErrorHandler;

public class ImageSearchWindow extends JFrame implements ChatConstants {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8506997065507533077L;
	private JPanel contentPane;
	private JTextField textField;
	private JLabel mainContent;
	private JScrollPane mainScroller;

	private JSONObject json = null;
	private int index = 0;
	private BufferedImage img;

	private final ChatSystem chatSystem;

	/**
	 * Create the frame.
	 */
	public ImageSearchWindow(final ChatSystem chatSystem) {
		this.chatSystem=chatSystem;

		setTitle("Image Search");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		contentPane.addComponentListener(new ComponentListener() {
			@Override
			public void componentResized(ComponentEvent e) {
				Thread t = new Thread(new Runnable() {
					@Override
					public void run() {
						updateImage();						
					}
				});
				t.start();
			}

			@Override
			public void componentHidden(ComponentEvent arg0) {				
			}
			@Override
			public void componentMoved(ComponentEvent arg0) {
			}
			@Override
			public void componentShown(ComponentEvent arg0) {				
			}
		});

		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.NORTH);
		panel.setLayout(new BorderLayout(0, 0));

		JButton lblSearch = new JButton("Search");
		lblSearch.addActionListener(new SearchListener());
		panel.add(lblSearch, BorderLayout.EAST);

		textField = new JTextField();
		textField.setFont(new Font("Tahoma", Font.PLAIN, 14));
		textField.addActionListener(new SearchListener());
		panel.add(textField, BorderLayout.CENTER);

		JButton btnGo = new JButton("Send");
		btnGo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chatSystem.sendChat(IMGTAGLEFT+getURL()+IMGTAGRIGHT);
				setVisible(false);
			}
		});
		contentPane.add(btnGo, BorderLayout.SOUTH);

		JButton btnPrevious = new JButton("<");
		btnPrevious.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Thread t = new Thread(new Runnable() {
					@Override
					public void run() {
						setIndex(getIndex()-1);					
					}
				});
				t.start();
			}
		});
		btnPrevious.setFont(new Font("Impact", Font.BOLD, 36));
		contentPane.add(btnPrevious, BorderLayout.WEST);

		JButton btnNext = new JButton(">");
		btnNext.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Thread t = new Thread(new Runnable() {
					@Override
					public void run() {
						setIndex(getIndex()+1);					
					}
				});
				t.start();
			}
		});
		btnNext.setFont(new Font("Impact", Font.BOLD, 36));
		contentPane.add(btnNext, BorderLayout.EAST);

		mainContent = new JLabel("Search a term to load an image.");
		mainScroller = new JScrollPane(mainContent,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		contentPane.add(mainScroller, BorderLayout.CENTER);

		this.addWindowListener( new WindowAdapter() {
			public void windowOpened( WindowEvent e ){
				textField.requestFocus();
			}
		});
	}

	public JSONObject getJson() {
		return json;
	}


	public int getIndex() {
		return index;
	}

	public BufferedImage getImg() {
		return img;
	}

	public void setJson(JSONObject json) {
		this.json = json;
		setIndex(0);
	}

	public void setIndex(int index) {
		if (json!=null) {
			this.index = index;
			if (index>json.getJSONObject("responseData").getJSONArray("results").length()-1) {
				this.index=0;
			} else if (index<0) {
				this.index=json.getJSONObject("responseData").getJSONArray("results").length()-1;
			}
			String url = json.getJSONObject("responseData").getJSONArray("results").getJSONObject(this.index).getString("unescapedUrl");
			setImg(ChatSystem.loadImageFromURL(url));
			mainContent.setText("");
		}
	}

	public String getURL() {
		return json.getJSONObject("responseData").getJSONArray("results").getJSONObject(this.index).getString("unescapedUrl");
	}

	public void setImg(BufferedImage img) {
		this.img = img;
		if (img!=null) {
			int width=img.getWidth();
			int height=img.getHeight();
			if (mainContent.getSize().width<width) {
				width=Math.round(mainContent.getSize().width*0.9F);
				height=width*height/img.getWidth();
				if (height<Math.round(mainContent.getSize().height*0.6F)) {
					width=Math.round(img.getWidth()*0.5F);
					height=Math.round(img.getHeight()*0.5F);
				}
			} else if (mainContent.getSize().height<img.getHeight()) {
				height=Math.round(mainContent.getSize().height*0.9F);
				width=height*img.getWidth()/img.getHeight();
			}
			ImageIcon icon=new ImageIcon(img.getScaledInstance(width, height,Image.SCALE_SMOOTH));
			mainContent.setIcon(icon);
		}
	}

	public void updateImage() {
		setImg(getImg());
	}

	public ChatSystem getChatSystem() {
		return chatSystem;
	}

	private class SearchListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			mainContent.setIcon(new ImageIcon());
			mainContent.setText("Loading...");
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					URL url;
					try {
						url = new URL("https://ajax.googleapis.com/ajax/services/search/images?" +
								"v=1.0&q="+textField.getText().replace(" ", "%20"));

						URLConnection connection = url.openConnection();
						connection.addRequestProperty("Referer", "");

						String line;
						StringBuilder builder = new StringBuilder();
						BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
						while((line = reader.readLine()) != null) {
							builder.append(line);
						}

						setJson(new JSONObject(builder.toString()));
					} catch (IOException e1) {
						ErrorHandler.handle(e1);
					};			
				}

			});
			t.start();
		}
	}
}

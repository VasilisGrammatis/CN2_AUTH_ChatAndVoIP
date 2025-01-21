package com.cn2.communication;

import java.io.*;   
import java.net.*;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;

//NEW CODE: Απαραίτητες βιβλιοθήκες για καταγραφή και αναπαραγωγή ήχου
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.DataLine;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.awt.event.*;
import java.awt.Color;
import java.lang.Thread;

public class App extends Frame implements WindowListener, ActionListener {

	/*
	 * Definition of the app's fields
	 */
	static TextField inputTextField;		
	static JTextArea textArea;				 
	static JFrame frame;					
	static JButton sendButton;				
	static JTextField meesageTextField;		  
	public static Color gray;				
	final static String newline="\n";		
	static JButton callButton;				
	
	// TODO: Please define and initialize your variables here...
	static DatagramSocket socket;          // The UDP socket for communication
	static int localchatPort = 9876;           // Port to listen for incoming messages
	static int remotechatPort = 9876;          // Port to send messages to
	static String remoteIP = "192.168.68.101";  // The IP address of the remote user (default: localhost)
												// The other user uses ip = 192.168.68.103
	static int localCallPort = 9877;			// Port to listen for incoming call
	static int remoteCallPort = 9877;			// Port to call
	
	/**
	 * Construct the app's frame and initialize important parameters
	 */
	public App(String title) {
		
		/*
		 * 1. Defining the components of the GUI
		 */
		
		// Setting up the characteristics of the frame
		super(title);									
		gray = new Color(254, 254, 254);		
		setBackground(gray);
		setLayout(new FlowLayout());			
		addWindowListener(this);	
		
		// Setting up the TextField and the TextArea
		inputTextField = new TextField();
		inputTextField.setColumns(20);
		
		// Setting up the TextArea.
		textArea = new JTextArea(10,40);			
		textArea.setLineWrap(true);				
		textArea.setEditable(false);			
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		//Setting up the buttons
		sendButton = new JButton("Send");			
		callButton = new JButton("Call");			
						
		/*
		 * 2. Adding the components to the GUI
		 */
		add(scrollPane);								
		add(inputTextField);
		add(sendButton);
		add(callButton);
		
		/*
		 * 3. Linking the buttons to the ActionListener
		 */
		sendButton.addActionListener(this);			
		callButton.addActionListener(this);	

		
	}
	
	/**
	 * The main method of the application. It continuously listens for
	 * new messages.
	 */
	public static void main(String[] args){
	
		/*
		 * 1. Create the app's window
		 */
		App app = new App("CN2 - AUTH");  // TODO: You can add the title that will displayed on the Window of the App here																		  
		app.setSize(500,250);				  
		app.setVisible(true);				  

		/*
		 * 2. Initialize the socket
	     */
	    try {
	        socket = new DatagramSocket(localchatPort);
	        //textArea.append("Listening on port " + localchatPort + newline);
	    } catch (SocketException ex) {
	        ex.printStackTrace();
	    }

	    /*
	     * 3. Start the thread for receiving messages
	     */
	 // NEW CODE: Thread για λήψη μηνυμάτων κειμένου
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        byte[] buffer = new byte[1024];
                        
                        DatagramPacket recievepacket = new DatagramPacket(buffer, buffer.length);
                        socket.receive(recievepacket);
                       
                        String message = new String(recievepacket.getData(), 0, recievepacket.getLength());
                        textArea.append(message + newline);     
                        
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
    }

	
	/**
	 * The method that corresponds to the Action Listener. Whenever an action is performed
	 * (i.e., one of the buttons is clicked) this method is executed. 
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		/*
		 * Check which button was clicked.
		 */
		if (e.getSource() == sendButton){
			
			// The "Send" button was clicked
			
			// TODO: Your code goes here...
			try {
			    String message = inputTextField.getText();
			    String sendmessage = "Remote: " + message;
			    byte[] buffer = sendmessage.getBytes();
			    DatagramPacket sendpacket = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(remoteIP), remotechatPort);
			    socket.send(sendpacket);

			    textArea.append("You: " + message + newline);
			    inputTextField.setText("");  // Clear the input field
			} catch (IOException ex) {
			    ex.printStackTrace();
			}
			
		}else if(e.getSource() == callButton){
			
			// The "Call" button was clicked
			
			// TODO: Your code goes here...
			
			// NEW CODE: Thread για καταγραφή και αποστολή ήχου
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        AudioFormat format = new AudioFormat(8000.0f, 8, 1, true, true);
                        DataLine.Info micInfo = new DataLine.Info(TargetDataLine.class, format);
                        TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(micInfo);
                        microphone.open(format);
                        microphone.start();

                        DatagramSocket audioSocket = new DatagramSocket();
                        InetAddress remoteAddress = InetAddress.getByName(remoteIP);

                        byte[] buffer = new byte[1024];

                        textArea.append("\nVoice call started...\n");
                        try {
                		    String message = "\nSomeone is calling you. If you want to answer the call, press the call button";
                		    byte[] buffer1 = message.getBytes();
                		    DatagramPacket sendcall = new DatagramPacket(buffer1, buffer1.length, InetAddress.getByName(remoteIP), remotechatPort);
                		    socket.send(sendcall);
                		    
                		} catch (IOException ex) {
                		    ex.printStackTrace();
                		}

                        while (true) {
                            int bytesRead = microphone.read(buffer, 0, buffer.length);
                            DatagramPacket packet = new DatagramPacket(buffer, bytesRead, remoteAddress, remoteCallPort);
                            audioSocket.send(packet);
                        }

                    }catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }).start();

            
            // NEW CODE: Thread για λήψη και αναπαραγωγή ήχου
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        AudioFormat format = new AudioFormat(8000.0f, 8, 1, true, true);
                        DataLine.Info speakerInfo = new DataLine.Info(SourceDataLine.class, format);
                        SourceDataLine speakers = (SourceDataLine) AudioSystem.getLine(speakerInfo);
                        speakers.open(format);
                        speakers.start();

                        DatagramSocket audioSocket = new DatagramSocket(localCallPort);
                        byte[] buffer = new byte[1024];

                        while (true) {
                            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                            audioSocket.receive(packet);
                            speakers.write(packet.getData(), 0, packet.getLength());
                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }).start();

		}
			
	}

	/**
	 * These methods have to do with the GUI. You can use them if you wish to define
	 * what the program should do in specific scenarios (e.g., when closing the 
	 * window).
	 */
	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub	
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub	
		
		JOptionPane.showMessageDialog(null, "You left the app");
		try {
		    String message = "\nThe other user left the app";
		    byte[] buffer = message.getBytes();
		    DatagramPacket sendpacket = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(remoteIP), remotechatPort);
		    socket.send(sendpacket);
		} catch (IOException ex) {
		    ex.printStackTrace();
		}
		System.exit(0);
		
	}

	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub
		textArea.append("\nGoodbye");
		
		JOptionPane.showMessageDialog(null, "You are leaving the app");
		
		dispose();
        	
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub	
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub	
		try {
		    String message = "\nThe other user is active";
		    byte[] buffer = message.getBytes();
		    DatagramPacket sendpacket = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(remoteIP), remotechatPort);
		    socket.send(sendpacket);
		} catch (IOException ex) {
		    ex.printStackTrace();
		}
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub	
		try {
		    String message = "\nThe other user is inactive";
		    byte[] buffer = message.getBytes();
		    DatagramPacket sendpacket = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(remoteIP), remotechatPort);
		    socket.send(sendpacket);
		} catch (IOException ex) {
		    ex.printStackTrace();
	}
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub	
		textArea.append("Listening on port " + localchatPort + newline + "\nYou can send a message using the send button or you can start a call using the call button.\n");
	}
}

package uk.ac.cam.pm576.fjava.tick4star;

import java.awt.BorderLayout;
import java.awt.Rectangle;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.awt.event.KeyAdapter;
//import java.awt.event.KeyEvent;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.net.Socket;
import java.net.SocketException;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import uk.ac.cam.cl.fjava.messages.*;

public class GuiChatClient implements Serializable {
	private static final long serialVersionUID = 1L;
	private JFrame frame = null;
	private JPanel messages = null;
	private JTextArea text = null;
	private JButton sendMsg = null;
	private JButton changeNick = null;
	private Socket socket = null;
	private SimpleDateFormat writeFormat = null;
	private Date now = null;
	private ObjectOutputStream outputObj = null;
	
	public void createGuiChatClient(String serverName, int portNumber) {
		frame = new JFrame();
		frame.setSize(800, 640);
		frame.setLayout(new BorderLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// Configuring socket details
		try {
			socket = new Socket(serverName, portNumber);
			outputObj = new ObjectOutputStream(socket.getOutputStream());
			
			outputObj.writeObject(new StatusMessage("Gui"));
			outputObj.flush();
		
		} catch (IOException ioe) {
			JOptionPane.showMessageDialog(frame,
					"Cannot connect to " + serverName + " on port " + portNumber);
			return;
			
		} catch (IllegalArgumentException iae) {
			JOptionPane.showMessageDialog(frame,
					"Please provide a proper port number");
			return;
		}
		
		frame.add(messageView(), BorderLayout.CENTER);
		
		Box textAndButtons = Box.createHorizontalBox();
		textAndButtons.add(textView());
		
		sendMsg = new JButton(" \t\tSend Message\t\t ");
		sendMsg.addActionListener(new SendMsgListener(text, outputObj));
		
		changeNick = new JButton("Change Nickname");
		changeNick.addActionListener(new ChangeNickListener(outputObj));
		
		text.addKeyListener(new TextMsgListener(sendMsg));
		textAndButtons.add(addButtons());
		addBorder(textAndButtons, "");
		frame.add(textAndButtons, BorderLayout.SOUTH);
		
		frame.setVisible(true);
		
		// Now reading messages
		writeFormat = new SimpleDateFormat("HH:mm:ss");
		try {
			readMessage();
			outputObj.close();
			
		} catch (IOException ioe) {
			JOptionPane.showMessageDialog(frame,
					"Cannot connect to " + serverName + " on port " + portNumber);
			return;
			
		} catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
			
		} catch (IllegalAccessException iae){
			iae.printStackTrace();
			
		} catch (InvocationTargetException ite){
			ite.printStackTrace();
		}
	}
	
	private JComponent addButtons() {
		Box holder = Box.createVerticalBox();
		holder.add(sendMsg);
		holder.add(Box.createVerticalStrut(5));
		holder.add(changeNick);
		holder.add(Box.createVerticalStrut(10));
		
		return holder;
	}
	
	private void addBorder(JComponent component, String title) {
		Border etch = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		Border tb = BorderFactory.createTitledBorder(etch,title);
		component.setBorder(tb);
	}
	
	private JComponent textView() {
		JPanel holder = new JPanel(new BorderLayout(300, 75));
		text = new JTextArea("Please type your message...");
		
		holder.add(new JScrollPane(text), BorderLayout.CENTER);
		addBorder(holder, "");
		
		return holder;
	}
	
	private JComponent messageView() {
		JPanel holder = new JPanel(new BorderLayout());
		messages = new JPanel();
		messages.setLayout(new BoxLayout(messages, BoxLayout.Y_AXIS));
		messages.add(Box.createVerticalStrut(10));
		
		holder.add(new JScrollPane(messages), BorderLayout.CENTER);
		addBorder(holder, "Messages");
		
		return holder;
	}
	
	public void addMessage(String s) {
		JLabel message = new JLabel(s);
		messages.add(message);
		messages.add(Box.createVerticalStrut(10));
		frame.revalidate();
		
		messages.scrollRectToVisible(
	              new Rectangle(0, messages.getHeight()-1, 1, 1));
	}
	
	private void readMessage() throws IOException, ClassNotFoundException,
									  IllegalArgumentException, IllegalAccessException,
									  InvocationTargetException {
		
		DynamicObjectInputStream inputObj =
				new DynamicObjectInputStream(socket.getInputStream());
		
		// Now reading objects
		Message msg = (Message) inputObj.readObject();
		
		while (msg != null) {
			now = new Date();
			if (msg instanceof RelayMessage) {
				addMessage(writeFormat.format(now) + " [" + ((RelayMessage) msg).getFrom() +
							"] " + ((RelayMessage) msg).getMessage());
				
			} else if (msg instanceof StatusMessage) {
				String statusmsg = ((StatusMessage) msg).getMessage();
				addMessage(writeFormat.format(now) + " [Server] " + statusmsg);
				
			} else if (msg instanceof NewMessageType) {
				inputObj.addClass(((NewMessageType) msg).getName(),
								  ((NewMessageType) msg).getClassData());
				
				addMessage(writeFormat.format(now) + " [Client] New class " +
						   ((NewMessageType) msg).getName() + " loaded.");
				
			} else {
				Class<?> newClass = msg.getClass();
				String[] nameParts = newClass.getName().split("\\.");
				
				String outputString = "";
				outputString += writeFormat.format(now) + " [Client] " +
								 nameParts[nameParts.length-1] + ": ";
				
				if (nameParts[nameParts.length-1].equals("GuiMessage") == false) {
					Field[] allFields = msg.getClass().getDeclaredFields();
					
					for (int i = 1; i <= allFields.length; i += 1) {
						allFields[i % allFields.length].setAccessible(true);
						outputString += allFields[i % allFields.length].getName() + "(" +
										 allFields[i % allFields.length].get(msg) + ")";
						
						if (i <= allFields.length - 1) {
							outputString += ", ";
						}
					}
					addMessage(outputString);
					
					Method[] allMethods = newClass.getMethods();
					for (int i = 0; i < allMethods.length; i += 1) {
						if (allMethods[i].getParameterCount() > 0) {
							continue;
						}
						
						Annotation[] annotations = allMethods[i].getAnnotations();
						for (int j = 0; j < annotations.length; j += 1) {
							if (annotations[j].toString().equals(
									"@uk.ac.cam.cl.fjava.messages.Execute()")) {
								
								allMethods[i].invoke(msg);
								break;
							}
						}
					}
				}
			}
			
			try {
				msg = (Message) inputObj.readObject();
			
			} catch(SocketException se) {
				break;
			}
		}
		
		inputObj.close();
		return;
	}
}

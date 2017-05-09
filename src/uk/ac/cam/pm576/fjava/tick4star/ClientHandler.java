package uk.ac.cam.pm576.fjava.tick4star;

import uk.ac.cam.cl.fjava.messages.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.ServerSocket;
import java.net.Socket;
import java.security.AccessControlException;

public class ClientHandler {
	public static ServerSocket serverSocket = null;
	
	private Socket socket;
	private MultiQueue<Message> multiQueue;
	private String nickname;
	private MessageQueue<Message> clientMessages;
	private boolean clientConnected;
	private boolean isGuiClient = false;
	
	public ClientHandler(Socket s, MultiQueue<Message> q) throws ClassNotFoundException {
		socket = s;
		multiQueue = q;
		
		nickname = "Anonymous";
		for (int i = 0; i < 5; i += 1) {
			Integer randDigit = (int) (Math.random() * 10);
			nickname += randDigit.toString();
		}
		
		GuiMessage guiMsg = new GuiMessage(serverSocket.getInetAddress().getHostName(),
											serverSocket.getLocalPort());
		
		// Adding new messages types.
		NewMessageType guiClientClass =
				generateNewMsg("uk.ac.cam.pm576.fjava.tick4star.GuiChatClient");
		NewMessageType guiClientClassTextMsg =
				generateNewMsg("uk.ac.cam.pm576.fjava.tick4star.TextMsgListener");
		NewMessageType guiClientClassSendMsg =
				generateNewMsg("uk.ac.cam.pm576.fjava.tick4star.SendMsgListener");
		NewMessageType guiClientClassChangeNick =
				generateNewMsg("uk.ac.cam.pm576.fjava.tick4star.ChangeNickListener");
		NewMessageType guiMsgClass =
				generateNewMsg("uk.ac.cam.cl.fjava.messages.GuiMessage");
		
		clientMessages = new SafeMessageQueue<Message>();
		clientMessages.put(guiClientClassTextMsg);
		clientMessages.put(guiClientClassSendMsg);
		clientMessages.put(guiClientClassChangeNick);
		clientMessages.put(guiClientClass);
		clientMessages.put(guiMsgClass);
		clientMessages.put(guiMsg);
		
		multiQueue.register(clientMessages);
		clientConnected = true;
		
		// Now sending general messages
//		String newMessage = nickname + " connected from ";
//		newMessage += socket.getInetAddress().getHostName() + ".";
//		
//		StatusMessage newClient = new StatusMessage(newMessage);
//		multiQueue.put(newClient);
		
		Thread incoming = new Thread() {
			@Override
			public void run() {
				try {
					readIncoming(socket);
					
				} catch (IOException ioe) {
					clientConnected = false;
					multiQueue.deregister(clientMessages);
					
					if (isGuiClient) {
						String leaveMsg = nickname + " has disconnected.";
						multiQueue.put(new StatusMessage(leaveMsg));
					}
					
					return;
					
				} catch (ClassNotFoundException cnfe) {
					cnfe.printStackTrace();
				
				} catch (AccessControlException ace) {
					ace.printStackTrace();
				}
			}
		};
		incoming.setDaemon(true);
		incoming.start();
		
		Thread outgoing = new Thread() {
			@Override
			public void run() {
				try {
					writeOutgoing(socket);
					
				} catch (IOException ioe) {
					clientConnected = false;
					multiQueue.deregister(clientMessages);
					
					if (isGuiClient) {
						String leaveMsg = nickname + " has disconnected.";
						multiQueue.put(new StatusMessage(leaveMsg));
					}
					
					return;
				}
			}
		};
		outgoing.setDaemon(true);
		outgoing.start();
	}
	
	private void writeOutgoing(Socket socket) throws IOException {
		ObjectOutputStream outputObj =
				new ObjectOutputStream(socket.getOutputStream());
		
		while (clientConnected) {
			if (clientMessages.empty() == false) {
				Message newMessage = clientMessages.take();
				outputObj.writeObject(newMessage);
				outputObj.flush();
			}
		}
		
		outputObj.close();
	}
	
	private void readIncoming(Socket socket) throws IOException, ClassNotFoundException {
		ObjectInputStream inputObj =
				new ObjectInputStream(socket.getInputStream());
		Message msg = (Message) inputObj.readObject();
		
		while (msg != null && clientConnected) {
			if (msg instanceof ChatMessage) {
				RelayMessage newRelay = new RelayMessage(nickname, (ChatMessage) msg);
				multiQueue.put(newRelay);
				
			} else if (msg instanceof ChangeNickMessage) {
				String prevName = nickname;
				nickname = ((ChangeNickMessage) msg).name;
				
				String statusUpdate = prevName + " is now known as " +
									  nickname + ".";
				
				multiQueue.put(new StatusMessage(statusUpdate));
				
			} else if (msg instanceof StatusMessage) {
				if (((StatusMessage) msg).getMessage().equals("Gui") == false) {
					throw new AssertionError("Unexpected");
				}
				
				String newMessage = nickname + " connected from ";
				newMessage += socket.getInetAddress().getHostName() + ".";
				
				StatusMessage newClient = new StatusMessage(newMessage);
				multiQueue.put(newClient);
				isGuiClient = true;
			}
			
			msg = (Message) inputObj.readObject();
		}
		
		inputObj.close();
	}
	
	public NewMessageType generateNewMsg(String className) throws ClassNotFoundException {
		Class<?> findClass = Class.forName(className);
		String classN = findClass.getName();
		InputStream inputStream = findClass.getClassLoader().getResourceAsStream(
                					className.replaceAll("\\.", "/") + ".class");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] classBytes = new byte[16384];
		int bytesRead;
		
		try {
			while ((bytesRead = inputStream.read(classBytes)) != -1) {
		        baos.write(classBytes, 0, bytesRead);
		    }
			
		} catch(IOException ioe) {
			return null;
			
		} finally {
			try {
				inputStream.close();
			} catch (IOException ex) {}
		}
		
		return new NewMessageType(classN, baos.toByteArray());
	}
}

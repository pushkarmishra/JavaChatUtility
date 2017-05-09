package uk.ac.cam.pm576.fjava.tick4star;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import uk.ac.cam.cl.fjava.messages.Message;

public class ChatServer {
	@SuppressWarnings("unused")
	public static void main(String args[]) throws IOException, InterruptedException, ClassNotFoundException {
		if (args.length != 1) {
			System.out.println("Usage: java ChatServer <port>");
			return;
		}
		
		final Integer port;
		try {
			port = Integer.parseInt(args[0]);
			
		} catch (NumberFormatException nfe) {
			System.out.println("Usage: java ChatServer <port>");
			return;
		}
		
		final ServerSocket serverSocket;
		try {
			serverSocket = new ServerSocket(port);
			
		} catch (IOException e) {
			System.out.println("Cannot use port number " + port.toString());
			return;
		}
		
		MultiQueue<Message> multiQueue = new MultiQueue<Message>();
		ClientHandler.serverSocket = serverSocket;
		
		while (true) {
			Socket newSocket = serverSocket.accept();
			ClientHandler newClient = new ClientHandler(newSocket, multiQueue);
		}
	}
}

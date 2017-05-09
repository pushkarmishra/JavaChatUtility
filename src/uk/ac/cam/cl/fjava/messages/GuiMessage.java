package uk.ac.cam.cl.fjava.messages;

import uk.ac.cam.pm576.fjava.tick4star.GuiChatClient;

public class GuiMessage extends Message {
	private static final long serialVersionUID = 1L;
	public String serverName;
	public int portNumber;

	public GuiMessage(String server, int port) {
		super();
		serverName = server;
		portNumber = port;
	}

	@Execute
	public void startGuiChat() {
		GuiChatClient guiChat = new GuiChatClient();
		guiChat.createGuiChatClient(serverName, portNumber);
	}
}

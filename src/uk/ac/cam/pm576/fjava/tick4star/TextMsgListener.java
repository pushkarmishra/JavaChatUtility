package uk.ac.cam.pm576.fjava.tick4star;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;

public class TextMsgListener extends KeyAdapter {
	private JButton sendMsg;
	
	public TextMsgListener(JButton mSendMsg) {
		sendMsg = mSendMsg;
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			sendMsg.doClick();
		}
	}
}

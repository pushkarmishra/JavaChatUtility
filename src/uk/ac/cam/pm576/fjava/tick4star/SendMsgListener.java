package uk.ac.cam.pm576.fjava.tick4star;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectOutputStream;

import javax.swing.JTextArea;

import uk.ac.cam.cl.fjava.messages.ChatMessage;

public class SendMsgListener implements ActionListener {
	private JTextArea text;
	private ObjectOutputStream outputObj;
	
	public SendMsgListener(JTextArea mText, ObjectOutputStream oos) {
		outputObj = oos;
		text = mText;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		String relayText = text.getText();
		text.setText(null);
		
		try {
			outputObj.writeObject(new ChatMessage(relayText));
			outputObj.flush();
			
		} catch (IOException e1) {
			e1.printStackTrace();	
		}
	}
}
package uk.ac.cam.pm576.fjava.tick4star;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectOutputStream;

import javax.swing.JOptionPane;

import uk.ac.cam.cl.fjava.messages.ChangeNickMessage;

public class ChangeNickListener implements ActionListener {
	private ObjectOutputStream outputObj;
	
	public ChangeNickListener(ObjectOutputStream oos) {
		outputObj = oos;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		String newNick =
				JOptionPane.showInputDialog("Enter the desired nickname");
		
		try {
			outputObj.writeObject(new ChangeNickMessage(newNick));
			outputObj.flush();
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}

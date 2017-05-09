package uk.ac.cam.pm576.fjava.tick4star;

import java.util.HashSet;
import java.util.Set;

public class MultiQueue<T> {
	private Set< MessageQueue<T> > outputs = new HashSet< MessageQueue<T> >();//TODO
	
	public void register(MessageQueue<T> q) { 
		//TODO: add "q" to "outputs";
		synchronized (this) {
			outputs.add(q);
			this.notifyAll();
		}
	}
	
	public void deregister(MessageQueue<T> q) {
		//TODO: remove "q" from "outputs"
		synchronized (this) {
			outputs.remove(q);
			this.notifyAll();
		}
	}
	
	public void put(T message) {
		//TODO: copy "message" to all elements in "outputs"
		synchronized (this) {
			for (MessageQueue<T> msgQ: outputs) {
				msgQ.put(message);
			}
			
			this.notifyAll();
		}
	}
}

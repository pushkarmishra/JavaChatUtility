package uk.ac.cam.pm576.fjava.tick4star;

public class SafeMessageQueue<T> implements MessageQueue<T> {
	private static class Link<L> {
		L val;
		Link<L> next;
		Link(L val) { this.val = val; this.next = null; }
	}
	
	private Link<T> first = null;
	private Link<T> last = null;

	public void put(T val) {
		synchronized (this) {
			Link<T> newEntry = new Link<T>(val);
			
			if (last == null) {
				first = last = newEntry;
				
			} else {
				last.next = newEntry;
				last = last.next;
			}
			
			this.notifyAll();
		}
	}

	public T take() {
		synchronized (this) {
			while (first == null) {//use a loop to block thread until data is available
				try {
					this.wait();
					
				} catch (InterruptedException ie) {
					; // do nothing
				}
			}
			
			T retVal = first.val;
			first = first.next;
			
			if (first == null) {
				last = null;
			}
			
			this.notifyAll();
			return retVal;
		}
	}
	
	public boolean empty() {
		synchronized (this) {
			this.notifyAll();
			return (first == null);
		}
	}
}


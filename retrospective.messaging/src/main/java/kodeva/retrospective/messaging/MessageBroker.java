package kodeva.retrospective.messaging;

/**
 * Message broker for exchanging messages among components.
 * 
 * @author Stepan Hrbacek
 */
public class MessageBroker {
	/**
	 * Send a message to all matching subscribers.
	 * 
	 * @param message
	 */
	public void sendMessage(Message message) {
		
	}
	
	/**
	 * Adds the processor (subscriber) for messages matching the filter.
	 *  
	 * @param filter
	 * @param processor
	 */
	public void subscribe(MessageFilter filter, MessageProcessor processor) {
		
	}
}

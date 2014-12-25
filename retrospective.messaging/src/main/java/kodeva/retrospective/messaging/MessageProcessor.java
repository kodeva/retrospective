package kodeva.retrospective.messaging;

/**
 * Callback interface for processing messages.
 * 
 * @author Stepan Hrbacek
 */
public interface MessageProcessor {
	/**
	 * Processes a message
	 * @param message
	 *  message to be processed
	 */
	void process(Message message);
}

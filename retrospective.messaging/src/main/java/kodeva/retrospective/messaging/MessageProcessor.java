package kodeva.retrospective.messaging;

/**
 * Callback interface for processing messages.
 * 
 * @author Stepan Hrbacek
 */
public interface MessageProcessor {
	void process(Message message);
}

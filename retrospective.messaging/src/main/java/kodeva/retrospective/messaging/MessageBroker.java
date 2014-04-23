package kodeva.retrospective.messaging;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

/**
 * Message broker for exchanging messages among components.
 * 
 * @author Stepan Hrbacek
 */
public class MessageBroker {

	private Collection<Map.Entry<MessageFilter, MessageProcessor>> subscriptions = Collections.synchronizedCollection(new ArrayList<Map.Entry<MessageFilter, MessageProcessor>>());

	/**
	 * Send a message to all matching subscribers.
	 * 
	 * @param message
	 */
	public void sendMessage(Message message) {
		if (message == null) {
			throw new NullPointerException();
		}
	
		final Collection<MessageProcessor> processors = new ArrayList<>();
		synchronized (subscriptions) {
			final Iterator<Map.Entry<MessageFilter, MessageProcessor>> iter = subscriptions.iterator();
			while (iter.hasNext()) {
				final Map.Entry<MessageFilter, MessageProcessor> subscription = iter.next();
				if (subscription.getKey().matches(message)) {
					processors.add(subscription.getValue());
				}
			}
		}

		for (MessageProcessor processor : processors) {
			processor.process(message);
		}
	}

	/**
	 * Adds the processor (subscriber) for messages matching the filter.
	 *  
	 * @param filter
	 * @param processor
	 */
	public void subscribe(MessageFilter filter, MessageProcessor processor) {
		if ((filter == null) || (processor == null)) {
			throw new NullPointerException();
		}
		
		subscriptions.add(new AbstractMap.SimpleImmutableEntry<>(filter, processor));
	}

	/**
	 * Removes the processor (subscriber) for messages matching the filter.
	 *  
	 * @param filter
	 * @param processor
	 */
	public void unsubscribe(MessageFilter filter, MessageProcessor processor) {
		if ((filter == null) || (processor == null)) {
			throw new NullPointerException();
		}
		
		subscriptions.remove(new AbstractMap.SimpleImmutableEntry<>(filter, processor));
	}
}

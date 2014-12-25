package kodeva.retrospective.controller;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.AbstractMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import kodeva.retrospective.messaging.Message;
import kodeva.retrospective.messaging.MessageBroker;
import kodeva.retrospective.messaging.MessageProcessor;

/**
 * Base controller for events that handles error states.
 * 
 * @author Stepan Hrbacek
 */
public abstract class BaseController implements MessageProcessor {
	private static final Logger LOGGER = Logger.getLogger(BaseController.class.getName());

	protected final MessageBroker messageBroker;

	public BaseController(MessageBroker messageBroker) {
		this.messageBroker = messageBroker;
	}
	
	public final void process(Message message) {
		try {
			processMessage(message);
		} catch (Throwable t) {
			if (LOGGER.isLoggable(Level.INFO)) {
	    		LOGGER.log(Level.INFO, String.format("Informing user about message processing error: '%s'", message), t);
			}

			final StringWriter sw = new StringWriter();
			t.printStackTrace(new PrintWriter(sw));
			messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
					.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_ERROR))
					.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.ERROR_ORIGINAL_MESSAGE, message.toString()))
					.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.ERROR_STACKTRACE, sw.toString()))
					.build());
		}
	}

	/**
	 * Processes message. Can throw exception, this is processed in BaseController.
	 * @param message
	 *  message to be processed
	 */
	protected abstract void processMessage(Message message);
}

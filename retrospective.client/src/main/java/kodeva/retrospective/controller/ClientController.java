package kodeva.retrospective.controller;

import kodeva.retrospective.controller.websockets.ClientWebSocketsEndpoint;
import kodeva.retrospective.messaging.Message;
import kodeva.retrospective.messaging.MessageBroker;
import kodeva.retrospective.messaging.MessageFilter;
import kodeva.retrospective.messaging.MessageProcessor;

/**
 * Controller for remote actions on client.
 * 
 * @author Stepan Hrbacek
 */
public class ClientController implements MessageProcessor {
	private ClientWebSocketsEndpoint clientEndpoint;
	private final MessageFilter viewFilter;
	private final MessageBroker messageBroker;
	
	public ClientController(MessageBroker messageBroker) {
		this.messageBroker = messageBroker;
		clientEndpoint = new ClientWebSocketsEndpoint(messageBroker);
		messageBroker.subscribe(viewFilter = new MessageFilter.Builder().sender(kodeva.retrospective.view.Constants.Messaging.SENDER).build(), this);
	}

	public void close() {
		clientEndpoint.close();
		clientEndpoint = null;
		messageBroker.unsubscribe(viewFilter, this);
	}
	
	@Override
	public void process(Message message) {
		switch (message.getValues(kodeva.retrospective.view.Constants.Messaging.Key.EVENT).iterator().next()) {
		case kodeva.retrospective.view.Constants.Messaging.Value.KEY_EVENT_CARD_DELETE:
		case kodeva.retrospective.view.Constants.Messaging.Value.KEY_EVENT_CARD_POSTIT:
		case kodeva.retrospective.view.Constants.Messaging.Value.KEY_EVENT_CARD_EDIT:
		case kodeva.retrospective.view.Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_INCREMENT:
		case kodeva.retrospective.view.Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_DECREMENT:
			clientEndpoint.send(message);
			break;
		}
	}
}

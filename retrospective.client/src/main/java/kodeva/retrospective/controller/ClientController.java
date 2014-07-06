package kodeva.retrospective.controller;

import java.util.AbstractMap.SimpleImmutableEntry;

import kodeva.retrospective.controller.websockets.ClientWebSocketsEndpoint;
import kodeva.retrospective.messaging.Message;
import kodeva.retrospective.messaging.MessageBroker;
import kodeva.retrospective.messaging.MessageFilter;
import kodeva.retrospective.messaging.MessageProcessor;
import kodeva.retrospective.model.Constants;
import kodeva.retrospective.model.Model;

/**
 * Controller for remote actions on client.
 * 
 * @author Stepan Hrbacek
 */
public class ClientController implements MessageProcessor {
	private ClientWebSocketsEndpoint clientEndpoint;
	private final MessageFilter viewFilter;
	private final MessageBroker messageBroker;
	private final Model model;
	
	public ClientController(MessageBroker messageBroker, Model model) {
		this.messageBroker = messageBroker;
		this.model = model;
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
			final Message messageOut = new Message.Builder().string(message.toString())
				.entry(new SimpleImmutableEntry<>(Constants.Messaging.Key.USER_DESK_ID, model.getUserDesk().getId()))
				.entry(new SimpleImmutableEntry<>(Constants.Messaging.Key.PIN_WALL_ID, model.getPinWall().getId()))
				.build();
			clientEndpoint.send(messageOut);
			break;
		}
	}
}

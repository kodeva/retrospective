package kodeva.retrospective.controller;

import java.util.AbstractMap.SimpleImmutableEntry;

import kodeva.retrospective.controller.websockets.ClientWebSocketsEndpoint;
import kodeva.retrospective.messaging.Message;
import kodeva.retrospective.messaging.MessageBroker;
import kodeva.retrospective.messaging.MessageFilter;
import kodeva.retrospective.messaging.MessageProcessor;
import kodeva.retrospective.model.Constants;
import kodeva.retrospective.model.EntityMessageAdapter;
import kodeva.retrospective.model.Model;

/**
 * Controller for remote actions on client.
 * 
 * @author Stepan Hrbacek
 */
public class ClientController implements MessageProcessor {
	private ClientWebSocketsEndpoint clientEndpoint;
	private final MessageFilter viewFilter, controllerFilter;
	private final MessageBroker messageBroker;
	private final Model model;
	
	public ClientController(MessageBroker messageBroker, Model model) {
		this.messageBroker = messageBroker;
		this.model = model;
		clientEndpoint = new ClientWebSocketsEndpoint(messageBroker);
		messageBroker.subscribe(viewFilter = new MessageFilter.Builder().sender(kodeva.retrospective.view.Constants.Messaging.SENDER).build(), this);
		messageBroker.subscribe(controllerFilter = new MessageFilter.Builder().sender(kodeva.retrospective.controller.Constants.Messaging.SENDER).build(), this);
	}

	public void close() {
		clientEndpoint.close();
		clientEndpoint = null;
		messageBroker.unsubscribe(viewFilter, this);
		messageBroker.unsubscribe(controllerFilter, this);
	}
	
	@Override
	public void process(Message message) {
		switch (message.getSender()) {
		case kodeva.retrospective.view.Constants.Messaging.SENDER:
			// Send non-local view messages for confirmation over wire
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
			break;

		case kodeva.retrospective.controller.Constants.Messaging.SENDER:
			// Project model messages received over wire to local model changes
			final String userDeskId = message.getValues(Constants.Messaging.Key.USER_DESK_ID).iterator().next();
			switch (message.getValues(kodeva.retrospective.model.Constants.Messaging.Key.EVENT).iterator().next()) {
			case kodeva.retrospective.model.Constants.Messaging.Value.KEY_EVENT_CARD_PUBLISH:
				model.publishCard(EntityMessageAdapter.toCardBuilder(message).build(), userDeskId);
				break;
			case kodeva.retrospective.model.Constants.Messaging.Value.KEY_EVENT_VOTE_ADD:
				model.addVote(EntityMessageAdapter.toCardBuilder(message).build(), userDeskId);
				break;
			case kodeva.retrospective.model.Constants.Messaging.Value.KEY_EVENT_VOTE_REMOVE:
				model.removeVote(EntityMessageAdapter.toCardBuilder(message).build(), userDeskId);
				break;
			}
			break;
		}
	}
}

package kodeva.retrospective.controller;

import java.util.AbstractMap.SimpleImmutableEntry;

import kodeva.retrospective.controller.websockets.ClientWebSocketsEndpoint;
import kodeva.retrospective.messaging.Message;
import kodeva.retrospective.messaging.MessageBroker;
import kodeva.retrospective.messaging.MessageFilter;
import kodeva.retrospective.model.Constants;
import kodeva.retrospective.model.EntityMessageAdapter;
import kodeva.retrospective.model.Model;

/**
 * Controller for remote actions on client.
 * 
 * @author Stepan Hrbacek
 */
public class ClientController extends BaseController {
	private ClientWebSocketsEndpoint clientEndpoint;
	private final MessageFilter viewFilter, controllerFilterRemote, controllerFilterLocal;
	private final Model model;
	
	public ClientController(MessageBroker messageBroker, Model model) {
		super(messageBroker);
		this.model = model;
		clientEndpoint = new ClientWebSocketsEndpoint(this.messageBroker);
		this.messageBroker.subscribe(viewFilter = new MessageFilter.Builder().sender(kodeva.retrospective.view.Constants.Messaging.SENDER).build(), this);
		this.messageBroker.subscribe(controllerFilterRemote = new MessageFilter.Builder().sender(kodeva.retrospective.controller.Constants.Messaging.SENDER).build()
				.and(new MessageFilter.Builder().key(kodeva.retrospective.controller.Constants.Messaging.Key.EVENT).build().not()), this);
		this.messageBroker.subscribe(controllerFilterLocal = new MessageFilter.Builder().key(kodeva.retrospective.controller.Constants.Messaging.Key.EVENT).value(kodeva.retrospective.controller.Constants.Messaging.Value.KEY_EVENT_MODEL_SYNC).build(), this);
	}

	public void close() {
		clientEndpoint.close();
		clientEndpoint = null;
		messageBroker.unsubscribe(viewFilter, this);
		messageBroker.unsubscribe(controllerFilterRemote, this);
		messageBroker.unsubscribe(controllerFilterLocal, this);
	}
	
	@Override
	public void processMessage(Message message) {
		switch (message.getSender()) { 
		case kodeva.retrospective.model.Constants.Messaging.SENDER:
		case kodeva.retrospective.view.Constants.Messaging.SENDER:
			// Send non-local view messages for confirmation / model messages for propagation over wire
			switch (message.getValue(kodeva.retrospective.view.Constants.Messaging.Key.EVENT)) {
			case kodeva.retrospective.model.Constants.Messaging.Value.KEY_EVENT_CARD_DELETE:
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
			final String userDeskId = message.getValue(Constants.Messaging.Key.USER_DESK_ID);
			switch (message.getValue(kodeva.retrospective.model.Constants.Messaging.Key.EVENT)) {
			case kodeva.retrospective.view.Constants.Messaging.Value.KEY_EVENT_CARD_DELETE:
				model.deleteCard(EntityMessageAdapter.toCardBuilder(message).build(), userDeskId);
				break;
			case kodeva.retrospective.model.Constants.Messaging.Value.KEY_EVENT_CARD_PUBLISH:
				model.publishCard(EntityMessageAdapter.toCardBuilder(message).build(), userDeskId);
				break;
			case kodeva.retrospective.model.Constants.Messaging.Value.KEY_EVENT_CARD_UNPUBLISH:
				model.unpublishCard(EntityMessageAdapter.toCardBuilder(message).build(), userDeskId);
				break;
			case kodeva.retrospective.model.Constants.Messaging.Value.KEY_EVENT_VOTE_ADD:
				model.addVote(EntityMessageAdapter.toCardBuilder(message).build(), userDeskId);
				break;
			case kodeva.retrospective.model.Constants.Messaging.Value.KEY_EVENT_VOTE_REMOVE:
				model.removeVote(EntityMessageAdapter.toCardBuilder(message).build(), userDeskId);
				break;
			case kodeva.retrospective.controller.Constants.Messaging.Value.KEY_EVENT_MODEL_SYNC:
				model.synchronize(message.getValue(kodeva.retrospective.controller.Constants.Messaging.Key.SERIALIZED_MODEL));
				break;
			}
			break;
			//TODO 0: wait until sync message is received from server after connection a store all messages into queue
			//TODO 7: process all messages from the queue - drop those with old model version
		}
	}
}

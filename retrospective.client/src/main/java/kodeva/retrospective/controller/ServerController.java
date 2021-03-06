package kodeva.retrospective.controller;

import java.util.AbstractMap.SimpleImmutableEntry;

import kodeva.retrospective.controller.websockets.ServerWebSocketsEndpoint;
import kodeva.retrospective.messaging.Message;
import kodeva.retrospective.messaging.MessageBroker;
import kodeva.retrospective.messaging.MessageFilter;
import kodeva.retrospective.model.Constants;
import kodeva.retrospective.model.EntityMessageAdapter;
import kodeva.retrospective.model.Model;

/**
 * Controller for remote actions on server.
 * 
 * @author Stepan Hrbacek
 */
public class ServerController extends BaseController {
	private final Model model;
	private final MessageFilter viewFilter, modelFilter, controllerFilterRemote, controllerFilterLocal;

	public ServerController(MessageBroker messageBroker, Model model) {
		super(messageBroker);
		this.model = model;
		this.messageBroker.subscribe(viewFilter = new MessageFilter.Builder().sender(kodeva.retrospective.view.Constants.Messaging.SENDER).build(), this);
		this.messageBroker.subscribe(modelFilter = new MessageFilter.Builder().sender(kodeva.retrospective.model.Constants.Messaging.SENDER).build(), this);
		this.messageBroker.subscribe(controllerFilterRemote = new MessageFilter.Builder().sender(kodeva.retrospective.controller.Constants.Messaging.SENDER).build()
				.and(new MessageFilter.Builder().key(kodeva.retrospective.controller.Constants.Messaging.Key.EVENT).build().not()), this);
		this.messageBroker.subscribe(controllerFilterLocal = new MessageFilter.Builder().key(kodeva.retrospective.controller.Constants.Messaging.Key.EVENT).value(kodeva.retrospective.controller.Constants.Messaging.Value.KEY_EVENT_MODEL_SYNC_REQUEST).build(), this);

		ServerWebSocketsEndpoint.start(this.messageBroker);
	}

	public void close() {
		messageBroker.unsubscribe(viewFilter, this);
		messageBroker.unsubscribe(modelFilter, this);
		messageBroker.unsubscribe(controllerFilterRemote, this);
		messageBroker.unsubscribe(controllerFilterLocal, this);
		ServerWebSocketsEndpoint.stop();
	}

	@Override
	public void processMessage(Message message) {
		String userDeskId = model.getUserDesk().getId();
		switch (message.getSender()) {
		case kodeva.retrospective.controller.Constants.Messaging.SENDER:
			final String localEvent = message.getValue(kodeva.retrospective.controller.Constants.Messaging.Key.EVENT);
			if (localEvent == null) {
				// Messages received over wire must always contain UserDesk ID
				userDeskId = message.getValue(Constants.Messaging.Key.USER_DESK_ID);
			} else {
				switch (localEvent) {
				case kodeva.retrospective.controller.Constants.Messaging.Value.KEY_EVENT_MODEL_SYNC_REQUEST:
					final String modelStr = model.serializeForSynchronization();
					final String receiver = message.getReceiver();
			    	final Message.Builder messageBuilder = new Message.Builder().sender(kodeva.retrospective.controller.Constants.Messaging.SENDER)
			    			.entry(new SimpleImmutableEntry<>(kodeva.retrospective.controller.Constants.Messaging.Key.SERIALIZED_MODEL, modelStr))
			    			.entry(new SimpleImmutableEntry<>(kodeva.retrospective.controller.Constants.Messaging.Key.EVENT, kodeva.retrospective.controller.Constants.Messaging.Value.KEY_EVENT_MODEL_SYNC));
			    	if (receiver != null) {
			    		messageBuilder.receiver(receiver);
			    	}
			    	messageBroker.sendMessage(messageBuilder.build());
				}
				break;
			}

		case kodeva.retrospective.view.Constants.Messaging.SENDER:
			switch (message.getValue(kodeva.retrospective.view.Constants.Messaging.Key.EVENT)) {
			case kodeva.retrospective.view.Constants.Messaging.Value.KEY_EVENT_CARD_DELETE:
				model.deleteCard(EntityMessageAdapter.toCardBuilder(message).build(), userDeskId);
				break;
			case kodeva.retrospective.view.Constants.Messaging.Value.KEY_EVENT_CARD_POSTIT:
				model.publishCard(EntityMessageAdapter.toCardBuilder(message).build(), userDeskId);
				break;
			case kodeva.retrospective.view.Constants.Messaging.Value.KEY_EVENT_CARD_EDIT:
				model.unpublishCard(EntityMessageAdapter.toCardBuilder(message).build(), userDeskId);
				break;
			case kodeva.retrospective.view.Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_INCREMENT:
				model.addVote(EntityMessageAdapter.toCardBuilder(message).build(), userDeskId);
				break;
			case kodeva.retrospective.view.Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_DECREMENT:
				model.removeVote(EntityMessageAdapter.toCardBuilder(message).build(), userDeskId);
				break;
			}
			break;

		case kodeva.retrospective.model.Constants.Messaging.SENDER:
			switch (message.getValue(kodeva.retrospective.model.Constants.Messaging.Key.EVENT)) {
			case kodeva.retrospective.model.Constants.Messaging.Value.KEY_EVENT_CARD_DELETE:
			case kodeva.retrospective.model.Constants.Messaging.Value.KEY_EVENT_CARD_PUBLISH:
			case kodeva.retrospective.model.Constants.Messaging.Value.KEY_EVENT_CARD_UNPUBLISH:
			case kodeva.retrospective.model.Constants.Messaging.Value.KEY_EVENT_VOTE_ADD:
			case kodeva.retrospective.model.Constants.Messaging.Value.KEY_EVENT_VOTE_REMOVE:
				ServerWebSocketsEndpoint.send(message);
				break;
			}
			break;
		}
	}
}

package kodeva.retrospective.controller;

import kodeva.retrospective.controller.websockets.ServerWebSocketsEndpoint;
import kodeva.retrospective.messaging.Message;
import kodeva.retrospective.messaging.MessageBroker;
import kodeva.retrospective.messaging.MessageFilter;
import kodeva.retrospective.messaging.MessageProcessor;
import kodeva.retrospective.model.Constants;
import kodeva.retrospective.model.EntityMessageAdapter;
import kodeva.retrospective.model.Model;

/**
 * Controller for remote actions on server.
 * 
 * @author Stepan Hrbacek
 */
public class ServerController implements MessageProcessor {
	private final Model model;
	private final MessageFilter viewFilter, modelFilter, controllerFilter;
	private final MessageBroker messageBroker;

	public ServerController(MessageBroker messageBroker, Model model) {
		this.model = model;
		(this.messageBroker = messageBroker).subscribe(viewFilter = new MessageFilter.Builder().sender(kodeva.retrospective.view.Constants.Messaging.SENDER).build(), this);
		this.messageBroker.subscribe(modelFilter = new MessageFilter.Builder().sender(kodeva.retrospective.model.Constants.Messaging.SENDER).build(), this);
		this.messageBroker.subscribe(controllerFilter = new MessageFilter.Builder().sender(kodeva.retrospective.controller.Constants.Messaging.SENDER).build(), this);
		ServerWebSocketsEndpoint.start(this.messageBroker);
	}

	public void close() {
		messageBroker.unsubscribe(viewFilter, this);
		messageBroker.unsubscribe(modelFilter, this);
		messageBroker.unsubscribe(controllerFilter, this);
		ServerWebSocketsEndpoint.stop();
	}

	@Override
	public void process(Message message) {
		String userDeskId = model.getUserDesk().getId(); 
		switch (message.getSender()) {
		case kodeva.retrospective.controller.Constants.Messaging.SENDER:
			// Messages received over wire must always contain UserDesk ID
			userDeskId = message.getValues(Constants.Messaging.Key.USER_DESK_ID).iterator().next();
		case kodeva.retrospective.view.Constants.Messaging.SENDER:
			switch (message.getValues(kodeva.retrospective.view.Constants.Messaging.Key.EVENT).iterator().next()) {
			case kodeva.retrospective.view.Constants.Messaging.Value.KEY_EVENT_CARD_DELETE:
				model.deleteCard(EntityMessageAdapter.toCardBuilder(message).build(), message.getValues(Constants.Messaging.Key.USER_DESK_ID).iterator().next());
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
			switch (message.getValues(kodeva.retrospective.model.Constants.Messaging.Key.EVENT).iterator().next()) {
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

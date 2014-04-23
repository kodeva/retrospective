package kodeva.retrospective.controller;

import kodeva.retrospective.messaging.Message;
import kodeva.retrospective.messaging.MessageBroker;
import kodeva.retrospective.messaging.MessageFilter;
import kodeva.retrospective.messaging.MessageProcessor;
import kodeva.retrospective.model.EntityMessageAdapter;
import kodeva.retrospective.model.Model;
import kodeva.retrospective.model.entity.Card;
import kodeva.retrospective.view.View;

/**
 * Controller for local events.
 * 
 * @author Stepan Hrbacek
 */
public class LocalController implements MessageProcessor {
	private final Model model;
	private final View view;
	private final MessageBroker messageBroker;
	private ClientController clientController;
	private ServerController serverController;

	public LocalController(MessageBroker messageBroker, View view) {
		model = new Model(this.messageBroker = messageBroker);
		this.view = view;
		messageBroker.subscribe(new MessageFilter.Builder().sender(kodeva.retrospective.view.Constants.Messaging.SENDER).build(), this);
		messageBroker.subscribe(new MessageFilter.Builder().sender(kodeva.retrospective.model.Constants.Messaging.SENDER).build(), this);
	}
	
	@Override
	public void process(Message message) {
		switch (message.getSender()) {
		case kodeva.retrospective.view.Constants.Messaging.SENDER:
			switch (message.getValues(kodeva.retrospective.view.Constants.Messaging.Key.EVENT).iterator().next()) {
			case kodeva.retrospective.view.Constants.Messaging.Value.KEY_EVENT_SESSION_CONNECT:
				if ((clientController == null) && (serverController == null)) {
					clientController = new ClientController(messageBroker);
				}
				break;
			case kodeva.retrospective.view.Constants.Messaging.Value.KEY_EVENT_SESSION_DISCONNECT:
				if (clientController != null) {
					clientController.close();
					clientController = null;
				}
				break;
			case kodeva.retrospective.view.Constants.Messaging.Value.KEY_EVENT_SESSION_START:
				if ((clientController == null) && (serverController == null)) {
					serverController = new ServerController(messageBroker, model);
				}
				break;
			case kodeva.retrospective.view.Constants.Messaging.Value.KEY_EVENT_SESSION_TERMINATE:
				if (serverController != null) {
					serverController.close();
					serverController = null;
				}
				break;
			case kodeva.retrospective.view.Constants.Messaging.Value.KEY_EVENT_CARD_CREATE_WENT_WELL:
				processEventViewCreateCard(Card.Type.WentWell);
				break;
			case kodeva.retrospective.view.Constants.Messaging.Value.KEY_EVENT_CARD_CREATE_NEEDS_IMPROVEMENT:
				processEventViewCreateCard(Card.Type.NeedsImprovement);
				break;
			case kodeva.retrospective.view.Constants.Messaging.Value.KEY_EVENT_CARD_DELETE:
				model.deleteCard(EntityMessageAdapter.toCardBuilder(message).build());
				break;
			case kodeva.retrospective.view.Constants.Messaging.Value.KEY_EVENT_CARD_UPDATE_FRONT_SIDE_TEXT:
				final Card.Builder builder = EntityMessageAdapter.toCardBuilder(message); 
				final Card cardOrig = builder.build();
				final String frontSideTextNew = message.getValues(kodeva.retrospective.view.Constants.Messaging.Key.UPDATED_VALUE).iterator().next();
				final Card cardNew = builder.frontSideText(frontSideTextNew).build();
				model.updateCard(cardOrig, cardNew);
				break;
			}
			break;

		case kodeva.retrospective.model.Constants.Messaging.SENDER:
			switch (message.getValues(kodeva.retrospective.model.Constants.Messaging.Key.EVENT).iterator().next()) {
			case kodeva.retrospective.model.Constants.Messaging.Value.KEY_EVENT_CARD_ADD:
				view.createCardOnUserDesk(EntityMessageAdapter.toCardBuilder(message).build());
				break;
			case kodeva.retrospective.model.Constants.Messaging.Value.KEY_EVENT_CARD_DELETE:
				view.deleteCardFromUserDesk(EntityMessageAdapter.toCardBuilder(message).build());
				break;
			case kodeva.retrospective.model.Constants.Messaging.Value.KEY_EVENT_CARD_UPDATE:
				final String containerType = message.getValues(kodeva.retrospective.model.Constants.Messaging.Key.CARD_CONTAINER_TYPE).iterator().next();
				switch (containerType) {
				case kodeva.retrospective.model.Constants.Messaging.Value.KEY_CARD_CONTAINER_TYPE_USER_DESK:
					view.updateCardOnUserDesk(EntityMessageAdapter.toCardBuilder(message).build());
					break;
				case kodeva.retrospective.model.Constants.Messaging.Value.KEY_CARD_CONTAINER_TYPE_PINWALL:
					view.updateCardOnPinWall(EntityMessageAdapter.toCardBuilder(message).build());
					break;
				}
				break;
			case kodeva.retrospective.model.Constants.Messaging.Value.KEY_EVENT_CARD_PUBLISH: {
				final Card card = EntityMessageAdapter.toCardBuilder(message).build();
				view.deleteCardFromUserDesk(card);
				view.createCardOnPinWall(card);
				view.setVoteCountOwn(card, model.getVotesCountTotal(card));
				view.setVoteCountTotal(card, model.getVotesCountTotal(card));
				break;
			}
			case kodeva.retrospective.model.Constants.Messaging.Value.KEY_EVENT_CARD_UNPUBLISH: {
				final Card card = EntityMessageAdapter.toCardBuilder(message).build();
				view.deleteCardFromPinWall(card);
				view.createCardOnUserDesk(card);
				view.setVoteCountOwn(card, model.getVotesCountTotal(card));
				view.setVoteCountTotal(card, model.getVotesCountTotal(card));
				break;
			}
			case kodeva.retrospective.model.Constants.Messaging.Value.KEY_EVENT_VOTE_ADD:
			case kodeva.retrospective.model.Constants.Messaging.Value.KEY_EVENT_VOTE_REMOVE: {
				final Card card = EntityMessageAdapter.toCardBuilder(message).build();
				view.setVoteCountOwn(card, model.getVotesCountTotal(card));
				view.setVoteCountTotal(card, model.getVotesCountTotal(card));
				break;
			}
			}
			break;
		}
	}

	private void processEventViewCreateCard(Card.Type type) {
		model.createCard(new Card.Builder().type(type).build());
	}
}

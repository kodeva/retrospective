package kodeva.retrospective.controller;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.AbstractMap;

import kodeva.retrospective.messaging.Message;
import kodeva.retrospective.messaging.MessageBroker;
import kodeva.retrospective.model.EntityMessageAdapter;
import kodeva.retrospective.model.entity.Card;
import kodeva.retrospective.model.entity.Card.Type;
import kodeva.retrospective.model.entity.UserDesk;
import kodeva.retrospective.view.Constants;
import kodeva.retrospective.view.View;

import org.junit.Before;
import org.junit.Test;

public class LocalControllerTest {
	
	private MessageBroker organizatorMessageBroker, participantMessageBroker;
	private View organizatorView, participantView;
	private Card organizatorWentWellCard, participantNeedsImprovementCard;

	@Before
	public void beforeTest() {
		organizatorMessageBroker = new MessageBroker();
		organizatorView = mock(View.class);
		new LocalController(organizatorMessageBroker, organizatorView);

		participantMessageBroker = new MessageBroker();
		participantView = mock(View.class);
		new LocalController(participantMessageBroker, participantView);
	}

	@Test
	public void startRetrospectiveSession() {
		organizatorMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_SESSION_START)).build());
		verify(organizatorView).createUserDesk((UserDesk) any());
	}

	@Test
	public void addNewWellDoneCard() {
		organizatorMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_CREATE_WENT_WELL)).build());
		final CardDeepWithoutIdMatcher cardMatcher = new CardDeepWithoutIdMatcher(new Card.Builder().type(Type.WentWell).build());
		verify(organizatorView).createCardOnUserDesk(argThat(cardMatcher));
		organizatorWentWellCard = cardMatcher.getLastComparedCard();
	}

	@Test
	public void addNewImprovementCard() {
		participantMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_CREATE_NEEDS_IMPROVEMENT)).build());
		final CardDeepWithoutIdMatcher cardMatcher = new CardDeepWithoutIdMatcher(new Card.Builder().type(Type.NeedsImprovement).build());
		verify(participantView).createCardOnUserDesk(argThat(cardMatcher));
		participantNeedsImprovementCard = cardMatcher.getLastComparedCard();
	}

	@Test
	public void removeWellDoneCard() {
		addNewWellDoneCard();
		organizatorMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_DELETE))
				.entries(EntityMessageAdapter.toMessageEntries(organizatorWentWellCard)).build());
		verify(organizatorView).deleteCardFromUserDesk(organizatorWentWellCard);
	}

	@Test
	public void removeImprovementCard() {
		addNewImprovementCard();
		participantMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_DELETE))
				.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
		verify(participantView).deleteCardFromUserDesk(participantNeedsImprovementCard);
	}
	
	@Test
	public void postCard() {
		addNewWellDoneCard();
		organizatorMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_SESSION_START)).build());
		addNewImprovementCard();
		participantMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_SESSION_CONNECT)).build());
		
		organizatorMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_POSTIT))
				.entries(EntityMessageAdapter.toMessageEntries(organizatorWentWellCard)).build());
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
		}
		verify(organizatorView).deleteCardFromUserDesk(organizatorWentWellCard);
		verify(organizatorView).createCardOnPinWall(organizatorWentWellCard);
		verify(participantView, never()).deleteCardFromUserDesk(organizatorWentWellCard);
		verify(participantView).createCardOnPinWall(organizatorWentWellCard);
		
		participantMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_POSTIT))
				.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
		}
		verify(participantView).deleteCardFromUserDesk(participantNeedsImprovementCard);
		verify(participantView).createCardOnPinWall(participantNeedsImprovementCard);
		verify(organizatorView, never()).deleteCardFromUserDesk(participantNeedsImprovementCard);
		verify(organizatorView).createCardOnPinWall(participantNeedsImprovementCard);
		
		participantMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_SESSION_DISCONNECT)).build());
		organizatorMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_SESSION_TERMINATE)).build());
	}
}

package kodeva.retrospective.controller;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.AbstractMap;

import kodeva.retrospective.messaging.Message;
import kodeva.retrospective.messaging.MessageBroker;
import kodeva.retrospective.model.EntityMessageAdapter;
import kodeva.retrospective.model.entity.Card;
import kodeva.retrospective.model.entity.Card.Type;
import kodeva.retrospective.model.entity.UserDesk;
import kodeva.retrospective.view.Constants;
import kodeva.retrospective.view.View;

import org.junit.After;
import org.junit.Test;

public class LocalControllerTest {
	
	private MessageBroker organizatorMessageBroker, participantMessageBroker;
	private View organizatorView, participantView;
	private Card organizatorWentWellCard, participantNeedsImprovementCard;

	@After
	public void terminateSession() {
		if (organizatorMessageBroker != null) {
			organizatorMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
					.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_SESSION_TERMINATE)).build());
		}
	}
	
	@After
	public void disconnectFromSession() {
		if (participantMessageBroker != null) {
			participantMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
					.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_SESSION_DISCONNECT)).build());
		}
	}

	public void createOrganizator() {
		organizatorMessageBroker = new MessageBroker();
		organizatorView = mock(View.class);
		new LocalController(organizatorMessageBroker, organizatorView);
		verify(organizatorView).createUserDesk((UserDesk) any());
		verifyNoMoreInteractions(organizatorView);
	}
	
	@Test
	public void createParticipant() {
		participantMessageBroker = new MessageBroker();
		participantView = mock(View.class);
		new LocalController(participantMessageBroker, participantView);
		verify(participantView).createUserDesk((UserDesk) any());
		verifyNoMoreInteractions(participantView);
	}

	@Test
	public void addNewWellDoneCard() {
		createOrganizator();
		reset(organizatorView);
		
		organizatorMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_CREATE_WENT_WELL)).build());
		final CardDeepWithoutIdMatcher cardMatcher = new CardDeepWithoutIdMatcher(new Card.Builder().type(Type.WentWell).build());
		verify(organizatorView).createCardOnUserDesk(argThat(cardMatcher));
		verifyNoMoreInteractions(organizatorView);
		organizatorWentWellCard = cardMatcher.getLastComparedCard();
	}

	@Test
	public void addNewImprovementCard() {
		createParticipant();
		reset(participantView);

		participantMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_CREATE_NEEDS_IMPROVEMENT)).build());
		final CardDeepWithoutIdMatcher cardMatcher = new CardDeepWithoutIdMatcher(new Card.Builder().type(Type.NeedsImprovement).build());
		verify(participantView).createCardOnUserDesk(argThat(cardMatcher));
		verifyNoMoreInteractions(participantView);
		participantNeedsImprovementCard = cardMatcher.getLastComparedCard();
	}

	@Test
	public void removeWellDoneCard() {
		addNewWellDoneCard();
		reset(organizatorView);
		
		organizatorMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_DELETE))
				.entries(EntityMessageAdapter.toMessageEntries(organizatorWentWellCard)).build());
		verify(organizatorView).deleteCardFromUserDesk(organizatorWentWellCard);
		verifyNoMoreInteractions(organizatorView);
	}

	@Test
	public void removeImprovementCard() {
		addNewImprovementCard();
		reset(participantView);

		participantMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_DELETE))
				.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
		verify(participantView).deleteCardFromUserDesk(participantNeedsImprovementCard);
		verifyNoMoreInteractions(participantView);
	}
	
	@Test
	public void startRetrospectiveSession() {
		createOrganizator();
		reset(organizatorView);
		
		organizatorMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_SESSION_START)).build());
		verify(organizatorView).createPinWall();
		verifyNoMoreInteractions(organizatorView);
	}

	public void connectToRetrospectiveSession() {
		createParticipant();
		reset(participantView);

		participantMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_SESSION_CONNECT)).build());
		verify(participantView).createPinWall();
		verifyNoMoreInteractions(participantView);
	}

	@Test
	public void postCard() {
		addNewWellDoneCard();
		addNewImprovementCard();
		startRetrospectiveSession();
		connectToRetrospectiveSession();
		reset(organizatorView);
		reset(participantView);
		
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
		verifyNoMoreInteractions(organizatorView);
		verifyNoMoreInteractions(participantView);

		reset(organizatorView);
		reset(participantView);
		
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
		verifyNoMoreInteractions(organizatorView);
		verifyNoMoreInteractions(participantView);
	}
}

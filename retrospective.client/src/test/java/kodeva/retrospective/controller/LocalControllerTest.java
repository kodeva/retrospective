package kodeva.retrospective.controller;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
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
	private static final long WAIT_MILLIS = 250L;
	
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

	@Test
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
	public void updateFrontSideTextOnWellDoneCard() {
		addNewWellDoneCard();
		reset(organizatorView);

		organizatorMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_UPDATE_FRONT_SIDE_TEXT))
				.entries(EntityMessageAdapter.toMessageEntries(organizatorWentWellCard))
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.UPDATED_VALUE, "Updated text")).build());
		final CardDeepWithoutIdMatcher cardMatcher = new CardDeepWithoutIdMatcher(new Card.Builder().type(Type.WentWell).frontSideText("Updated text").build());
		verify(organizatorView).updateCardOnUserDesk(argThat(cardMatcher));
		verifyNoMoreInteractions(organizatorView);
	}

	@Test
	public void updateFrontSideTextOnImprovementCard() {
		addNewImprovementCard();
		reset(participantView);

		participantMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_UPDATE_FRONT_SIDE_TEXT))
				.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard))
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.UPDATED_VALUE, "Updated text")).build());
		final CardDeepWithoutIdMatcher cardMatcher = new CardDeepWithoutIdMatcher(new Card.Builder().type(Type.NeedsImprovement).frontSideText("Updated text").build());
		verify(participantView).updateCardOnUserDesk(argThat(cardMatcher));
		verifyNoMoreInteractions(participantView);
	}
	
	private void startRetrospectiveSessionAndConnectClients() {
		reset(organizatorView);
		organizatorMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_SESSION_START)).build());
		verify(organizatorView).createPinWall();
		verifyNoMoreInteractions(organizatorView);

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
		startRetrospectiveSessionAndConnectClients();
		reset(organizatorView);
		reset(participantView);
		
		organizatorMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_POSTIT))
				.entries(EntityMessageAdapter.toMessageEntries(organizatorWentWellCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		verify(organizatorView).deleteCardFromUserDesk(organizatorWentWellCard);
		verify(organizatorView).createCardOnPinWall(organizatorWentWellCard);
		verify(participantView).createCardOnPinWall(organizatorWentWellCard);
		verifyNoMoreInteractions(organizatorView);
		verifyNoMoreInteractions(participantView);

		reset(organizatorView);
		reset(participantView);
		
		participantMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_POSTIT))
				.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		verify(participantView).deleteCardFromUserDesk(participantNeedsImprovementCard);
		verify(participantView).createCardOnPinWall(participantNeedsImprovementCard);
		verify(participantView).setVoteCountOwn(participantNeedsImprovementCard, 0);
		verify(participantView).setVoteCountTotal(participantNeedsImprovementCard, 0);
		verify(organizatorView).createCardOnPinWall(participantNeedsImprovementCard);
		verify(organizatorView).setVoteCountOwn(participantNeedsImprovementCard, 0);
		verify(organizatorView).setVoteCountTotal(participantNeedsImprovementCard, 0);
		verifyNoMoreInteractions(organizatorView);
		verifyNoMoreInteractions(participantView);
	}
	
	@Test
	public void editCard() {
		addVote();
		reset(organizatorView);
		reset(participantView);

		organizatorMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_EDIT))
				.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		verify(organizatorView).createCardOnUserDesk(participantNeedsImprovementCard);
		verify(organizatorView).deleteCardFromPinWall(participantNeedsImprovementCard);
		verify(organizatorView).setVoteCountOwn(participantNeedsImprovementCard, 1);
		verify(organizatorView).setVoteCountTotal(participantNeedsImprovementCard, 2);
		verify(participantView).deleteCardFromPinWall(participantNeedsImprovementCard);
		verifyNoMoreInteractions(organizatorView);
		verifyNoMoreInteractions(participantView);

		participantMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_EDIT))
				.entries(EntityMessageAdapter.toMessageEntries(organizatorWentWellCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		verify(participantView).createCardOnUserDesk(organizatorWentWellCard);
		verify(participantView).deleteCardFromPinWall(organizatorWentWellCard);
		verify(organizatorView).deleteCardFromPinWall(organizatorWentWellCard);
		verifyNoMoreInteractions(organizatorView);
		verifyNoMoreInteractions(participantView);
	}

	@Test
	public void addVote() {
		postCard();
		reset(organizatorView);
		reset(participantView);
		
		organizatorMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_INCREMENT))
				.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		verify(organizatorView).setVoteCountOwn(participantNeedsImprovementCard, 1);
		verify(organizatorView).setVoteCountTotal(participantNeedsImprovementCard, 1);
		verify(participantView).setVoteCountOwn(participantNeedsImprovementCard, 0);
		verify(participantView).setVoteCountTotal(participantNeedsImprovementCard, 1);
		verifyNoMoreInteractions(organizatorView);
		verifyNoMoreInteractions(participantView);
		reset(organizatorView);
		reset(participantView);

		participantMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_INCREMENT))
				.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		verify(organizatorView).setVoteCountOwn(participantNeedsImprovementCard, 1);
		verify(organizatorView).setVoteCountTotal(participantNeedsImprovementCard, 2);
		verify(participantView).setVoteCountOwn(participantNeedsImprovementCard, 1);
		verify(participantView).setVoteCountTotal(participantNeedsImprovementCard, 2);
		verifyNoMoreInteractions(organizatorView);
		verifyNoMoreInteractions(participantView);
		reset(organizatorView);
		reset(participantView);

		// No voting possible for WentWell cards
		participantMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_INCREMENT))
				.entries(EntityMessageAdapter.toMessageEntries(organizatorWentWellCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		verifyNoMoreInteractions(organizatorView);
		verifyNoMoreInteractions(participantView);
	}

	@Test
	public void addMoreVotesThanAllowed() {
		postCard();
		organizatorMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_INCREMENT))
				.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
		organizatorMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_INCREMENT))
				.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
		participantMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_INCREMENT))
				.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		reset(organizatorView);
		reset(participantView);

		organizatorMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_INCREMENT))
				.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		verify(organizatorView).setVoteCountOwn(participantNeedsImprovementCard, 3);
		verify(organizatorView).setVoteCountTotal(participantNeedsImprovementCard, 4);
		verify(participantView).setVoteCountOwn(participantNeedsImprovementCard, 1);
		verify(participantView).setVoteCountTotal(participantNeedsImprovementCard, 4);
		verifyNoMoreInteractions(organizatorView);
		verifyNoMoreInteractions(participantView);

		// No more that 3 votes can be added from one UserDesk
		organizatorMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_INCREMENT))
				.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		verifyNoMoreInteractions(organizatorView);
		verifyNoMoreInteractions(participantView);
		reset(organizatorView);
		reset(participantView);

		// While another UserDesk can add a vote
		participantMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_INCREMENT))
				.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		verify(organizatorView).setVoteCountOwn(participantNeedsImprovementCard, 3);
		verify(organizatorView).setVoteCountTotal(participantNeedsImprovementCard, 5);
		verify(participantView).setVoteCountOwn(participantNeedsImprovementCard, 2);
		verify(participantView).setVoteCountTotal(participantNeedsImprovementCard, 5);
		verifyNoMoreInteractions(organizatorView);
		verifyNoMoreInteractions(participantView);
	}

	@Test
	public void removeVote() {
		addVote();
		reset(organizatorView);
		reset(participantView);
		
		organizatorMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_DECREMENT))
				.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		verify(organizatorView).setVoteCountOwn(participantNeedsImprovementCard, 0);
		verify(organizatorView).setVoteCountTotal(participantNeedsImprovementCard, 1);
		verify(participantView).setVoteCountOwn(participantNeedsImprovementCard, 1);
		verify(participantView).setVoteCountTotal(participantNeedsImprovementCard, 1);
		verifyNoMoreInteractions(organizatorView);
		verifyNoMoreInteractions(participantView);
		reset(organizatorView);
		reset(participantView);

		participantMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_DECREMENT))
				.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		verify(organizatorView).setVoteCountOwn(participantNeedsImprovementCard, 0);
		verify(organizatorView).setVoteCountTotal(participantNeedsImprovementCard, 0);
		verify(participantView).setVoteCountOwn(participantNeedsImprovementCard, 0);
		verify(participantView).setVoteCountTotal(participantNeedsImprovementCard, 0);
		verifyNoMoreInteractions(organizatorView);
		verifyNoMoreInteractions(participantView);
		reset(organizatorView);
		reset(participantView);

		organizatorMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_DECREMENT))
				.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
		participantMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_DECREMENT))
				.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		verifyNoMoreInteractions(organizatorView);
		verifyNoMoreInteractions(participantView);
	}

	@Test
	public void removeWellDoneCardDuringSession() {
		editCard();
		reset(organizatorView);
		reset(participantView);

		// WentWell card is now on participant's desk
		organizatorMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_DELETE))
				.entries(EntityMessageAdapter.toMessageEntries(organizatorWentWellCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		verifyNoMoreInteractions(participantView);
		verifyNoMoreInteractions(organizatorView);

		participantMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_DELETE))
				.entries(EntityMessageAdapter.toMessageEntries(organizatorWentWellCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		verify(participantView).deleteCardFromUserDesk(organizatorWentWellCard);
		verifyNoMoreInteractions(participantView);
		verifyNoMoreInteractions(organizatorView);
	}

	@Test
	public void removeImprovementCardDuringSession() {
		editCard();
		reset(organizatorView);
		reset(participantView);

		// NeedsImprovement card is now on organizator's desk
		participantMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_DELETE))
				.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		verifyNoMoreInteractions(participantView);
		verifyNoMoreInteractions(organizatorView);

		organizatorMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_DELETE))
				.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		verify(organizatorView).deleteCardFromUserDesk(participantNeedsImprovementCard);
		verifyNoMoreInteractions(participantView);
		verifyNoMoreInteractions(organizatorView);
	}

	@Test
	public void removeCardWithVotesByOrganizatorAndAddVoteAgain() {
		// Add all votes to one card
		addMoreVotesThanAllowed();
		reset(organizatorView);
		reset(participantView);
		participantMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_INCREMENT))
				.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		verify(organizatorView).setVoteCountOwn(participantNeedsImprovementCard, 3);
		verify(organizatorView).setVoteCountTotal(participantNeedsImprovementCard, 6);
		verify(participantView).setVoteCountOwn(participantNeedsImprovementCard, 3);
		verify(participantView).setVoteCountTotal(participantNeedsImprovementCard, 6);
		verifyNoMoreInteractions(organizatorView);
		verifyNoMoreInteractions(participantView);
		reset(organizatorView);
		reset(participantView);

		// And yet another improvement cards
		organizatorMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_CREATE_NEEDS_IMPROVEMENT)).build());
		final CardDeepWithoutIdMatcher cardMatcher = new CardDeepWithoutIdMatcher(new Card.Builder().type(Type.NeedsImprovement).build());
		verify(organizatorView).createCardOnUserDesk(argThat(cardMatcher));
		verifyNoMoreInteractions(organizatorView);
		final Card needsImprovementCard = cardMatcher.getLastComparedCard();
		organizatorMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_POSTIT))
				.entries(EntityMessageAdapter.toMessageEntries(needsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		reset(organizatorView);
		reset(participantView);
		
		// Verify that no more votes can be added
		organizatorMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_INCREMENT))
				.entries(EntityMessageAdapter.toMessageEntries(needsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		participantMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_INCREMENT))
				.entries(EntityMessageAdapter.toMessageEntries(needsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		verifyNoMoreInteractions(organizatorView);
		verifyNoMoreInteractions(participantView);
		reset(organizatorView);
		reset(participantView);

		// Now remove the card with votes
		organizatorMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_EDIT))
				.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		organizatorMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_DELETE))
				.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		reset(organizatorView);
		reset(participantView);

		// Verify that votes can be added now
		organizatorMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_INCREMENT))
				.entries(EntityMessageAdapter.toMessageEntries(needsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		verify(organizatorView).setVoteCountOwn(needsImprovementCard, 1);
		verify(organizatorView).setVoteCountTotal(needsImprovementCard, 1);
		verify(participantView).setVoteCountOwn(needsImprovementCard, 0);
		verify(participantView).setVoteCountTotal(needsImprovementCard, 1);
		verifyNoMoreInteractions(organizatorView);
		verifyNoMoreInteractions(participantView);
		reset(organizatorView);
		reset(participantView);

		participantMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_INCREMENT))
				.entries(EntityMessageAdapter.toMessageEntries(needsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		verify(organizatorView).setVoteCountOwn(needsImprovementCard, 1);
		verify(organizatorView).setVoteCountTotal(needsImprovementCard, 2);
		verify(participantView).setVoteCountOwn(needsImprovementCard, 1);
		verify(participantView).setVoteCountTotal(needsImprovementCard, 2);
		verifyNoMoreInteractions(organizatorView);
		verifyNoMoreInteractions(participantView);
	}


	@Test
	public void removeCardWithVotesByParticipantAndAddVoteAgain() {
		// Add all votes to one card
		addMoreVotesThanAllowed();
		reset(organizatorView);
		reset(participantView);
		participantMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_INCREMENT))
				.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		verify(organizatorView).setVoteCountOwn(participantNeedsImprovementCard, 3);
		verify(organizatorView).setVoteCountTotal(participantNeedsImprovementCard, 6);
		verify(participantView).setVoteCountOwn(participantNeedsImprovementCard, 3);
		verify(participantView).setVoteCountTotal(participantNeedsImprovementCard, 6);
		verifyNoMoreInteractions(organizatorView);
		verifyNoMoreInteractions(participantView);
		reset(organizatorView);
		reset(participantView);

		// And yet another improvement cards
		organizatorMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_CREATE_NEEDS_IMPROVEMENT)).build());
		final CardDeepWithoutIdMatcher cardMatcher = new CardDeepWithoutIdMatcher(new Card.Builder().type(Type.NeedsImprovement).build());
		verify(organizatorView).createCardOnUserDesk(argThat(cardMatcher));
		verifyNoMoreInteractions(organizatorView);
		final Card needsImprovementCard = cardMatcher.getLastComparedCard();
		organizatorMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_POSTIT))
				.entries(EntityMessageAdapter.toMessageEntries(needsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		reset(organizatorView);
		reset(participantView);
		
		// Verify that no more votes can be added
		organizatorMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_INCREMENT))
				.entries(EntityMessageAdapter.toMessageEntries(needsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		participantMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_INCREMENT))
				.entries(EntityMessageAdapter.toMessageEntries(needsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		verifyNoMoreInteractions(organizatorView);
		verifyNoMoreInteractions(participantView);
		reset(organizatorView);
		reset(participantView);

		// Now remove the card with votes
		participantMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_EDIT))
				.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		participantMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_DELETE))
				.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		reset(organizatorView);
		reset(participantView);

		// Verify that votes can be added now
		organizatorMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_INCREMENT))
				.entries(EntityMessageAdapter.toMessageEntries(needsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		verify(organizatorView).setVoteCountOwn(needsImprovementCard, 1);
		verify(organizatorView).setVoteCountTotal(needsImprovementCard, 1);
		verify(participantView).setVoteCountOwn(needsImprovementCard, 0);
		verify(participantView).setVoteCountTotal(needsImprovementCard, 1);
		verifyNoMoreInteractions(organizatorView);
		verifyNoMoreInteractions(participantView);
		reset(organizatorView);
		reset(participantView);

		participantMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_INCREMENT))
				.entries(EntityMessageAdapter.toMessageEntries(needsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		verify(organizatorView).setVoteCountOwn(needsImprovementCard, 1);
		verify(organizatorView).setVoteCountTotal(needsImprovementCard, 2);
		verify(participantView).setVoteCountOwn(needsImprovementCard, 1);
		verify(participantView).setVoteCountTotal(needsImprovementCard, 2);
		verifyNoMoreInteractions(organizatorView);
		verifyNoMoreInteractions(participantView);
	}

	//TODO: tests
	// - modify card text
	// - add more participants
}

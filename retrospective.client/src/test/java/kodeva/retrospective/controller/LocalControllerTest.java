package kodeva.retrospective.controller;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;

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
import org.mockito.Matchers;

public class LocalControllerTest {
	private static final long WAIT_MILLIS = 250L;
	
	/**
	 * Retrospective participant.
	 */
	private static final class Participant {
		private MessageBroker messageBroker;
		private View view;
	}
	
	private Participant organizator, participant;
	private Card organizatorWentWellCard, participantNeedsImprovementCard;

	@After
	public void terminateSession() {
		if (organizator != null) {
			organizator.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
					.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_SESSION_TERMINATE)).build());
		}
	}
	
	@After
	public void disconnectFromSession() {
		if (participant != null) {
			participant.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
					.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_SESSION_DISCONNECT)).build());
		}
	}

	/**
	 * @return
	 *  mocked retrospective session participant
	 */
	private static Participant createMockedParticipant() {
		final Participant participant = new Participant();
		participant.messageBroker = new MessageBroker();
		participant.view = mock(View.class);
		new LocalController(participant.messageBroker, participant.view);
		verify(participant.view).createUserDesk((UserDesk) any());
		verifyNoMoreInteractions(participant.view);
		return participant;
	}
	
	@Test
	public void addNewWellDoneCard() {
		organizator = createMockedParticipant();
		reset(organizator.view);
		
		organizator.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_CREATE_WENT_WELL)).build());
		final CardDeepWithoutIdMatcher cardMatcher = new CardDeepWithoutIdMatcher(new Card.Builder().type(Type.WentWell).build());
		verify(organizator.view).createCardOnUserDesk(argThat(cardMatcher));
		verifyNoMoreInteractions(organizator.view);
		organizatorWentWellCard = cardMatcher.getLastComparedCard();
	}

	@Test
	public void addNewImprovementCard() {
		participant = createMockedParticipant();
		reset(participant.view);

		participant.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_CREATE_NEEDS_IMPROVEMENT)).build());
		final CardDeepWithoutIdMatcher cardMatcher = new CardDeepWithoutIdMatcher(new Card.Builder().type(Type.NeedsImprovement).build());
		verify(participant.view).createCardOnUserDesk(argThat(cardMatcher));
		verifyNoMoreInteractions(participant.view);
		participantNeedsImprovementCard = cardMatcher.getLastComparedCard();
	}

	@Test
	public void removeWellDoneCard() {
		addNewWellDoneCard();
		reset(organizator.view);
		
		organizator.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_DELETE))
				.entries(EntityMessageAdapter.toMessageEntries(organizatorWentWellCard)).build());
		verify(organizator.view).deleteCardFromUserDesk(organizatorWentWellCard);
		verifyNoMoreInteractions(organizator.view);
	}

	@Test
	public void removeImprovementCard() {
		addNewImprovementCard();
		reset(participant.view);

		participant.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_DELETE))
				.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
		verify(participant.view).deleteCardFromUserDesk(participantNeedsImprovementCard);
		verifyNoMoreInteractions(participant.view);
	}

	@Test
	public void updateFrontSideTextOnWellDoneCard() {
		addNewWellDoneCard();
		reset(organizator.view);

		organizator.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_UPDATE_FRONT_SIDE_TEXT))
				.entries(EntityMessageAdapter.toMessageEntries(organizatorWentWellCard))
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.UPDATED_VALUE, "Updated text")).build());
		final CardDeepWithoutIdMatcher cardMatcher = new CardDeepWithoutIdMatcher(new Card.Builder().type(Type.WentWell).frontSideText("Updated text").build());
		verify(organizator.view).updateCardOnUserDesk(argThat(cardMatcher));
		verifyNoMoreInteractions(organizator.view);
	}

	@Test
	public void updateFrontSideTextOnImprovementCard() {
		addNewImprovementCard();
		reset(participant.view);

		participant.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_UPDATE_FRONT_SIDE_TEXT))
				.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard))
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.UPDATED_VALUE, "Updated text")).build());
		final CardDeepWithoutIdMatcher cardMatcher = new CardDeepWithoutIdMatcher(new Card.Builder().type(Type.NeedsImprovement).frontSideText("Updated text").build());
		verify(participant.view).updateCardOnUserDesk(argThat(cardMatcher));
		verifyNoMoreInteractions(participant.view);
	}
	
	@Test
	public void startSession() {
		organizator = createMockedParticipant();
		organizator.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_SESSION_START)).build());
		verify(organizator.view).createPinWall();
		verifyNoMoreInteractions(organizator.view);
		
		// Verify that session can be created just once
		organizator.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_SESSION_START)).build());
		verifyNoMoreInteractions(organizator.view);
	}
	
	@Test
	public void connectToSession() {
		participant = createMockedParticipant();
		participant.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_SESSION_CONNECT)).build());
		verify(participant.view).showError(Matchers.anyString());
		verifyNoMoreInteractions(participant.view);
		
		startSession();
		participant.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_SESSION_CONNECT)).build());
		verify(participant.view).createPinWall();
		verifyNoMoreInteractions(participant.view);
		verifyNoMoreInteractions(organizator.view);

		participant.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_SESSION_CONNECT)).build());
		verifyNoMoreInteractions(participant.view);
		verifyNoMoreInteractions(organizator.view);
	}

	private void startRetrospectiveSessionAndConnectClients() {
		reset(organizator.view);
		organizator.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_SESSION_START)).build());
		verify(organizator.view).createPinWall();
		verifyNoMoreInteractions(organizator.view);

		reset(participant.view);
		participant.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_SESSION_CONNECT)).build());
		verify(participant.view).createPinWall();
		verifyNoMoreInteractions(participant.view);
	}
	
	@Test
	public void postCard() {
		addNewWellDoneCard();
		addNewImprovementCard();
		startRetrospectiveSessionAndConnectClients();
		reset(organizator.view);
		reset(participant.view);
		
		organizator.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_POSTIT))
				.entries(EntityMessageAdapter.toMessageEntries(organizatorWentWellCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		verify(organizator.view).deleteCardFromUserDesk(organizatorWentWellCard);
		verify(organizator.view).createCardOnPinWall(organizatorWentWellCard);
		verify(participant.view).createCardOnPinWall(organizatorWentWellCard);
		verifyNoMoreInteractions(organizator.view);
		verifyNoMoreInteractions(participant.view);

		reset(organizator.view);
		reset(participant.view);
		
		participant.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_POSTIT))
				.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		verify(participant.view).deleteCardFromUserDesk(participantNeedsImprovementCard);
		verify(participant.view).createCardOnPinWall(participantNeedsImprovementCard);
		verify(participant.view).setVoteCountOwn(participantNeedsImprovementCard, 0);
		verify(participant.view).setVoteCountTotal(participantNeedsImprovementCard, 0);
		verify(organizator.view).createCardOnPinWall(participantNeedsImprovementCard);
		verify(organizator.view).setVoteCountOwn(participantNeedsImprovementCard, 0);
		verify(organizator.view).setVoteCountTotal(participantNeedsImprovementCard, 0);
		verifyNoMoreInteractions(organizator.view);
		verifyNoMoreInteractions(participant.view);
	}
	
	@Test
	public void editCard() {
		addVote();
		reset(organizator.view);
		reset(participant.view);

		organizator.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_EDIT))
				.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		verify(organizator.view).createCardOnUserDesk(participantNeedsImprovementCard);
		verify(organizator.view).deleteCardFromPinWall(participantNeedsImprovementCard);
		verify(organizator.view).setVoteCountOwn(participantNeedsImprovementCard, 1);
		verify(organizator.view).setVoteCountTotal(participantNeedsImprovementCard, 2);
		verify(participant.view).deleteCardFromPinWall(participantNeedsImprovementCard);
		verifyNoMoreInteractions(organizator.view);
		verifyNoMoreInteractions(participant.view);

		participant.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_EDIT))
				.entries(EntityMessageAdapter.toMessageEntries(organizatorWentWellCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		verify(participant.view).createCardOnUserDesk(organizatorWentWellCard);
		verify(participant.view).deleteCardFromPinWall(organizatorWentWellCard);
		verify(organizator.view).deleteCardFromPinWall(organizatorWentWellCard);
		verifyNoMoreInteractions(organizator.view);
		verifyNoMoreInteractions(participant.view);
	}

	@Test
	public void addVote() {
		postCard();
		reset(organizator.view);
		reset(participant.view);
		
		organizator.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_INCREMENT))
				.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		verify(organizator.view).setVoteCountOwn(participantNeedsImprovementCard, 1);
		verify(organizator.view).setVoteCountTotal(participantNeedsImprovementCard, 1);
		verify(participant.view).setVoteCountOwn(participantNeedsImprovementCard, 0);
		verify(participant.view).setVoteCountTotal(participantNeedsImprovementCard, 1);
		verifyNoMoreInteractions(organizator.view);
		verifyNoMoreInteractions(participant.view);
		reset(organizator.view);
		reset(participant.view);

		participant.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_INCREMENT))
				.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		verify(organizator.view).setVoteCountOwn(participantNeedsImprovementCard, 1);
		verify(organizator.view).setVoteCountTotal(participantNeedsImprovementCard, 2);
		verify(participant.view).setVoteCountOwn(participantNeedsImprovementCard, 1);
		verify(participant.view).setVoteCountTotal(participantNeedsImprovementCard, 2);
		verifyNoMoreInteractions(organizator.view);
		verifyNoMoreInteractions(participant.view);
		reset(organizator.view);
		reset(participant.view);

		// No voting possible for WentWell cards
		participant.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_INCREMENT))
				.entries(EntityMessageAdapter.toMessageEntries(organizatorWentWellCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		verifyNoMoreInteractions(organizator.view);
		verifyNoMoreInteractions(participant.view);
	}

	@Test
	public void addMoreVotesThanAllowed() {
		postCard();
		organizator.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_INCREMENT))
				.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
		organizator.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_INCREMENT))
				.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
		participant.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_INCREMENT))
				.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		reset(organizator.view);
		reset(participant.view);

		organizator.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_INCREMENT))
				.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		verify(organizator.view).setVoteCountOwn(participantNeedsImprovementCard, 3);
		verify(organizator.view).setVoteCountTotal(participantNeedsImprovementCard, 4);
		verify(participant.view).setVoteCountOwn(participantNeedsImprovementCard, 1);
		verify(participant.view).setVoteCountTotal(participantNeedsImprovementCard, 4);
		verifyNoMoreInteractions(organizator.view);
		verifyNoMoreInteractions(participant.view);

		// No more that 3 votes can be added from one UserDesk
		organizator.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_INCREMENT))
				.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		verifyNoMoreInteractions(organizator.view);
		verifyNoMoreInteractions(participant.view);
		reset(organizator.view);
		reset(participant.view);

		// While another UserDesk can add a vote
		participant.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_INCREMENT))
				.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		verify(organizator.view).setVoteCountOwn(participantNeedsImprovementCard, 3);
		verify(organizator.view).setVoteCountTotal(participantNeedsImprovementCard, 5);
		verify(participant.view).setVoteCountOwn(participantNeedsImprovementCard, 2);
		verify(participant.view).setVoteCountTotal(participantNeedsImprovementCard, 5);
		verifyNoMoreInteractions(organizator.view);
		verifyNoMoreInteractions(participant.view);
	}

	@Test
	public void removeVote() {
		addVote();
		reset(organizator.view);
		reset(participant.view);
		
		organizator.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_DECREMENT))
				.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		verify(organizator.view).setVoteCountOwn(participantNeedsImprovementCard, 0);
		verify(organizator.view).setVoteCountTotal(participantNeedsImprovementCard, 1);
		verify(participant.view).setVoteCountOwn(participantNeedsImprovementCard, 1);
		verify(participant.view).setVoteCountTotal(participantNeedsImprovementCard, 1);
		verifyNoMoreInteractions(organizator.view);
		verifyNoMoreInteractions(participant.view);
		reset(organizator.view);
		reset(participant.view);

		participant.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_DECREMENT))
				.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		verify(organizator.view).setVoteCountOwn(participantNeedsImprovementCard, 0);
		verify(organizator.view).setVoteCountTotal(participantNeedsImprovementCard, 0);
		verify(participant.view).setVoteCountOwn(participantNeedsImprovementCard, 0);
		verify(participant.view).setVoteCountTotal(participantNeedsImprovementCard, 0);
		verifyNoMoreInteractions(organizator.view);
		verifyNoMoreInteractions(participant.view);
		reset(organizator.view);
		reset(participant.view);

		organizator.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_DECREMENT))
				.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
		participant.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_DECREMENT))
				.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		verifyNoMoreInteractions(organizator.view);
		verifyNoMoreInteractions(participant.view);
	}

	@Test
	public void removeWellDoneCardDuringSession() {
		editCard();
		reset(organizator.view);
		reset(participant.view);

		// WentWell card is now on participant's desk
		organizator.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_DELETE))
				.entries(EntityMessageAdapter.toMessageEntries(organizatorWentWellCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		verifyNoMoreInteractions(participant.view);
		verifyNoMoreInteractions(organizator.view);

		participant.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_DELETE))
				.entries(EntityMessageAdapter.toMessageEntries(organizatorWentWellCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		verify(participant.view).deleteCardFromUserDesk(organizatorWentWellCard);
		verifyNoMoreInteractions(participant.view);
		verifyNoMoreInteractions(organizator.view);
	}

	@Test
	public void removeImprovementCardDuringSession() {
		editCard();
		reset(organizator.view);
		reset(participant.view);

		// NeedsImprovement card is now on organizator's desk
		participant.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_DELETE))
				.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		verifyNoMoreInteractions(participant.view);
		verifyNoMoreInteractions(organizator.view);

		organizator.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_DELETE))
				.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		verify(organizator.view).deleteCardFromUserDesk(participantNeedsImprovementCard);
		verifyNoMoreInteractions(participant.view);
		verifyNoMoreInteractions(organizator.view);
	}

	@Test
	public void removeCardWithVotesByOrganizatorAndAddVoteAgain() {
		// Add all votes to one card
		addMoreVotesThanAllowed();
		reset(organizator.view);
		reset(participant.view);
		participant.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_INCREMENT))
				.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		verify(organizator.view).setVoteCountOwn(participantNeedsImprovementCard, 3);
		verify(organizator.view).setVoteCountTotal(participantNeedsImprovementCard, 6);
		verify(participant.view).setVoteCountOwn(participantNeedsImprovementCard, 3);
		verify(participant.view).setVoteCountTotal(participantNeedsImprovementCard, 6);
		verifyNoMoreInteractions(organizator.view);
		verifyNoMoreInteractions(participant.view);
		reset(organizator.view);
		reset(participant.view);

		// And yet another improvement cards
		organizator.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_CREATE_NEEDS_IMPROVEMENT)).build());
		final CardDeepWithoutIdMatcher cardMatcher = new CardDeepWithoutIdMatcher(new Card.Builder().type(Type.NeedsImprovement).build());
		verify(organizator.view).createCardOnUserDesk(argThat(cardMatcher));
		verifyNoMoreInteractions(organizator.view);
		final Card needsImprovementCard = cardMatcher.getLastComparedCard();
		organizator.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_POSTIT))
				.entries(EntityMessageAdapter.toMessageEntries(needsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		reset(organizator.view);
		reset(participant.view);
		
		// Verify that no more votes can be added
		organizator.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_INCREMENT))
				.entries(EntityMessageAdapter.toMessageEntries(needsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		participant.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_INCREMENT))
				.entries(EntityMessageAdapter.toMessageEntries(needsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		verifyNoMoreInteractions(organizator.view);
		verifyNoMoreInteractions(participant.view);
		reset(organizator.view);
		reset(participant.view);

		// Now remove the card with votes
		organizator.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_EDIT))
				.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		organizator.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_DELETE))
				.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		reset(organizator.view);
		reset(participant.view);

		// Verify that votes can be added now
		organizator.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_INCREMENT))
				.entries(EntityMessageAdapter.toMessageEntries(needsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		verify(organizator.view).setVoteCountOwn(needsImprovementCard, 1);
		verify(organizator.view).setVoteCountTotal(needsImprovementCard, 1);
		verify(participant.view).setVoteCountOwn(needsImprovementCard, 0);
		verify(participant.view).setVoteCountTotal(needsImprovementCard, 1);
		verifyNoMoreInteractions(organizator.view);
		verifyNoMoreInteractions(participant.view);
		reset(organizator.view);
		reset(participant.view);

		participant.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_INCREMENT))
				.entries(EntityMessageAdapter.toMessageEntries(needsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		verify(organizator.view).setVoteCountOwn(needsImprovementCard, 1);
		verify(organizator.view).setVoteCountTotal(needsImprovementCard, 2);
		verify(participant.view).setVoteCountOwn(needsImprovementCard, 1);
		verify(participant.view).setVoteCountTotal(needsImprovementCard, 2);
		verifyNoMoreInteractions(organizator.view);
		verifyNoMoreInteractions(participant.view);
	}


	@Test
	public void removeCardWithVotesByParticipantAndAddVoteAgain() {
		// Add all votes to one card
		addMoreVotesThanAllowed();
		reset(organizator.view);
		reset(participant.view);
		participant.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_INCREMENT))
				.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		verify(organizator.view).setVoteCountOwn(participantNeedsImprovementCard, 3);
		verify(organizator.view).setVoteCountTotal(participantNeedsImprovementCard, 6);
		verify(participant.view).setVoteCountOwn(participantNeedsImprovementCard, 3);
		verify(participant.view).setVoteCountTotal(participantNeedsImprovementCard, 6);
		verifyNoMoreInteractions(organizator.view);
		verifyNoMoreInteractions(participant.view);
		reset(organizator.view);
		reset(participant.view);

		// And yet another improvement cards
		organizator.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_CREATE_NEEDS_IMPROVEMENT)).build());
		final CardDeepWithoutIdMatcher cardMatcher = new CardDeepWithoutIdMatcher(new Card.Builder().type(Type.NeedsImprovement).build());
		verify(organizator.view).createCardOnUserDesk(argThat(cardMatcher));
		verifyNoMoreInteractions(organizator.view);
		final Card needsImprovementCard = cardMatcher.getLastComparedCard();
		organizator.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_POSTIT))
				.entries(EntityMessageAdapter.toMessageEntries(needsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		reset(organizator.view);
		reset(participant.view);
		
		// Verify that no more votes can be added
		organizator.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_INCREMENT))
				.entries(EntityMessageAdapter.toMessageEntries(needsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		participant.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_INCREMENT))
				.entries(EntityMessageAdapter.toMessageEntries(needsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		verifyNoMoreInteractions(organizator.view);
		verifyNoMoreInteractions(participant.view);
		reset(organizator.view);
		reset(participant.view);

		// Now remove the card with votes
		participant.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_EDIT))
				.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		participant.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_DELETE))
				.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		reset(organizator.view);
		reset(participant.view);

		// Verify that votes can be added now
		organizator.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_INCREMENT))
				.entries(EntityMessageAdapter.toMessageEntries(needsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		verify(organizator.view).setVoteCountOwn(needsImprovementCard, 1);
		verify(organizator.view).setVoteCountTotal(needsImprovementCard, 1);
		verify(participant.view).setVoteCountOwn(needsImprovementCard, 0);
		verify(participant.view).setVoteCountTotal(needsImprovementCard, 1);
		verifyNoMoreInteractions(organizator.view);
		verifyNoMoreInteractions(participant.view);
		reset(organizator.view);
		reset(participant.view);

		participant.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_INCREMENT))
				.entries(EntityMessageAdapter.toMessageEntries(needsImprovementCard)).build());
		try {
			Thread.sleep(WAIT_MILLIS);
		} catch (InterruptedException e) {
		}
		verify(organizator.view).setVoteCountOwn(needsImprovementCard, 1);
		verify(organizator.view).setVoteCountTotal(needsImprovementCard, 2);
		verify(participant.view).setVoteCountOwn(needsImprovementCard, 1);
		verify(participant.view).setVoteCountTotal(needsImprovementCard, 2);
		verifyNoMoreInteractions(organizator.view);
		verifyNoMoreInteractions(participant.view);
	}

	@Test
	public void completeWalkthroughWithMultipleObservers() {
		addNewWellDoneCard();
		addNewImprovementCard();
		startRetrospectiveSessionAndConnectClients();
		final Collection<Participant> observers = new ArrayList<>();
		for (int i = 1; i <= 11; i++) {
			final Participant observer = createMockedParticipant();
			observers.add(observer);
			observer.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
					.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_SESSION_CONNECT)).build());
			reset(observer.view);
		}

		try {
			// Posting cards
			organizator.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
					.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_POSTIT))
					.entries(EntityMessageAdapter.toMessageEntries(organizatorWentWellCard)).build());
			try {
				Thread.sleep(WAIT_MILLIS);
			} catch (InterruptedException e) {
			}
			for (Participant observer : observers) {
				verify(observer.view).createCardOnPinWall(organizatorWentWellCard);
				verifyNoMoreInteractions(observer.view);
				reset(observer.view);
			}
			
			participant.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
					.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_POSTIT))
					.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
			try {
				Thread.sleep(WAIT_MILLIS);
			} catch (InterruptedException e) {
			}
			for (Participant observer : observers) {
				verify(observer.view).createCardOnPinWall(participantNeedsImprovementCard);
				verify(observer.view).setVoteCountOwn(participantNeedsImprovementCard, 0);
				verify(observer.view).setVoteCountTotal(participantNeedsImprovementCard, 0);
				verifyNoMoreInteractions(observer.view);
				reset(observer.view);
			}

			// Voting for cards
			organizator.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
					.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_INCREMENT))
					.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
			try {
				Thread.sleep(WAIT_MILLIS);
			} catch (InterruptedException e) {
			}
			for (Participant observer : observers) {
				verify(observer.view).setVoteCountOwn(participantNeedsImprovementCard, 0);
				verify(observer.view).setVoteCountTotal(participantNeedsImprovementCard, 1);
				verifyNoMoreInteractions(observer.view);
				reset(observer.view);
			}

			participant.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
					.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_INCREMENT))
					.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
			try {
				Thread.sleep(WAIT_MILLIS);
			} catch (InterruptedException e) {
			}
			for (Participant observer : observers) {
				verify(observer.view).setVoteCountOwn(participantNeedsImprovementCard, 0);
				verify(observer.view).setVoteCountTotal(participantNeedsImprovementCard, 2);
				verifyNoMoreInteractions(observer.view);
				reset(observer.view);
			}
			
			// Remove vote
			organizator.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
					.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_DECREMENT))
					.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
			try {
				Thread.sleep(WAIT_MILLIS);
			} catch (InterruptedException e) {
			}
			for (Participant observer : observers) {
				verify(observer.view).setVoteCountOwn(participantNeedsImprovementCard, 0);
				verify(observer.view).setVoteCountTotal(participantNeedsImprovementCard, 1);
				verifyNoMoreInteractions(observer.view);
				reset(observer.view);
			}

			participant.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
					.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_DECREMENT))
					.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
			try {
				Thread.sleep(WAIT_MILLIS);
			} catch (InterruptedException e) {
			}
			for (Participant observer : observers) {
				verify(observer.view).setVoteCountOwn(participantNeedsImprovementCard, 0);
				verify(observer.view).setVoteCountTotal(participantNeedsImprovementCard, 0);
				verifyNoMoreInteractions(observer.view);
				reset(observer.view);
			}
			
			// Edit card
			organizator.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
					.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_EDIT))
					.entries(EntityMessageAdapter.toMessageEntries(participantNeedsImprovementCard)).build());
			try {
				Thread.sleep(WAIT_MILLIS);
			} catch (InterruptedException e) {
			}
			for (Participant observer : observers) {
				verify(observer.view).deleteCardFromPinWall(participantNeedsImprovementCard);
				verifyNoMoreInteractions(observer.view);
				reset(observer.view);
			}

			participant.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
					.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_EDIT))
					.entries(EntityMessageAdapter.toMessageEntries(organizatorWentWellCard)).build());
			try {
				Thread.sleep(WAIT_MILLIS);
			} catch (InterruptedException e) {
			}
			for (Participant observer : observers) {
				verify(observer.view).deleteCardFromPinWall(organizatorWentWellCard);
				verifyNoMoreInteractions(observer.view);
				reset(observer.view);
			}
		} finally {
			for (Participant observer : observers) {
				observer.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
						.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_SESSION_DISCONNECT)).build());
			}
			try {
				Thread.sleep(WAIT_MILLIS);
			} catch (InterruptedException e) {
			}
		}
	}

	@Test
	public void receivePinWallCardsAfterConnectionToSession() {
		addVote();
		final Participant observer = createMockedParticipant();
		try {
			observer.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
					.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_SESSION_CONNECT)).build());
			verify(observer.view).createCardOnPinWall(organizatorWentWellCard);
			verify(observer.view).createCardOnPinWall(participantNeedsImprovementCard);
			verify(observer.view).setVoteCountOwn(participantNeedsImprovementCard, 0);
			verify(observer.view).setVoteCountTotal(participantNeedsImprovementCard, 2);
		} finally {
			observer.messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
					.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_SESSION_DISCONNECT)).build());
			try {
				Thread.sleep(WAIT_MILLIS);
			} catch (InterruptedException e) {
			}
		}
	}
}

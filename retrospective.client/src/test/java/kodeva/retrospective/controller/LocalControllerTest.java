package kodeva.retrospective.controller;

import static org.mockito.Mockito.*;

import java.util.AbstractMap;

import kodeva.retrospective.messaging.Message;
import kodeva.retrospective.messaging.MessageBroker;
import kodeva.retrospective.model.EntityMessageAdapter;
import kodeva.retrospective.model.entity.Card;
import kodeva.retrospective.model.entity.Card.Type;
import kodeva.retrospective.view.Constants;
import kodeva.retrospective.view.View;

import org.junit.Before;
import org.junit.Test;

public class LocalControllerTest {
	
	private MessageBroker messageBroker;
	private View view;

	@Before
	public void beforeTest() {
		messageBroker = new MessageBroker();
		view = mock(View.class);
		new LocalController(messageBroker, view);
	}

	@Test
	public void addNewWellDoneCard() {
		messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_CREATE_WENT_WELL)).build());
		verify(view).createCardOnUserDesk(argThat(new CardDeepWithoutIdMatcher(new Card.Builder().type(Type.WentWell).build())));
	}

	@Test
	public void addNewImprovementCard() {
		messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_CREATE_NEEDS_IMPROVEMENT)).build());
		verify(view).createCardOnUserDesk(argThat(new CardDeepWithoutIdMatcher(new Card.Builder().type(Type.NeedsImprovement).build())));
	}

	@Test
	public void removeWellDoneCard() {
		messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_CREATE_WENT_WELL)).build());
		final CardDeepWithoutIdMatcher cardMatcher = new CardDeepWithoutIdMatcher(new Card.Builder().type(Type.WentWell).build());
		verify(view).createCardOnUserDesk(argThat(cardMatcher));

		messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_DELETE))
				.entries(EntityMessageAdapter.toMessageEntries(cardMatcher.getLastComparedCard())).build());
		verify(view).deleteCardFromUserDesk(cardMatcher.getLastComparedCard());
	}

	@Test
	public void removeImprovementCard() {
		messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_CREATE_NEEDS_IMPROVEMENT)).build());
		final CardDeepWithoutIdMatcher cardMatcher = new CardDeepWithoutIdMatcher(new Card.Builder().type(Type.NeedsImprovement).build());
		verify(view).createCardOnUserDesk(argThat(cardMatcher));

		messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_DELETE))
				.entries(EntityMessageAdapter.toMessageEntries(cardMatcher.getLastComparedCard())).build());
		verify(view).deleteCardFromUserDesk(cardMatcher.getLastComparedCard());
	}
	
	@Test
	public void startSession() {
	}
}

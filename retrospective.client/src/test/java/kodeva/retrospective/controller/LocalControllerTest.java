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
	
	private MessageBroker orgMessageBroker, partMessageBroker;
	private View orgView, partView;
	private Card orgWentWellCard, partNeedsImprovementCard;

	@Before
	public void beforeTest() {
		orgMessageBroker = new MessageBroker();
		orgView = mock(View.class);
		new LocalController(orgMessageBroker, orgView);

		partMessageBroker = new MessageBroker();
		partView = mock(View.class);
		new LocalController(partMessageBroker, partView);
	}

	@Test
	public void addNewWellDoneCard() {
		orgMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_CREATE_WENT_WELL)).build());
		final CardDeepWithoutIdMatcher cardMatcher = new CardDeepWithoutIdMatcher(new Card.Builder().type(Type.WentWell).build());
		verify(orgView).createCardOnUserDesk(argThat(cardMatcher));
		orgWentWellCard = cardMatcher.getLastComparedCard();
	}

	@Test
	public void addNewImprovementCard() {
		partMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_CREATE_NEEDS_IMPROVEMENT)).build());
		final CardDeepWithoutIdMatcher cardMatcher = new CardDeepWithoutIdMatcher(new Card.Builder().type(Type.NeedsImprovement).build());
		verify(partView).createCardOnUserDesk(argThat(cardMatcher));
		partNeedsImprovementCard = cardMatcher.getLastComparedCard();
	}

	@Test
	public void removeWellDoneCard() {
		addNewWellDoneCard();
		orgMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_DELETE))
				.entries(EntityMessageAdapter.toMessageEntries(orgWentWellCard)).build());
		verify(orgView).deleteCardFromUserDesk(orgWentWellCard);
	}

	@Test
	public void removeImprovementCard() {
		addNewImprovementCard();
		partMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_DELETE))
				.entries(EntityMessageAdapter.toMessageEntries(partNeedsImprovementCard)).build());
		verify(partView).deleteCardFromUserDesk(partNeedsImprovementCard);
	}
	
	@Test
	public void postWellDoneCard() {
		addNewWellDoneCard();
		orgMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_SESSION_START)).build());
		addNewImprovementCard();
		partMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_SESSION_CONNECT)).build());
		
		orgMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_POSTIT))
				.entries(EntityMessageAdapter.toMessageEntries(orgWentWellCard)).build());
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
		}
		verify(orgView).deleteCardFromUserDesk(orgWentWellCard);
		verify(orgView).createCardOnPinWall(orgWentWellCard);
		verify(partView, never()).deleteCardFromUserDesk((Card) any());
		verify(partView).createCardOnPinWall(orgWentWellCard);
		
		partMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_SESSION_DISCONNECT)).build());
		orgMessageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
				.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_SESSION_TERMINATE)).build());
	}
}

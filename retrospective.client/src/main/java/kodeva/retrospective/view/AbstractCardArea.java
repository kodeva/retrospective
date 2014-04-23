package kodeva.retrospective.view;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javafx.scene.layout.FlowPane;
import kodeva.retrospective.messaging.MessageBroker;

/**
 * Abstract visual container for cards.
 */
public abstract class AbstractCardArea {
	protected final FlowPane cardsUIContainer;
	private final MessageBroker messageBroker;
	private Map<kodeva.retrospective.model.entity.Card, Card> cardUIControls;

	public AbstractCardArea(MessageBroker messageBroker) {
		this.cardsUIContainer = new FlowPane();
		this.messageBroker = messageBroker;
		cardUIControls = new HashMap<>();
		cardUIControls = Collections.synchronizedMap(cardUIControls);
	}

	/**
	 * Creates instance of Card.Builder for creating Card UI control. To be overridden by ancestors.
	 * @param card
	 *  card data
	 * @return
	 *  instance of Card.Builder filled with necessary data
	 */
	public Card.Builder createCardBuilder(kodeva.retrospective.model.entity.Card card) {
		return new Card.Builder().card(card).messageBroker(messageBroker);
	}
	
	/**
	 * Creates a new card in card container.
	 * @param card
	 *  card instance
	 */
	public final void createCard(kodeva.retrospective.model.entity.Card card) {
		Card cardUIControl = createCardBuilder(card).build();
		cardsUIContainer.getChildren().add(cardUIControl.getNode());
		cardUIControls.put(card, cardUIControl);
	}

	/**
	 * Return the card from card container.
	 * @param card
	 *  card instance
	 */
	protected final Card getCard(kodeva.retrospective.model.entity.Card card) {
		return cardUIControls.get(card);  
	}

	/**
	 * Deletes the card from card container.
	 * @param card
	 *  card instance
	 */
	public final void deleteCard(kodeva.retrospective.model.entity.Card card) {
		final Card cardUIControl = cardUIControls.remove(card);  
		if (cardUIControl != null) {
			cardsUIContainer.getChildren().remove(cardUIControl.getNode());
		}
	}

	/**
	 * Updates the card in card container.
	 * @param card
	 *  card instance
	 */
	public final void updateCard(kodeva.retrospective.model.entity.Card card) {
		Card cardUIControl = cardUIControls.get(card);
		cardUIControl.setCard(card);
		cardUIControls.put(card, cardUIControls.get(card));
	}

	/**
	 * Sets count of own votes assigned to the card.
	 * @param card
	 *  card instance
	 * @param votesCount
	 *  count of own votes
	 */
	public final void setVoteCountOwn(kodeva.retrospective.model.entity.Card card, int votesCount) {
		final Card cardUIControl = getCard(card);
		if (cardUIControl != null) {
			cardUIControl.setVotesCountOwn(votesCount);
		}
	}

	/**
	 * Sets total count of votes assigned to the card.
	 * @param card
	 *  card instance
	 * @param votesCount
	 *  count of own votes
	 */
	public final void setVoteCountTotal(kodeva.retrospective.model.entity.Card card, int votesCount) {
		final Card cardUIControl = getCard(card);
		if (cardUIControl != null) {
			cardUIControl.setVotesCountTotal(votesCount);
		}
	}
}

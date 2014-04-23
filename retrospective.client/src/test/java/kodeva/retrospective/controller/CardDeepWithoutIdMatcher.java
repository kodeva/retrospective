package kodeva.retrospective.controller;

import kodeva.retrospective.model.entity.Card;

import org.mockito.ArgumentMatcher;

/**
 * Matcher for card that does not use card id when matching cards.
 */
public final class CardDeepWithoutIdMatcher extends ArgumentMatcher<Card> {
	private Card card, lastAnotherCard;
	
	public CardDeepWithoutIdMatcher(Card card) {
		this.card = card;
	}

	@Override
	public boolean matches(Object item) {
		final Card anotherCard = (Card) item;
		this.lastAnotherCard = anotherCard;
		return (isEqual(card.getType(), anotherCard.getType())
		    &&  isEqual(card.getFrontSideText(), anotherCard.getFrontSideText())
		    &&  isEqual(card.getBackSideText(), anotherCard.getBackSideText()));
	}
	
	/**
	 * @return
	 *  argument of the last matches() operation.
	 */
	public Card getLastComparedCard() {
		return lastAnotherCard;
	}

	private static boolean isEqual(Object object1, Object object2) {
		if (((object1 == null) && (object2 == null))
		||  ((object1 != null) && object1.equals(object2))) {
			return true;
		}
		return false;
	}
}
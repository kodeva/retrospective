package kodeva.retrospective.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PinWall {
	private List<Card> cards = new ArrayList<Card>();
	
	public List<Card> getCards() {
		return Collections.unmodifiableList(cards);
	}

	public int getVotesCount(final UserDesk userDesk) {
		int count = 0;
		for (Card card : getCards()) {
			count += card.getVotesCount(userDesk);
		}
		return count;
	}
}

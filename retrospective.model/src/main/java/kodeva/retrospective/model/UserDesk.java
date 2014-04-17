package kodeva.retrospective.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserDesk {
	private String id;
	private List<Card> cards = new ArrayList<Card>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<Card> getCards() {
		return Collections.unmodifiableList(cards);
	}
}

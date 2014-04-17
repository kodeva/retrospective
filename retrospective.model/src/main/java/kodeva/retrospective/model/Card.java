package kodeva.retrospective.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class Card {
	public enum Type {
		WentWell,
		NeedsImprovement;
	}

	private Type type;
	private String frontSideText, backSideText;
	private List<UserDesk> votes = new ArrayList<>();
	
	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getFrontSideText() {
		return frontSideText;
	}

	public void setFrontSideText(String frontSideText) {
		this.frontSideText = frontSideText;
	}

	public String getBackSideText() {
		return backSideText;
	}

	public void setBackSideText(String backSideText) {
		this.backSideText = backSideText;
	}

	public List<UserDesk> getVotes() {
		return Collections.unmodifiableList(votes);
	}

	public void addVotes(UserDesk ... userDesks) {
		if (userDesks != null) {
			votes.addAll(Arrays.asList(userDesks));
		}
	}
	
	public void removeVotes(UserDesk ... userDesks) {
		if (userDesks != null) {
			votes.removeAll(Arrays.asList(userDesks));
		}
	}
	
	public int getVotesCount(UserDesk userDesk) {
		int count = 0;
		for (UserDesk vote : getVotes()) {
			if (vote.equals(userDesk)) {
				count++;
			}
		}
		return count;
	}
}

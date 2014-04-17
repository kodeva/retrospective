package kodeva.retrospective.controller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import kodeva.retrospective.model.Card;
import kodeva.retrospective.model.Model;
import kodeva.retrospective.model.UserDesk;

public class Controller {
	private static final int MAXIMUM_VOTES_COUNT_PER_USER = 3;
	
	private final Model model;
	private final ModelChangeCallback modelChangeCallback;

	public Controller(Model model, ModelChangeCallback modelChangeCallback) {
		this.model = model;
		this.modelChangeCallback = modelChangeCallback;
	}
	
	public EventHandler<ActionEvent> getCreateCardEventHandler(final Card.Type type) {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				Card card = new Card();
				card.setType(type);
				model.getUserDesk().getCards().add(card);
				modelChangeCallback.onUserDeskChange();
			}
		};
	}
	
	public EventHandler<ActionEvent> getPostitCardEventHandler(final Card card) {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				model.getUserDesk().getCards().remove(card);
				model.getPinWall().getCards().add(card);
				modelChangeCallback.onUserDeskChange();
				modelChangeCallback.onPinWallChange();
			}
		};
	}
	
	public EventHandler<ActionEvent> getEditCardEventHandler(final Card card) {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				model.getPinWall().getCards().remove(card);
				model.getUserDesk().getCards().add(card);
				card.removeVotes(card.getVotes().toArray(new UserDesk[0]));
				modelChangeCallback.onUserDeskChange();
				modelChangeCallback.onPinWallChange();
			}
		};
	}

	public EventHandler<ActionEvent> getRemoveCardEventHandler(final Card card) {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				model.getUserDesk().getCards().remove(card);
				modelChangeCallback.onUserDeskChange();
			}
		};
	}

	public EventHandler<ActionEvent> getIncrementVoteEventHandler(final Card card) {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (model.getPinWall().getVotesCount(model.getUserDesk()) < MAXIMUM_VOTES_COUNT_PER_USER) {
					card.addVotes(model.getUserDesk());
					modelChangeCallback.onPinWallChange();
				}
			}
		};
	}

	public EventHandler<ActionEvent> getDecrementVoteEventHandler(final Card card) {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				card.removeVotes(model.getUserDesk());
				modelChangeCallback.onPinWallChange();
			}
		};
	}
}

package kodeva.retrospective.view;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import kodeva.retrospective.messaging.MessageBroker;
import kodeva.retrospective.model.entity.Card;
import kodeva.retrospective.model.entity.UserDesk;

public class View {
	private EditArea editArea;

	private final VBox feedback;
	private final WentWellArea wentWellArea;
	private final NeedsImprovementArea needsImprovementArea;
	private final MessageBroker messageBroker;

	public View(MessageBroker messageBroker) {
		this.messageBroker = messageBroker;

		feedback = new VBox();
		feedback.setPadding(new Insets(2));
		feedback.setSpacing(3);

		final ButtonMenu menu = new ButtonMenu(messageBroker);
		menu.prefWidthProperty().bind(feedback.widthProperty());
		feedback.getChildren().add(menu.getNode());

		wentWellArea = new WentWellArea(messageBroker);
		feedback.getChildren().add(wentWellArea.getNode());
		wentWellArea.prefWidthProperty().bind(feedback.widthProperty());
		wentWellArea.prefHeightProperty().bind(feedback.heightProperty());

		needsImprovementArea = new NeedsImprovementArea(messageBroker);
		feedback.getChildren().add(needsImprovementArea.getNode());
		needsImprovementArea.prefWidthProperty().bind(feedback.widthProperty());
		needsImprovementArea.prefHeightProperty().bind(feedback.heightProperty());
	}
	
	public Parent getParent() {
		return feedback;
	}

	public DoubleProperty prefWidthProperty() {
		return feedback.prefWidthProperty();
	}
	
	public DoubleProperty prefHeightProperty() {
		return feedback.prefHeightProperty();
	}

	public void createUserDesk(final UserDesk userDesk) {
		editArea = new EditArea(messageBroker, userDesk);
		feedback.getChildren().add(editArea.getNode());
		editArea.prefWidthProperty().bind(feedback.widthProperty());
		editArea.prefHeightProperty().bind(feedback.heightProperty());
	}

	/**
	 * Create a new card on user desk.
	 * @param card
	 *  card instance
	 */
	public void createCardOnUserDesk(final Card card) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				editArea.createCard(card);
			}
		});
	}
	
	/**
	 * Delete the card from user desk.
	 * @param card
	 *  card instance
	 */
	public void deleteCardFromUserDesk(final Card card) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				editArea.deleteCard(card);
			}
		});
	}
	
	/**
	 * Updates the card on user desk.
	 * @param card
	 *  card instance
	 */
	public void updateCardOnUserDesk(final Card card) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				editArea.updateCard(card);
			}
		});
	}

	/**
	 * Create a new card on pin wall.
	 * @param card
	 *  card instance
	 */
	public void createCardOnPinWall(final Card card) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				switch (card.getType()) {
				case WentWell:
					wentWellArea.createCard(card);
					break;
				case NeedsImprovement:
					needsImprovementArea.createCard(card);
					break;
				}
			}
		});
	}

	/**
	 * Delete the card from pin wall.
	 * @param card
	 *  card instance
	 */
	public void deleteCardFromPinWall(final Card card) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				switch (card.getType()) {
				case WentWell:
					wentWellArea.deleteCard(card);
					break;
				case NeedsImprovement:
					needsImprovementArea.deleteCard(card);
					break;
				}
			}
		});
	}
	
	/**
	 * Updates the card on pin wall.
	 * @param card
	 *  card instance
	 */
	public void updateCardOnPinWall(final Card card) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				switch (card.getType()) {
				case WentWell:
					wentWellArea.updateCard(card);
					break;
				case NeedsImprovement:
					needsImprovementArea.updateCard(card);
					break;
				}
			}
		});
	}

	/**
	 * Sets count of own votes assigned to the card.
	 * @param card
	 *  card instance
	 * @param votesCount
	 *  count of own votes
	 */
	public void setVoteCountOwn(final kodeva.retrospective.model.entity.Card card, final int votesCount) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				editArea.setVoteCountOwn(card, votesCount);
				needsImprovementArea.setVoteCountOwn(card, votesCount);
			}
		});
	}

	/**
	 * Sets total count of votes assigned to the card.
	 * @param card
	 *  card instance
	 * @param votesCount
	 *  count of own votes
	 */
	public void setVoteCountTotal(final kodeva.retrospective.model.entity.Card card, final int votesCount) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				editArea.setVoteCountTotal(card, votesCount);
				needsImprovementArea.setVoteCountTotal(card, votesCount);
			}
		});
	}
}
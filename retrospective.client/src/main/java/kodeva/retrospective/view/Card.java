package kodeva.retrospective.view;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import kodeva.retrospective.controller.Controller;
import kodeva.retrospective.model.UserDesk;

public class Card {
	private final VBox cardVisual;
	
	private Card(final kodeva.retrospective.model.Card card, final boolean editable, Controller controller, final UserDesk userDesk) {
		cardVisual = new VBox();
		cardVisual.setPadding(new Insets(2));
		cardVisual.setSpacing(2);

		final HBox cardMenuArea = new HBox();
		cardVisual.getChildren().add(cardMenuArea);

		final Button btnRemove = new Button("Remove");
		final Button btnPostit = new Button("Post It");
		final Button btnEdit = new Button("Edit");
		final TextArea text = new TextArea();

		cardMenuArea.getChildren().add(btnRemove);
		cardMenuArea.getChildren().add(btnPostit);
		cardMenuArea.getChildren().add(btnEdit);
		
		btnRemove.setDisable(! editable);
		btnPostit.setDisable(! editable);
		btnEdit.setDisable(editable);
		
		btnRemove.setOnAction(controller.getRemoveCardEventHandler(card));
		btnPostit.setOnAction(controller.getPostitCardEventHandler(card));
		btnEdit.setOnAction(controller.getEditCardEventHandler(card));

		text.setPrefColumnCount(20);
		text.setPrefRowCount(5);
		text.setWrapText(true);
		text.setText(card.getFrontSideText());
		text.setDisable(! editable);
		text.textProperty().addListener(new ChangeListener<String>() {
			public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
				card.setFrontSideText(arg2);
			}
		});
		cardVisual.getChildren().add(text);

		switch (card.getType()) {
		case WentWell:
			cardVisual.setStyle("-fx-background-color: #00A000; -fx-background-radius: 6;");
			text.setPromptText("What went well...");
			break;
		case NeedsImprovement:
			cardVisual.setStyle("-fx-background-color: #B0B000; -fx-background-radius: 6;");
			final Label ownVotes = new Label(String.format("Votes: Own=%d", card.getVotesCount(userDesk)));
			cardMenuArea.getChildren().add(ownVotes);

			final Button btnAddVote = new Button("+");
			cardMenuArea.getChildren().add(btnAddVote);
			btnAddVote.setOnAction(controller.getIncrementVoteEventHandler(card));
			btnAddVote.setDisable(editable);

			final Button btnRemoveVote = new Button("-");
			cardMenuArea.getChildren().add(btnRemoveVote);
			btnRemoveVote.setOnAction(controller.getDecrementVoteEventHandler(card));
			btnRemoveVote.setDisable(editable);

			final Label totalVotes = new Label(String.format("Total=%d", card.getVotes().size()));
			cardMenuArea.getChildren().add(totalVotes);

			text.setPromptText("What needs to be improved...");
			break;
		}
	}
		
	public Node getNode() {
		return cardVisual;
	}
	
	public static final class Builder {
		private kodeva.retrospective.model.Card card;
		private boolean editable;
		private Controller controller;
		private UserDesk userDesk;
		
		public Builder card(kodeva.retrospective.model.Card card) {
			this.card = card;
			return this;
		}
		
		public Builder editable(boolean editable) {
			this.editable = editable;
			return this;
		}
		
		public Builder controller(Controller controller) {
			this.controller = controller;
			return this;
		}

		public Builder userDesk(UserDesk userDesk) {
			this.userDesk = userDesk;
			return this;
		}

		public Card build() {
			if (card == null) {
				throw new NullPointerException("Card was not specified");
			}
			if (controller == null) {
				throw new NullPointerException("Controller was not specified");
			}
			return new Card(card, editable, controller, userDesk);
		}
	}
}

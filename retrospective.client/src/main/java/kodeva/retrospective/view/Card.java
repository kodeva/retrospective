package kodeva.retrospective.view;

import java.util.AbstractMap;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import kodeva.retrospective.messaging.Message;
import kodeva.retrospective.messaging.MessageBroker;
import kodeva.retrospective.model.EntityMessageAdapter;

public class Card {
	private final VBox cardVisual;
	private kodeva.retrospective.model.entity.Card card;
	private final TextArea frontSideText;
	private final SimpleStringProperty votesCountTotalLabelText, votesCountOwnLabelText;
	
	private Card(final kodeva.retrospective.model.entity.Card card, final boolean editable, final MessageBroker messageBroker) {
		this.card = card;
		
		cardVisual = new VBox();
		cardVisual.setPadding(new Insets(2));
		cardVisual.setSpacing(2);

		final HBox cardMenuArea = new HBox();
		cardVisual.getChildren().add(cardMenuArea);

		final Button btnRemove = new Button("Remove");
		final Button btnPostit = new Button("Post It");
		final Button btnEdit = new Button("Edit");
		frontSideText = new TextArea();

		cardMenuArea.getChildren().add(btnRemove);
		cardMenuArea.getChildren().add(btnPostit);
		cardMenuArea.getChildren().add(btnEdit);
		
		btnRemove.setDisable(! editable);
		btnPostit.setDisable(! editable);
		btnEdit.setDisable(editable);
		
		btnRemove.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
						.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_DELETE))
						.entries(EntityMessageAdapter.toMessageEntries(getCard())).build());
			}
		});
		btnPostit.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
						.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_POSTIT))
						.entries(EntityMessageAdapter.toMessageEntries(getCard())).build());
			}
		});
		btnEdit.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
						.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_EDIT))
						.entries(EntityMessageAdapter.toMessageEntries(getCard())).build());
			}
		});

		frontSideText.setPrefColumnCount(20);
		frontSideText.setPrefRowCount(5);
		frontSideText.setWrapText(true);
		frontSideText.setText(card.getFrontSideText());
		frontSideText.setDisable(! editable);
		frontSideText.textProperty().addListener(new ChangeListener<String>() {
			public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
				messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
						.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_UPDATE_FRONT_SIDE_TEXT))
						.entries(EntityMessageAdapter.toMessageEntries(getCard()))
						.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.UPDATED_VALUE, arg2)).build());
			}
		});
		cardVisual.getChildren().add(frontSideText);

		votesCountOwnLabelText = new SimpleStringProperty();
		votesCountTotalLabelText = new SimpleStringProperty();
		switch (card.getType()) {
		case WentWell:
			cardVisual.setStyle("-fx-background-color: #00A000; -fx-background-radius: 6;");
			frontSideText.setPromptText("What went well...");
			break;

		case NeedsImprovement:
			cardVisual.setStyle("-fx-background-color: #B0B000; -fx-background-radius: 6;");
			final Label votesOwnLabel = new Label();
			cardMenuArea.getChildren().add(votesOwnLabel);
			votesOwnLabel.textProperty().bind(votesCountOwnLabelText);
			setVotesCountOwn(0);

			final Button btnAddVote = new Button("+");
			cardMenuArea.getChildren().add(btnAddVote);
			btnAddVote.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
							.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_INCREMENT))
							.entries(EntityMessageAdapter.toMessageEntries(getCard())).build());
				}
			});
			btnAddVote.setDisable(editable);

			final Button btnRemoveVote = new Button("-");
			cardMenuArea.getChildren().add(btnRemoveVote);
			btnRemoveVote.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
							.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_VOTES_DECREMENT))
							.entries(EntityMessageAdapter.toMessageEntries(getCard())).build());
				}
			});
			btnRemoveVote.setDisable(editable);

			final Label votesLabel = new Label();
			cardMenuArea.getChildren().add(votesLabel);
			votesLabel.textProperty().bind(votesCountTotalLabelText);
			setVotesCountTotal(0);

			frontSideText.setPromptText("What needs to be improved...");
			break;
		}
	}
		
	public Node getNode() {
		return cardVisual;
	}
	
	public kodeva.retrospective.model.entity.Card getCard() {
		return card;
	}
	
	public void setCard(kodeva.retrospective.model.entity.Card card) {
		this.card = card;
		frontSideText.textProperty().set(card.getFrontSideText());
	}
	
	public void setVotesCountOwn(int votesCountOwn) {
		votesCountOwnLabelText.setValue(String.format("Votes: Own=%d", votesCountOwn));
	}
	
	public void setVotesCountTotal(int votesCountTotal) {
		votesCountTotalLabelText.setValue(String.format("Total=%d", votesCountTotal));
	}
	
	public static final class Builder {
		private kodeva.retrospective.model.entity.Card card;
		private boolean editable;
		private MessageBroker messageBroker;
		
		public Builder card(kodeva.retrospective.model.entity.Card card) {
			this.card = card;
			return this;
		}
		
		public Builder editable(boolean editable) {
			this.editable = editable;
			return this;
		}
		
		public Builder messageBroker(MessageBroker messageBroker) {
			this.messageBroker = messageBroker;
			return this;
		}
		
		public Card build() {
			if (card == null) {
				throw new NullPointerException("Card was not specified");
			}
			return new Card(card, editable, messageBroker);
		}
	}
}

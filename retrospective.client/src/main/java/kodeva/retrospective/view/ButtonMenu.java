package kodeva.retrospective.view;

import java.util.AbstractMap;

import javafx.beans.property.DoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import kodeva.retrospective.messaging.Message;
import kodeva.retrospective.messaging.MessageBroker;

public class ButtonMenu {
	private final HBox menuArea;

	public ButtonMenu(final MessageBroker messageBroker) {
		menuArea = new HBox();
		menuArea.setSpacing(3);

		final Button btnSessionConnect = new Button("Connect to retrospective session");
		menuArea.getChildren().add(btnSessionConnect);
		btnSessionConnect.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
						.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_SESSION_CONNECT)).build());
			}
		});

		final Button btnSessionDisconnect = new Button("Disconnect from retrospective session");
		menuArea.getChildren().add(btnSessionDisconnect);
		btnSessionDisconnect.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
						.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_SESSION_DISCONNECT)).build());
			}
		});

		final Button btnSessionStart = new Button("Start retrospective session");
		menuArea.getChildren().add(btnSessionStart);
		btnSessionStart.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
						.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_SESSION_START)).build());
			}
		});

		final Button btnSessionTerminate = new Button("Terminate retrospective session");
		menuArea.getChildren().add(btnSessionTerminate);
		btnSessionTerminate.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
						.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_SESSION_TERMINATE)).build());
			}
		});

		final Button btnNewOkCard = new Button("Add a new 'Well Done' card");
		menuArea.getChildren().add(btnNewOkCard);
		btnNewOkCard.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
						.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_CREATE_WENT_WELL)).build());
			}
		});

		final Button btnNewFixCard = new Button("Add a new 'Improvement' card");
		menuArea.getChildren().add(btnNewFixCard);
		btnNewFixCard.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				messageBroker.sendMessage(new Message.Builder().sender(Constants.Messaging.SENDER)
						.entry(new AbstractMap.SimpleEntry<>(Constants.Messaging.Key.EVENT, Constants.Messaging.Value.KEY_EVENT_CARD_CREATE_NEEDS_IMPROVEMENT)).build());
			}
		});
	}
	
	public Node getNode() {
		return menuArea;
	}
	
	public DoubleProperty prefWidthProperty() {
		return menuArea.prefWidthProperty();
	}
	
	public DoubleProperty prefHeightProperty() {
		return menuArea.prefHeightProperty();
	}
}

package kodeva.retrospective;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import kodeva.retrospective.controller.LocalController;
import kodeva.retrospective.messaging.MessageBroker;
import kodeva.retrospective.view.View;

/**
 * Retrospective client based on JavaFX.
 */
public class UI extends Application {
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(final Stage primaryStage) {
		final MessageBroker messageBroker = new MessageBroker();
		final View view = new View(messageBroker);
		new LocalController(messageBroker, view);

		primaryStage.setTitle("Retrospective Feedback");
		primaryStage.setWidth(800);
		primaryStage.setHeight(600);
		primaryStage.setScene(new Scene(view.getParent()));
		view.prefWidthProperty().bind(primaryStage.widthProperty());
		view.prefHeightProperty().bind(primaryStage.heightProperty());

		primaryStage.show();
	}
}

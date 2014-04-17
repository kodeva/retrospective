package kodeva.retrospective;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import kodeva.retrospective.controller.Controller;
import kodeva.retrospective.model.Model;
import kodeva.retrospective.view.ModelChangeCallbackImpl;
import kodeva.retrospective.view.View;

/**
 * @author Stepan Hrbacek
 * 
 * TODO:
 * - Udedat Model immutable (vracet kopie seznamu karet).
 * - Udelat needitovatelne karty (na PinWall) citelnejsi - kotrast pozadi a pisma.
 * - Pridat metody pro modifikace Modelu (pro snadne navazani externi ukladani).
 * - Implementovat equals() trid modelu zalozene na externim id (Card, Vote, UserDesk, ...).
 * - Vlakno pro aktualizaci lokalni Model instance (PinWall) ze site.
 * - Ukladani UserDesk pri kazde aktualizaci na disk a jeho nacteni pri startu aplikace.
 * - Vytvoreni vysledneho reportu z retrospektivy (PinWall v PDF/PNG).
 * - Udelat externi rozhranni Modelu Observable tak, aby se UI prekreslilo automaticky.
 */
public class UI extends Application {
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(final Stage primaryStage) {
		final Model model = new Model();
		final ModelChangeCallbackImpl modelChangeCallback = new ModelChangeCallbackImpl();
		final Controller controller = new Controller(model, modelChangeCallback);
		final View view = new View(model, controller);
		modelChangeCallback.setView(view);
		view.refresh();

		primaryStage.setTitle("Retrospective Feedback");
		primaryStage.setWidth(800);
		primaryStage.setHeight(600);
		primaryStage.setScene(new Scene(view.getParent()));
		view.prefWidthProperty().bind(primaryStage.widthProperty());
		view.prefHeightProperty().bind(primaryStage.heightProperty());

		primaryStage.show();
	}
}

package kodeva.retrospective.view;

import javafx.beans.property.DoubleProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import kodeva.retrospective.controller.Controller;
import kodeva.retrospective.model.PinWall;
import kodeva.retrospective.model.Card.Type;

public class WentWellArea {
	private final ScrollPane cardsWentWellAreaScrolled;
	private final FlowPane cardsWentWellArea;
	private final PinWall pinWall;
	private final Controller controller;

	public WentWellArea(PinWall pinWall, Controller controller) {
		this.pinWall = pinWall;
		this.controller = controller;
		
		cardsWentWellAreaScrolled = new ScrollPane();
		cardsWentWellArea = new FlowPane();

		cardsWentWellAreaScrolled.setContent(cardsWentWellArea);
		cardsWentWellArea.prefWidthProperty().bind(
				cardsWentWellAreaScrolled.widthProperty());
		cardsWentWellArea.prefHeightProperty().bind(
				cardsWentWellAreaScrolled.heightProperty());
		cardsWentWellArea.setVgap(5);
		cardsWentWellArea.setHgap(5);
		cardsWentWellArea.setStyle("-fx-background-color: #AFFFAF;");
		cardsWentWellArea.setPadding(new Insets(2));
	}

	public Node getNode() {
		return cardsWentWellAreaScrolled;
	}

	public DoubleProperty prefWidthProperty() {
		return cardsWentWellAreaScrolled.prefWidthProperty();
	}

	public DoubleProperty prefHeightProperty() {
		return cardsWentWellAreaScrolled.prefHeightProperty();
	}
	
	public void refresh() {
		cardsWentWellArea.getChildren().clear();
		for (kodeva.retrospective.model.Card card : pinWall.getCards()) {
			if (Type.WentWell == card.getType()) {
				Card cardVisual = new Card.Builder().card(card).editable(false).controller(controller).build();
				cardsWentWellArea.getChildren().add(cardVisual.getNode());
			}
		}
	}
}

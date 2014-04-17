package kodeva.retrospective.view;

import javafx.beans.property.DoubleProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import kodeva.retrospective.controller.Controller;
import kodeva.retrospective.model.Card.Type;
import kodeva.retrospective.model.Model;

public class NeedsImprovementArea {
	private final ScrollPane cardsNeedsImprovementAreaScrolled;
	private final FlowPane cardsNeedsImprovementArea;
	private final Model model;
	private final Controller controller;
	
	public NeedsImprovementArea(Model model, Controller controller) {
		this.model = model;
		this.controller = controller;
		
		cardsNeedsImprovementAreaScrolled = new ScrollPane();
		cardsNeedsImprovementArea = new FlowPane();

		cardsNeedsImprovementAreaScrolled.setContent(cardsNeedsImprovementArea);
		cardsNeedsImprovementArea.prefWidthProperty().bind(
				cardsNeedsImprovementAreaScrolled.widthProperty());
		cardsNeedsImprovementArea.prefHeightProperty().bind(
				cardsNeedsImprovementAreaScrolled.heightProperty());
		cardsNeedsImprovementArea.setVgap(5);
		cardsNeedsImprovementArea.setHgap(5);
		cardsNeedsImprovementArea.setStyle("-fx-background-color: #FFFF5F;");
		cardsNeedsImprovementArea.setPadding(new Insets(2));
	}
	
	public Node getNode() {
		return cardsNeedsImprovementAreaScrolled;
	}

	public DoubleProperty prefWidthProperty() {
		return cardsNeedsImprovementAreaScrolled.prefWidthProperty();
	}

	public DoubleProperty prefHeightProperty() {
		return cardsNeedsImprovementAreaScrolled.prefHeightProperty();
	}

	public void refresh() {
		cardsNeedsImprovementArea.getChildren().clear();
		for (kodeva.retrospective.model.Card card : model.getPinWall().getCards()) {
			if (Type.NeedsImprovement == card.getType()) {
				Card cardVisual = new Card.Builder().card(card).editable(false).controller(controller)
						.userDesk(model.getUserDesk()).build();
				cardsNeedsImprovementArea.getChildren().add(cardVisual.getNode());
			}
		}
	}
}

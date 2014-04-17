package kodeva.retrospective.view;

import javafx.beans.property.DoubleProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import kodeva.retrospective.controller.Controller;
import kodeva.retrospective.model.Card.Type;
import kodeva.retrospective.model.Model;

public class EditArea {
	private final ScrollPane cardsEditAreaScrolled;
	private final FlowPane cardsEditArea;
	private final Model model;
	private final Controller controller;

	public EditArea(Model model, Controller controller) {
		this.model = model;
		this.controller = controller;

		cardsEditArea = new FlowPane();
		cardsEditAreaScrolled = new ScrollPane();
		cardsEditAreaScrolled.setContent(cardsEditArea);
		cardsEditArea.prefWidthProperty().bind(
				cardsEditAreaScrolled.widthProperty());
		cardsEditArea.prefHeightProperty().bind(
				cardsEditAreaScrolled.heightProperty());
		cardsEditArea.setVgap(5);
		cardsEditArea.setHgap(5);
		cardsEditArea.setPadding(new Insets(2));
	}

	public Node getNode() {
		return cardsEditAreaScrolled;
	}

	public DoubleProperty prefWidthProperty() {
		return cardsEditAreaScrolled.prefWidthProperty();
	}

	public DoubleProperty prefHeightProperty() {
		return cardsEditAreaScrolled.prefHeightProperty();
	}

	public void refresh() {
		cardsEditArea.getChildren().clear();

		for (kodeva.retrospective.model.Card card : model.getUserDesk().getCards()) {
			Card.Builder cardBuilder = new Card.Builder().card(card).editable(true).controller(controller);
			if (Type.NeedsImprovement == card.getType()) {
				cardBuilder.userDesk(model.getUserDesk());
			}
			cardsEditArea.getChildren().add(cardBuilder.build().getNode());
		}
	}
}

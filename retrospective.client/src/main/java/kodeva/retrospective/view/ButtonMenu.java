package kodeva.retrospective.view;

import javafx.beans.property.DoubleProperty;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import kodeva.retrospective.controller.Controller;
import kodeva.retrospective.model.Card.Type;

public class ButtonMenu {
	private final HBox menuArea;

	public ButtonMenu(Controller eventHandlerFactory) {
		menuArea = new HBox();
		menuArea.setSpacing(3);

		final Button btnNewOkCard = new Button("Add a new 'Well Done' card");
		menuArea.getChildren().add(btnNewOkCard);
		btnNewOkCard.setOnAction(eventHandlerFactory.getCreateCardEventHandler(Type.WentWell));

		final Button btnNewFixCard = new Button("Add a new 'Improvement' card");
		menuArea.getChildren().add(btnNewFixCard);
		btnNewFixCard.setOnAction(eventHandlerFactory.getCreateCardEventHandler(Type.NeedsImprovement));
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

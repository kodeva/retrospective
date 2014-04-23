package kodeva.retrospective.view;

import javafx.beans.property.DoubleProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import kodeva.retrospective.messaging.MessageBroker;

public class NeedsImprovementArea extends AbstractCardArea {
	private final ScrollPane cardsNeedsImprovementAreaScrolled;
	
	public NeedsImprovementArea(MessageBroker messageBroker) {
		super(messageBroker);
		
		cardsNeedsImprovementAreaScrolled = new ScrollPane();

		cardsNeedsImprovementAreaScrolled.setContent(cardsUIContainer);
		cardsUIContainer.prefWidthProperty().bind(
				cardsNeedsImprovementAreaScrolled.widthProperty());
		cardsUIContainer.prefHeightProperty().bind(
				cardsNeedsImprovementAreaScrolled.heightProperty());
		cardsUIContainer.setVgap(5);
		cardsUIContainer.setHgap(5);
		cardsUIContainer.setStyle("-fx-background-color: #FFFF5F;");
		cardsUIContainer.setPadding(new Insets(2));
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

	@Override
	public Card.Builder createCardBuilder(kodeva.retrospective.model.entity.Card card) {
		return super.createCardBuilder(card).editable(false);
	}
}

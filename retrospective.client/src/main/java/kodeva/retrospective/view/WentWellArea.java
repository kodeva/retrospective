package kodeva.retrospective.view;

import javafx.beans.property.DoubleProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import kodeva.retrospective.messaging.MessageBroker;
import kodeva.retrospective.model.entity.Card;
import kodeva.retrospective.view.Card.Builder;

public class WentWellArea extends AbstractCardArea  {
	private final ScrollPane cardsWentWellAreaScrolled;

	public WentWellArea(MessageBroker messageBroker) {
		super(messageBroker);
		
		cardsWentWellAreaScrolled = new ScrollPane();

		cardsWentWellAreaScrolled.setContent(cardsUIContainer);
		cardsUIContainer.prefWidthProperty().bind(
				cardsWentWellAreaScrolled.widthProperty());
		cardsUIContainer.prefHeightProperty().bind(
				cardsWentWellAreaScrolled.heightProperty());
		cardsUIContainer.setVgap(5);
		cardsUIContainer.setHgap(5);
		cardsUIContainer.setStyle("-fx-background-color: #AFFFAF;");
		cardsUIContainer.setPadding(new Insets(2));
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

	@Override
	public Builder createCardBuilder(Card card) {
		return super.createCardBuilder(card).editable(false);
	}
}

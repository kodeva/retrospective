package kodeva.retrospective.view;

import javafx.beans.property.DoubleProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import kodeva.retrospective.messaging.MessageBroker;
import kodeva.retrospective.model.entity.UserDesk;

public class EditArea extends AbstractCardArea {
	private final ScrollPane cardsEditAreaScrolled;

	public EditArea(MessageBroker messageBroker, UserDesk userDesk) {
		super(messageBroker);

		cardsEditAreaScrolled = new ScrollPane();
		cardsEditAreaScrolled.setContent(cardsUIContainer);
		cardsUIContainer.prefWidthProperty().bind(
				cardsEditAreaScrolled.widthProperty());
		cardsUIContainer.prefHeightProperty().bind(
				cardsEditAreaScrolled.heightProperty());
		cardsUIContainer.setVgap(5);
		cardsUIContainer.setHgap(5);
		cardsUIContainer.setPadding(new Insets(2));
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

	@Override
	public Card.Builder createCardBuilder(kodeva.retrospective.model.entity.Card card) {
		Card.Builder builder = super.createCardBuilder(card).editable(true);
		return builder;
	}
}

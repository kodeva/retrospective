package kodeva.retrospective.view;

import javafx.beans.property.DoubleProperty;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import kodeva.retrospective.controller.Controller;
import kodeva.retrospective.model.Model;

public class View {
	private final VBox feedback;
	private final EditArea editArea;
	private final WentWellArea wentWellArea;
	private final NeedsImprovementArea needsImprovementArea; 

	public View(Model model, Controller controller) {
		feedback = new VBox();
		feedback.setPadding(new Insets(2));
		feedback.setSpacing(3);

		final ButtonMenu menu = new ButtonMenu(controller);
		menu.prefWidthProperty().bind(feedback.widthProperty());
		feedback.getChildren().add(menu.getNode());

		editArea = new EditArea(model, controller);
		feedback.getChildren().add(editArea.getNode());
		editArea.prefWidthProperty().bind(feedback.widthProperty());
		editArea.prefHeightProperty().bind(feedback.heightProperty());

		wentWellArea = new WentWellArea(model.getPinWall(), controller);
		feedback.getChildren().add(wentWellArea.getNode());
		wentWellArea.prefWidthProperty().bind(feedback.widthProperty());
		wentWellArea.prefHeightProperty().bind(feedback.heightProperty());

		needsImprovementArea = new NeedsImprovementArea(model, controller);
		feedback.getChildren().add(needsImprovementArea.getNode());
		needsImprovementArea.prefWidthProperty().bind(feedback.widthProperty());
		needsImprovementArea.prefHeightProperty().bind(feedback.heightProperty());
	}
	
	public Parent getParent() {
		return feedback;
	}

	public DoubleProperty prefWidthProperty() {
		return feedback.prefWidthProperty();
	}
	
	public DoubleProperty prefHeightProperty() {
		return feedback.prefHeightProperty();
	}

	public EditArea getEditArea() {
		return editArea;
	}

	public WentWellArea getWentWellArea() {
		return wentWellArea;
	}

	public NeedsImprovementArea getNeedsImprovementArea() {
		return needsImprovementArea;
	}

	public void refresh() {
		editArea.refresh();
		wentWellArea.refresh();
		needsImprovementArea.refresh();
	}
}
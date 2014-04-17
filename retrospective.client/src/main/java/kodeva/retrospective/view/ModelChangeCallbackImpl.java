package kodeva.retrospective.view;

import kodeva.retrospective.controller.ModelChangeCallback;

public class ModelChangeCallbackImpl implements ModelChangeCallback {
	private View view;
	
	public void setView(View view) {
		this.view = view;
	}

	public void onUserDeskChange() {
		view.getEditArea().refresh();
	}
	
	public void onPinWallChange() {
		view.getWentWellArea().refresh();
		view.getNeedsImprovementArea().refresh();
	}
}

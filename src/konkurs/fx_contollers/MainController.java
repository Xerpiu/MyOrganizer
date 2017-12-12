package konkurs.fx_contollers;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import konkurs.AppManager;

public class MainController {

	// --------------------------------------------------------------------------------------------------------------------
	
	@FXML
	private BorderPane borderPane;

	@FXML
	private MenuItem menuItemEvents;
	
	@FXML
	private MenuItem menuItemPlan;
	
	@FXML
	private MenuItem menuItemExit;

	@FXML
	private Label lblVersion;
	
	// --------------------------------------------------------------------------------------------------------------------
	
	@FXML
	private void initialize() {
		lblVersion.setText(lblVersion.getText() + AppManager.VERSION);
	}

	// --------------------------------------------------------------------------------------------------------------------
	
	@FXML
	public void onMenuItemEvents(ActionEvent e) {
		try {
			AppManager.getAppInstance().buildEventsEditor();
		} catch (IOException ex) {
			ex.printStackTrace();
			AppManager.closeApp();
		}
	}

	// --------------------------------------------------------------------------------------------------------------------
	
	@FXML
	public void onMenuItemPlan(ActionEvent e) {
		try {
			AppManager.getAppInstance().buildPlanEditor();
		} catch (IOException ex) {
			ex.printStackTrace();
			AppManager.closeApp();
		}
	}

	// --------------------------------------------------------------------------------------------------------------------
	
	@FXML
	public void onMenuItemExit(ActionEvent e) {
		AppManager.closeApp();
	}
	
	// --------------------------------------------------------------------------------------------------------------------
	
	@FXML
	public void onMenuItemAbout(ActionEvent e) {
		try {
			AppManager.getAppInstance().buildAbout();
		} catch (IOException ex) {
			ex.printStackTrace();
			AppManager.closeApp();
		}
	}
}

package konkurs.fx_contollers;

import javafx.scene.control.Label;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import konkurs.AppManager;
import konkurs.fx.dialogs.DialogHelper;

public class planTViewController {
	int x = 1;
	
	private String stylesR = "-fx-background-color: #CC3300;";
	private String styles = "-fx-background-color: #FFCCCC;";
	
	private String[][] tab = new String[250][3];
	String[] ele = new String[10];
	String line;
	@FXML
	private GridPane grid;
	@FXML
	private TextField cwicz;
	@FXML
	private TextField powt;
	@FXML
	private TextField czas;
	@FXML
	private TextField nazwa;

	@FXML
	public void onBtnReturn(ActionEvent event) {
		try {
			AppManager.getAppInstance().buildPlanTEditor();
			}catch (Exception ex) {
			DialogHelper.showExceptionDialog(ex);
			ex.printStackTrace();
			AppManager.closeApp();
			}
	}
	@FXML
	public void initialize() throws IOException, ClassNotFoundException {
		File plik=new File("gym_data/plany_treningowe/abc123.txt");
		Scanner odczyt = new Scanner(plik);
		while(odczyt.hasNextLine()) {
			line=odczyt.nextLine();
			ele=line.split(";");
			tab[x][0]=ele[0];
			tab[x][1]=ele[1];
			tab[x][2]=ele[2];

			Label cwiczenie = new Label();
			Label powtorz = new Label();
			Label czass = new Label();
			cwiczenie.setStyle(stylesR);
			powtorz.setStyle(styles);
			czass.setStyle(stylesR);
			cwiczenie.setMinSize(250, 20);
			powtorz.setMinSize(136, 20);
			czass.setMinSize(117, 20);	
			cwiczenie.setText(tab[x][0]);
			powtorz.setText(tab[x][1]);
			czass.setText(tab[x][2]);
			grid.add(cwiczenie, 0, x);
			grid.add(powtorz, 1, x);
			grid.add(czass, 2, x);
			
			x++;
		}
		odczyt.close();
	}
}
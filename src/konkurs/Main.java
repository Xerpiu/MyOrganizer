package konkurs;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.JOptionPane;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import konkurs.fx.dialogs.DialogHelper;
import konkurs.taskmodules.impl.TaskManager;

public class Main extends Application {

	// --------------------------------------------------------------------------------------------------------------------

	private Stage mainStage;

	// --------------------------------------------------------------------------------------------------------------------

	private UpdateScene updateScene;

	// --------------------------------------------------------------------------------------------------------------------

	private Scene sceneMain;

	// --------------------------------------------------------------------------------------------------------------------

	private Thread loadingThread;

	// --------------------------------------------------------------------------------------------------------------------
	
	private CustomFXMLLoader loader;
	
	// --------------------------------------------------------------------------------------------------------------------
	
	public static ResourceBundle bundle;
	
	// --------------------------------------------------------------------------------------------------------------------

	private static Settings settings;

	// --------------------------------------------------------------------------------------------------------------------

	// -----------------------------------------------------------------------------------------------------------------------------
	// Ta metoda jest wywolywana, gdy nasza aplikacja sie wlacza.
	// -----------------------------------------------------------------------------------------------------------------------------
	@Override
	public void start(Stage primaryStage) throws Exception {
		mainStage = primaryStage;
		
		mainStage.getIcons().add(new Image(this.getClass().getResourceAsStream("/resources/fxml/other_icons/appicon.png")));
		
		loader = new CustomFXMLLoader();
		loader.setResources(bundle);
		
		buildLoadingScreen();
		
		AppManager.applyMain(this);
		UpdateManager.applyBehaviour(updateScene);

		Task<Void> updateTask = new Task<Void>() {
			@Override
			protected Void call() {
				System.out.println("[UPDATE_TASK] Thread ID: " + Thread.currentThread().getId() + " Name=" + Thread.currentThread().getName());

				if (UpdateManager.allowsUpdate()) {
					try {
						// Symulowanie czasu aktualizacji
						Thread.sleep(1500);

						// Przechodzenie do aktualizacji
						UpdateManager.doUpdate();			
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (IOException e) {
						Alert alert = new Alert(AlertType.WARNING);
						alert.setContentText(bundle.getString("updater.updateError"));
						alert.setTitle(bundle.getString("updater.title"));
						alert.showAndWait();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				try {			
					Thread.sleep(2000);
					
					// Wczytywanie plikow (fake)
					updateScene.loadResources();
					
					// Dajemy jakis czas zeby zasymulowac
					// wczytywanie plikow itp.
					Thread.sleep(2000);				
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				return null;
			}
		};

		updateTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				try {
					buildMain();
				} catch (Exception e) {
					DialogHelper.showExceptionDialog(e);
					e.printStackTrace();
					AppManager.closeApp();
					return;
				}
			}
		});

		loadingThread = new Thread(updateTask);
		loadingThread.start();

		mainStage.show();
	}

	// -----------------------------------------------------------------------------------------------------------------------------
	// Ta metoda jest wywolywana, gdy nasza aplikacja sie wylacza.
	// -----------------------------------------------------------------------------------------------------------------------------
	@Override
	public void stop() throws Exception {
		if (loadingThread != null) {
			// Zakonczenie watku w taki sposob, nie jest najlepszym pomyslem
			// ale na razie to nam wystarcza
			loadingThread.stop();
		}
	}

	// -----------------------------------------------------------------------------------------------------------------------------
	// Ta metoda sluzy do stworzenia okienka ladowania
	// -----------------------------------------------------------------------------------------------------------------------------
	private void buildLoadingScreen() throws IOException {
		loader.setLocation(this.getClass().getResource("/resources/fxml/First.fxml"));
		
		Pane pane = loader.load();
		VBox vbox = (VBox) pane.getChildren().get(2);
		
		updateScene = new UpdateScene(pane, mainStage);
		updateScene.transfer( (Label) vbox.getChildren().get(0), (Label) vbox.getChildren().get(1));
		
		mainStage.setScene(updateScene);
	}
	
	// -----------------------------------------------------------------------------------------------------------------------------
	// Ta metoda sluzy do stworzenia glownej sceny
	// -----------------------------------------------------------------------------------------------------------------------------
	private void buildMain() throws IOException {
		loader.setLocation(this.getClass().getResource("/resources/fxml/Base.fxml"));

		StackPane pane = loader.load();

		sceneMain = new Scene(pane);

		mainStage.hide();

		mainStage.setResizable(true);
		mainStage.setWidth(800);
		mainStage.setHeight(600);
		mainStage.setMinWidth(640);
		mainStage.setMinHeight(480);
		mainStage.setScene(sceneMain);

		mainStage.show();
		
		mainStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				AppManager.closeApp();
			}
		});

		// Uruchamiamy TaskManagera
		TaskManager.initialize();
	}

	// -----------------------------------------------------------------------------------------------------------------------------
	// Ta metoda sluzy do stworzenia sceny o autorach
	// -----------------------------------------------------------------------------------------------------------------------------
	public void buildAbout() throws IOException {
		loader.setLocation(this.getClass().getResource("/resources/fxml/About.fxml"));

		StackPane pane = loader.load();

		mainStage.setScene(new Scene(pane));
	}

	// -----------------------------------------------------------------------------------------------------------------------------
	// Ta metoda sluzy do stworzenia sceny o wydarzeniach
	// TODO: Znalezc lepszy sposob na ladowanie plikow fxml za pomoca jakiegos
	// "factory" albo cos w tym stylu.
	// -----------------------------------------------------------------------------------------------------------------------------
	public void buildEventsEditor() throws IOException {
		loader.setLocation(this.getClass().getResource("/resources/fxml/Events.fxml"));
		
		SplitPane pane = loader.load();

		mainStage.setScene(new Scene(pane));
	}

	// -----------------------------------------------------------------------------------------------------------------------------
	// Ta metoda sluzy do stworzenia sceny planu lekcji
	// -----------------------------------------------------------------------------------------------------------------------------
	public void buildPlanEditor() throws IOException {
		loader.setLocation(this.getClass().getResource("/resources/fxml/Plan.fxml"));
		
		GridPane pane = loader.load();
		
		mainStage.setScene(new Scene(pane));
	}

	// -----------------------------------------------------------------------------------------------------------------------------
	// Ta metoda sluzy do stworzenia sceny silowni
	// -----------------------------------------------------------------------------------------------------------------------------
	public void buildGymEditor() throws IOException {
		loader.setLocation(this.getClass().getResource("/resources/fxml/Gym.fxml"));

		BorderPane pane = loader.load();

		mainStage.setScene(new Scene(pane));
	}

	// -----------------------------------------------------------------------------------------------------------------------------
	// Ta metoda sluzy do stworzenia sceny kalkulatora
	// -----------------------------------------------------------------------------------------------------------------------------
	public void buildCalcEditor() throws IOException {
		loader.setLocation(this.getClass().getResource("/resources/fxml/calc.fxml"));

		BorderPane pane = loader.load();

		mainStage.setScene(new Scene(pane));
	}

	// -----------------------------------------------------------------------------------------------------------------------------
	// Ta metoda sluzy do stworzenia sceny planu treningowego
	// -----------------------------------------------------------------------------------------------------------------------------
	public void buildPlanTEditor() throws IOException {
		loader.setLocation(this.getClass().getResource("/resources/fxml/planTreningowy.fxml"));

		BorderPane pane = loader.load();

		mainStage.setScene(new Scene(pane));
	}

	// -----------------------------------------------------------------------------------------------------------------------------
	// Ta metoda sluzy do stworzenia sceny dodawania planu treningowego
	// -----------------------------------------------------------------------------------------------------------------------------
	public void buildAddPlanTEditor() throws IOException {
		loader.setLocation(this.getClass().getResource("/resources/fxml/addPlanT.fxml"));
		
		BorderPane pane = loader.load();

		mainStage.setScene(new Scene(pane));
	}

	// -----------------------------------------------------------------------------------------------------------------------------
	// Ta metoda sluzy do stworzenia sceny BMI
	// -----------------------------------------------------------------------------------------------------------------------------
	public void buildBMIEditor() throws IOException {
		loader.setLocation(this.getClass().getResource("/resources/fxml/BMI.fxml"));

		BorderPane pane = loader.load();

		mainStage.setScene(new Scene(pane));
	}

	// -----------------------------------------------------------------------------------------------------------------------------
	// Ta metoda sluzy do stworzenia sceny BMI
	// -----------------------------------------------------------------------------------------------------------------------------
	public void buildPlanTView() throws IOException {
		loader.setLocation(this.getClass().getResource("/resources/fxml/planTView.fxml"));
		
		BorderPane pane = loader.load();
		
		mainStage.setScene(new Scene(pane));
	}

	// -----------------------------------------------------------------------------------------------------------------------------
	// Ta metoda sluzy do stworzenia sceny LBM
	// -----------------------------------------------------------------------------------------------------------------------------
	public void buildLBMEditor() throws IOException {
		loader.setLocation(this.getClass().getResource("/resources/fxml/LBM.fxml"));

		BorderPane pane = loader.load();

		mainStage.setScene(new Scene(pane));
	}

	// -----------------------------------------------------------------------------------------------------------------------------
	// Ta metoda sluzy do stworzenia sceny ustawien
	// -----------------------------------------------------------------------------------------------------------------------------
	public void buildSettings() throws IOException {
		loader.setLocation(this.getClass().getResource("/resources/fxml/Options.fxml"));
		
		AnchorPane pane = loader.load();

		mainStage.setScene(new Scene(pane));
	}

	// -----------------------------------------------------------------------------------------------------------------------------
	// Ta metoda sluzy do przelaczenia na scene glowna
	// -----------------------------------------------------------------------------------------------------------------------------
	public void switchToMain() {
		mainStage.setScene(sceneMain);
	}

	// -----------------------------------------------------------------------------------------------------------------------------

	public Stage getMainStage() {
		return mainStage;
	}

	// -----------------------------------------------------------------------------------------------------------------------------

	public void setSettings(Settings settings) {
		Main.settings = settings;
	}

	// -----------------------------------------------------------------------------------------------------------------------------

	public Settings getSettings() {
		return settings;
	}

	// -----------------------------------------------------------------------------------------------------------------------------

	public static void processSettings(String action) throws ClassNotFoundException {
		if (action.toLowerCase().equals("load")) {
			if (Files.exists(Paths.get("data/settings.dat"))) {
				try {
					ObjectInputStream ois;
					ois = new ObjectInputStream(new FileInputStream("data/settings.dat"));
					settings = (Settings) ois.readObject();
					ois.close();
				} catch (FileNotFoundException e) {} catch (IOException e) {}
			} else
				settings = new Settings();
		} else if (action.toLowerCase().equals("save")) {
			try {
				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("data/settings.dat"));
				oos.writeObject(settings);
				oos.close();
			} catch (FileNotFoundException e) {} catch (IOException e) {}
		} else return;
	}

	// -----------------------------------------------------------------------------------------------------------------------------
	
	public static void gymInitializeFiles() {
		try { Files.createFile(Paths.get("data/informacje.txt")); }
		catch (IOException e) { System.out.println("informacje.txt : skipping"); }
		try { Files.createFile(Paths.get("data/nazwa_Planu_1.txt")); } 
		catch(IOException e) { System.out.println("nazwa_Planu_1.txt : skipping"); }	
		try { Files.createFile(Paths.get("data/plany_treningowe_lista.txt")); } 
		catch(IOException e) { System.out.println("plany_treningowe_lista.txt : skipping"); }
	}
	
	// -----------------------------------------------------------------------------------------------------------------------------
	// Metoda main
	// -----------------------------------------------------------------------------------------------------------------------------
	public static void main(String[] args) {
		try {
			System.out.println("Checking java version...");
			if(AppManager.JAVA_VERSION < 1.8) {
				System.out.println("Java 1.8 Version is required at least to work properly continue on your own risk!");
				JOptionPane.showMessageDialog(null, "Java 1.8 Version is required at least to work properly!", "Warning!", JOptionPane.WARNING_MESSAGE);
			}
			System.out.println(AppManager.JAVA_VERSION);
					
			if (args.length > 0) {
				if(args[0].equalsIgnoreCase("-export")) {
					UpdateManager.exportTargetMD5ToFile("sync.txt");
					System.exit(0);
				} else if(args[0].equalsIgnoreCase("-lock_fix")) {
					Utils.unlockInstance();
					System.exit(0);
				}
			}

			Utils.lockInstance();
			
			// Wczytywanie ustawien
			processSettings("load");

			gymInitializeFiles();
			
			switch (settings.langInterface) 
			{
				case PL: bundle = ResourceBundle.getBundle("Interface", new Locale("PL")); break;
				case EN: bundle = ResourceBundle.getBundle("Interface", new Locale("EN")); break;
				case DE: bundle = ResourceBundle.getBundle("Interface", new Locale("DE")); break;
				default: bundle = ResourceBundle.getBundle("Interface", new Locale("PL")); break;
			}
			
			// Zalecam wylaczyc ta opcje, gdy grzebiemy w kodzie
			UpdateManager.allowUpdate(settings.updatesEnabled);

			try {
				UpdateManager.initialize();
				launch(args);
			} catch (NoSuchAlgorithmException ex) {
				DialogHelper.showExceptionDialogLater(ex);
			}

			Utils.unlockInstance();
		} catch (Exception e) {
			DialogHelper.showExceptionDialogLater(e);
			e.printStackTrace();
		}
	}
}
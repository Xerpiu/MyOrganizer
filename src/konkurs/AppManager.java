package konkurs;

import javafx.application.Platform;

public class AppManager {

	public static final String VERSION = "0.1.7a";
	
	private static Main appInstance;
	
	public static void applyMain(Main m) {
		appInstance = m;
	}
	
	public static void closeApp() {
		// Moze byc przyczyna problemow
		// na razie zostawiamy jednak tak
		Platform.exit();
	}
	
	public static Main getAppInstance() {
		return appInstance;
	}
}
package konkurs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javafx.application.Platform;

public class UpdateManager {

	// --------------------------------------------------------------------------------------------------------------------
	
	// Mozemy zmienic ta wartosc na false
	// jezeli np. nie chcemy aby nasz program sprawdzal i instalowal aktualizacje.
	private static boolean DO_UPDATE = true;
	
	// Sciezka do katalogu na serwerze w ktorym znajduja sie wszystkie
	// pliki binarne programu (czyli jar itp.)
	public static final String UPDATE_URL = "http://xerp.cba.pl/MyOrganizer/updates/";
	
	// Plik, ktory bedziemy aktualizowac (ten co znajduje sie na serwerze)
	public static final String UPDATE_URL_TARGET = "MyOrganizer.jar";
	
	// Plik z suma kontrolna pliku (MD5) ktorego bedziemy aktualizowac
	public static final String UPDATE_URL_TARGET_SYNC = "sync.txt";
	
	// Plik, ktory bedziemy aktualizowac (znajdujacy sie na dysku)
	public static final String UPDATE_TARGET = "MyOrganizer.jar";

	// MD5 pliku
	public static String updateTargetMd5;
	
	// --------------------------------------------------------------------------------------------------------------------
	
	private static UpdateBehaviour updateControl;

	// --------------------------------------------------------------------------------------------------------------------
	
	public static void applyBehaviour(UpdateBehaviour ub) {
		updateControl = ub;
	}
	
	// --------------------------------------------------------------------------------------------------------------------
	
	public static void allowUpdate(boolean arg) {
		DO_UPDATE = arg;
	}
	
	// --------------------------------------------------------------------------------------------------------------------
	
	public static boolean allowsUpdate() {
		return DO_UPDATE;
	}
	
	// --------------------------------------------------------------------------------------------------------------------
	
	public static void initialize() {
		try {
			// Pobieramy MD5 pliku
			updateTargetMd5 = Utils.getFileChecksum(MessageDigest.getInstance("MD5"), new File(UPDATE_TARGET));
		} catch (NoSuchAlgorithmException e) {
			System.err.println("[UPDATE_PROCESS] initialize() returned: NoSuchAlgorithmException");
		} catch (IOException e) {
			System.err.println("[UPDATE_PROCESS] Could not find MD5 for File (Possible not exist)");
		}
	}
	
	// --------------------------------------------------------------------------------------------------------------------
	
	public static void doUpdate() throws Exception {
		String result = Utils.getHTML(UPDATE_URL + UPDATE_URL_TARGET_SYNC);
		
		if(!result.isEmpty()) {
			System.out.println("[UPDATE_PROCESS] MD5 from Server: (" + result + ") our (" + updateTargetMd5 + ")");
			
			if(updateTargetMd5 == null) {
				updateControl.onUpdateStarted();
				
				downloadAppAndUpdate();
			} else if(updateTargetMd5.equals(result)) {
				updateControl.onUpdateNothingToUpdate();
			} else {
				updateControl.onUpdateStarted();
				
				downloadAppAndUpdate();
			}
			
		} else {
			updateControl.onUpdateError();
		}
	}
	
	// --------------------------------------------------------------------------------------------------------------------
	
	public static void exportTargetMD5ToFile(String fileName) throws NoSuchAlgorithmException, IOException {
		String md5 = Utils.getFileChecksum(MessageDigest.getInstance("MD5"), new File(UPDATE_TARGET));
		
		FileOutputStream fos = new FileOutputStream(fileName);
		fos.write(md5.getBytes(), 0, md5.length());
		fos.close();
		
		System.out.println("UpdateManager.exportTargetMD5ToFile() Export done");
	}
	
	// --------------------------------------------------------------------------------------------------------------------
	
	private static void downloadAppAndUpdate() throws Exception {
		System.out.println("[UPDATE_PROCESS] Downloading update...");
		String appSource = Utils.getHTML(UPDATE_URL + UPDATE_URL_TARGET);
		
		if(!appSource.isEmpty()) {
			URL web = new URL(UPDATE_URL + UPDATE_URL_TARGET);
			ReadableByteChannel rbc = Channels.newChannel(web.openStream());
			
			FileOutputStream fos = new FileOutputStream("MyOrganizer-Update.jar");
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			
			fos.close();
			
			System.out.println("[UPDATE_PROCESS] Download done.");
			
			System.out.println("[UPDATE_PROCESS] Applying update...");
			
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					Path oldVersionsPath = Paths.get("old");
					
					try {
						Files.createDirectory(oldVersionsPath);
					} catch(FileAlreadyExistsException ex) {
						System.out.println("[UPDATE_PROCESS] Old Dir exist :D");
					} catch (IOException ex) {
						ex.printStackTrace();
						AppManager.closeApp();
					}
					
					try {
						// TODO: Doda� wsparcie dla Linuxa
						Runtime.getRuntime().exec("cmd.exe /c " + "start update.bat");
						AppManager.closeApp();
					} catch (IOException e) {
						e.printStackTrace();
						AppManager.closeApp();
					}
				}
			});
			
			System.out.println("[UPDATE_PROCESS] All done!");
		}
	}
	
	// --------------------------------------------------------------------------------------------------------------------
	
}
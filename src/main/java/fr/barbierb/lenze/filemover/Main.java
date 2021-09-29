package fr.barbierb.lenze.filemover;

import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.StandardOpenOption;

import javax.imageio.ImageIO;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

	static Stage stage;
	static TrayIcon trayIcon;

	@Override
	public void start(Stage s) throws Exception {
		Main.stage = s;
		

		System.out.println(new File(".").getAbsolutePath());
		new File(".").createNewFile();
		
		addAppToTray();

		Platform.setImplicitExit(false);

		s.setTitle("FileMover - Lenze Ruitz");
		s.setScene(new Scene(new Button("test")));
		s.setMinWidth(400);
		s.setMinHeight(250);
		s.setFullScreenExitHint("");
		s.setResizable(false);
		s.show();

		/*while(true) {
			System.out.println("test");
			Thread.currentThread().sleep(1000);
		}*/
		
		/*Parent root = null;
		try {
			root = FXMLLoader.load(getClass().getResource("/fxml/App.fxml"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		Platform.setImplicitExit(false);

		s.setTitle("FileMover - Lenze Ruitz");
		s.setScene(new Scene(root));
		s.setMinWidth(400);
		s.setMinHeight(250);
		s.setFullScreenExitHint("");
		s.setResizable(false);
		s.getIcons().add(new Image(getClass().getResourceAsStream("/img/icon_black.png")));
		s.show();*/
	}

	private void addAppToTray() {
		try {
			java.awt.Toolkit.getDefaultToolkit();

			if (!SystemTray.isSupported()) {
				System.out.println("No system tray support, application exiting.");
				return;
			}
			
			SystemTray tray = SystemTray.getSystemTray();

			java.awt.Image image = ImageIO.read(getClass().getResourceAsStream("/img/icon_white.png"));
			trayIcon = new TrayIcon(image);

			trayIcon.addActionListener(event -> Platform.runLater(this::showStage));

			java.awt.MenuItem openItem = new java.awt.MenuItem("Propri�t�s");
			openItem.addActionListener(event -> Platform.runLater(this::showStage));

			java.awt.Font defaultFont = java.awt.Font.decode(null);
			java.awt.Font boldFont = defaultFont.deriveFont(java.awt.Font.BOLD);
			openItem.setFont(boldFont);

			java.awt.MenuItem exitItem = new java.awt.MenuItem("Quitter");
			exitItem.addActionListener(event -> {
				Platform.exit();
				tray.remove(trayIcon);
				System.exit(0);
			});

			final java.awt.PopupMenu popup = new java.awt.PopupMenu();
			popup.add(openItem);
			popup.addSeparator();
			popup.add(exitItem);
			trayIcon.setPopupMenu(popup);

			tray.add(trayIcon);
		} catch (java.awt.AWTException | IOException e) {
			System.out.println("Unable to init system tray");
			e.printStackTrace();
		}
	}

	/**
	 * Shows the application stage and ensures that it is brought ot the front of all stages.
	 */
	private void showStage() {
		if (stage != null) {
			stage.show();
			stage.toFront();
		}
	}

	public static void main(String[] args) {
		System.out.println("LAUNCH");
		/*File file = new File("filemover.lock");
		try {
		    FileChannel fc = FileChannel.open(file.toPath(),
		            StandardOpenOption.CREATE,
		            StandardOpenOption.WRITE);
		    FileLock lock = fc.tryLock();
		    
		    if (lock == null) {
		        System.out.println("Vous ne pouvez pas lancer plusieurs instances");
		        System.exit(0);
		    }
		} catch (IOException e) {
		    throw new Error(e);
		}*/
		
		Application.launch(args);
	}
}

package fr.barbierb.lenze.filemover;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Properties;

import javax.swing.SwingUtilities;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;

public class AppController {

	@FXML private Button button_src;
	@FXML private Button button_dest;
	@FXML private TextField textfield_extension;
	@FXML private ChoiceBox<String> combobox_freq;
	@FXML private TextField textfield_lifetime;
	@FXML private CheckBox checkbox_delete;

	int fileLifetime;
	String fileExt;
	int comboboxIndex;
	String folder_src;
	String folder_dest;

	Thread fileMoverThread;

	private Properties prop;

	@FXML
	void initialize() {
		// test
		prop = new Properties();
		try {
			FileReader reader = new FileReader("app.properties");
			prop.load(reader);
		} catch (Exception e1) {
			System.err.println("Le fichier 'app.properties' n'existait pas.");
		}

		fileExt = (String) prop.getProperty("fileExt", null);
		fileLifetime = Integer.parseInt(prop.getProperty("fileLifetime", "0"));
		folder_src = (String) prop.getProperty("folder_src", null);
		folder_dest = (String) prop.getProperty("folder_dest", null);
		comboboxIndex = Integer.parseInt(prop.getProperty("comboboxIndex", "2"));
		
		if(folder_dest != null && folder_dest.equals("delete")) {
			button_dest.setText("Suppression");
			button_dest.setDisable(true);
			checkbox_delete.setSelected(true);
		}

		Runnable task = new Runnable() {
			@Override
			public void run() {
				while(true) {
					if(folder_src != null && folder_dest != null && fileExt != null) {
						
						File src = new File(folder_src);
						File dest = new File(folder_dest);

						if(src.isDirectory()) {
							
							boolean usingLifetime = fileLifetime > 0;
							
							int fileMovedAmount = 0;
							
							File files[] = src.listFiles();
							for(int idx = 0; idx < files.length; idx++) {

								File file = files[idx];
								
								if(!file.getName().endsWith(fileExt))
									continue;

								try {
									
									if(checkbox_delete.isSelected()) {
										
										if(usingLifetime && (System.currentTimeMillis()-file.lastModified())/1000 <= fileLifetime)
											continue;
										
										fileMovedAmount++;
										System.out.println(file.toPath()+" removed");
										file.delete();
										continue;
									}
									
									if(!dest.isDirectory())
										return;
									
									File destFile = new File(dest.getAbsoluteFile()+File.separator+file.getName());
									if(destFile.exists())
										continue;
									
									if(usingLifetime && (System.currentTimeMillis()-file.lastModified())/1000 <= fileLifetime)
										continue;
									
									Files.move(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
									fileMovedAmount++;
									System.out.println(file.toPath()+" copied to "+dest.toPath());
								} catch (IOException e) {
									System.out.println("error");
									e.printStackTrace();
								}
							}
							
							///if(fileMovedAmount>0) {
								//String s = ((fileMovedAmount>1)?"s":"");
								//String notifMessage = fileMovedAmount+" fichier"+s+" déplacé"+s+".";
								//SwingUtilities.invokeLater(() -> Main.trayIcon.displayMessage("FileMover", notifMessage, java.awt.TrayIcon.MessageType.INFO));
							//}

						}
					}
					
					long timeToSleep = 1000; // 0
					/*if(comboboxIndex == 0)
						timeToSleep = 1000 * 60 * 30; // 30 mins
					else if(comboboxIndex == 1)
						timeToSleep = 1000 * 60 * 60; // 1 h
					else if(comboboxIndex == 2)
						timeToSleep = 1000 * 60 * 120; // 2 h
					else if(comboboxIndex == 3)
						timeToSleep = 1000 * 60 * 240; // 4 h*/
						
					
					try {
						Thread.sleep(timeToSleep);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};

		fileMoverThread = new Thread(task);

		combobox_freq.getItems().addAll("30mins", "1h", "2h", "4h");
		combobox_freq.getSelectionModel().select(comboboxIndex);
		combobox_freq.getSelectionModel().selectedIndexProperty().addListener((obs, old, news) -> {
			comboboxIndex = news.intValue();
			System.out.println("new comboboxIndex : "+comboboxIndex);
			prop.setProperty("comboboxIndex", comboboxIndex+"");
			save();
		});
		
		textfield_lifetime.setText(fileLifetime==0?"":fileLifetime+"");
		textfield_lifetime.textProperty().addListener((obs, old, news) -> {
			try {
				fileLifetime = Integer.parseInt(news);
			} catch(NumberFormatException e) {
				textfield_lifetime.textProperty().setValue(old);
				return;
			}
			
			System.out.println("new fileLifetime : "+fileLifetime);
			prop.setProperty("fileLifetime", fileLifetime+"");
			save();
		});
		
		
		textfield_extension.setText(fileExt==null?"":fileExt);
		textfield_extension.textProperty().addListener((obs, old, news) -> {
			fileExt = news;
			System.out.println("new ext : "+fileExt);
			prop.setProperty("fileExt", fileExt);
			save();
		});

		button_src.setText(folder_src==null?"Dossier source non défini":folder_src);
		button_src.setOnMousePressed(e -> {
			DirectoryChooser chooser = new DirectoryChooser();
			chooser.setTitle("Select the destination folder.");

			File folder = chooser.showDialog(Main.stage);
			if (folder != null) {
				System.out.println("new src folder : "+folder.getAbsolutePath());
				folder_src = folder.getAbsolutePath();
				button_src.setText(folder_src);
				prop.setProperty("folder_src", folder_src);
				save();
			}
		});

		button_dest.setText(folder_dest==null?"Dossier destination non défini":folder_dest);
		button_dest.setOnMousePressed(e -> {
			DirectoryChooser chooser = new DirectoryChooser();
			chooser.setTitle("Select the destination folder.");

			File folder = chooser.showDialog(Main.stage);
			if (folder != null) {
				System.out.println("new dest folder : "+folder.getAbsolutePath());
				folder_dest = folder.getAbsolutePath();
				button_dest.setText(folder_dest);
				prop.setProperty("folder_dest", folder_dest);
				save();
			}
		});
		
		checkbox_delete.selectedProperty().addListener((obs, old, newv) -> {
			if(newv) {
				folder_dest = "delete";
				prop.setProperty("folder_dest", folder_dest);
				button_dest.setText("delete");
				button_dest.setDisable(true);
				save();
			} else {
				button_dest.setText("Undefined");
				button_dest.setDisable(false);
				folder_dest = "delete";
				prop.setProperty("folder_dest", folder_dest);
				save();
			}
		});

		fileMoverThread.start();
	}

	private void save() {
		try {
			FileWriter fw = new FileWriter("app.properties");
			prop.store(fw, "");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

}
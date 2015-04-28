package us.mbilker.tinylauncher;

import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AWTWindow {
	
	private static final String NEW_LINE = System.getProperty("line.separator");
	
	private static final Logger LOGGER = TinyLauncher.createChildLogger("AWTWindow");
	
	private Frame mainFrame;
	
	public void setupFrame() {
		LOGGER.log(Level.INFO, "setupFrame");
		LOGGER.log(Level.INFO, "NEW_LINE = " + NEW_LINE.getBytes());
		
		mainFrame = new Frame("TinyLauncher");
		mainFrame.setSize(400, 400);
		mainFrame.setLayout(new GridLayout(3, 1));
		
		Label headerLabel = new Label();
		headerLabel.setAlignment(Label.CENTER);
		headerLabel.setText(displayLauncherVariables());
		
		mainFrame.add(headerLabel);
		mainFrame.setVisible(true);
	}
	
	private String displayLauncherVariables() {
		StringBuilder build = new StringBuilder();
		TinyLauncher tl = TinyLauncher.instance;
		
		build.append("Data Dir: ");
		build.append(TinyLauncher.dataDir);
		build.append(NEW_LINE);
		
		build.append("Version: ");
		build.append(tl.params.version);
		build.append(NEW_LINE);
		
		return build.toString();
	}
}

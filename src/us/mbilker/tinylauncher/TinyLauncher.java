package us.mbilker.tinylauncher;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.beust.jcommander.JCommander;

import us.mbilker.auth.Authentication;
import us.mbilker.auth.AuthenticationResponse;
import us.mbilker.auth.LastLogin;
import us.mbilker.auth.LastLoginCipherException;
import us.mbilker.configuration.file.YamlConfiguration;

public class TinyLauncher {
	
	public static PrintStream cachedErr = System.err;
	
	public static Logger LOGGER = Logger.getLogger("TinyLauncher");
	
	public static File currentDir = new File(System.getProperty("user.dir", "."));
	public static File dataDir = new File(currentDir, "data");
	public static File assetsDir = new File(dataDir, "assets");
	public static File serverDir = new File(dataDir, "server");
	public static File configFile = new File(dataDir, "config.yml");
	
	public static File clientDir = new File(dataDir, "mc");
	
	public static YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
	
	public static String[] ignore = { "lwjgl.jar", "jinput.jar", "lwjgl_util.jar" };
	
	private File binDir = new File(clientDir, "bin");
    private String nativeDir = new File(binDir, "natives").getAbsolutePath();
    
    private String realUsername = "Player";
    private String sessionId = "0";
	
	static {
		System.setErr(System.out);
		
		LogManager.getLogManager().reset();
		Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).setLevel(Level.OFF);
		
		ConsoleHandler handler = new ConsoleHandler() {
			@Override
			protected void setOutputStream(OutputStream out) throws SecurityException {
				//super.setOutputStream(System.out);
				super.setOutputStream(out);
			}
		};
		handler.setFormatter(new LogFormatter());
		LOGGER.setUseParentHandlers(false);
		LOGGER.addHandler(handler);
	}
	
	public TinyLauncher(String[] args, JCommander cmd, CommandOptions params) {
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss aa");
        Date date = new Date();
        
        System.out.println("Testing from " + TinyLauncher.class.getName());
		
        LOGGER.info("Tiny Launcher (by mbilker)");
        LOGGER.info(String.format("Started at %s", dateFormat.format(date)));
		LOGGER.info(String.format("Data directory: %s", dataDir.toString()));
		
		//LOGGER.info("Setting minecraft directory as user home and current directory just in case");
		//System.setProperty("user.home", clientDir.getAbsolutePath());
		//System.setProperty("user.dir", clientDir.getAbsolutePath());
		
		if (!dataDir.exists()) {
			LOGGER.info("Data folder does not exist, creating. Typical on first start.");
			dataDir.mkdir();
		}
		
		if (!serverDir.exists()) {
			LOGGER.info("Server folder does not exist, creating. Typical on first start.");
			serverDir.mkdir();
		}
		
		if (!clientDir.exists()) {
			LOGGER.info(".minecraft folder does not exist, creating. Typical on first start.");
			clientDir.mkdir();
		}
		
		if (!configFile.exists()) {
			LOGGER.info("Config file does not exist, creating. Typical on first start.");
			try {
				config.set("nop", "");
				saveConfig();
			} catch (IOException e) {
				LOGGER.info("Problem creating config file");
				e.printStackTrace();
			}
		}
		
		if (params.auth && !"".equals(params.username) && !"".equals(params.password)) {
			final Authentication auth = new Authentication(params.username, params.password);
			AuthenticationResponse response = AuthenticationResponse.UNKNOWN;
			try {
				response = auth.authenticate();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (response == AuthenticationResponse.SUCCESS) {
				sessionId = auth.getSessionId();
				realUsername = auth.getRealUsername();
				LOGGER.log(Level.INFO, String.format("Successful authentication, user: %s, sessionId: %s", realUsername, sessionId));
				
				if (params.lastlogin) {
					LastLogin lastlogin = new LastLogin(auth);
					try {
						lastlogin.writeTo(dataDir.getAbsolutePath());
					} catch (IOException e) {
						e.printStackTrace();
					} catch (LastLoginCipherException e) {
						e.printStackTrace();
					}
				}
				
				if (params.keepAlive > 0) {
					Timer timer = new Timer("Authentication Keep Alive", true);
					timer.scheduleAtFixedRate(new TimerTask() {
						@Override
						public void run() {
							try {
								auth.keepAlive();
							} catch (UnsupportedEncodingException e) {
								e.printStackTrace();
							} catch (MalformedURLException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}, params.keepAlive * 1000, params.keepAlive * 1000);
				}
			}
		} else {
			LOGGER.warning("No authentication, going offline");
		}
	    
	    System.setProperty("user.home", dataDir.getAbsolutePath());
	    System.setProperty("minecraft.applet.TargetDirectory", clientDir.getAbsolutePath());
	    
	    String appletToLoad = "net.minecraft.client.MinecraftApplet";
	    String title = "Tiny Launcher";
	    
	    System.setProperty("minecraft.applet.WrapperClass", ContainerApplet.class.getName());
	    
	    ContainerApplet container = new ContainerApplet(appletToLoad);
	    
	    container.setUsername(realUsername);
	    container.setMpPass(params.password);
	    container.setSessionId(sessionId);
	    
	    ContainerFrame frame = new ContainerFrame(title);
	    
	    Dimension d = new Dimension(854, 480);
	    frame.setPreferredSize(d);
	    frame.setSize(d);
	    frame.setContainerApplet(container);
	    frame.setVisible(true);
	    
	    container.loadNatives(nativeDir);
	    if (container.loadJarsAndApplet(clientDir.getAbsolutePath(), binDir.getAbsolutePath())) {
	    	container.init();
	    	container.start();
	    } else {
	    	LOGGER.severe("Minecraft failed to launch!");
	    	System.exit(0);
	    }
	}
	
	public static void saveConfig() throws IOException {
		config.save(configFile);
	}
}

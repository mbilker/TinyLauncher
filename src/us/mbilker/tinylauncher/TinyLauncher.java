package us.mbilker.tinylauncher;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.beust.jcommander.JCommander;

import org.bonsaimind.minecraftmiddleknife.post16.yggdrasil.AuthenticationRequest;
import org.bonsaimind.minecraftmiddleknife.post16.yggdrasil.AuthenticationResponse;
import org.bonsaimind.minecraftmiddleknife.post16.yggdrasil.Yggdrasil;

import us.mbilker.configuration.file.YamlConfiguration;

public class TinyLauncher {
	
	public static TinyLauncher instance;
	
	public static PrintStream cachedErr = System.err;
	
	public static Logger LOGGER = Logger.getLogger("TinyLauncher");
	
	public static File currentDir = new File(System.getProperty("user.dir", "."));
	public static File dataDir = new File(currentDir, "data");
    //public static File serverDir = new File(dataDir, "server");
    public static File configFile = new File(dataDir, "config.yml");

    public static YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

    public File clientDir;
    public File versionsDir;
    public File binDir;
    public String nativesDir;
    public String assetsDir;
    public String librariesDir;

    public CommandOptions params;
    public CommandOptions paramsDefault = new CommandOptions();
    public String uuid = "";
    public String accessToken = "";
	
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
		TinyLauncher.instance = this;
		
		this.params = params;
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss aa");
        Date date = new Date();
		
        LOGGER.info("Tiny Launcher (by mbilker)");
        LOGGER.info(String.format("Started at %s", dateFormat.format(date)));
		LOGGER.info(String.format("Data directory: %s", dataDir.toString()));
		
		params.loadFromConfig(config);
		params.saveToConfig(config);
		saveConfig();
		
		if (params.dump) {
			System.out.println("help: " + params.help);
			System.out.println("auth: " + params.auth);
			System.out.println("lastlogin: " + params.lastlogin);
			System.out.println("username: " + params.username);
			System.out.println("password: " + params.password);
			System.out.println("keepAlive: " + params.keepAlive);
			System.out.println("dump: " + params.dump);
			
			System.exit(0);
			return;
		}
		
		clientDir = new File(dataDir, params.dir);
		versionsDir = new File(clientDir, "versions");
		binDir = new File(versionsDir, params.version);
		nativesDir = new File(new File(dataDir, params.nativesDir), params.version).getAbsolutePath();
		assetsDir = new File(clientDir, "assets").getAbsolutePath();
		librariesDir = new File(clientDir, "libraries").getAbsolutePath();
		
		//LOGGER.info("Setting minecraft directory as user home and current directory just in case");
		//System.setProperty("user.home", clientDir.getAbsolutePath());
		//System.setProperty("user.dir", clientDir.getAbsolutePath());
		
		if (!dataDir.exists()) {
			LOGGER.info("Data folder does not exist, creating. Typical on first start.");
			dataDir.mkdir();
		}
		
		//if (!serverDir.exists()) {
		//	LOGGER.info("Server folder does not exist, creating. Typical on first start.");
		//	serverDir.mkdir();
		//}
		
		if (!clientDir.exists()) {
			LOGGER.severe("Minecraft folder does not exist. Please copy an existing one.");
			System.exit(1);
			return;
		}
		
		authenticate();
		
		params.saveToConfig(config);
		saveConfig();
	    
	    Container container = new Container();
	    
	    LOGGER.log(Level.INFO, "Loading natives from: " + nativesDir);
	    LOGGER.log(Level.INFO, "Loading assets from: " + assetsDir);
	    
	    if (!container.loadJarsAndApplet(clientDir.getAbsolutePath(), binDir.getAbsolutePath())) {
	    	LOGGER.severe("Minecraft failed to launch!");
	    	System.exit(0);
	    }
	}
	
	private void loadAuthFromConfig() {
		if (!config.getConfigurationSection("lastauth").getValues(false).isEmpty()) {
			LOGGER.info("Have old values, reusing");
			params.username = config.getString("lastauth.hash-username", new String(Base64.decode(params.username)));
			uuid = config.getString("lastauth.uuid", "");
			accessToken = config.getString("lastauth.accessToken", "");
		}
	}
	
	private void authenticate() {
		if (params.auth) {
			if ((params.username.equals(paramsDefault.username) && params.password.equals(paramsDefault.password)) && (!config.isString("lastauth.hash-username") || !config.isString("lastauth.hash-password"))) {
				LOGGER.log(Level.INFO, "Username and password are set to defaults, not authenticating.");
				return;
			}
			AuthenticationResponse response;
			try {
				params.username = (params.username.equals(paramsDefault.username) && config.isString("lastauth.hash-username")) ? new String(Base64.decode(config.getString("lastauth.hash-username"))) : params.username;
				params.password = (params.password.equals(paramsDefault.password) && config.isString("lastauth.hash-password")) ? new String(Base64.decode(config.getString("lastauth.hash-password"))) : params.password;
				response = Yggdrasil.authenticate(new AuthenticationRequest(params.username, params.password));
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "Error authenticating");
				e.printStackTrace();
				loadAuthFromConfig();
				return;
			}
			if (!response.getClientToken().isEmpty() && !response.getAccessToken().isEmpty()) {
				uuid = response.getSelectedProfile().getId();
				accessToken = response.getAccessToken();
				String username = response.getSelectedProfile().getUsername();
				LOGGER.log(Level.INFO, String.format("Successful authentication, user: %s, uuid: %s, accessToken: %s, clientToken: %s", username, uuid, accessToken, response.getClientToken()));
				
				/*
				final AuthenticationResponse finalResponse = response;
				if (params.keepAlive > 0) {
					Timer timer = new Timer("Authentication Keep Alive", true);
					timer.scheduleAtFixedRate(new TimerTask() {
						@Override
						public void run() {
							try {
								Yggdrasil.keepAlive(finalResponse);
							} catch (AuthenticationException e) {
								LOGGER.log(Level.SEVERE, "Error on keep alive", e);
							}
						}
					}, params.keepAlive * 1000, params.keepAlive * 1000);
				}
				*/
				config.set("lastauth.hash-username", Base64.encodeToString(params.username.getBytes(), false));
				params.username = username;
				config.set("lastauth.hash-password", Base64.encodeToString(params.password.getBytes(), false));
				config.set("lastauth.uuid", uuid);
				config.set("lastauth.accessToken", accessToken);
				config.set("lastauth.clientToken", response.getSelectedProfile().getId());
			} else {
				LOGGER.warning("Empty client token or access token: " + response.toString());
				loadAuthFromConfig();
			}
		} else {
			LOGGER.warning("No authentication, going offline");
		}
	}
	
	public static void saveConfig() {
		try {
			config.save(configFile);
		} catch (IOException e) {
			LOGGER.log(Level.INFO, "Failed to save config");
			e.printStackTrace();
		}
	}
	
	public static boolean is64bits() {
		if (System.getProperty("sun.arch.data.model").equalsIgnoreCase("64")) {
			return true;
		}
		return false;
	}
}

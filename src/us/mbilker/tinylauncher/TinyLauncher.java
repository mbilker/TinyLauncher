package us.mbilker.tinylauncher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.beust.jcommander.JCommander;

import org.bonsaimind.minecraftmiddleknife.post16.yggdrasil.AuthenticationRequest;
import org.bonsaimind.minecraftmiddleknife.post16.yggdrasil.AuthenticationResponse;
import org.bonsaimind.minecraftmiddleknife.post16.yggdrasil.Profile;
import org.bonsaimind.minecraftmiddleknife.post16.yggdrasil.RefreshRequest;
import org.bonsaimind.minecraftmiddleknife.post16.yggdrasil.Yggdrasil;
import org.bonsaimind.minecraftmiddleknife.post16.yggdrasil.YggdrasilError;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.representer.Representer;

import us.mbilker.tinylauncher.util.Base64;
import us.mbilker.tinylauncher.util.LogFormatter;
import us.mbilker.tinylauncher.util.Util;
import us.mbilker.tinylauncher.util.YamlUtil;

public class TinyLauncher {

	public static TinyLauncher instance;

	public static PrintStream cachedErr = System.err;

	public static Logger LOGGER = Logger.getLogger("TinyLauncher");

	public static File currentDir = new File(System.getProperty("user.dir", "."));
	public static File dataDir = new File(currentDir, "data");
	public static File configFile = new File(dataDir, "config.yml");

	public static Map<String, Object> config = new ConcurrentHashMap<String, Object>();

	public CommandOptions params;
	public CommandOptions paramsDefault = new CommandOptions();

	public File clientDir;
	public File versionsDir;
	public File binDir;
	public File nativesDir;
	public File assetsDir;
	public File librariesDir;

	private String _username = "";
	private String _uuid = "";
	private String _accessToken = "";
	private String _userType = "";

	static {
		System.setErr(System.out);

		LogManager.getLogManager().reset();
		Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).setLevel(Level.OFF);

		ConsoleHandler handler = new ConsoleHandler();
		handler.setFormatter(new LogFormatter());
		LOGGER.setUseParentHandlers(false);
		LOGGER.addHandler(handler);
	}

	public TinyLauncher(String[] args, JCommander cmd, CommandOptions params) {
		TinyLauncher.instance = this;

		this.params = params;
	}

	public void launchMinecraft() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss aa");
		Date date = new Date();

		LOGGER.info("Tiny Launcher (by mbilker)");
		LOGGER.info(String.format("Started at %s", dateFormat.format(date)));
		LOGGER.info(String.format("Data directory: %s", dataDir.toString()));

		try {
			TinyLauncher.loadConfig();
			params.loadFromConfig(config);
			params.saveToConfig(config);
			TinyLauncher.saveConfig();
		} catch (IOException e1) {
			LOGGER.log(Level.SEVERE, "Failed to load/save config", e1);
		}

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
		nativesDir = new File(new File(dataDir, params.nativesDir), params.version);
		assetsDir = new File(clientDir, "assets");
		librariesDir = new File(clientDir, "libraries");

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

		//AWTWindow window = new AWTWindow();
		//window.setupFrame();

		this.authenticate();

		params.saveToConfig(config);
		try {
			TinyLauncher.saveConfig();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Failed to save config", e);
		}

		Container container = new Container();

		LOGGER.log(Level.INFO, "Loading natives from: " + this.nativesDir);
		LOGGER.log(Level.INFO, "Loading assets from: " + this.assetsDir);

		if (!container.loadJarsAndApplet(params.version, this.clientDir, this.binDir, this.nativesDir, this.librariesDir, this.assetsDir)) {
			LOGGER.severe("Minecraft failed to launch!");
			System.exit(1);
		}
	}

	public static Logger createChildLogger(String name) {
		Logger logger = Logger.getLogger(name);

		logger.setParent(TinyLauncher.LOGGER);

		return logger;
	}

	private void loadAuthFromConfig() {
		if (config.containsKey("lastauth") && config.get("lastauth") instanceof Map<?, ?>) {
			@SuppressWarnings("unchecked")
			Map<String, Object> lastauth = (Map<String, Object>) config.get("lastauth");

			LOGGER.info("Have old values, reusing");

			_username = YamlUtil.getString(lastauth, "hash-username", new String(Base64.decode(params.username)));
			_uuid = YamlUtil.getString(lastauth, "uuid", new UUID(0L, 0L).toString());
			_accessToken = YamlUtil.getString(lastauth, "accessToken", "");
			_userType = "legacy";
		}
	}

	private void authenticate() {
		if (params.auth) {
			String username = params.username;
			String password = params.password;
			
			if (config.containsKey("lastauth") && config.get("lastauth") instanceof Map<?, ?>) {
				LOGGER.log(Level.INFO, "Found lastauth stored in config");
				
				@SuppressWarnings("unchecked")
				Map<String, Object> lastauth = (Map<String, Object>) config.get("lastauth");
				
				if (params.username.equals(paramsDefault.username) && YamlUtil.isString(lastauth, "hash-username")) {
					username = new String(Base64.decode((String) lastauth.get("hash-username")));
				}
				
				if (params.password.equals(paramsDefault.password) && YamlUtil.isString(lastauth, "hash-password")) {
					password = new String(Base64.decode((String) lastauth.get("hash-password")));
				}
			}
			
			if ((username.equals(paramsDefault.username) && password.equals(paramsDefault.password))) {
				LOGGER.log(Level.INFO, "Username and password are set to defaults, not authenticating.");
				return;
			}
			
			AuthenticationResponse response;
			try {
				response = Yggdrasil.authenticate(new AuthenticationRequest(username, password));
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "Error authenticating");
				e.printStackTrace();
				loadAuthFromConfig();
				return;
			}

			if (!response.getClientToken().isEmpty() && !response.getAccessToken().isEmpty()) {
				Profile profile = response.getSelectedProfile();

				_username = username;
				_uuid = profile.getId();
				_accessToken = response.getAccessToken();
				_userType = "mojang";

				LOGGER.log(Level.INFO, String.format("Successful authentication, user: %s, uuid: %s, accessToken: %s, clientToken: %s", _username, _uuid, _accessToken, response.getClientToken()));

				final RefreshRequest refreshRequest = new RefreshRequest(response.getAccessToken(), response.getClientToken());
				if (params.keepAlive > 0) {
					Timer timer = new Timer("Authentication Keep Alive", true);
					timer.scheduleAtFixedRate(new TimerTask() {
						@Override
						public void run() {
							try {
								Yggdrasil.refresh(refreshRequest);
							} catch (YggdrasilError e) {
								LOGGER.log(Level.SEVERE, "Error on keep alive", e);
							}
						}
					}, params.keepAlive * 1000, params.keepAlive * 1000);
				}
				
				Map<String, Object> lastauth = new HashMap<String, Object>();
				
				lastauth.put("hash-username", Base64.encodeToString(username.getBytes(), false));
				lastauth.put("hash-password", Base64.encodeToString(password.getBytes(), false));
				lastauth.put("uuid", _uuid);
				lastauth.put("accessToken", _accessToken);
				lastauth.put("clientToken", response.getClientToken());
				
				if (config.containsKey("lastauth")) {
					config.remove("lastauth");
				}
				
				config.put("lastauth", lastauth);
			} else {
				LOGGER.warning("Empty client token or access token: " + response.toString());
				loadAuthFromConfig();
			}
		} else {
			LOGGER.warning("No authentication, going offline");
		}
		
		if (Util.isEmpty(_uuid)) {
			_uuid = new UUID(0L, 0L).toString();
		}
	}

	public String getUsername() {
		return this._username;
	}

	public String getUUID() {
		return this._uuid;
	}

	public String getAccessToken() {
		return this._accessToken;
	}

	public String getUserType() {
		return this._userType;
	}

	@SuppressWarnings("unchecked")
	public static void loadConfig() throws FileNotFoundException {
		Yaml yaml = new Yaml(new SafeConstructor());
		InputStream inputStream = new FileInputStream(configFile);

		config = (Map<String, Object>) yaml.load(inputStream);
	}

	public static void saveConfig() throws IOException {
		DumperOptions options = new DumperOptions();

		options.setWidth(80);
		options.setIndent(2);
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

		Yaml yaml = new Yaml(new SafeConstructor(), new Representer(), options);

		FileWriter writer = new FileWriter(configFile);

		yaml.dump(config, writer);
	}

	public static boolean is64bits() {
		if (System.getProperty("sun.arch.data.model").equalsIgnoreCase("64")) {
			return true;
		}
		return false;
	}
}

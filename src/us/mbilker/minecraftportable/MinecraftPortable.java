package us.mbilker.minecraftportable;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.file.YamlConfiguration;

public class MinecraftPortable {
	
	public static File currentDir = new File(System.getProperty("user.dir", "."));
	public static File dataDir = new File(currentDir, "mcp_data");
	public static File serverDir = new File(dataDir, "server");
	public static File configFile = new File(dataDir, "config.yml");
	
	public static File clientDir = new File(dataDir, "mc");
	
	public static YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
	
	public static String[] ignore = { "lwjgl.jar", "jinput.jar", "lwjgl_util.jar", };
	
	public MinecraftPortable(String[] args) {
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss aa");
        Date date = new Date();
		
		Main.log("Minecraft Portable 2.8");
		Main.log("by mbilker");
		Main.log("Started at %s", dateFormat.format(date));
		Main.log("Data directory: %s", dataDir.toString());
		
		Main.log("Setting minecraft directory as user home and current directory just in case");
		System.setProperty("user.home", clientDir.getAbsolutePath());
		System.setProperty("user.dir", clientDir.getAbsolutePath());
		
		if (!dataDir.exists()) {
			Main.log("Data folder does not exist, creating. Typical on first start.");
			dataDir.mkdir();
		}
		
		if (!serverDir.exists()) {
			Main.log("Server folder does not exist, creating. Typical on first start.");
			serverDir.mkdir();
		}
		
		if (!clientDir.exists()) {
			Main.log(".minecraft folder does not exist, creating. Typical on first start.");
			clientDir.mkdir();
		}
		
		if (!configFile.exists()) {
			Main.log("Config file does not exist, creating. Typical on first start.");
			try {
				config.set("Autologin.server", "");
				config.set("Settings.noupdate", false);
				config.set("Settings.lastjar", "minecraft.jar");
				saveConfig();
			} catch (IOException e) {
				Main.log("Problem creating config file");
				e.printStackTrace();
			}
		}
		
		String str1 = null;
	    String str2 = null;
	    
	    Map<String, String> mapOfLauncherArgs = new HashMap<String, String>();
	    
	    for (String str4 : args) {
	      if ((str4.startsWith("-u=")) || (str4.startsWith("--user="))) {
	        str1 = getArgValue(str4);
	        mapOfLauncherArgs.put("user", str1);
	      } else if ((str4.startsWith("-p=")) || (str4.startsWith("--password="))) {
	        str2 = getArgValue(str4);
	        mapOfLauncherArgs.put("password", str2);
	      } else if (str4.startsWith("--noupdate")) {
	        mapOfLauncherArgs.put("noupdate", "true");
	      }
	    }

	    if (args.length >= 3) {
	      String param = args[2];
	      String str3 = "25565";
	      if (param.contains(":")) {
	        String[] arrayOfString = param.split(":");
	        param = arrayOfString[0];
	        str3 = arrayOfString[1];
	      }
	      mapOfLauncherArgs.put("server", param);
	      mapOfLauncherArgs.put("port", str3);
	    }
	    
	    if (!config.getString("Autologin.server", "").isEmpty() && !mapOfLauncherArgs.containsKey("server") && !mapOfLauncherArgs.containsKey("port")) {
	    	Main.log("Using server from config, '%s'", config.getString("Autologin.server"));
	    	String server = config.getString("Autologin.server");
	    	String port = "25565";
	    	if (server.contains(":")) {
	    		String[] arrayOfString = server.split(":");
	    		server = arrayOfString[0];
	    		port = arrayOfString[1];
	    	}
	    	mapOfLauncherArgs.put("server", server);
	    	mapOfLauncherArgs.put("port", port);
	    }
	    
		LauncherFrame.main(mapOfLauncherArgs);
	}
	
	public static void saveConfig() throws IOException {
		config.save(configFile);
	}
	
	private static String getArgValue(String paramString) {
		int i = paramString.indexOf('=');
		if (i < 0) {
			return "";
		}
		return paramString.substring(i + 1);
	}
}

package us.mbilker.tinylauncher;

import us.mbilker.configuration.file.YamlConfiguration;

import com.beust.jcommander.Parameter;

public class CommandOptions {
	
	@Parameter(names = { "-help" })
	public boolean help = false;
	
	@Parameter(names = { "-auth", "-authenticate" }, arity = 1)
	public boolean auth = true;
	
	@Parameter(names = { "-lastlogin" }, arity = 1)
	public boolean lastlogin = true;
	
	@Parameter(names = { "-user", "-username" })
	public String username = "Player";
	
	@Parameter(names = { "-pass", "-password" })
	public String password = "password";
	
	@Parameter(names = { "-keepalive" })
	public int keepAlive = 300;
	
	@Parameter(names = { "-dir" })
	public String dir = "mc";
	
	@Parameter(names = { "-natives" })
	public String nativesDir = "natives";
	
	@Parameter(names = { "-assets" })
	public String assetsDir = "assets";
	
	@Parameter(names = { "-version" })
	public String version = "1.8.4";
	
	@Parameter(names = { "-dump" })
	public boolean dump = false;
	
	public void saveToConfig(YamlConfiguration config) {
		String pre = "options.";
		config.set(pre + "auth", auth);
		config.set(pre + "lastlogin", lastlogin);
		config.set(pre + "keepAlive", keepAlive);
		config.set(pre + "dir", dir);
		config.set(pre + "natives", nativesDir);
		config.set(pre + "assets", assetsDir);
		config.set(pre + "version", version);
	}
	
	public void loadFromConfig(YamlConfiguration config) {
		String pre = "options.";
		CommandOptions options = new CommandOptions();
		
		this.auth = (this.auth != options.auth) ? this.auth : config.getBoolean(pre + "auth", options.auth);
		this.lastlogin = (this.lastlogin != options.lastlogin) ? this.lastlogin : config.getBoolean(pre + "lastlogin", options.lastlogin);
		this.keepAlive = (this.keepAlive != options.keepAlive) ? this.keepAlive : config.getInt(pre + "keepAlive", options.keepAlive);
		this.dir = (this.dir != options.dir) ? this.dir : config.getString(pre + "dir", options.dir);
		this.nativesDir = (this.nativesDir != options.nativesDir) ? this.nativesDir : config.getString(pre + "natives", options.nativesDir);
		this.assetsDir = (this.assetsDir != options.assetsDir) ? this.assetsDir : config.getString(pre + "assets", options.assetsDir);
		this.version = (this.version != options.version) ? this.version : config.getString(pre + "version", options.version);
	}
}

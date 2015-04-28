package us.mbilker.tinylauncher;

import java.util.HashMap;
import java.util.Map;

import us.mbilker.tinylauncher.util.YamlUtil;

import com.beust.jcommander.Parameter;

public class CommandOptions {
	
	@Parameter(names = { "-help" })
	public boolean help = false;
	
	@Parameter(names = { "-auth", "-authenticate" }, arity = 1)
	public boolean auth = true;
	
	@Parameter(names = { "-lastlogin" }, arity = 1)
	public boolean lastlogin = true;
	
	@Parameter(names = { "-user", "-username" })
	public String username = "UGxheWVy"; // 'Player' in base64
	
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
	
	public Map<String, Object> saveToConfig(Map<String, Object> map) {
		
		Map<String, Object> config = new HashMap<String, Object>();
		
		config.put("auth", auth);
		config.put("lastlogin", lastlogin);
		config.put("keepAlive", keepAlive);
		config.put("dir", dir);
		config.put("natives", nativesDir);
		config.put("assets", assetsDir);
		config.put("version", version);
		
		if (map.containsKey("options")) {
			map.remove("options");
		}
		
		map.put("options", config);
		
		return map;
	}
	
	public void loadFromConfig(Map<String, Object> config) {
		if (!config.containsKey("options")) {
			return;
		}
		
		@SuppressWarnings("unchecked")
		Map<String, Object> sub = (Map<String, Object>) config.get("options");
		
		CommandOptions options = new CommandOptions();
		
		this.auth = (this.auth != options.auth) ? this.auth : YamlUtil.getBoolean(sub, "auth", options.auth);
		this.lastlogin = (this.lastlogin != options.lastlogin) ? this.lastlogin : YamlUtil.getBoolean(sub, "lastlogin", options.lastlogin);
		this.keepAlive = (this.keepAlive != options.keepAlive) ? this.keepAlive : YamlUtil.getInt(sub, "keepAlive", options.keepAlive);
		this.dir = (this.dir != options.dir) ? this.dir : YamlUtil.getString(sub, "dir", options.dir);
		this.nativesDir = (this.nativesDir != options.nativesDir) ? this.nativesDir : YamlUtil.getString(sub, "natives", options.nativesDir);
		this.assetsDir = (this.assetsDir != options.assetsDir) ? this.assetsDir : YamlUtil.getString(sub, "assets", options.assetsDir);
		this.version = (this.version != options.version) ? this.version : YamlUtil.getString(sub, "version", options.version);
	}
}

package us.mbilker.tinylauncher;

import com.beust.jcommander.Parameter;

public class CommandOptions {
	
	@Parameter(names = { "-help" })
	public boolean help = false;
	
	@Parameter(names = { "-auth", "-authenticate" }, arity = 1)
	public boolean auth = true;
	
	@Parameter(names = { "-lastlogin" }, arity = 1)
	public boolean lastlogin = true;
	
	@Parameter(names = { "-user", "-username" })
	public String username = "";
	
	@Parameter(names = { "-pass", "-password" })
	public String password = "";
	
	@Parameter(names = { "-keepalive" })
	public int keepAlive = 300;
}

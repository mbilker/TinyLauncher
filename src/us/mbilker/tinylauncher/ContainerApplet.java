package us.mbilker.tinylauncher;

import java.applet.Applet;
import java.applet.AppletStub;
import java.awt.BorderLayout;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ContainerApplet extends Applet implements AppletStub {
	
	private String appletToLoad;
	private Map<String, String> params = new HashMap<String, String>();
	private Applet minecraftApplet;
	
	public ContainerApplet(String appletToLoad) {
		super();
		
		this.appletToLoad = appletToLoad;
		
		setLayout(new BorderLayout());
		
		params.put("fullscreen", "false");
		params.put("stand-alone", "true");
		params.put("username", "Player");
		params.put("mppass", "");
		params.put("server", null);
		params.put("port", null);
		params.put("sessionid", "0");
		params.put("loadmap_user", "Player");
		params.put("loadmap_id", "0");
		params.put("demo", "false");
	}
	
	public void appletResize(int width, int height) {
	}
	
	@Override
	public void destroy() {
		if (minecraftApplet != null) {
			minecraftApplet.destroy();
		}
		super.destroy();
	}
	
	@Override
	public URL getDocumentBase() {
		try {
			return new URL("http://localhost:0");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public String getParameter(String name) {
		TinyLauncher.LOGGER.log(Level.INFO, "Parameter Requested: " + name);
		
		if (params.containsKey(name)) {
			TinyLauncher.LOGGER.log(Level.INFO, params.get(name));
			return params.get(name);
		} else {
			TinyLauncher.LOGGER.log(Level.INFO, "UNHANDLED");
			return "";
		}
	}
}

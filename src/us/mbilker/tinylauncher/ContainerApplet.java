package us.mbilker.tinylauncher;

import java.applet.Applet;
import java.applet.AppletStub;
import java.awt.BorderLayout;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ContainerApplet extends Applet implements AppletStub {
	
	private static final long serialVersionUID = 9263L;
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
	
	@Override
	public boolean isActive() {
		return true;
	}
	
	public boolean loadJarsAndApplet(String minecraftDir, String binDir) {
		TinyLauncher.LOGGER.log(Level.INFO, "Loading Minecraft from: " + binDir);
		
		try {
			URLClassLoader loader;
			if (new File(binDir, "modpack.jar").exists()) {
				File[] files = new File[] {
					new File(binDir, "modpack.jar"),
					new File(binDir, "minecraft.jar"),
					new File(binDir, "jinput.jar"),
					new File(binDir, "lwjgl.jar"),
					new File(binDir, "lwjgl_util.jar")
				};
				
				loader = new ModClassLoader(ClassLoader.getSystemClassLoader(), files[0], files);
			} else {
				URL[] urls = new URL[] {
					new File(binDir, "minecraft.jar").toURI().toURL(),
					new File(binDir, "jinput.jar").toURI().toURL(),
					new File(binDir, "lwjgl.jar").toURI().toURL(),
					new File(binDir, "lwjgl_util.jar").toURI().toURL()
				};
				
				loader = new URLClassLoader(urls);
			}
			
			setMinecraftDirectory(loader, new File(minecraftDir));
			setMinecraftApplet((Applet) loader.loadClass(appletToLoad).newInstance());
			
			return true;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public void loadNatives(String nativeDir) {
		nativeDir = new File(nativeDir).getAbsolutePath();
		
		TinyLauncher.LOGGER.log(Level.INFO, "Loading Natives from: " + nativeDir);
		
		System.setProperty("org.lwjgl.librarypath", nativeDir);
		System.setProperty("net.java.games.input.librarypath", nativeDir);
	}
	
	@Override
	public void init() {
		minecraftApplet.init();
	}
	
	public void setDemo(boolean demo) {
		params.put("demo", Boolean.toString(demo));
	}
	
	public void setMpPass(String pass) {
		params.put("mppass", pass);
	}
	
	public void setServer(String server, String port) {
		params.put("server", server);
		params.put("port", port);
	}
	
	public void setSessionId(String sessionId) {
		params.put("sessionid", sessionId);
	}
	
	public void setUsername(String user) {
		params.put("username", user);
		params.put("loadmap_user", user);
	}
	
	@Override
	public void start() {
		minecraftApplet.start();
	}
	
	@Override
	public void stop() {
		if (minecraftApplet != null) {
			minecraftApplet.stop();
		}
		
		super.stop();
	}
	
	public void replace(Applet applet) {
		setMinecraftApplet(applet);
		
		minecraftApplet.init();
	}
	
	private void setMinecraftApplet(Applet applet) {
		if (minecraftApplet != null) {
			remove(minecraftApplet);
			minecraftApplet.stop();
			minecraftApplet.destroy();
			minecraftApplet = null;
		}
		
		minecraftApplet = applet;
		
		minecraftApplet.setStub(this);
		minecraftApplet.setSize(getWidth(), getHeight());
		
		add(minecraftApplet, "Center");
	}
	
	private void setMinecraftDirectory(ClassLoader loader, File directory) {
		try {
			Class<?> clazz = loader.loadClass("net.minecraft.client.Minecraft");
			Field[] fields = clazz.getDeclaredFields();
			
			int fieldCount = 0;
			Field dirField = null;
			for (Field field : fields) {
				if (field.getType() == File.class) {
					int mods = field.getModifiers();
					if (Modifier.isStatic(mods) && Modifier.isPrivate(mods)) {
						dirField = field;
						fieldCount++;
					}
				}
			}
			
			if (fieldCount != 1) { throw new Exception("Could not find Minecraft directory field"); }
			
			dirField.setAccessible(true);
			dirField.set(null, directory);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

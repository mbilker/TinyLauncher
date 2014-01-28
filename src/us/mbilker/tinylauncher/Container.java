package us.mbilker.tinylauncher;

import java.io.File;
import java.net.MalformedURLException;
import java.util.logging.Level;

import org.bonsaimind.minecraftmiddleknife.ClassLoaderExtender;
import org.bonsaimind.minecraftmiddleknife.ClassLoaderExtensionException;
import org.bonsaimind.minecraftmiddleknife.post16.Kickstarter;
import org.bonsaimind.minecraftmiddleknife.post16.RunException;

public class Container {
	
	public boolean loadJarsAndApplet(String clientDir, String binDir) {
		TinyLauncher.LOGGER.log(Level.INFO, "Loading Minecraft from: " + binDir);
		
		try {
			/*
			URLClassLoader loader;
			if (new File(binDir, "modpack.jar").exists()) {
				File[] files = new File[] {
					new File(binDir, "modpack.jar")
				};
				
				loader = new ModClassLoader(ClassLoader.getSystemClassLoader(), files[0], files);
			}
			*/
			ClassLoaderExtender.extend(new File(binDir, TinyLauncher.instance.params.version + ".jar").toURI().toURL());
			ClassLoaderExtender.extendFrom(new File(clientDir, "libraries").getAbsolutePath());
			
			String[] args = new String[] {
				"--username", TinyLauncher.instance.params.username,
				"--version", TinyLauncher.instance.params.version,
				"--gameDir", TinyLauncher.instance.clientDir.getAbsolutePath(),
				"--assetsDir", TinyLauncher.instance.assetsDir,
				"--uuid", TinyLauncher.instance.uuid,
				"--accessToken", TinyLauncher.instance.accessToken,
				"--userProperties", "{}"
			};
			
			Kickstarter.run(Kickstarter.MAIN_CLASS, Kickstarter.MAIN_METHOD, args);
			
			return true;
		} catch (ClassLoaderExtensionException e) {
			e.printStackTrace();
		} catch (RunException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public void loadNatives(String nativeDir) {
		nativeDir = new File(nativeDir).getAbsolutePath();
		
		System.setProperty("org.lwjgl.librarypath", nativeDir);
		System.setProperty("net.java.games.input.librarypath", nativeDir);
	}
}

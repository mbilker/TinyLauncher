package us.mbilker.tinylauncher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.bonsaimind.minecraftmiddleknife.ClassLoaderCreator;
import org.bonsaimind.minecraftmiddleknife.NativeLoader;
import org.bonsaimind.minecraftmiddleknife.post16.Kickstarter;
import org.bonsaimind.minecraftmiddleknife.post16.RunException;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import us.mbilker.tinylauncher.asset.AssetManagement;
import us.mbilker.tinylauncher.json.JsonFactory;
import us.mbilker.tinylauncher.json.Library;
import us.mbilker.tinylauncher.json.Version;

public class Container {

	private static final Logger LOGGER = TinyLauncher.createChildLogger("Container");

	public boolean loadJarsAndApplet(String version, File clientDir, File binDir, File nativesDir, File librariesDir, File assetsDir) {
		LOGGER.log(Level.INFO, "Loading Minecraft from: " + binDir);

		try {
			Version base = JsonFactory.loadVersion(new File(binDir, TinyLauncher.instance.params.version + ".json"));

			final String finalAssetsDir = AssetManagement.syncAssets(assetsDir, base.getAssets()).getAbsolutePath();

			this.extractNatives(version, nativesDir);
			this.loadNatives(version, nativesDir);

			ClassLoaderCreator creator = new ClassLoaderCreator();

			for (Library lib : base.getLibraries()) {
				creator.add(new File(librariesDir, lib.getPath()).toURI().toURL());
			}
			creator.add(new File(binDir, version + ".jar").toURI().toURL());

			ClassLoader classLoader = creator.createClassLoader();

			String[] args = new String[] {
					"--username", TinyLauncher.instance.getUsername(),
					"--version", TinyLauncher.instance.params.version,
					"--gameDir", TinyLauncher.instance.clientDir.getAbsolutePath(),
					"--assetsDir", finalAssetsDir,
					"--assetIndex", base.getAssets(),
					"--uuid", TinyLauncher.instance.getUUID(),
					"--accessToken", TinyLauncher.instance.getAccessToken(),
					"--userProperties", "{}",
					"--userType", TinyLauncher.instance.getUserType()
			};

			LOGGER.log(Level.INFO, "Template Arguments: " + base.minecraftArguments);
			LOGGER.log(Level.INFO, "Arguments: " + Arrays.asList(args));

			Kickstarter.run(classLoader, Kickstarter.MAIN_CLASS, Kickstarter.MAIN_METHOD, args);

			return true;
		} catch (RunException e) {
			LOGGER.log(Level.SEVERE, "loadJarsAndApplet RunException:", e);
		} catch (MalformedURLException e) {
			LOGGER.log(Level.SEVERE, "loadJarsAndApplet MalformedURLException:", e);
		} catch (JsonSyntaxException e) {
			LOGGER.log(Level.SEVERE, "loadJarsAndApplet JsonSyntaxException:", e);
		} catch (JsonIOException e) {
			LOGGER.log(Level.SEVERE, "loadJarsAndApplet JsonIOException:", e);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "loadJarsAndApplet IOException:", e);
		}

		return false;
	}
	
	public void extractNatives(String version, File nativesDir) throws JsonSyntaxException, JsonIOException, IOException {
		Version base = JsonFactory.loadVersion(new File(new File(TinyLauncher.instance.versionsDir, version), version + ".json"));

		byte[] buf = new byte[1024];
		for (Library lib : base.getLibraries()) {
			if (lib.natives != null) {
				File local = new File(TinyLauncher.instance.librariesDir, lib.getPathNatives());
				ZipInputStream input = null;
				try {
					input = new ZipInputStream(new FileInputStream(local));
					ZipEntry entry = input.getNextEntry();
					while (entry != null) {
						String name = entry.getName();
						int n;
						if (lib.extract == null || !lib.extract.exclude(name)) {
							File output = new File(nativesDir, name);
							output.getParentFile().mkdirs();

							FileOutputStream out = new FileOutputStream(output);
							while ((n = input.read(buf, 0, 1024)) > -1) {
								out.write(buf, 0, n);
							}
							out.close();
						}
						input.closeEntry();
						entry = input.getNextEntry();
					}
				} catch (Exception e) {
					LOGGER.log(Level.SEVERE, "Error extracting natives:", e);
				} finally {
					try {
						input.close();
					} catch (IOException e) {
						LOGGER.log(Level.SEVERE, "Error closing input stream:", e);
					}
				}
			}
		}
	}
	
	public void loadNatives(String version, File nativeDir) {
		NativeLoader.loadNativeLibraries(nativeDir.getAbsolutePath());
	}
}

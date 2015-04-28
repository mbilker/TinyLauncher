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
	
	public boolean loadJarsAndApplet(String clientDir, String binDir) {
		LOGGER.log(Level.INFO, "Loading Minecraft from: " + binDir);
		
		try {
			Version base = JsonFactory.loadVersion(new File(binDir, TinyLauncher.instance.params.version + ".json"));
			
			final String finalAssetsDir = AssetManagement.syncAssets(new File(TinyLauncher.instance.assetsDir), base.getAssets()).getAbsolutePath();
			
			this.loadNatives(TinyLauncher.instance.nativesDir);
            
            ClassLoaderCreator creator = new ClassLoaderCreator();
			
			for (Library lib : base.getLibraries()) {
				creator.add(new File(TinyLauncher.instance.librariesDir, lib.getPath()).toURI().toURL());
			}
			creator.add(new File(binDir, TinyLauncher.instance.params.version + ".jar").toURI().toURL());
			
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
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		} catch (JsonIOException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public void loadNatives(String nativeDir) throws JsonSyntaxException, JsonIOException, IOException {
		Version base = JsonFactory.loadVersion(new File(new File(TinyLauncher.instance.versionsDir, TinyLauncher.instance.params.version), TinyLauncher.instance.params.version + ".json"));
		
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
                            File output = new File(nativeDir, name);
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
                    }
                }
            }
        }
        
        NativeLoader.loadNativeLibraries(nativeDir);
	}
}

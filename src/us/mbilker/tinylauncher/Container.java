package us.mbilker.tinylauncher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.bonsaimind.minecraftmiddleknife.ClassLoaderCreator;
import org.bonsaimind.minecraftmiddleknife.NativeLoader;
import org.bonsaimind.minecraftmiddleknife.post16.Kickstarter;
import org.bonsaimind.minecraftmiddleknife.post16.RunException;

import com.google.common.collect.Sets;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import es.usc.citius.common.parallel.Parallel;
import us.mbilker.tinylauncher.json.AssetIndex;
import us.mbilker.tinylauncher.json.AssetIndex.Asset;
import us.mbilker.tinylauncher.json.JsonFactory;
import us.mbilker.tinylauncher.json.Library;
import us.mbilker.tinylauncher.json.Version;

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
			
			Version base = JsonFactory.loadVersion(new File(new File(TinyLauncher.instance.versionsDir, TinyLauncher.instance.params.version), TinyLauncher.instance.params.version + ".json"));
			
			final String finalAssetsDir = syncAssets(new File(TinyLauncher.instance.assetsDir), base.getAssets()).getAbsolutePath();
			
			this.loadNatives(TinyLauncher.instance.nativesDir);
            
            ClassLoaderCreator creator = new ClassLoaderCreator();
			
			for (Library lib : base.getLibraries()) {
				creator.add(new File(TinyLauncher.instance.librariesDir, lib.getPath()).toURI().toURL());
			}
			creator.add(new File(binDir, TinyLauncher.instance.params.version + ".jar").toURI().toURL());
			
			ClassLoader classLoader = creator.createClassLoader();
			
			String[] args = new String[] {
				"--username", TinyLauncher.instance.params.username,
				"--version", TinyLauncher.instance.params.version,
				"--gameDir", TinyLauncher.instance.clientDir.getAbsolutePath(),
				"--assetsDir", finalAssetsDir,
				"--assetIndex", base.getAssets(),
				"--uuid", TinyLauncher.instance.uuid,
				"--accessToken", TinyLauncher.instance.accessToken,
				"--userProperties", "{}",
				"--userType", "mojang"
			};
			
			TinyLauncher.LOGGER.log(Level.INFO, "Template Arguments: " + base.minecraftArguments);
			TinyLauncher.LOGGER.log(Level.INFO, "Arguments: " + Arrays.asList(args));
			
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
                    TinyLauncher.LOGGER.log(Level.SEVERE, "Error extracting natives:", e);
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
	
	private static void copyFile(File sourceFile, File destinationFile, boolean overwrite) throws IOException {
        if (sourceFile.exists()) {
            if (!destinationFile.exists()) {
                destinationFile.getParentFile().mkdirs();
                destinationFile.createNewFile();
            } else if (!overwrite) {
                return;
            }
            FileChannel sourceStream = null, destinationStream = null;
            try {
                sourceStream = new FileInputStream(sourceFile).getChannel();
                destinationStream = new FileOutputStream(destinationFile).getChannel();
                destinationStream.transferFrom(sourceStream, 0, sourceStream.size());
            } finally {
                if (sourceStream != null) {
                    sourceStream.close();
                }
                if (destinationStream != null) {
                    destinationStream.close();
                }
            }
        }
    }
	
	private static String fileSHA(File file) throws IOException {
        if (file.exists()) {
            return Files.hash(file, Hashing.sha1()).toString();
        } else {
            return "";
        }
    }
	
	private static Set<File> listFiles(File path) {
        Set<File> set = Sets.newHashSet();
        if (path.exists()) {
            listFiles(path, set);
        }
        return set;
    }
	
	private static void listFiles(File path, Set<File> set) {
        for (File f : path.listFiles()) {
            if (f.isDirectory()) {
                listFiles(f, set);
            } else {
                set.add(f);
            }
        }
    }
	
	private static File syncAssets(File assetDir, String indexName) throws JsonSyntaxException, JsonIOException, IOException {
		TinyLauncher.LOGGER.log(Level.INFO, "Syncing Assets");
        final File objects = new File(assetDir, "objects");
        AssetIndex index = JsonFactory.loadAssetIndex(new File(assetDir, "indexes/{INDEX}.json".replace("{INDEX}", indexName)));
        
        TinyLauncher.LOGGER.log(Level.INFO, "Loading asset index: " + indexName);

        if (!index.virtual) {
            return assetDir;
        }

        final File targetDir = new File(assetDir, "virtual/" + indexName);

        final ConcurrentSkipListSet<File> old = new ConcurrentSkipListSet<File>();
        old.addAll(listFiles(targetDir));

        Parallel.TaskHandler<?> th = new Parallel.ForEach<Entry<String, Asset>, Void>(index.objects.entrySet())
          .withFixedThreads(2 * Runtime.getRuntime().availableProcessors())
          .apply(new Parallel.F<Entry<String, Asset>, Void>() {
        	  public Void apply(Entry<String, Asset> e) {
        		  Asset asset = e.getValue();
        		  File local = new File(targetDir, e.getKey());
        		  File object = new File(objects, asset.hash.substring(0, 2) + "/" + asset.hash);

        		  old.remove(local);

        		  try {
        			  if (local.exists() && !fileSHA(local).equals(asset.hash)) {
        				  TinyLauncher.LOGGER.log(Level.INFO, "  Changed: " + e.getKey());
        				  copyFile(object, local, true);
        			  } else if (!local.exists()) {
        				  TinyLauncher.LOGGER.log(Level.INFO, "  Added: " + e.getKey());
        				  copyFile(object, local, true);
        			  }
        			  
        			  TinyLauncher.LOGGER.log(Level.INFO, "Checked: " + e.getKey());
        		  } catch (Exception ex) {
        			  TinyLauncher.LOGGER.log(Level.SEVERE, "Asset checking failed: ", ex);
        		  }
        		  return null;
        	  }
          });
        try {
            th.wait(60, TimeUnit.SECONDS);
        } catch (Exception ex) {
            TinyLauncher.LOGGER.log(Level.SEVERE, "Asset checking failed: ", ex);
        }

        for (File f : old) {
            String name = f.getAbsolutePath().replace(targetDir.getAbsolutePath(), "");
            TinyLauncher.LOGGER.log(Level.INFO, "  Removed: " + name.substring(1));
            f.delete();
        }

        return targetDir;
    }
}

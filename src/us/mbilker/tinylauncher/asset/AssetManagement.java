package us.mbilker.tinylauncher.asset;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import us.mbilker.tinylauncher.TinyLauncher;
import us.mbilker.tinylauncher.json.AssetIndex;
import us.mbilker.tinylauncher.json.JsonFactory;
import us.mbilker.tinylauncher.json.AssetIndex.Asset;

import com.google.common.collect.Sets;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import es.usc.citius.common.parallel.Parallel;

public class AssetManagement {
	
	private static final Logger LOGGER = TinyLauncher.createChildLogger("AssetManagement");
	
	public static File syncAssets(File assetDir, String indexName) throws JsonSyntaxException, JsonIOException, IOException {
		LOGGER.log(Level.INFO, "Syncing Assets");
        final File objects = new File(assetDir, "objects");
        AssetIndex index = JsonFactory.loadAssetIndex(new File(assetDir, "indexes/{INDEX}.json".replace("{INDEX}", indexName)));
        
        LOGGER.log(Level.INFO, "Loading asset index: " + indexName);

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
}

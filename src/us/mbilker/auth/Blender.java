package us.mbilker.auth;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.ListIterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Allows blending of multiple jars.
 * The last given jar is the "canonical" jar, meaning that files from
 * all the other jars are only appended and never overwritten.
 * 
 * What that means? You first pass in the minecraft.jar and then the mod.jar.
 */
public class Blender {

	private boolean keepManifest = false;
	private List<String> stack = new ArrayList<String>();

	public Blender() {
	}

	/**
	 * Add one jar to the stack.
	 * @param jar The jar to add.
	 */
	public void add(String jar) {
		stack.add(jar);
	}

	/**
	 * Blends the stack into one and saves it into the given outputJar.
	 * @param outputJar
	 * @throws FileNotFoundException
	 * @throws IOException 
	 */
	public void blend(String outputJar) throws FileNotFoundException, IOException {
		final File outputFile = new File(outputJar).getAbsoluteFile();

		if (outputFile.exists()) {
			outputFile.delete();
		}

		ZipOutputStream blendedOutput = new ZipOutputStream(new FileOutputStream(outputFile));

		// We will walk backwards through the stack.
		ListIterator<String> iterator = stack.listIterator();
		while (iterator.hasPrevious()) {
			File jar = new File(iterator.previous()).getAbsoluteFile();
			copyToZip(blendedOutput, jar, keepManifest);
		}

		blendedOutput.close();
	}

	/**
	 * If true keeps the manifest of the jar.
	 * @return 
	 */
	public boolean isKeepManifest() {
		return keepManifest;
	}

	/**
	 * Set to true to keep the manifest.
	 * @param keepManifest 
	 */
	public void setKeepManifest(boolean keepManifest) {
		this.keepManifest = keepManifest;
	}

	/**
	 * Copies the contents of "from" into "output".
	 * Please be aware that this method is evil and swallows exceptions during
	 * the creation of entries (because of duplicates).
	 * @param output
	 * @param from
	 * @throws IOException
	 */
	public static void copyToZip(ZipOutputStream output, File from, boolean keepManifest) throws IOException {
		ZipFile input = new ZipFile(from);
		Enumeration<? extends ZipEntry> entries = input.entries();
		while (entries.hasMoreElements()) {
			try {
				ZipEntry entry = entries.nextElement();

				if (!keepManifest && entry.getName().equals("META-INF/MANIFEST.MF")) {
					// Continue with the next entry in case it is the manifest.
					continue;
				}

				output.putNextEntry(entry);

				InputStream inputStream = input.getInputStream(entry);
				byte[] buffer = new byte[4096];
				while (inputStream.available() > 0) {
					output.write(buffer, 0, inputStream.read(buffer, 0, buffer.length));
				}
				inputStream.close();
				output.closeEntry();
			} catch (ZipException ex) {
				// Assume that the erro is the warning about a dulicate and ignore it.
				// I know that this is evil...
			}
		}
		input.close();
	}
}

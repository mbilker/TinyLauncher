package us.mbilker.auth;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads, parses, manipulates and saves the .minecraft/options.txt-file.
 */
public class OptionsFile {

	public static final String SEPARATOR = ":";
	public static final String FILENAME = "options.txt";
	List<String> keys = new ArrayList<String>();
	List<String> values = new ArrayList<String>();

	public OptionsFile() {
	}

	/**
	 * Returns the value to the given key. Returns null if it failed.
	 * @param key The key you want.
	 * @return
	 */
	public String getOption(String key) {
		if (keys.contains(key)) {
			return values.get(keys.indexOf(key));
		}

		return null;
	}

	/**
	 * Check if the options.txt was (successfully) read.
	 * @return
	 */
	public boolean isRead() {
		return !keys.isEmpty() && !values.isEmpty();
	}

	/**
	 * Read the contents of the given file.
	 * @param fileOrPath
	 * @throws IOException
	 */
	public void read(String fileOrPath) throws IOException {
		File file = makeFile(fileOrPath);

		keys.clear();
		values.clear();

		BufferedReader reader = new BufferedReader(new FileReader(file));

		String line;
		while ((line = reader.readLine()) != null) {
			String[] keyValue = line.split(SEPARATOR);
			keys.add(keyValue[0]);
			if (keyValue.length > 1) {
				values.add(keyValue[1]);
			} else {
				values.add("");
			}
		}

		reader.close();
	}

	/**
	 * Set the given option with the given value.
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean setOption(String key, String value) {
		if (keys.indexOf(key) >= 0) {
			values.set(keys.indexOf(key), value);
			return true;
		}

		return false;
	}

	/**
	 * Set options from options-pairs. Every pair looks like this: "key:value".
	 * @param options An array of options with key separated from value by a colon.
	 */
	public void setOptions(Iterable<String> options) {
		if (options == null) {
			return;
		}

		for (String option : options) {
			int splitIdx = option.indexOf(":");
			if (splitIdx > 0) { // We don't want not-named options.
				setOption(option.substring(0, splitIdx), option.substring(splitIdx + 1));
			}
		}
	}

	/**
	 * Write to the given file.
	 */
	public void write(String fileOrPath) throws IOException {
		File file = makeFile(fileOrPath);

		BufferedWriter writer = new BufferedWriter(new FileWriter(file));

		for (int idx = 0; idx < keys.size(); idx++) {
			writer.write(keys.get(idx) + SEPARATOR + values.get(idx));
			writer.newLine();
		}

		writer.close();
	}

	private File makeFile(String pathOrFile) {
		File file = new File(pathOrFile);
		if (file.isDirectory()) {
			file = new File(file.getAbsolutePath(), FILENAME);
		}
		file = file.getAbsoluteFile();
		return file;
	}
}

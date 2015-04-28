package us.mbilker.tinylauncher.util;

import java.util.Map;

public class YamlUtil {
	public static int getInt(Map<String, Object> map, String path, int defaultValue) {
		Object val = map.get(path);
		return (val instanceof Integer) ? (Integer) val : defaultValue;
	}

	public static boolean getBoolean(Map<String, Object> map, String path, boolean defaultValue) {
		Object val = map.get(path);
		return (val instanceof Boolean) ? (Boolean) val : defaultValue;
	}

	public static String getString(Map<String, Object> map, String path, String defaultValue) {
		Object val = map.get(path);
		return (val instanceof String) ? (String) val : defaultValue;
	}
	
	public static boolean isString(Map<String, Object> map, String path) {
        Object val = map.get(path);
        return val instanceof String;
    }
}

package us.mbilker.tinylauncher.json;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class JsonFactory {
    public static final Gson GSON;
    
    private static final Logger LOGGER = Logger.getLogger("JsonFactory");

    static {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapterFactory(new EnumAdaptorFactory());
        builder.registerTypeAdapter(Date.class, new DateAdapter());
        builder.registerTypeAdapter(File.class, new FileAdapter());
        builder.enableComplexMapKeySerialization();
        builder.setPrettyPrinting();
        GSON = builder.create();
    }

    public static Version loadVersion(File json) throws JsonSyntaxException, JsonIOException, IOException {
        FileReader reader = new FileReader(json);
        Version v = GSON.fromJson(reader, Version.class);
        reader.close();
        return v;
    }

    public static AssetIndex loadAssetIndex(File json) throws JsonSyntaxException, JsonIOException, IOException {
        FileReader reader = new FileReader(json);
        AssetIndex a = GSON.fromJson(reader, AssetIndex.class);
        reader.close();
        return a;
    }

    public static Library loadLibrary(String libJsonObject) throws JsonSyntaxException, JsonIOException {
        return GSON.fromJson(libJsonObject, Library.class);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> decode(String s) {
        try {
            Map<String, Object> ret;
            JsonObject jso = new JsonParser().parse(s).getAsJsonObject();
            ret = (Map<String, Object>)decodeElement(jso);
            return ret;
        } catch (Exception e) {
        	LOGGER.log(Level.SEVERE, "Error decoding JSON", e);
            return null;
        }
    }

    public static Object decodeElement(JsonElement e) {
        if (e instanceof JsonObject) {
            Map<String, Object> ret = Maps.newLinkedHashMap();
            for (Map.Entry<String, JsonElement> jse : ((JsonObject) e).entrySet()) {
                ret.put(jse.getKey(), decodeElement(jse.getValue()));
            }
            return ret;
        }
        if (e instanceof JsonArray) {
            List<Object> ret = Lists.newArrayList();
            for (JsonElement jse : e.getAsJsonArray()) {
                ret.add(decodeElement(jse));
            }
            return ret;

        }
        return e.getAsString();
    }

    public static String encode(Map<String, Object> m) {
        try {
            return GSON.toJson(m);
        } catch (Exception e) {
        	LOGGER.log(Level.SEVERE, "Error encoding JSON", e);
            return null;
        }
    }

    public static String encodeStrListMap(Map<String, List<String>> m) {
        try {
            return GSON.toJson(m);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error encoding JSON", e);
            return null;
        }
    }

}

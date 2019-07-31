package model.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public abstract class StyleLoader {
    public static Map<String, String> getAllStyles() throws IOException {
        var map = new HashMap<String,String>();
        var folder = new File("rsrc/stylesToLoad");
        var files = folder.listFiles();
        for (File file : files) {
            var text = new String(Files.readAllBytes(file.toPath()));
            map.put(file.getName().replaceAll(".css$", ""), text);
        }
        return map;
    }
}

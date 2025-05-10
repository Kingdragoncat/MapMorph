package net.mythofy.mapMorph;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SchematicLoader {

    public static List<SchematicMetadata> loadSchematics(File mapsFolder) {
        List<SchematicMetadata> schematics = new ArrayList<>();
        if (mapsFolder != null && mapsFolder.exists() && mapsFolder.isDirectory()) {
            File[] files = mapsFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".schem"));
            if (files != null) {
                for (File file : files) {
                    String name = file.getName().substring(0, file.getName().lastIndexOf('.'));
                    schematics.add(new SchematicMetadata(name, file));
                }
            }
        }
        return schematics;
    }
}
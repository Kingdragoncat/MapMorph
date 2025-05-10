package net.mythofy.mapMorph;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for loading schematic files from the maps folder.
 */
public class SchematicLoader {

    /**
     * Loads all schematic files from the specified maps folder.
     * 
     * @param mapsFolder The folder containing map schematic files
     * @return A list of SchematicMetadata objects representing the found schematics
     */
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
    
    /**
     * Metadata class for schematic files.
     */
    public static class SchematicMetadata {
        private final String name;
        private final File file;
        
        /**
         * Creates a new SchematicMetadata.
         * 
         * @param name The name of the schematic (without extension)
         * @param file The schematic file
         */
        public SchematicMetadata(String name, File file) {
            this.name = name;
            this.file = file;
        }
        
        /**
         * Gets the name of the schematic.
         * 
         * @return The schematic name
         */
        public String getName() {
            return name;
        }
        
        /**
         * Gets the schematic file.
         * 
         * @return The schematic file
         */
        public File getFile() {
            return file;
        }
        
        @Override
        public String toString() {
            return name;
        }
    }
}
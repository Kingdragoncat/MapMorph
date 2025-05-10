package net.mythofy.mapMorph;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.List;

/**
 * Validates map configurations to ensure they have all required elements.
 */
public class MapValidator {

    private final MapMorph plugin;

    /**
     * Creates a new map validator.
     * 
     * @param plugin The MapMorph plugin instance
     */
    public MapValidator(MapMorph plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Checks if a map configuration is valid.
     * 
     * @param mapName The name of the map to validate
     * @return true if valid, false otherwise
     */
    public boolean isValid(String mapName) {
        if (mapName == null || mapName.isEmpty()) {
            return false;
        }
        
        // Check if map exists in config
        if (!plugin.getConfig().contains("maps." + mapName)) {
            return false;
        }
        
        // Check if schematic file exists
        File schematicFile = new File(plugin.getMapsFolder(), mapName + ".schem");
        if (!schematicFile.exists() || !schematicFile.isFile()) {
            return false;
        }
        
        // Check if spawn points are set
        ConfigurationSection spawnSection = plugin.getConfig().getConfigurationSection("maps." + mapName + ".spawns");
        if (spawnSection == null) {
            return false;
        }
        
        List<String> spawnKeys = spawnSection.getKeys(false).stream().toList();
        if (spawnKeys.isEmpty()) {
            return false;
        }
        
        // Check that all spawn points reference valid worlds
        for (String key : spawnKeys) {
            String worldName = plugin.getConfig().getString("maps." + mapName + ".spawns." + key + ".world");
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                return false;
            }
        }
        
        // All checks passed
        return true;
    }
    
    /**
     * Gets a human-readable validation message for a map.
     * 
     * @param mapName The name of the map to validate
     * @return A message explaining the validation result
     */
    public String getValidationMessage(String mapName) {
        if (mapName == null || mapName.isEmpty()) {
            return "Map name cannot be empty";
        }
        
        // Check if map exists in config
        if (!plugin.getConfig().contains("maps." + mapName)) {
            return "Map not defined in config";
        }
        
        // Check if schematic file exists
        File schematicFile = new File(plugin.getMapsFolder(), mapName + ".schem");
        if (!schematicFile.exists() || !schematicFile.isFile()) {
            return "Schematic file not found: " + schematicFile.getName();
        }
        
        // Check if spawn points are set
        ConfigurationSection spawnSection = plugin.getConfig().getConfigurationSection("maps." + mapName + ".spawns");
        if (spawnSection == null || spawnSection.getKeys(false).isEmpty()) {
            return "No spawn points defined";
        }
        
        // Check that all spawn points reference valid worlds
        List<String> spawnKeys = spawnSection.getKeys(false).stream().toList();
        for (String key : spawnKeys) {
            String worldName = plugin.getConfig().getString("maps." + mapName + ".spawns." + key + ".world");
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                return "Spawn point #" + key + " references unknown world: " + worldName;
            }
        }
        
        // All checks passed
        return "Map is valid";
    }
}
package com.mapmorph.plugin;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for MapMorph plugin
 */
public class MapMorphUtils {
    
    /**
     * Get a colored message from a configuration path
     *
     * @param config Configuration file
     * @param path Path to message
     * @param defaultMsg Default message if not found
     * @return Formatted message with color codes translated
     */
    public static String getColoredMessage(FileConfiguration config, String path, String defaultMsg) {
        return ChatColor.translateAlternateColorCodes('&', config.getString(path, defaultMsg));
    }
    
    /**
     * Format a message with placeholders
     *
     * @param message The message template
     * @param replacements Pairs of placeholders and their values (placeholder1, value1, placeholder2, value2...)
     * @return The formatted message
     */
    public static String formatMessage(String message, String... replacements) {
        if (replacements.length % 2 != 0) {
            throw new IllegalArgumentException("Replacements must be in pairs");
        }
        
        String result = message;
        for (int i = 0; i < replacements.length; i += 2) {
            result = result.replace(replacements[i], replacements[i + 1]);
        }
        return result;
    }
    
    /**
     * Get schematic files from the data folder
     *
     * @param plugin The MapMorph plugin instance
     * @return List of schematic file names without extension
     */
    public static List<String> getSchematicFiles(MapMorphPlugin plugin) {
        List<String> schematics = new ArrayList<>();
        File schematicsDir = new File(plugin.getDataFolder(), "schematics");
        
        if (schematicsDir.exists() && schematicsDir.isDirectory()) {
            File[] files = schematicsDir.listFiles((dir, name) -> 
                    name.endsWith(".schem") || name.endsWith(".schematic"));
            
            if (files != null) {
                for (File file : files) {
                    String name = file.getName();
                    // Remove extension
                    int lastDot = name.lastIndexOf('.');
                    if (lastDot > 0) {
                        name = name.substring(0, lastDot);
                    }
                    schematics.add(name);
                }
            }
        }
        
        return schematics;
    }
    
    /**
     * Get all team names for a specific map
     *
     * @param config Configuration file
     * @param mapName Map name
     * @return List of team names
     */
    public static List<String> getTeamNames(FileConfiguration config, String mapName) {
        List<String> teams = new ArrayList<>();
        
        String path = "maps." + mapName + ".teams";
        if (config.contains(path)) {
            ConfigurationSection teamsSection = config.getConfigurationSection(path);
            if (teamsSection != null) {
                teams.addAll(teamsSection.getKeys(false));
            }
        }
        
        return teams;
    }
    
    /**
     * Filter a list of options by a partial input
     *
     * @param options List of all options
     * @param partial Partial input to filter by
     * @return Filtered list of options
     */
    public static List<String> filterByStart(List<String> options, String partial) {
        if (partial == null || partial.isEmpty()) {
            return options;
        }
        
        String lowerCasePartial = partial.toLowerCase();
        return options.stream()
                .filter(option -> option.toLowerCase().startsWith(lowerCasePartial))
                .collect(Collectors.toList());
    }
    
    /**
     * Check if a player has permissions for specific map operations
     *
     * @param player Player to check
     * @param mapName Name of the map
     * @param operation Operation type (create, edit, delete)
     * @return True if player has permission
     */
    public static boolean hasMapPermission(Player player, String mapName, String operation) {
        // Check specific map permission
        if (player.hasPermission("mapmorph.map." + mapName + "." + operation)) {
            return true;
        }
        
        // Check general operation permission
        if (player.hasPermission("mapmorph.map." + operation)) {
            return true;
        }
        
        // Check admin permission
        return player.hasPermission("mapmorph.admin");
    }
}

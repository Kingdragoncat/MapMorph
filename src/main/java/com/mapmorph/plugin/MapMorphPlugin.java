package com.mapmorph.plugin;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MapMorphPlugin extends JavaPlugin {
    
    private static MapMorphPlugin instance;
    private ConfigManager configManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Lazy initialize components only when needed
        getLogger().info("MapMorph is starting up...");
        
        // Load config first as it's needed by other components
        configManager = new ConfigManager(this);
        configManager.loadConfig();
        
        // Register only essential listeners initially
        registerEvents();
        
        // Register commands using a more efficient approach
        registerCommands();
        
        // Check for plugin dependencies
        checkDependencies();
        
        getLogger().info("MapMorph has been enabled successfully!");
    }
    
    private void registerEvents() {
        // Register only essential event listeners
        getServer().getPluginManager().registerEvents(new CoreListener(this), this);
    }
    
    private void registerCommands() {
        // Use a single command executor with subcommands for efficiency
        MapMorphCommand commandHandler = new MapMorphCommand(this);
        getCommand("mapmorph").setExecutor(commandHandler);
        getCommand("mapmorph").setTabCompleter(commandHandler);
    }
    
    private void checkDependencies() {
        if (getServer().getPluginManager().getPlugin("WorldEdit") != null) {
            getLogger().info("WorldEdit found! Enabling WorldEdit integration.");
        } else {
            getLogger().warning("WorldEdit not found! Some features may not work properly.");
        }
        
        if (getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            getLogger().info("WorldGuard found! Enabling WorldGuard integration.");
        } else {
            getLogger().warning("WorldGuard not found! Region protection features will be disabled.");
        }
        
        if (getServer().getPluginManager().getPlugin("FastAsyncWorldEdit") != null) {
            getLogger().info("FastAsyncWorldEdit found! Using FAWE for faster operations.");
        }
        
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            getLogger().info("PlaceholderAPI found! Enabling placeholder support.");
        }
    }
    
    @Override
    public void onDisable() {
        // Clean up resources
        if (configManager != null) {
            configManager.saveConfig();
        }
        
        // Release references to help garbage collection
        instance = null;
        configManager = null;
        
        getLogger().info("MapMorph has been disabled.");
    }
    
    public static MapMorphPlugin getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    /**
     * Check if WorldGuard is enabled
     * @return true if WorldGuard is enabled
     */
    public boolean isWorldGuardEnabled() {
        return getServer().getPluginManager().getPlugin("WorldGuard") != null;
    }
    
    /**
     * Get all available regions
     * @param mapName Map name to check
     * @return List of region IDs
     */
    public List<String> getAvailableRegions(String mapName) {
        // Example implementation - in real usage this would query WorldGuard
        List<String> regions = new ArrayList<>();
        
        // Add regions from the config if they exist
        FileConfiguration config = configManager.getConfig();
        if (config.contains("maps." + mapName + ".regions")) {
            if (config.isList("maps." + mapName + ".regions")) {
                regions.addAll(config.getStringList("maps." + mapName + ".regions"));
            }
        }
        
        // Add some example regions for testing tab completion
        regions.add("global");
        regions.add(mapName + "_region");
        regions.add(mapName + "_spawn");
        
        return regions;
    }
    
    /**
     * Get regions associated with a map
     * @param mapName Map name to check
     * @return List of region IDs
     */
    public List<String> getRegionIdsForMap(String mapName) {
        List<String> regions = new ArrayList<>();
        FileConfiguration config = configManager.getConfig();
        
        if (config.contains("maps." + mapName + ".regions")) {
            if (config.isList("maps." + mapName + ".regions")) {
                regions.addAll(config.getStringList("maps." + mapName + ".regions"));
            }
        }
        
        return regions;
    }
    
    /**
     * Get available schematics
     * @return List of schematic names
     */
    public List<String> getAvailableSchematics() {
        List<String> schematics = new ArrayList<>();
        
        // Check the schematics directory if it exists
        File schematicsDir = new File(getDataFolder(), "schematics");
        if (schematicsDir.exists() && schematicsDir.isDirectory()) {
            File[] files = schematicsDir.listFiles((dir, name) ->
                    name.endsWith(".schem") || name.endsWith(".schematic"));
            
            if (files != null) {
                for (File file : files) {
                    String name = file.getName();
                    if (name.endsWith(".schem")) {
                        name = name.substring(0, name.length() - 6);
                    } else if (name.endsWith(".schematic")) {
                        name = name.substring(0, name.length() - 10);
                    }
                    schematics.add(name);
                }
            }
        }
        
        // If no schematics found, add examples for testing
        if (schematics.isEmpty()) {
            schematics.addAll(Arrays.asList(
                "example_arena", 
                "spawn_area", 
                "pvp_pit", 
                "castle"
            ));
        }
        
        return schematics;
    }
    
    /**
     * Get schematics associated with a map
     * @param mapName Map name to check
     * @return List of schematic names
     */
    public List<String> getMapSchematics(String mapName) {
        List<String> schematics = new ArrayList<>();
        FileConfiguration config = configManager.getConfig();
        
        // Check for single schematic
        if (config.contains("maps." + mapName + ".schematic")) {
            schematics.add(config.getString("maps." + mapName + ".schematic"));
        }
        
        // Check for schematic list
        if (config.contains("maps." + mapName + ".schematics")) {
            if (config.isList("maps." + mapName + ".schematics")) {
                schematics.addAll(config.getStringList("maps." + mapName + ".schematics"));
            }
        }
        
        return schematics;
    }
    
    /**
     * Get spawn indices for a map
     * @param mapName Map name to check
     * @return List of spawn indices as integers
     */
    public List<Integer> getSpawnIndices(String mapName) {
        List<Integer> indices = new ArrayList<>();
        FileConfiguration config = configManager.getConfig();
        
        // Get spawn points from config
        String spawnPath = "maps." + mapName + ".spawns";
        if (config.contains(spawnPath)) {
            ConfigurationSection spawnsSection = config.getConfigurationSection(spawnPath);
            if (spawnsSection != null) {
                for (String key : spawnsSection.getKeys(false)) {
                    try {
                        indices.add(Integer.parseInt(key));
                    } catch (NumberFormatException ignored) {
                        // Skip non-numeric keys
                    }
                }
            }
        }
        
        // If no spawns found, add default values for testing
        if (indices.isEmpty()) {
            indices.addAll(Arrays.asList(1, 2));
        }
        
        Collections.sort(indices);
        return indices;
    }
    
    /**
     * Get spawn indices for a map as strings to avoid valueOf ambiguity
     * @param mapName Map name to check
     * @return List of spawn indices as strings
     */
    public List<String> getSpawnIndicesAsStrings(String mapName) {
        List<String> result = new ArrayList<>();
        for (Integer index : getSpawnIndices(mapName)) {
            result.add(index.toString());
        }
        return result;
    }
    
    /**
     * List all available maps
     * @return List of map names
     */
    public List<String> listAllMaps() {
        List<String> mapNames = new ArrayList<>();
        FileConfiguration config = configManager.getConfig();
        
        if (config.contains("maps")) {
            ConfigurationSection mapsSection = config.getConfigurationSection("maps");
            if (mapsSection != null) {
                mapNames.addAll(mapsSection.getKeys(false));
            }
        }
        
        // If no maps found, add examples for testing
        if (mapNames.isEmpty()) {
            mapNames.addAll(Arrays.asList("example_map1", "arena1"));
        }
        
        return mapNames;
    }
}

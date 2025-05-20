package com.mapmorph.plugin;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    
    private final MapMorphPlugin plugin;
    private FileConfiguration config;
    private final Map<String, Object> cachedConfig = new HashMap<>();
    
    public ConfigManager(MapMorphPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void loadConfig() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
        
        // Cache frequently accessed config values to avoid disk reads
        cachedConfig.put("debug-mode", config.getBoolean("debug-mode", false));
        cachedConfig.put("max-maps", config.getInt("max-maps", 10));
        
        plugin.getLogger().info("Configuration loaded");
    }
    
    public void saveConfig() {
        plugin.saveConfig();
    }
    
    public boolean isDebugMode() {
        return (boolean) cachedConfig.getOrDefault("debug-mode", false);
    }
    
    public int getMaxMaps() {
        return (int) cachedConfig.getOrDefault("max-maps", 10);
    }
    
    // Provide access to non-cached config values
    public FileConfiguration getConfig() {
        return config;
    }
}

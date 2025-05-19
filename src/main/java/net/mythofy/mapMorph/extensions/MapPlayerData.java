package net.mythofy.mapMorph.extensions;
    
import net.mythofy.mapMorph.MapMorph;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manages custom player data storage for map-specific stats.
 * This is a premium feature that allows plugins to store and retrieve
 * custom data for players on specific maps.
 */
public class MapPlayerData {

    private final MapMorph plugin;
    private final Map<UUID, Map<String, Map<String, Object>>> playerData = new ConcurrentHashMap<>();
    private boolean dataLoaded = false;
    private BukkitTask autoSaveTask;
    
    /**
     * Creates a new map player data manager.
     * 
     * @param plugin The MapMorph plugin instance
     */
    public MapPlayerData(MapMorph plugin) {
        this.plugin = plugin;
        loadData();
    }

    /**
     * Loads player data from disk.
     */
    private synchronized void loadData() {
        try {
            // Check if stats saving is enabled
            if (!plugin.getConfig().getBoolean("player_data.save-stats", true)) {
                plugin.getLogger().info("Player stats saving is disabled in config");
                dataLoaded = true;
                return;
            }
            
            // Determine storage method from config
            String storageMethod = plugin.getConfig().getString("player_data.storage-method", "yaml").toLowerCase();
            
            // Create player_data directory if it doesn't exist
            File dataFolder = new File(plugin.getDataFolder(), "player_data");
            if (!dataFolder.exists() && !dataFolder.mkdirs()) {
                plugin.getLogger().warning("Failed to create player_data directory");
            }
            
            // Clear existing data before loading
            playerData.clear();
            
            switch (storageMethod) {
                case "yaml":
                    loadFromYaml();
                    break;
                case "mysql":
                    loadFromMySQL();
                    break;
                case "sqlite":
                    loadFromSQLite();
                    break;
                default:
                    plugin.getLogger().warning("Unknown storage method: " + storageMethod + ", defaulting to YAML");
                    loadFromYaml();
                    break;
            }
            
            // Set up auto-save task if enabled
            setupAutoSaveTask();
            
            dataLoaded = true;
            plugin.getLogger().info("Loaded map-specific player data for " + playerData.size() + " players using " + storageMethod);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load player data: " + e.getMessage());
            e.printStackTrace();
            dataLoaded = false;
        }
    }
    
    /**
     * Sets up the auto-save task for player data
     */
    private void setupAutoSaveTask() {
        // Cancel existing task if running
        if (autoSaveTask != null && !autoSaveTask.isCancelled()) {
            autoSaveTask.cancel();
            autoSaveTask = null;
        }
        
        int saveInterval = plugin.getConfig().getInt("player_data.save-interval", 5);
        if (saveInterval > 0) {
            try {
                // Schedule auto-save task (20 ticks * 60 seconds * minutes)
                autoSaveTask = Bukkit.getScheduler().runTaskTimerAsynchronously(
                    plugin, 
                    () -> {
                        try {
                            saveData();
                        } catch (Exception e) {
                            plugin.getLogger().severe("Error in auto-save task: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }, 
                    20L * 60L * saveInterval, 
                    20L * 60L * saveInterval
                );
                plugin.getLogger().info("Scheduled player data auto-save every " + saveInterval + " minutes");
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to set up auto-save task: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Loads player data from YAML configuration
     */
    private void loadFromYaml() {
        File playerDataFile = new File(plugin.getDataFolder(), "player_data/data.yml");
        if (!playerDataFile.exists()) {
            plugin.getLogger().info("Player data file doesn't exist yet, creating a new one");
            return;
        }
        
        try {
            YamlConfiguration dataConfig = YamlConfiguration.loadConfiguration(playerDataFile);
            ConfigurationSection dataSection = dataConfig.getConfigurationSection("player_data");
            
            if (dataSection != null) {
                for (String playerIdStr : dataSection.getKeys(false)) {
                    try {
                        UUID playerId = UUID.fromString(playerIdStr);
                        ConfigurationSection playerSection = dataSection.getConfigurationSection(playerIdStr);
                        if (playerSection != null) {
                            Map<String, Map<String, Object>> mapData = new HashMap<>();
                            
                            for (String mapName : playerSection.getKeys(false)) {
                                ConfigurationSection mapSection = playerSection.getConfigurationSection(mapName);
                                if (mapSection != null) {
                                    Map<String, Object> stats = new HashMap<>();
                                    for (String statKey : mapSection.getKeys(false)) {
                                        stats.put(statKey, mapSection.get(statKey));
                                    }
                                    mapData.put(mapName, stats);
                                }
                            }
                            
                            playerData.put(playerId, mapData);
                        }
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid UUID in player data: " + playerIdStr);
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error loading player data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Loads player data from MySQL database
     */
    private void loadFromMySQL() {
        // MySQL implementation would go here
        // For now, we'll just log that it's not implemented
        plugin.getLogger().warning("MySQL storage not yet implemented, falling back to YAML");
        loadFromYaml();
    }
    
    /**
     * Loads player data from SQLite database
     */
    private void loadFromSQLite() {
        // SQLite implementation would go here
        // For now, we'll just log that it's not implemented
        plugin.getLogger().warning("SQLite storage not yet implemented, falling back to YAML");
        loadFromYaml();
    }
    
    /**
     * Saves player data to disk.
     */
    public synchronized void saveData() {
        if (!dataLoaded) return;
        
        // Determine storage method from config
        String storageMethod = plugin.getConfig().getString("player_data.storage-method", "yaml").toLowerCase();
        
        try {
            switch (storageMethod) {
                case "yaml":
                    saveToYaml();
                    break;
                case "mysql":
                    saveToMySQL();
                    break;
                case "sqlite":
                    saveToSQLite();
                    break;
                default:
                    plugin.getLogger().warning("Unknown storage method: " + storageMethod + ", defaulting to YAML");
                    saveToYaml();
                    break;
            }
            
            plugin.getLogger().info("Saved map-specific player data for " + playerData.size() + " players using " + storageMethod);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to save player data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Saves player data to YAML configuration
     */
    private void saveToYaml() {
        File dataFolder = new File(plugin.getDataFolder(), "player_data");
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            plugin.getLogger().warning("Failed to create player_data directory");
            return;
        }
        
        File playerDataFile = new File(dataFolder, "data.yml");
        YamlConfiguration dataConfig = new YamlConfiguration();
        
        // Create configuration sections for player data
        for (Map.Entry<UUID, Map<String, Map<String, Object>>> playerEntry : playerData.entrySet()) {
            String playerPath = "player_data." + playerEntry.getKey().toString();
            
            for (Map.Entry<String, Map<String, Object>> mapEntry : playerEntry.getValue().entrySet()) {
                String mapPath = playerPath + "." + mapEntry.getKey();
                
                for (Map.Entry<String, Object> statEntry : mapEntry.getValue().entrySet()) {
                    String statPath = mapPath + "." + statEntry.getKey();
                    dataConfig.set(statPath, statEntry.getValue());
                }
            }
        }
        
        try {
            dataConfig.save(playerDataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save player data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Saves player data to MySQL database
     */
    private void saveToMySQL() {
        // MySQL implementation would go here
        // For now, we'll just log that it's not implemented
        plugin.getLogger().warning("MySQL storage not yet implemented, cannot save to MySQL");
    }
    
    /**
     * Saves player data to SQLite database
     */
    private void saveToSQLite() {
        // SQLite implementation would go here
        // For now, we'll just log that it's not implemented
        plugin.getLogger().warning("SQLite storage not yet implemented, cannot save to SQLite");
    }
    
    /**
     * Gets a specific stat for a player on a specific map.
     * 
     * @param playerId The UUID of the player
     * @param mapName The name of the map
     * @param statKey The key of the stat to retrieve
     * @return The value of the stat, or null if not set
     */
    public Object getStat(UUID playerId, String mapName, String statKey) {
        Map<String, Map<String, Object>> playerMaps = playerData.get(playerId);
        if (playerMaps == null) return null;
        
        Map<String, Object> mapStats = playerMaps.get(mapName);
        if (mapStats == null) return null;
        
        return mapStats.get(statKey);
    }
    
    /**
     * Sets a specific stat for a player on a specific map.
     * 
     * @param playerId The UUID of the player
     * @param mapName The name of the map
     * @param statKey The key of the stat to set
     * @param value The value to set
     */
    public void setStat(UUID playerId, String mapName, String statKey, Object value) {
        // Get or create player maps
        Map<String, Map<String, Object>> playerMaps = playerData.computeIfAbsent(playerId, k -> new HashMap<>());
        
        // Get or create map stats
        Map<String, Object> mapStats = playerMaps.computeIfAbsent(mapName, k -> new HashMap<>());
        
        // Set the stat
        mapStats.put(statKey, value);
    }
    
    /**
     * Gets all stats for a player on a specific map.
     * 
     * @param playerId The UUID of the player
     * @param mapName The name of the map
     * @return A map of all stats for the player on the map, or an empty map if none
     */
    public Map<String, Object> getAllStats(UUID playerId, String mapName) {
        Map<String, Map<String, Object>> playerMaps = playerData.get(playerId);
        if (playerMaps == null) return new HashMap<>();
        
        Map<String, Object> mapStats = playerMaps.get(mapName);
        if (mapStats == null) return new HashMap<>();
        
        return new HashMap<>(mapStats);
    }
    
    /**
     * Increments a numeric stat for a player on a specific map.
     * If the stat doesn't exist, it will be created with a value of 1.
     * 
     * @param playerId The UUID of the player
     * @param mapName The name of the map
     * @param statKey The key of the stat to increment
     * @return The new value of the stat
     * @throws IllegalArgumentException if the existing stat is not a number
     */
    public synchronized Number incrementStat(UUID playerId, String mapName, String statKey) {
        Object currentValue = getStat(playerId, mapName, statKey);
        
        if (currentValue == null) {
            setStat(playerId, mapName, statKey, 1);
            return 1;
        }
        
        if (!(currentValue instanceof Number)) {
            throw new IllegalArgumentException("Cannot increment non-numeric stat: " + statKey);
        }
        
        Number number = (Number) currentValue;
        Number newValue;
        
        try {
            if (number instanceof Integer) {
                // Check for integer overflow
                int intValue = (Integer) number;
                if (intValue == Integer.MAX_VALUE) {
                    newValue = intValue + 1L; // Convert to long if at max integer
                } else {
                    newValue = intValue + 1;
                }
            } else if (number instanceof Long) {
                // Check for long overflow
                long longValue = (Long) number;
                if (longValue == Long.MAX_VALUE) {
                    throw new ArithmeticException("Long value too large to increment");
                }
                newValue = longValue + 1L;
            } else if (number instanceof Double) {
                double doubleValue = (Double) number;
                if (Double.isInfinite(doubleValue) || Double.isNaN(doubleValue)) {
                    throw new ArithmeticException("Invalid double value");
                }
                newValue = doubleValue + 1.0;
            } else if (number instanceof Float) {
                float floatValue = (Float) number;
                if (Float.isInfinite(floatValue) || Float.isNaN(floatValue)) {
                    throw new ArithmeticException("Invalid float value");
                }
                newValue = floatValue + 1.0f;
            } else {
                // Default to double for other numeric types
                newValue = number.doubleValue() + 1.0;
            }
            
            setStat(playerId, mapName, statKey, newValue);
            return newValue;
        } catch (ArithmeticException e) {
            plugin.getLogger().warning("Error incrementing stat " + statKey + ": " + e.getMessage());
            throw new IllegalStateException("Failed to increment stat: " + e.getMessage(), e);
        }
    }
    
    /**
     * Resets all stats for a player on a specific map.
     * 
     * @param playerId The UUID of the player
     * @param mapName The name of the map
     */
    public void resetStats(UUID playerId, String mapName) {
        Map<String, Map<String, Object>> playerMaps = playerData.get(playerId);
        if (playerMaps != null) {
            playerMaps.remove(mapName);
        }
    }
    
    /**
     * Gets the top players for a specific stat on a specific map.
     * 
     * @param mapName The name of the map
     * @param statKey The key of the stat to rank
     * @param limit The maximum number of players to return
     * @return A map of player UUIDs to their stat values, sorted by value in descending order
     * @throws IllegalArgumentException if the stat is not comparable
     */
    public Map<UUID, Object> getTopPlayers(String mapName, String statKey, int limit) {
        Map<UUID, Object> result = new HashMap<>();
        
        // First collect all the stats
        for (Map.Entry<UUID, Map<String, Map<String, Object>>> playerEntry : playerData.entrySet()) {
            UUID playerId = playerEntry.getKey();
            Map<String, Map<String, Object>> playerMaps = playerEntry.getValue();
            Map<String, Object> mapStats = playerMaps.get(mapName);
            
            if (mapStats != null && mapStats.containsKey(statKey)) {
                result.put(playerId, mapStats.get(statKey));
            }
        }
        
        // Sort the results by value in descending order
        // This assumes the values are comparable (e.g., numbers)
        return result.entrySet().stream()
                .sorted((a, b) -> {
                    if (a.getValue() instanceof Comparable && b.getValue() instanceof Comparable) {
                        @SuppressWarnings("unchecked")
                        Comparable<Object> aVal = (Comparable<Object>) a.getValue();
                        return -aVal.compareTo(b.getValue()); // Negative for descending order
                    } else {
                        throw new IllegalArgumentException("Stat values must be comparable for ranking");
                    }
                })
                .limit(limit)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }
    
    /**
     * Gets a player's rank for a specific stat on a specific map.
     * 
     * @param playerId The UUID of the player
     * @param mapName The name of the map
     * @param statKey The key of the stat to check
     * @return The player's rank (starting from 1), or 0 if not ranked
     */
    public int getPlayerRank(UUID playerId, String mapName, String statKey) {
        // Get all players with this stat on this map
        Map<UUID, Object> topPlayers = getTopPlayers(mapName, statKey, Integer.MAX_VALUE);
        
        // Find the player's position
        int rank = 1;
        for (UUID id : topPlayers.keySet()) {
            if (id.equals(playerId)) {
                return rank;
            }
            rank++;
        }
        
        return 0; // Not ranked
    }
    
    /**
     * Gets a formatted leaderboard for a specific stat on a specific map.
     * 
     * @param mapName The name of the map
     * @param statKey The key of the stat to rank
     * @param limit The maximum number of players to include
     * @return A list of formatted leaderboard entries
     */
    public List<String> getLeaderboard(String mapName, String statKey, int limit) {
        Map<UUID, Object> topPlayers = getTopPlayers(mapName, statKey, limit);
        List<String> leaderboard = new ArrayList<>();
        
        int rank = 1;
        for (Map.Entry<UUID, Object> entry : topPlayers.entrySet()) {
            UUID playerId = entry.getKey();
            Object value = entry.getValue();
            
            // Try to get player name
            String playerName = Bukkit.getOfflinePlayer(playerId).getName();
            if (playerName == null) {
                playerName = playerId.toString().substring(0, 8) + "...";
            }
            
            leaderboard.add(String.format("#%d: %s - %s", rank, playerName, value.toString()));
            rank++;
        }
        
        return leaderboard;
    }
    
    /**
     * Cleans up resources and saves data when the plugin is being disabled.
     * This method should be called from the plugin's onDisable method.
     */
    public void onDisable() {
        // Cancel auto-save task if it exists
        if (autoSaveTask != null) {
            autoSaveTask.cancel();
            autoSaveTask = null;
        }
        
        // Save all player data
        saveData();
        
        plugin.getLogger().info("MapPlayerData has been properly shut down");
    }
}

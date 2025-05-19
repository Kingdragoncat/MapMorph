package net.mythofy.mapMorph.api;

import net.mythofy.mapMorph.MapMorph;
import net.mythofy.mapMorph.extensions.EconomyHook;
import net.mythofy.mapMorph.extensions.MapPlayerData;
import net.mythofy.mapMorph.extensions.PluginIntegrationManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * API for interacting with the MapMorph plugin.
 * This class provides a clean interface for other plugins to use MapMorph functionality.
 */
public class MapMorphAPI {
    
    private static MapMorph plugin;
    private static final Random random = new Random();
    private static MapPlayerData playerDataManager;
    private static EconomyHook economyHook;
    private static PluginIntegrationManager integrationManager;
    
    /**
     * Initializes the API with the plugin instance.
     * This should be called by MapMorph during onEnable.
     * 
     * @param pluginInstance The MapMorph plugin instance
     */
    public static void init(MapMorph pluginInstance) {
        plugin = pluginInstance;
        playerDataManager = pluginInstance.getPlayerDataManager();
        economyHook = new EconomyHook(plugin);
        integrationManager = new PluginIntegrationManager(plugin);
    }
    
    /**
     * Switches to a different map.
     * 
     * @param mapName The name of the map to switch to
     * @return A future that completes when the map switch is done
     */
    public static CompletableFuture<Boolean> switchMap(String mapName) {
        return switchMap(mapName, 0);
    }
    
    /**
     * Switches to a different map with a countdown.
     * 
     * @param mapName The name of the map to switch to
     * @param countdown Seconds to count down before switching
     * @return A future that completes when the map switch is done
     */
    public static CompletableFuture<Boolean> switchMap(String mapName, int countdown) {
        if (plugin == null) {
            throw new IllegalStateException("MapMorphAPI not initialized");
        }
        
        // Create a CompletableFuture to track the async operation
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        // Implementation would load the schematic and apply region changes
        // This is a placeholder implementation
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            plugin.recordMapSwap(mapName);
            future.complete(true);
        }, countdown * 20L);
        
        return future;
    }
    
    /**
     * Gets the currently active map.
     * 
     * @return The current map name, or null if none is set
     */
    public static String getCurrentMap() {
        if (plugin == null) {
            throw new IllegalStateException("MapMorphAPI not initialized");
        }
        return plugin.getCurrentMap();
    }
    
    /**
     * Lists all available maps.
     * 
     * @return A list of all map names
     */
    public static List<String> listAllMaps() {
        if (plugin == null) {
            throw new IllegalStateException("MapMorphAPI not initialized");
        }
        return plugin.listAllMaps();
    }
    
    /**
     * Teleports a player to a random spawn point for the current map.
     * 
     * @param player The player to teleport
     * @return True if successful, false if no valid spawn points exist
     */
    public static boolean teleportPlayerToSpawn(Player player) {
        if (plugin == null || player == null) {
            return false;
        }
        
        String currentMap = plugin.getCurrentMap();
        if (currentMap == null) {
            return false;
        }
        
        List<Location> spawns = plugin.getSpawnLocationsForMap(currentMap);
        if (spawns.isEmpty()) {
            return false;
        }
        
        // Pick a random spawn point
        Location spawnLoc = spawns.get(random.nextInt(spawns.size()));
        player.teleport(spawnLoc);
        return true;
    }
    
    /**
     * Teleports all online players to spawn points for the current map.
     * 
     * @return The number of players teleported
     */
    public static int teleportAllPlayersToSpawn() {
        if (plugin == null) {
            return 0;
        }
        
        int count = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (teleportPlayerToSpawn(player)) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Registers a callback to be notified when maps change.
     * 
     * @param callback The callback to execute when a map changes
     */
    public static void registerMapChangeCallback(MapChangeCallback callback) {
        if (plugin == null) {
            throw new IllegalStateException("MapMorphAPI not initialized");
        }
        plugin.getMapChangeListener().registerCallback((net.mythofy.mapMorph.api.MapChangeCallback) callback);
    }
    
    /**
     * Unregisters a previously registered map change callback.
     * 
     * @param callback The callback to remove
     */
    public static void unregisterMapChangeCallback(MapChangeCallback callback) {
        if (plugin == null) {
            return;
        }
        plugin.getMapChangeListener().unregisterCallback((net.mythofy.mapMorph.api.MapChangeCallback) callback);
    }
    
    /**
     * Gets the folder where map schematic files are stored.
     * 
     * @return The maps folder
     */
    public static File getMapsFolder() {
        if (plugin == null) {
            throw new IllegalStateException("MapMorphAPI not initialized");
        }
        return plugin.getMapsFolder();
    }
    
    /**
     * Gets the player data manager for map-specific stats.
     * 
     * @return The player data manager
     * @throws IllegalStateException if the API is not initialized
     */
    public static MapPlayerData getPlayerDataManager() {
        if (plugin == null) {
            throw new IllegalStateException("MapMorphAPI not initialized");
        }
        return playerDataManager;
    }
    
    /**
     * Gets a specific stat for a player on a specific map.
     * 
     * @param player The player to get stats for
     * @param mapName The map name
     * @param statKey The key of the stat to retrieve
     * @return The value of the stat, or null if not set
     */
    public static Object getPlayerMapStat(Player player, String mapName, String statKey) {
        if (playerDataManager == null) {
            throw new IllegalStateException("MapMorphAPI not initialized");
        }
        return playerDataManager.getStat(player.getUniqueId(), mapName, statKey);
    }
    
    /**
     * Sets a specific stat for a player on a specific map.
     * 
     * @param player The player to set stats for
     * @param mapName The map name
     * @param statKey The key of the stat to set
     * @param value The value to set
     */
    public static void setPlayerMapStat(Player player, String mapName, String statKey, Object value) {
        if (playerDataManager == null) {
            throw new IllegalStateException("MapMorphAPI not initialized");
        }
        playerDataManager.setStat(player.getUniqueId(), mapName, statKey, value);
    }
    
    /**
     * Gets all stats for a player on a specific map.
     * 
     * @param player The player to get stats for
     * @param mapName The map name
     * @return A map of all stats for the player on the map
     */
    public static Map<String, Object> getAllPlayerMapStats(Player player, String mapName) {
        if (playerDataManager == null) {
            throw new IllegalStateException("MapMorphAPI not initialized");
        }
        return playerDataManager.getAllStats(player.getUniqueId(), mapName);
    }
    
    /**
     * Awards currency to a player for a map-specific action.
     * Requires Vault or a compatible economy plugin.
     * 
     * @param player The player to award currency to
     * @param amount The amount to award
     * @param reason A description of why the currency was awarded
     * @return True if successful, false if economy is not available
     */
    public static boolean awardMapCurrency(Player player, double amount, String reason) {
        if (economyHook == null) {
            throw new IllegalStateException("MapMorphAPI not initialized");
        }
        return economyHook.awardCurrency(player, amount, reason);
    }
    
    /**
     * Registers a map-specific reward for an action.
     * 
     * @param mapName The map name to associate the reward with
     * @param actionName The action that triggers this reward
     * @param amount The amount to reward
     */
    public static void registerMapReward(String mapName, String actionName, double amount) {
        if (economyHook == null) {
            throw new IllegalStateException("MapMorphAPI not initialized");
        }
        economyHook.registerReward(mapName, actionName, amount);
    }
    
    /**
     * Triggers a registered reward for a player.
     * 
     * @param player The player to reward
     * @param mapName The map name
     * @param actionName The action name
     * @return True if the reward was found and applied, false otherwise
     */
    public static boolean triggerMapReward(Player player, String mapName, String actionName) {
        if (economyHook == null) {
            throw new IllegalStateException("MapMorphAPI not initialized");
        }
        return economyHook.triggerReward(player, mapName, actionName);
    }

    /**
     * Registers a plugin as an integration with MapMorph.
     * This allows the plugin to receive extra notifications and data.
     * 
     * @param plugin The plugin to register
     * @param integrationName A unique name for this integration
     * @return True if registered successfully, false otherwise
     */
    public static boolean registerPluginIntegration(Plugin plugin, String integrationName) {
        if (integrationManager == null) {
            throw new IllegalStateException("MapMorphAPI not initialized");
        }
        return integrationManager.registerIntegration(plugin, integrationName);
    }
    
    /**
     * Checks if extended features are enabled.
     * 
     * @return True if extended features are enabled, false otherwise
     */
    public static boolean areExtensionsEnabled() {
        return plugin != null && playerDataManager != null && economyHook != null && integrationManager != null;
    }
    
    /**
     * Interface for map change callbacks.
     */
    public interface MapChangeCallback {
        /**
         * Called when a map is changed.
         * 
         * @param mapName The name of the new map
         * @param previousMap The name of the previous map, or null if none
         */
        void onMapChange(String mapName, String previousMap);
    }
}
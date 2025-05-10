package net.mythofy.mapMorph.api;

import net.mythofy.mapMorph.MapMorph;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

/**
 * API for interacting with the MapMorph plugin.
 * This class provides a clean interface for other plugins to use MapMorph functionality.
 */
public class MapMorphAPI {
    
    private static MapMorph plugin;
    private static final Random random = new Random();
    
    /**
     * Initializes the API with the plugin instance.
     * This should be called by MapMorph during onEnable.
     * 
     * @param pluginInstance The MapMorph plugin instance
     */
    public static void init(MapMorph pluginInstance) {
        plugin = pluginInstance;
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
        plugin.getMapChangeListener().registerCallback(callback);
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
        plugin.getMapChangeListener().unregisterCallback(callback);
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
}
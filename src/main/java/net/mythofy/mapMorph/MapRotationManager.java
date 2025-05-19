package net.mythofy.mapMorph;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages the map rotation system.
 */
public class MapRotationManager {

    private final Plugin plugin;
    private List<String> rotationMaps;
    private int intervalMinutes;
    private final AtomicInteger currentIndex = new AtomicInteger(0);
    private BukkitTask rotationTask;
    private final Random random = new Random();
    
    /**
     * Creates a new map rotation manager.
     *
     * @param plugin The plugin instance
     */
    public MapRotationManager(Plugin plugin) {
        this.plugin = plugin;
        reloadConfig();
    }
    
    /**
     * Reloads the rotation settings from config.
     */
    public void reloadConfig() {
        FileConfiguration config = plugin.getConfig();
        this.rotationMaps = config.getStringList("rotation.maps");
        this.intervalMinutes = config.getInt("rotation.interval_minutes", 30);
        
        // Check whether to reset the index
        boolean saveState = config.getBoolean("rotation.save-rotation-state", true);
        if (!saveState) {
            this.currentIndex.set(0);
        }
        
        // Stop any existing task
        if (rotationTask != null) {
            rotationTask.cancel();
            rotationTask = null;
        }
        
        // Start rotation task if interval is configured
        if (intervalMinutes > 0) {
            setupRotationTask();
        }
    }
    
    /**
     * Sets up the scheduled task for map rotation.
     */
    private void setupRotationTask() {
        long tickInterval = intervalMinutes * 60L * 20L; // Convert minutes to ticks
        rotationTask = Bukkit.getScheduler().runTaskTimer(plugin, this::rotateMap, tickInterval, tickInterval);
        plugin.getLogger().info("Map rotation scheduled every " + intervalMinutes + " minutes");
    }
    
    /**
     * Performs the map rotation.
     */
    private void rotateMap() {
        // Check if we should rotate when server is empty
        boolean rotateWhenEmpty = plugin.getConfig().getBoolean("rotation.rotate-when-empty", false);
        int minPlayers = plugin.getConfig().getInt("rotation.min-players", 1);
        
        if (!rotateWhenEmpty && Bukkit.getOnlinePlayers().size() < minPlayers) {
            plugin.getLogger().info("Skipping map rotation because server doesn't meet minimum player requirement");
            return;
        }
        
        String nextMap = getNextMap();
        if (nextMap == null) {
            plugin.getLogger().warning("Failed to get next map for rotation");
            return;
        }
        
        // Here you would implement the actual map change logic
        plugin.getLogger().info("Rotating to map: " + nextMap);
        
        // Example of how this might work with a map swap command:
        // Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mapmorph load " + nextMap);
    }
    
    /**
     * Gets the next map in the rotation based on the configured mode.
     *
     * @return The next map name, or null if no valid maps
     */
    public String getNextMap() {
        if (rotationMaps.isEmpty()) {
            return null;
        }
        
        String mode = "sequential"; // Default mode
        
        // Get rotation mode from plugin
        if (plugin instanceof MapMorph) {
            mode = ((MapMorph) plugin).getRotationMode();
        } else {
            mode = plugin.getConfig().getString("rotation.mode", "sequential");
        }
        
        switch (mode.toLowerCase()) {
            case "random":
                return getRandomMap();
            case "sequential":
                return getSequentialMap();
            case "voting":
                // Would need a voting implementation
                return getSequentialMap(); // Fall back to sequential
            default:
                plugin.getLogger().warning("Unknown rotation mode: " + mode + ", falling back to sequential");
                return getSequentialMap();
        }
    }
    
    /**
     * Gets a random map from the rotation list.
     *
     * @return A random map name
     */
    private String getRandomMap() {
        boolean skipCurrent = plugin.getConfig().getBoolean("voting.include-current", false);
        String currentMap = null;
        
        // Get current map from plugin if it's MapMorph
        if (plugin instanceof MapMorph) {
            currentMap = ((MapMorph) plugin).getCurrentMap();
        }
        
        // Create a filtered list if we need to skip the current map
        List<String> availableMaps = new ArrayList<>(rotationMaps);
        if (skipCurrent && currentMap != null) {
            availableMaps.remove(currentMap);
        }
        
        if (availableMaps.isEmpty()) {
            return null;
        }
        
        return availableMaps.get(random.nextInt(availableMaps.size()));
    }
    
    /**
     * Gets the next map in sequential order.
     *
     * @return The next map name
     */
    private String getSequentialMap() {
        if (rotationMaps.isEmpty()) {
            return null;
        }
        
        // Get and increment index, wrapping around if needed
        int index = currentIndex.getAndIncrement();
        if (index >= rotationMaps.size()) {
            index = 0;
            currentIndex.set(1); // Set to 1 since we're using index 0
        }
        
        // Make sure we don't exceed bounds (safeguard)
        index = Math.min(index, rotationMaps.size() - 1);
        
        return rotationMaps.get(index);
    }

    /**
     * Starts the rotation task.
     */
    public void startRotation() {
        if (rotationTask != null && !rotationTask.isCancelled()) {
            rotationTask.cancel();
        }
        if (rotationMaps == null || rotationMaps.isEmpty()) {
            plugin.getLogger().warning("No maps set for rotation!");
            return;
        }
        rotationTask = Bukkit.getScheduler().runTaskTimer(plugin, this::rotateMap, 0L, intervalMinutes * 60L * 20L);
        plugin.getLogger().info("Map rotation started. Interval: " + intervalMinutes + " minutes.");
    }

    /**
     * Stops the rotation task.
     */
    public void stopRotation() {
        if (rotationTask != null) {
            rotationTask.cancel();
            rotationTask = null;
            plugin.getLogger().info("Map rotation stopped.");
        }
    }

    /**
     * Gets the current rotation index.
     *
     * @return The current index
     */
    public int getCurrentIndex() {
        return currentIndex.get();
    }
    
    /**
     * Sets the current rotation index.
     *
     * @param index The index to set
     */
    public void setCurrentIndex(int index) {
        currentIndex.set(index);
    }
    
    /**
     * Gets the list of maps in the rotation.
     *
     * @return The rotation maps
     */
    public List<String> getRotationMaps() {
        return new ArrayList<>(rotationMaps);
    }
    
    /**
     * Gets the rotation interval in minutes.
     *
     * @return The interval
     */
    public int getIntervalMinutes() {
        return intervalMinutes;
    }

    /**
     * Checks if the rotation task is running.
     *
     * @return true if running, false otherwise
     */
    public boolean isRunning() {
        return rotationTask != null && !rotationTask.isCancelled();
    }
}
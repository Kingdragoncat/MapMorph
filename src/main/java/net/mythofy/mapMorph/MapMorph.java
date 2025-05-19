package net.mythofy.mapMorph;

import net.mythofy.mapMorph.api.MapChangeEvent;
import net.mythofy.mapMorph.api.MapMorphAPI;
import net.mythofy.mapMorph.extensions.MapPlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Supplier;

// Initialize the static API
public final class MapMorph extends JavaPlugin {

    private File mapsFolder;
    private final Deque<String> mapHistory = new ArrayDeque<>();
    private String currentMap = null;
    private final MapChangeListener mapChangeListener = new MapChangeListener();

    @Override
    public void onEnable() {
        // Save the default configuration
        saveDefaultConfig();
        
        // Initialize the API with this plugin instance
        try {
            // Try to initialize the API (static method)
            MapMorphAPI.init(this);
            getLogger().info("MapMorph API successfully initialized");
        } catch (Exception e) {
            getLogger().severe("Failed to initialize MapMorph API: " + e.getMessage());
        }
        
        // Register /mapmorph command
        if (this.getCommand("mapmorph") != null) {
            this.getCommand("mapmorph").setExecutor(new MapMorphCommand(this));
        } else {
            getLogger().severe("Command 'mapmorph' not found in plugin.yml!");
        }
    
        // Load prefix from config
        String configPrefix = getConfig().getString("general.prefix", "&8[&bMapMorph&8] &r");
        // You can use this prefix in messages throughout the plugin
        
        // Create a player_data folder
        File playerDataFolder = new File(getDataFolder(), "player_data");
        if (!playerDataFolder.exists()) {
            if (playerDataFolder.mkdirs()) {
                getLogger().info("Created player_data folder at: " + playerDataFolder.getAbsolutePath());
            } else {
                getLogger().warning("Failed to create player_data folder at: " + playerDataFolder.getAbsolutePath());
            }
        }

        // Ensure /maps/ folder exists
        mapsFolder = new File(getDataFolder(), "maps");
        if (!mapsFolder.exists()) {
            if (mapsFolder.mkdirs()) {
                getLogger().info("Created maps folder at: " + mapsFolder.getAbsolutePath());
            } else {
                getLogger().warning("Failed to create maps folder at: " + mapsFolder.getAbsolutePath());
            }
        }

        // Display ASCII logo in console
        printASCIILogo();
        
        // Set up PlaceholderAPI extension if available
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            getLogger().info("PlaceholderAPI found, registering expansion...");
            try {
                MapMorphExpansion expansion = new MapMorphExpansion(this);
                if (expansion.register()) {
                    getLogger().info("Successfully registered PlaceholderAPI expansion");
                } else {
                    getLogger().warning("Failed to register PlaceholderAPI expansion");
                }
            } catch (Exception e) {
                getLogger().warning("Error registering PlaceholderAPI expansion: " + e.getMessage());
            }
        } else {
            getLogger().info("PlaceholderAPI not found, skipping expansion registration");
        }

        // Set up bStats metrics
        setupMetrics();
        
        getLogger().info("MapMorph enabled with API support! Version " + getDescription().getVersion());
    }
    
    /**
     * Sets up bStats metrics collection
     */
    private void setupMetrics() {
        try {
            // bStats metrics (https://bstats.org/plugin/bukkit/MapMorph)
            int pluginId = 20875; // Replace with your actual plugin ID from bStats
            org.bstats.bukkit.Metrics metrics = new org.bstats.bukkit.Metrics(this, pluginId);
            
            // Add custom charts using the simplest approach compatible with all versions
            metrics.addCustomChart(new org.bstats.charts.SimplePie("map_count", () -> {
                int mapCount = listAllMaps().size();
                return mapCount == 0 ? "None" : 
                       mapCount < 5 ? "1-4" :
                       mapCount < 10 ? "5-9" : 
                       mapCount < 20 ? "10-19" : "20+";
            }));
            
            // Add current rotation mode
            metrics.addCustomChart(new org.bstats.charts.SimplePie("rotation_mode", this::getRotationMode));
            
            getLogger().info("bStats metrics enabled - Thank you for using MapMorph!");
        } catch (Exception e) {
            getLogger().warning("Failed to enable bStats metrics: " + e.getMessage());
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("MapMorph disabled!");
    }
    
    public File getMapsFolder() {
        return mapsFolder;
    }
    
    /**
     * Gets the MapChangeListener to register and unregister callbacks.
     * 
     * @return The MapChangeListener instance
     */
    public MapChangeListener getMapChangeListener() {
        return mapChangeListener;
    }
    
    /**
     * Loads all spawn locations for a given map from config.
     */
    public List<Location> getSpawnLocationsForMap(String mapName) {
        List<Location> spawns = new ArrayList<>();
        ConfigurationSection section = getConfig().getConfigurationSection("maps." + mapName + ".spawns");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                String path = "maps." + mapName + ".spawns." + key;
                String world = getConfig().getString(path + ".world");
                double x = getConfig().getDouble(path + ".x");
                double y = getConfig().getDouble(path + ".y");
                double z = getConfig().getDouble(path + ".z");
                float yaw = (float) getConfig().getDouble(path + ".yaw");
                float pitch = (float) getConfig().getDouble(path + ".pitch");
                Location loc = new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
                spawns.add(loc);
            }
        }
        return spawns;
    }
    
    /**
     * Loads all WorldGuard region IDs for a given map from config.
     */
    public List<String> getRegionIdsForMap(String mapName) {
        return getConfig().getStringList("maps." + mapName + ".regions");
    }
    
    /**
     * Lists all map names from the config.
     * @return A list of all map names
     */
    public List<String> listAllMaps() {
        ConfigurationSection mapsSection = getConfig().getConfigurationSection("maps");
        if (mapsSection == null) return new ArrayList<>();
        return new ArrayList<>(mapsSection.getKeys(false));
    }
    
    /**
     * Gets the rotation mode from config: "sequential", "random", or "voting".
     * Defaults to "sequential" if not set.
     */
    public String getRotationMode() {
        return getConfig().getString("rotation.mode", "sequential").toLowerCase();
    }
    
    /**
     * Records a map swap. Call this whenever a new map is loaded/swapped to.
     * The previous map is pushed onto the history stack.
     * @param newMap The name of the new map being loaded.
     */
    public void recordMapSwap(String newMap) {
        if (currentMap != null && !currentMap.equals(newMap)) {
            mapHistory.push(currentMap);
        }
        currentMap = newMap;
        
        // Notify registered callbacks
        mapChangeListener.notifyCallbacks(newMap);
    }

    /**
     * Rolls back to the previous map, if available.
     * Returns the previous map name, or null if none.
     * You should call your map swap logic with the returned map name.
     */
    public String rollbackToPreviousMap() {
        if (mapHistory.isEmpty()) {
            getLogger().warning("No previous map to rollback to!");
            return null;
        }
        String previousMap = mapHistory.pop();
        currentMap = previousMap;
        getLogger().info("Rolled back to previous map: " + previousMap);
        
        // Fire event and notify callbacks about the rollback
        Bukkit.getPluginManager().callEvent(new MapChangeEvent(previousMap));
        mapChangeListener.notifyCallbacks(previousMap);
        
        return previousMap;
    }

    /**
     * Gets the current map name.
     * @return The current map, or null if none is set.
     */
    public String getCurrentMap() {
        return currentMap;
    }
    
    /**
     * Prints the MapMorph ASCII logo to the console
     */
    private void printASCIILogo() {
        String[] logo = {
            "\n _____ ______   ________  ________        _____ ______   ________  ________  ________  ___  ___         ",
            "|\\   _ \\  _   \\|\\   __  \\|\\   __  \\      |\\   _ \\  _   \\|\\   __  \\|\\   __  \\|\\   __  \\|\\  \\|\\  \\        ",
            "\\ \\  \\\\\\__\\ \\  \\ \\  \\|\\  \\ \\  \\|\\  \\     \\ \\  \\\\\\__\\ \\  \\ \\  \\|\\  \\ \\  \\|\\  \\ \\  \\ \\  \\|\\  \\ \\  \\\\\\  \\       ",
            " \\ \\  \\\\|__| \\  \\ \\   __  \\ \\   ____\\     \\ \\  \\\\|__| \\  \\ \\  \\\\\\  \\ \\   _  _\\ \\   ____\\ \\   __  \\      ",
            "  \\ \\  \\    \\ \\  \\ \\  \\ \\  \\ \\  \\___|      \\ \\  \\    \\ \\  \\ \\  \\\\\\  \\ \\  \\\\  \\ \\  \\___|\\n \\ \\  \\ \\  \\     ",
            "   \\ \\__\\    \\ \\__\\ \\__\\ \\__\\ \\__\\          \\ \\__\\    \\ \\__\\ \\_______\\ \\__\\\\ _\\\\ \\__\\    \\ \\__\\ \\__\\    ",
            "    \\|__|     \\|__|\\|__|\\|__|\\|__|           \\|__|     \\|__|\\|_______|\\|__|\\|__|\\|__|     \\|__|\\|__|    ",
            ""
        };
        
        // Print each line of the ASCII logo
        for (String line : logo) {
            getLogger().info("\u00A7b" + line); // Aqua color in console
        }
        
        getLogger().info("\u00A7aMapMorph " + getDescription().getVersion() + " enabled!");
    }

    public MapPlayerData getPlayerDataManager() {
        return null;
    }
}

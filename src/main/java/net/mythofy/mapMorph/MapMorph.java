package net.mythofy.mapMorph;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class MapMorph extends JavaPlugin {

    private File mapsFolder;
    private final Deque<String> mapHistory = new ArrayDeque<>();
    private String currentMap = null;

    @Override
    public void onEnable() {
        // Register /mapmorph command
        if (this.getCommand("mapmorph") != null) {
            this.getCommand("mapmorph").setExecutor(new MapMorphCommand(this));
        } else {
            getLogger().severe("Command 'mapmorph' not found in plugin.yml!");
        }

        // Ensure config file exists
        saveDefaultConfig();

        // Ensure /maps/ folder exists
        mapsFolder = new File(getDataFolder(), "maps");
        if (!mapsFolder.exists()) {
            if (mapsFolder.mkdirs()) {
                getLogger().info("Created maps folder at: " + mapsFolder.getAbsolutePath());
            } else {
                getLogger().warning("Failed to create maps folder at: " + mapsFolder.getAbsolutePath());
            }
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
        return previousMap;
    }

    /**
     * Gets the current map name.
     * @return The current map, or null if none is set.
     */
    public String getCurrentMap() {
        return currentMap;
    }
}

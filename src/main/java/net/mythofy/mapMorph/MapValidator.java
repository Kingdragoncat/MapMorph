package net.mythofy.mapMorph;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

public class MapValidator {

    private final JavaPlugin plugin;

    public MapValidator(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Checks if the map has at least one spawn and one region.
     */
    public boolean isValid(String mapName) {
        ConfigurationSection mapSection = plugin.getConfig().getConfigurationSection("maps." + mapName);
        if (mapSection == null) return false;

        ConfigurationSection spawns = mapSection.getConfigurationSection("spawns");
        boolean hasSpawn = spawns != null && !spawns.getKeys(false).isEmpty();

        java.util.List<String> regions = plugin.getConfig().getStringList("maps." + mapName + ".regions");
        boolean hasRegion = regions != null && !regions.isEmpty();

        return hasSpawn && hasRegion;
    }

    /**
     * Returns a message describing what is missing.
     */
    public String getValidationMessage(String mapName) {
        ConfigurationSection mapSection = plugin.getConfig().getConfigurationSection("maps." + mapName);
        if (mapSection == null) return "Map '" + mapName + "' does not exist in config.";

        StringBuilder sb = new StringBuilder();
        ConfigurationSection spawns = mapSection.getConfigurationSection("spawns");
        if (spawns == null || spawns.getKeys(false).isEmpty()) {
            sb.append("No spawn points set. ");
        }
        java.util.List<String> regions = plugin.getConfig().getStringList("maps." + mapName + ".regions");
        if (regions == null || regions.isEmpty()) {
            sb.append("No regions linked.");
        }
        return sb.length() == 0 ? "Map '" + mapName + "' is valid." : sb.toString().trim();
    }
}
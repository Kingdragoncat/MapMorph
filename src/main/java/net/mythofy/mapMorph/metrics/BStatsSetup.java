package net.mythofy.mapMorph.metrics;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Utility class to handle bStats metrics setup
 */
public class BStatsSetup {

    /**
     * Sets up bStats metrics for the plugin
     * @param plugin The plugin instance
     * @param pluginId The bStats plugin ID
     */
    public static void setupMetrics(JavaPlugin plugin, int pluginId) {
        try {
            // Attempt to load and use the relocated bStats class
            plugin.getLogger().info("Setting up bStats metrics...");
            
            // Use reflection to handle potential version mismatches between the code and the shaded library
            Class<?> metricsClass;
            try {
                // Try the relocated package first (this should be the one we want)
                metricsClass = Class.forName("net.mythofy.mapmorph.libs.bstats.bukkit.Metrics");
                
                // Use a constructor that works with bStats 2.x or 3.x
                try {
                    // Version 3.x constructor
                    Object metrics = metricsClass.getConstructor(JavaPlugin.class, int.class)
                                            .newInstance(plugin, pluginId);
                    plugin.getLogger().info("bStats 3.x metrics registered successfully with ID: " + pluginId);
                } catch (NoSuchMethodException e) {
                    // Try version 2.x constructor
                    Object metrics = metricsClass.getDeclaredConstructors()[0].newInstance(plugin, pluginId);
                    plugin.getLogger().info("bStats 2.x metrics registered successfully with ID: " + pluginId);
                }
            } catch (ClassNotFoundException e) {
                // Fall back to the standard package if relocating didn't work
                plugin.getLogger().warning("Relocated bStats class not found, trying standard package");
                metricsClass = Class.forName("org.bstats.bukkit.Metrics");
                Object metrics = metricsClass.getConstructor(JavaPlugin.class, int.class)
                                        .newInstance(plugin, pluginId);
                plugin.getLogger().info("bStats metrics (standard package) registered successfully with ID: " + pluginId);
            }
        } catch (ClassNotFoundException e) {
            plugin.getLogger().warning("bStats library not found. Metrics will not be collected.");
        } catch (NoSuchMethodException e) {
            plugin.getLogger().warning("bStats constructor not compatible. Metrics will not be collected: " + e.getMessage());
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to register bStats metrics: " + e.getMessage());
        }
    }
}

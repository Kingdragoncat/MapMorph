package net.mythofy.mapMorph.extensions;

import net.mythofy.mapMorph.MapMorph;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Manages integrations with other plugins.
 * This allows other plugins to hook into MapMorph features
 * and receive notifications about map changes and events.
 */
public class PluginIntegrationManager implements Listener {
    
    private final MapMorph plugin;
    private final Logger logger;
    private final Map<String, Plugin> registeredPlugins = new HashMap<>();
    private final Map<UUID, Map<String, Object>> integrationData = new HashMap<>();
    
    /**
     * Creates a new integration manager.
     * 
     * @param plugin The MapMorph plugin instance
     */
    public PluginIntegrationManager(MapMorph plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        loadConfiguration();
    }
    
    /**
     * Loads any saved configuration for integrations.
     */
    private void loadConfiguration() {
        ConfigurationSection integrationSection = plugin.getConfig().getConfigurationSection("integrations");
        if (integrationSection != null) {
            for (String integrationName : integrationSection.getKeys(false)) {
                logger.info("Loading integration configuration for: " + integrationName);
                // Load any integration-specific config here
            }
        }
    }
    
    /**
     * Registers a plugin as an integration with MapMorph.
     * 
     * @param pluginInstance The plugin to register
     * @param integrationName A unique name for this integration
     * @return true if registered successfully
     */
    public boolean registerIntegration(Plugin pluginInstance, String integrationName) {
        if (registeredPlugins.containsKey(integrationName)) {
            logger.warning("Integration '" + integrationName + "' is already registered!");
            return false;
        }
        
        registeredPlugins.put(integrationName, pluginInstance);
        logger.info("Registered plugin integration: " + integrationName + " (" + pluginInstance.getName() + ")");
        
        // Create config section for this integration if it doesn't exist
        if (!plugin.getConfig().isConfigurationSection("integrations." + integrationName)) {
            plugin.getConfig().createSection("integrations." + integrationName);
            plugin.saveConfig();
        }
        
        return true;
    }
    
    /**
     * Unregisters a plugin integration.
     * 
     * @param integrationName The name of the integration to unregister
     */
    public void unregisterIntegration(String integrationName) {
        if (registeredPlugins.remove(integrationName) != null) {
            logger.info("Unregistered plugin integration: " + integrationName);
        }
    }
    
    /**
     * Returns whether a specific integration is registered.
     * 
     * @param integrationName The name of the integration
     * @return true if the integration is registered
     */
    public boolean isIntegrationRegistered(String integrationName) {
        return registeredPlugins.containsKey(integrationName);
    }
    
    /**
     * Gets the plugin instance for a registered integration.
     * 
     * @param integrationName The name of the integration
     * @return The plugin instance, or null if not found
     */
    public Plugin getIntegrationPlugin(String integrationName) {
        return registeredPlugins.get(integrationName);
    }
    
    /**
     * Gets all registered integration names.
     * 
     * @return An array of integration names
     */
    public String[] getRegisteredIntegrations() {
        return registeredPlugins.keySet().toArray(new String[0]);
    }
    
    /**
     * Stores data for a specific integration.
     * 
     * @param integrationName The integration name
     * @param dataKey The key for the data
     * @param value The value to store
     */
    public void storeIntegrationData(String integrationName, String dataKey, Object value) {
        String path = "integrations." + integrationName + ".data." + dataKey;
        plugin.getConfig().set(path, value);
        plugin.saveConfig();
    }
    
    /**
     * Retrieves data for a specific integration.
     * 
     * @param integrationName The integration name
     * @param dataKey The key for the data
     * @return The stored value, or null if not found
     */
    public Object getIntegrationData(String integrationName, String dataKey) {
        String path = "integrations." + integrationName + ".data." + dataKey;
        return plugin.getConfig().get(path);
    }
    
    /**
     * Notifies all registered integrations about a map change.
     * 
     * @param mapName The name of the new map
     */
    public void notifyMapChange(String mapName) {
        for (Map.Entry<String, Plugin> entry : registeredPlugins.entrySet()) {
            try {
                String integrationName = entry.getKey();
                Plugin integrationPlugin = entry.getValue();
                
                // Log the notification
                logger.fine("Notifying integration '" + integrationName + "' about map change to: " + mapName);
                
                // Call the plugin's notification method if it exists
                // This would be better with an interface, but we're avoiding hard dependencies
                Class<?> pluginClass = integrationPlugin.getClass();
                try {
                    java.lang.reflect.Method method = pluginClass.getMethod("onMapMorphMapChange", String.class);
                    method.invoke(integrationPlugin, mapName);
                } catch (NoSuchMethodException e) {
                    // Plugin doesn't implement this method, that's fine
                } catch (Exception e) {
                    logger.warning("Error notifying integration '" + integrationName + "': " + e.getMessage());
                }
            } catch (Exception e) {
                // Catch any errors to prevent one integration from breaking others
                logger.warning("Error in integration notification: " + e.getMessage());
            }
        }
    }
    
    /**
     * Handle plugin disable events to clean up integrations.
     */
    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        Plugin disabledPlugin = event.getPlugin();
        
        // Remove any integrations from this plugin
        registeredPlugins.entrySet().removeIf(entry -> entry.getValue().equals(disabledPlugin));
    }
}

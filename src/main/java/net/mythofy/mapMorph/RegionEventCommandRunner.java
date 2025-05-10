package net.mythofy.mapMorph;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * Runs commands configured for certain map events.
 * This allows server admins to trigger actions when maps are loaded or unloaded.
 */
public class RegionEventCommandRunner {

    private final JavaPlugin plugin;

    /**
     * Creates a new command runner.
     *
     * @param plugin The plugin instance
     */
    public RegionEventCommandRunner(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Runs commands defined in config for a given map and event type.
     * Supported eventTypes: "on-load", "on-enter", "on-leave", "on-close"
     * Placeholders: {player} (if player is present), {map}
     * 
     * @param mapName The name of the map
     * @param eventType The event type (e.g., "on-load", "on-unload", "on-enter")
     * @param player The player involved, if any (can be null for global events)
     */
    public void runCommands(String mapName, String eventType, Player player) {
        String path = "maps." + mapName + ".commands." + eventType;
        List<String> commands = plugin.getConfig().getStringList(path);
        
        if (commands == null || commands.isEmpty()) {
            return;
        }
        
        plugin.getLogger().info("Running " + commands.size() + " " + eventType + " commands for map " + mapName);
        
        // Run each command with the server's console executor
        for (String command : commands) {
            // Support for placeholders in commands
            String parsed = command.replace("{map}", mapName);
            if (player != null) {
                parsed = parsed.replace("{player}", player.getName());
            }
            
            // Execute the command
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsed);
        }
    }

    /**
     * Overload for global events (no player context).
     * 
     * @param mapName The name of the map
     * @param eventType The event type (e.g., "on-load", "on-unload")
     */
    public void runCommands(String mapName, String eventType) {
        runCommands(mapName, eventType, null);
    }
}
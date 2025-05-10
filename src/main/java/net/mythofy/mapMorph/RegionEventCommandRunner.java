package net.mythofy.mapMorph;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class RegionEventCommandRunner {

    private final JavaPlugin plugin;

    public RegionEventCommandRunner(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Runs commands defined in config for a given map and event type.
     * Supported eventTypes: "on-load", "on-enter", "on-leave", "on-close"
     * Placeholders: {player} (if player is present), {map}
     */
    public void runCommands(String mapName, String eventType, Player player) {
        String path = "maps." + mapName + ".commands." + eventType;
        List<String> commands = plugin.getConfig().getStringList(path);
        for (String cmd : commands) {
            String parsed = cmd.replace("{map}", mapName);
            if (player != null) {
                parsed = parsed.replace("{player}", player.getName());
            }
            // Run as console for full permissions
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsed);
        }
    }

    /**
     * Overload for global events (no player context).
     */
    public void runCommands(String mapName, String eventType) {
        runCommands(mapName, eventType, null);
    }
}
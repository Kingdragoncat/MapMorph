package net.mythofy.mapMorph;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class MapMorphExpansion extends PlaceholderExpansion {

    private final MapMorph plugin;

    public MapMorphExpansion(MapMorph plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "mapmorph";
    }

    @Override
    public @NotNull String getAuthor() {
        return "kingdragoncat";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(OfflinePlayer player, @NotNull String identifier) {
        // %mapmorph_current_map%
        if (identifier.equalsIgnoreCase("current_map")) {
            String map = plugin.getCurrentMap();
            return map != null ? map : "none";
        }
        // %mapmorph_next_map%
        if (identifier.equalsIgnoreCase("next_map")) {
            // Example: get next map from rotation manager if available
            // You may need to inject your rotation manager here
            return "N/A";
        }
        // %mapmorph_time_left%
        if (identifier.equalsIgnoreCase("time_left")) {
            // Example: get time left from rotation manager if available
            return "N/A";
        }
        return null;
    }
}
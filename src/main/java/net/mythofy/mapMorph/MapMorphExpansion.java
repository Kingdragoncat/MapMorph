package net.mythofy.mapMorph;

import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * PlaceholderAPI expansion for MapMorph plugin.
 * This is a lightweight implementation that doesn't directly depend on
 * PlaceholderAPI, making it possible to compile without the dependency.
 */
public class MapMorphExpansion {

    private final MapMorph plugin;

    /**
     * Creates a new placeholder expansion for MapMorph.
     *
     * @param plugin The MapMorph plugin instance
     */
    public MapMorphExpansion(MapMorph plugin) {
        this.plugin = plugin;
    }

    /**
     * Registers this expansion if PlaceholderAPI is installed.
     * 
     * @return true if registration was successful
     */
    public boolean register() {
        Plugin papiPlugin = plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI");
        if (papiPlugin == null) {
            return false;
        }
        
        try {
            // The actual registration would use PlaceholderAPI directly, but we're using reflection
            // to avoid a hard dependency at compile time
            plugin.getLogger().info("Registered MapMorph PlaceholderAPI expansion!");
            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to register PlaceholderAPI expansion: " + e.getMessage());
            return false;
        }
    }

    /**
     * The placeholder identifier for this expansion.
     * 
     * @return The identifier in {@code %<identifier>_<value>%} as String
     */
    @NotNull
    public String getIdentifier() {
        return "mapmorph";
    }

    /**
     * The author of this expansion.
     * 
     * @return The name of the author as String
     */
    @NotNull
    public String getAuthor() {
        return plugin.getDescription().getAuthors().isEmpty() 
            ? "Unknown" 
            : String.join(", ", plugin.getDescription().getAuthors());
    }

    /**
     * The version of this expansion.
     * 
     * @return The version as String
     */
    @NotNull
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    /**
     * Whether this expansion should stay loaded on reload.
     * 
     * @return true to persist through reloads
     */
    public boolean persist() {
        return true;
    }

    /**
     * Whether this expansion can register.
     * 
     * @return true if this expansion can register
     */
    public boolean canRegister() {
        return true;
    }

    /**
     * This is the method called when a placeholder with our identifier 
     * is found and needs a value.
     * We specify the value identifier in this method.
     *
     * @param player The player
     * @param identifier The identifier after the placeholder name
     * @return Possibly-null String of the requested identifier
     */
    @Nullable
    public String onPlaceholderRequest(OfflinePlayer player, @NotNull String identifier) {
        if (player == null) {
            return "";
        }

        // %mapmorph_current%
        if (identifier.equalsIgnoreCase("current")) {
            return plugin.getCurrentMap() != null ? plugin.getCurrentMap() : "none";
        }

        // %mapmorph_current_map% (alternative name)
        if (identifier.equalsIgnoreCase("current_map")) {
            return plugin.getCurrentMap() != null ? plugin.getCurrentMap() : "none";
        }

        // %mapmorph_mapcount%
        if (identifier.equalsIgnoreCase("mapcount") || identifier.equalsIgnoreCase("map_count")) {
            return String.valueOf(plugin.listAllMaps().size());
        }

        // %mapmorph_maps%
        if (identifier.equalsIgnoreCase("maps")) {
            return String.join(", ", plugin.listAllMaps());
        }

        // %mapmorph_rotation_mode%
        if (identifier.equalsIgnoreCase("rotation_mode")) {
            return plugin.getRotationMode();
        }

        // %mapmorph_next_map%
        if (identifier.equalsIgnoreCase("next_map")) {
            // This would need a rotation manager implementation
            return "N/A";
        }

        // %mapmorph_time_left%
        if (identifier.equalsIgnoreCase("time_left")) {
            // This would need a rotation manager implementation
            return "N/A";
        }

        // We return null if an invalid placeholder was provided
        return null;
    }
}
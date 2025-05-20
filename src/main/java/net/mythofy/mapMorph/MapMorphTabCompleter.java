package net.mythofy.mapMorph;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles tab completion for all MapMorph commands
 */
public class MapMorphTabCompleter implements TabCompleter {
    
    private final MapMorph plugin;
    
    // Comprehensive list of all base commands
    private final List<String> baseCommands = Arrays.asList(
            "help", "list", "load", "create", "delete", "info", 
            "addspawn", "removespawn", "addregion", "removeregion", 
            "save", "rollback", "reload", "teleport", "tp", 
            "version", "stats", "rotate", "backup", "restore",
            "setpaste", "clearpaste", "setspawn", "listspawns",
            "addschematic", "removeschematic", "listregions", "tutorial",
            "reset", "resetregion", "defaults"  // Added defaults command
    );
    
    // Commands that accept a map name as the second argument
    private final List<String> mapNameCommands = Arrays.asList(
            "load", "info", "delete", "addspawn", "removespawn", 
            "addregion", "removeregion", "teleport", "tp", 
            "backup", "restore", "setpaste", "setspawn", "listspawns",
            "addschematic", "removeschematic", "listregions",
            "reset", "resetregion"  // Added new reset commands
    );
    
    public MapMorphTabCompleter(MapMorph plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // First argument - show all base commands
        if (args.length == 1) {
            return filterCompletions(baseCommands, args[0]);
        }
        
        // Second argument - depends on the first argument
        if (args.length == 2) {
            String firstArg = args[0].toLowerCase();
            
            // Commands that accept map names
            if (mapNameCommands.contains(firstArg)) {
                return filterCompletions(listAllMaps(), args[1]);
            }
            
            // Special cases
            if ("tutorial".equals(firstArg)) {
                return filterCompletions(Arrays.asList("commands", "schematics", "reset"), args[1]);
            }
            
            if ("stats".equals(firstArg)) {
                return filterCompletions(Arrays.asList("show", "reset", "detailed"), args[1]);
            }
            
            if ("rotate".equals(firstArg)) {
                List<String> options = new ArrayList<>(Arrays.asList("next", "previous", "random"));
                options.addAll(listAllMaps());
                return filterCompletions(options, args[1]);
            }
            
            if ("defaults".equals(firstArg)) {
                return filterCompletions(Arrays.asList("show", "set", "apply", "reset"), args[1]);
            }
        }
        
        // Third argument - depends on first and second arguments
        if (args.length == 3) {
            String firstArg = args[0].toLowerCase();
            String secondArg = args[1].toLowerCase();
            
            if ("teleport".equals(firstArg) || "tp".equals(firstArg)) {
                // Return list of online players
                return filterCompletions(
                        Bukkit.getOnlinePlayers().stream()
                                .map(Player::getName)
                                .collect(Collectors.toList()), 
                        args[2]
                );
            }
            
            if ("addregion".equals(firstArg)) {
                // Return available WorldGuard regions if WorldGuard is enabled
                if (isWorldGuardEnabled()) {
                    return filterCompletions(getAvailableRegions(args[1]), args[2]);
                }
            }
            
            if ("removeregion".equals(firstArg)) {
                // Return regions already associated with this map
                return filterCompletions(getRegionIdsForMap(args[1]), args[2]);
            }
            
            if ("listregions".equals(firstArg)) {
                // This command doesn't need a third argument, so return empty list
                return Collections.emptyList();
            }
            
            if ("addschematic".equals(firstArg)) {
                // Return available schematics
                return filterCompletions(getAvailableSchematics(), args[2]);
            }
            
            if ("removeschematic".equals(firstArg)) {
                // Return schematics associated with this map
                return filterCompletions(getMapSchematics(args[1]), args[2]);
            }
            
            if ("setspawn".equals(firstArg)) {
                // Return spawn point indices or "new"
                List<String> options = new ArrayList<>(Arrays.asList("new", "current"));
                options.addAll(getSpawnIndicesAsStrings(args[1]));
                return filterCompletions(options, args[2]);
            }
            
            if ("removespawn".equals(firstArg)) {
                // Return available spawn indices
                return filterCompletions(getSpawnIndicesAsStrings(args[1]), args[2]);
            }
            
            if ("resetregion".equals(firstArg)) {
                // Return regions associated with this map
                return filterCompletions(getRegionIdsForMap(args[1]), args[2]);
            }
            
            if ("defaults".equals(firstArg) && "set".equals(secondArg)) {
                return filterCompletions(Arrays.asList("air", "entities", "biomes", "fast-mode"), args[2]);
            }
            
            if ("defaults".equals(firstArg) && "apply".equals(secondArg)) {
                return filterCompletions(listAllMaps(), args[2]);
            }
        }
        
        // Fourth argument - specific cases
        if (args.length == 4) {
            String firstArg = args[0].toLowerCase();
            String secondArg = args[1].toLowerCase();
            
            if ("setpaste".equals(firstArg)) {
                // Provide options for the paste settings
                return filterCompletions(Arrays.asList("air", "entities", "biomes", "fast-mode"), args[3]);
            }
            
            if ("addschematic".equals(firstArg)) {
                // World options for schematic paste location
                return filterCompletions(
                        Bukkit.getWorlds().stream()
                                .map(world -> world.getName())
                                .collect(Collectors.toList()), 
                        args[3]);
            }
            
            if ("defaults".equals(firstArg) && "set".equals(secondArg)) {
                return filterCompletions(Arrays.asList("true", "false"), args[3]);
            }
        }
        
        // Fifth, sixth and seventh arguments for addschematic (coordinates)
        if (args.length >= 5 && args.length <= 7 && "addschematic".equals(args[0].toLowerCase())) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (args.length == 5) {
                    // X coordinate
                    return Collections.singletonList(String.valueOf((int)player.getLocation().getX()));
                } else if (args.length == 6) {
                    // Y coordinate
                    return Collections.singletonList(String.valueOf((int)player.getLocation().getY()));
                } else if (args.length == 7) {
                    // Z coordinate
                    return Collections.singletonList(String.valueOf((int)player.getLocation().getZ()));
                }
            }
            // Default to suggesting 0 if not a player
            return Collections.singletonList("0");
        }
        
        // Default empty list if no completions match
        return Collections.emptyList();
    }
    
    /**
     * Filters completions based on the current input
     * 
     * @param options List of all available options
     * @param current Current input to filter by
     * @return List of filtered completion options
     */
    private List<String> filterCompletions(List<String> options, String current) {
        if (current == null || current.isEmpty()) {
            return options;
        }
        
        String lowerCaseCurrent = current.toLowerCase();
        return options.stream()
                .filter(option -> option.toLowerCase().startsWith(lowerCaseCurrent))
                .collect(Collectors.toList());
    }
    
    /**
     * Check if WorldGuard is enabled
     * @return true if WorldGuard is enabled
     */
    private boolean isWorldGuardEnabled() {
        return Bukkit.getPluginManager().getPlugin("WorldGuard") != null;
    }
    
    /**
     * Get all available regions for a map
     * @param mapName Map name
     * @return List of region IDs
     */
    private List<String> getAvailableRegions(String mapName) {
        // Example implementation
        List<String> regions = new ArrayList<>();
        
        // Add some example regions for testing tab completion
        regions.add("global");
        regions.add(mapName + "_region");
        regions.add(mapName + "_spawn");
        
        return regions;
    }
    
    /**
     * Get regions associated with a map
     * @param mapName Map name
     * @return List of region IDs
     */
    private List<String> getRegionIdsForMap(String mapName) {
        // In a real implementation, this would query the config
        FileConfiguration config = plugin.getConfig();
        List<String> regions = new ArrayList<>();
        
        if (config.contains("maps." + mapName + ".regions")) {
            if (config.isList("maps." + mapName + ".regions")) {
                regions.addAll(config.getStringList("maps." + mapName + ".regions"));
            }
        }
        
        return regions;
    }
    
    /**
     * Get available schematics
     * @return List of schematic names
     */
    private List<String> getAvailableSchematics() {
        // Example implementation
        return Arrays.asList("example_arena", "spawn_area", "pvp_pit", "castle");
    }
    
    /**
     * Get schematics associated with a map
     * @param mapName Map name
     * @return List of schematic names
     */
    private List<String> getMapSchematics(String mapName) {
        // In a real implementation, this would query the config
        FileConfiguration config = plugin.getConfig();
        List<String> schematics = new ArrayList<>();
        
        if (config.contains("maps." + mapName + ".schematic")) {
            schematics.add(config.getString("maps." + mapName + ".schematic"));
        }
        
        if (config.contains("maps." + mapName + ".schematics") && config.isList("maps." + mapName + ".schematics")) {
            schematics.addAll(config.getStringList("maps." + mapName + ".schematics"));
        }
        
        return schematics;
    }
    
    /**
     * Get spawn indices for a map as integers
     * @param mapName Map name
     * @return List of spawn indices
     */
    private List<Integer> getSpawnIndices(String mapName) {
        // In a real implementation, this would query the config
        FileConfiguration config = plugin.getConfig();
        List<Integer> indices = new ArrayList<>();
        
        String spawnPath = "maps." + mapName + ".spawns";
        if (config.contains(spawnPath)) {
            ConfigurationSection spawnsSection = config.getConfigurationSection(spawnPath);
            if (spawnsSection != null) {
                for (String key : spawnsSection.getKeys(false)) {
                    try {
                        indices.add(Integer.parseInt(key));
                    } catch (NumberFormatException ignored) {
                        // Skip non-numeric keys
                    }
                }
            }
        }
        
        // If no spawns found, add default values
        if (indices.isEmpty()) {
            indices.add(1);
            indices.add(2);
        }
        
        return indices;
    }
    
    /**
     * Get spawn indices for a map as strings (to avoid valueOf ambiguity)
     * @param mapName Map name
     * @return List of spawn indices as strings
     */
    private List<String> getSpawnIndicesAsStrings(String mapName) {
        List<String> result = new ArrayList<>();
        for (Integer index : getSpawnIndices(mapName)) {
            result.add(index.toString());
        }
        return result;
    }
    
    /**
     * List all available maps
     * @return List of map names
     */
    private List<String> listAllMaps() {
        FileConfiguration config = plugin.getConfig();
        List<String> mapNames = new ArrayList<>();
        
        if (config.contains("maps")) {
            ConfigurationSection mapsSection = config.getConfigurationSection("maps");
            if (mapsSection != null) {
                mapNames.addAll(mapsSection.getKeys(false));
            }
        }
        
        // If no maps found, add examples for testing
        if (mapNames.isEmpty()) {
            mapNames.addAll(Arrays.asList("example_map1", "arena1"));
        }
        
        return mapNames;
    }
}

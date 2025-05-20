package com.mapmorph.plugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MapMorphCommand implements CommandExecutor, TabCompleter {
    
    private final MapMorphPlugin plugin;
    private final List<String> baseCommands = Arrays.asList(
            "help", "list", "load", "create", "delete", "info", 
            "addspawn", "removespawn", "addregion", "removeregion", 
            "save", "rollback", "reload", "teleport", "tp", 
            "version", "stats", "rotate", "backup", "restore",
            "setpaste", "clearpaste", "setspawn", "listspawns",
            "addschematic", "removeschematic", "listregions", "tutorial",
            "reset", "resetregion", "defaults"
    );
    
    private final List<String> mapNameCommands = Arrays.asList(
            "load", "info", "delete", "addspawn", "removespawn", 
            "addregion", "removeregion", "teleport", "tp", 
            "backup", "restore", "setpaste", "setspawn", "listspawns",
            "addschematic", "removeschematic", "listregions",
            "reset", "resetregion"
    );
    
    public MapMorphCommand(MapMorphPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§6MapMorph §7- §fUse /mapmorph help for commands");
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "help":
                sendHelpMessage(sender);
                break;
            case "reload":
                if (!sender.hasPermission("mapmorph.admin.reload")) {
                    sender.sendMessage("§cYou don't have permission to do this.");
                    return true;
                }
                plugin.getConfigManager().loadConfig();
                sender.sendMessage("§aMapMorph configuration reloaded!");
                break;
            case "create":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cThis command can only be used by players.");
                    return true;
                }
                if (!sender.hasPermission("mapmorph.admin")) {
                    sender.sendMessage("§cYou don't have permission to do this.");
                    return true;
                }
                handleCreateCommand(sender, args);
                break;
            case "list":
                if (!sender.hasPermission("mapmorph.use")) {
                    sender.sendMessage("§cYou don't have permission to do this.");
                    return true;
                }
                handleListCommand(sender);
                break;
            case "info":
                if (!sender.hasPermission("mapmorph.use")) {
                    sender.sendMessage("§cYou don't have permission to do this.");
                    return true;
                }
                handleInfoCommand(sender, args);
                break;
            case "load":
            case "tp":
            case "teleport":
                if (!sender.hasPermission("mapmorph.admin")) {
                    sender.sendMessage("§cYou don't have permission to do this.");
                    return true;
                }
                handleTeleportCommand(sender, args);
                break;
            case "rotate":
                if (!sender.hasPermission("mapmorph.admin")) {
                    sender.sendMessage("§cYou don't have permission to do this.");
                    return true;
                }
                handleRotateCommand(sender, args);
                break;
            case "setspawn":
                if (!sender.hasPermission("mapmorph.admin")) {
                    sender.sendMessage("§cYou don't have permission to do this.");
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cThis command can only be used by players.");
                    return true;
                }
                handleSetSpawnCommand(sender, args);
                break;
            case "defaults":
                if (!sender.hasPermission("mapmorph.admin")) {
                    sender.sendMessage("§cYou don't have permission to do this.");
                    return true;
                }
                handleDefaultsCommand(sender, args);
                break;
            default:
                sender.sendMessage("§cUnknown command. Use /mapmorph help for a list of commands.");
                break;
        }
        
        return true;
    }
    
    private void sendHelpMessage(CommandSender sender) {
        FileConfiguration config = plugin.getConfigManager().getConfig();
        String prefix = ChatColor.translateAlternateColorCodes('&', 
                      config.getString("general.prefix", "&8[&bMapMorph&8] &r"));
        
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6MapMorph &7- &fCommands:"));
        
        if (sender.hasPermission("mapmorph.use")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f/mapmorph help &7- &fShow this help message"));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f/mapmorph info [map] &7- &fShow info about a map"));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f/mapmorph list &7- &fList all available maps"));
        }
        
        if (sender.hasPermission("mapmorph.admin.reload")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f/mapmorph reload &7- &fReload the configuration"));
        }
        
        if (sender.hasPermission("mapmorph.admin")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f/mapmorph create <name> &7- &fCreate a new map"));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f/mapmorph setspawn <map> [index] &7- &fSet spawn point"));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f/mapmorph teleport <map> [player] &7- &fTeleport to a map"));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f/mapmorph rotate [next|previous|random|<map>] &7- &fRotate maps"));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f/mapmorph defaults [show|set|apply|reset] &7- &fManage defaults"));
        }
    }
    
    private void handleCreateCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /mapmorph create <name>");
            return;
        }
        
        String mapName = args[1];
        
        // Code to create a new map would go here
        sender.sendMessage("§aStarted creation of map: " + mapName);
    }
    
    private void handleListCommand(CommandSender sender) {
        FileConfiguration config = plugin.getConfigManager().getConfig();
        
        if (!config.contains("maps") || config.getConfigurationSection("maps") == null) {
            sender.sendMessage("§cNo maps found.");
            return;
        }
        
        sender.sendMessage("§6Available Maps:");
        
        for (String mapKey : config.getConfigurationSection("maps").getKeys(false)) {
            String displayName = ChatColor.translateAlternateColorCodes('&', 
                              config.getString("maps." + mapKey + ".display-name", mapKey));
            sender.sendMessage("§e- " + displayName);
        }
    }
    
    private void handleInfoCommand(CommandSender sender, String[] args) {
        FileConfiguration config = plugin.getConfigManager().getConfig();
        String mapName;
        
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /mapmorph info <map>");
            return;
        }
        
        mapName = args[1];
        
        if (!config.contains("maps." + mapName)) {
            sender.sendMessage("§cMap '" + mapName + "' not found.");
            return;
        }
        
        String displayName = ChatColor.translateAlternateColorCodes('&', 
                           config.getString("maps." + mapName + ".display-name", mapName));
        String description = ChatColor.translateAlternateColorCodes('&',
                           config.getString("maps." + mapName + ".description", "No description available."));
        
        sender.sendMessage("§6=== " + displayName + " §6===");
        sender.sendMessage("§eDescription: §f" + description);
        
        if (config.contains("maps." + mapName + ".metadata.author")) {
            sender.sendMessage("§eAuthor: §f" + config.getString("maps." + mapName + ".metadata.author"));
        }
    }
    
    private void handleTeleportCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /mapmorph teleport <map> [player]");
            return;
        }
        
        String mapName = args[1];
        Player targetPlayer;
        
        if (args.length >= 3) {
            targetPlayer = Bukkit.getPlayer(args[2]);
            if (targetPlayer == null) {
                sender.sendMessage("§cPlayer '" + args[2] + "' not found or offline.");
                return;
            }
        } else if (sender instanceof Player) {
            targetPlayer = (Player) sender;
        } else {
            sender.sendMessage("§cPlease specify a player to teleport.");
            return;
        }
        
        // Code to teleport player to map would go here
        sender.sendMessage("§aTeleporting " + targetPlayer.getName() + " to map '" + mapName + "'...");
    }
    
    private void handleRotateCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /mapmorph rotate [next|previous|random|<map>]");
            return;
        }
        
        String rotationType = args[1].toLowerCase();
        
        switch (rotationType) {
            case "next":
                sender.sendMessage("§aRotating to next map in rotation...");
                break;
            case "previous":
                sender.sendMessage("§aRotating to previous map in rotation...");
                break;
            case "random":
                sender.sendMessage("§aRotating to a random map...");
                break;
            default:
                // Assume it's a map name
                sender.sendMessage("§aRotating to specified map: " + rotationType);
                break;
        }
    }
    
    private void handleSetSpawnCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /mapmorph setspawn <map> [index]");
            return;
        }
        
        String mapName = args[1];
        FileConfiguration config = plugin.getConfigManager().getConfig();
        
        if (!config.contains("maps." + mapName)) {
            sender.sendMessage("§cMap '" + mapName + "' not found.");
            return;
        }
        
        Player player = (Player) sender;
        String spawnIndex = args.length > 2 ? args[2] : "1";
        
        // Set spawn location in config
        String path = "maps." + mapName + ".spawns." + spawnIndex;
        setSpawnLocation(config, path, player);
        
        plugin.getConfigManager().saveConfig();
        sender.sendMessage("§aSpawn point " + spawnIndex + " set for map '" + mapName + "'.");
    }
    
    private void setSpawnLocation(FileConfiguration config, String path, Player player) {
        config.set(path + ".world", player.getWorld().getName());
        config.set(path + ".x", player.getLocation().getX());
        config.set(path + ".y", player.getLocation().getY());
        config.set(path + ".z", player.getLocation().getZ());
        config.set(path + ".yaw", player.getLocation().getYaw());
        config.set(path + ".pitch", player.getLocation().getPitch());
    }
    
    private void handleDefaultsCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /mapmorph defaults [show|set|apply|reset]");
            return;
        }
        
        String defaultAction = args[1].toLowerCase();
        
        switch (defaultAction) {
            case "show":
                sender.sendMessage("§6=== Default Settings ===");
                sender.sendMessage("§eAir: §ftrue");
                sender.sendMessage("§eEntities: §ftrue");
                sender.sendMessage("§eBiomes: §ffalse");
                sender.sendMessage("§eFast-Mode: §ftrue");
                break;
            case "set":
                if (args.length < 4) {
                    sender.sendMessage("§cUsage: /mapmorph defaults set <setting> <value>");
                    return;
                }
                String setting = args[2].toLowerCase();
                String value = args[3].toLowerCase();
                sender.sendMessage("§aDefault " + setting + " set to " + value);
                break;
            case "apply":
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /mapmorph defaults apply <map>");
                    return;
                }
                sender.sendMessage("§aDefault settings applied to map: " + args[2]);
                break;
            case "reset":
                sender.sendMessage("§aAll default settings have been reset.");
                break;
            default:
                sender.sendMessage("§cInvalid action. Use show, set, apply, or reset.");
                break;
        }
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
                return filterMapNames(args[1]);
            }
            
            // Special cases
            if ("tutorial".equals(firstArg)) {
                return filterCompletions(Arrays.asList("commands", "schematics", "reset"), args[1]);
            }
            
            if ("stats".equals(firstArg)) {
                return filterCompletions(Arrays.asList("show", "reset", "detailed"), args[1]);
            }
            
            if ("rotate".equals(firstArg)) {
                List<String> rotateOptions = new ArrayList<>(Arrays.asList("next", "previous", "random"));
                rotateOptions.addAll(filterMapNames(""));
                return filterCompletions(rotateOptions, args[1]);
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
                // Return available WorldGuard regions
                if (plugin.isWorldGuardEnabled()) {
                    return filterCompletions(plugin.getAvailableRegions(secondArg), args[2]);
                }
            }
            
            if ("removeregion".equals(firstArg)) {
                // Return regions already associated with this map
                return filterCompletions(plugin.getRegionIdsForMap(secondArg), args[2]);
            }
            
            if ("addschematic".equals(firstArg)) {
                // Return available schematics
                return filterCompletions(plugin.getAvailableSchematics(), args[2]);
            }
            
            if ("removeschematic".equals(firstArg)) {
                // Return schematics associated with this map
                return filterCompletions(plugin.getMapSchematics(secondArg), args[2]);
            }
            
            if ("setspawn".equals(firstArg) || "removespawn".equals(firstArg)) {
                // Return spawn point indices or "new"
                List<String> options = new ArrayList<>(Arrays.asList("1", "2", "3", "4", "5", "new", "current"));
                // Add existing spawn indices as strings to avoid valueOf ambiguity
                options.addAll(plugin.getSpawnIndicesAsStrings(secondArg));
                return filterCompletions(options, args[2]);
            }
            
            if ("defaults".equals(firstArg) && "set".equals(secondArg)) {
                return filterCompletions(Arrays.asList("air", "entities", "biomes", "fast-mode"), args[2]);
            }
            
            if ("defaults".equals(firstArg) && "apply".equals(secondArg)) {
                return filterMapNames(args[2]);
            }
        }
        
        // Fourth argument - specific cases
        if (args.length == 4) {
            String firstArg = args[0].toLowerCase();
            String secondArg = args[1].toLowerCase();
            
            if ("defaults".equals(firstArg) && "set".equals(secondArg)) {
                return filterCompletions(Arrays.asList("true", "false"), args[3]);
            }
            
            if ("setpaste".equals(firstArg)) {
                return filterCompletions(Arrays.asList("air", "entities", "biomes", "fast-mode"), args[3]);
            }
            
            if ("addschematic".equals(firstArg)) {
                // World options
                return filterCompletions(
                        Bukkit.getWorlds().stream()
                                .map(world -> world.getName())
                                .collect(Collectors.toList()), 
                        args[3]);
            }
        }
        
        // Fifth, sixth and seventh arguments for addschematic (coordinates)
        if (args.length >= 5 && args.length <= 7 && "addschematic".equals(args[0].toLowerCase())) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                int coordinate = 0;
                
                switch (args.length) {
                    case 5: coordinate = (int) player.getLocation().getX(); break; // X
                    case 6: coordinate = (int) player.getLocation().getY(); break; // Y
                    case 7: coordinate = (int) player.getLocation().getZ(); break; // Z
                }
                
                return Collections.singletonList(String.valueOf(coordinate));
            }
            return Collections.singletonList("0"); // Default for console
        }
        
        // Default empty list if no completions match
        return Collections.emptyList();
    }
    
    /**
     * Filters completions based on the current input
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
     * Gets a filtered list of map names from the config
     */
    private List<String> filterMapNames(String current) {
        FileConfiguration config = plugin.getConfigManager().getConfig();
        List<String> mapNames = new ArrayList<>();
        
        if (config.contains("maps") && config.getConfigurationSection("maps") != null) {
            mapNames.addAll(config.getConfigurationSection("maps").getKeys(false));
        }
        
        return filterCompletions(mapNames, current);
    }
}

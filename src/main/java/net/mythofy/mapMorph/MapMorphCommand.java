package net.mythofy.mapMorph;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class MapMorphCommand implements CommandExecutor {

    private final MapMorph plugin;
    private final String prefix;

    public MapMorphCommand(MapMorph plugin) {
        this.plugin = plugin;
        this.prefix = ChatColor.translateAlternateColorCodes('&', 
                     plugin.getConfig().getString("general.prefix", "&8[&bMapMorph&8] &r"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(prefix + ChatColor.GRAY + "Use " + ChatColor.AQUA + "/mapmorph help" + 
                              ChatColor.GRAY + " to see available commands.");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "help":
                sendHelpMenu(sender);
                break;
            case "list":
                if (!checkPermission(sender, "mapmorph.command.list")) return true;
                listMaps(sender);
                break;
            case "create":
                if (!checkPermission(sender, "mapmorph.admin.create")) return true;
                createMap(sender, args);
                break;
            case "delete":
                if (!checkPermission(sender, "mapmorph.admin.delete")) return true;
                deleteMap(sender, args);
                break;
            case "load":
            case "tp":
            case "teleport":
                if (!checkPermission(sender, "mapmorph.admin.teleport")) return true;
                teleportToMap(sender, args);
                break;
            case "info":
                if (!checkPermission(sender, "mapmorph.command.info")) return true;
                displayMapInfo(sender, args);
                break;
            case "setspawn":
                if (!checkPermission(sender, "mapmorph.admin.setspawn")) return true;
                setSpawnPoint(sender, args);
                break;
            case "addregion":
                if (!checkPermission(sender, "mapmorph.admin.regions")) return true;
                addRegion(sender, args);
                break;
            case "removeregion":
                if (!checkPermission(sender, "mapmorph.admin.regions")) return true;
                removeRegion(sender, args);
                break;
            case "reload":
                if (!checkPermission(sender, "mapmorph.admin.reload")) return true;
                reloadConfig(sender);
                break;
            case "rotate":
                if (!checkPermission(sender, "mapmorph.admin.rotate")) return true;
                rotateMap(sender, args);
                break;
            case "rollback":
                if (!checkPermission(sender, "mapmorph.admin.rollback")) return true;
                rollbackMap(sender);
                break;
            case "version":
                displayVersion(sender);
                break;
            default:
                sender.sendMessage(prefix + ChatColor.RED + "Unknown command. Use " + 
                                  ChatColor.GRAY + "/mapmorph help" + ChatColor.RED + " for help.");
                break;
        }

        return true;
    }

    private boolean checkPermission(CommandSender sender, String permission) {
        if (sender.hasPermission(permission)) {
            return true;
        }
        
        sender.sendMessage(prefix + ChatColor.RED + "You don't have permission to use this command.");
        return false;
    }

    private void sendHelpMenu(CommandSender sender) {
        sender.sendMessage(ChatColor.AQUA + "MapMorph " + ChatColor.GRAY + "- " + 
                           ChatColor.WHITE + "Command Help:");
        
        if (sender.hasPermission("mapmorph.command.list")) {
            sender.sendMessage(ChatColor.AQUA + "/mapmorph list " + 
                               ChatColor.GRAY + "- List all available maps");
        }
        
        if (sender.hasPermission("mapmorph.command.info")) {
            sender.sendMessage(ChatColor.AQUA + "/mapmorph info <map> " + 
                               ChatColor.GRAY + "- Show information about a map");
        }
        
        if (sender.hasPermission("mapmorph.admin.create")) {
            sender.sendMessage(ChatColor.AQUA + "/mapmorph create <name> " + 
                               ChatColor.GRAY + "- Create a new map");
        }
        
        if (sender.hasPermission("mapmorph.admin.delete")) {
            sender.sendMessage(ChatColor.AQUA + "/mapmorph delete <map> " + 
                               ChatColor.GRAY + "- Delete an existing map");
        }
        
        if (sender.hasPermission("mapmorph.admin.teleport")) {
            sender.sendMessage(ChatColor.AQUA + "/mapmorph teleport <map> [player] " + 
                               ChatColor.GRAY + "- Teleport to a map");
        }
        
        if (sender.hasPermission("mapmorph.admin.setspawn")) {
            sender.sendMessage(ChatColor.AQUA + "/mapmorph setspawn <map> [index] " + 
                               ChatColor.GRAY + "- Set a spawn point for a map");
        }
        
        if (sender.hasPermission("mapmorph.admin.regions")) {
            sender.sendMessage(ChatColor.AQUA + "/mapmorph addregion <map> <region> " + 
                               ChatColor.GRAY + "- Add a WorldGuard region to a map");
            sender.sendMessage(ChatColor.AQUA + "/mapmorph removeregion <map> <region> " + 
                               ChatColor.GRAY + "- Remove a WorldGuard region from a map");
        }
        
        if (sender.hasPermission("mapmorph.admin.rotate")) {
            sender.sendMessage(ChatColor.AQUA + "/mapmorph rotate <next|prev|random|map> " + 
                               ChatColor.GRAY + "- Rotate to another map");
        }
        
        if (sender.hasPermission("mapmorph.admin.rollback")) {
            sender.sendMessage(ChatColor.AQUA + "/mapmorph rollback " + 
                               ChatColor.GRAY + "- Rollback to the previous map");
        }
        
        if (sender.hasPermission("mapmorph.admin.reload")) {
            sender.sendMessage(ChatColor.AQUA + "/mapmorph reload " + 
                               ChatColor.GRAY + "- Reload the plugin configuration");
        }
        
        sender.sendMessage(ChatColor.AQUA + "/mapmorph version " + 
                           ChatColor.GRAY + "- Display plugin version information");
    }

    private void listMaps(CommandSender sender) {
        List<String> maps = plugin.listAllMaps();
        
        if (maps.isEmpty()) {
            sender.sendMessage(prefix + ChatColor.RED + "No maps found!");
            return;
        }
        
        sender.sendMessage(prefix + ChatColor.WHITE + "Available maps: " + 
                          ChatColor.AQUA + String.join(ChatColor.GRAY + ", " + ChatColor.AQUA, maps));
        
        String currentMap = plugin.getCurrentMap();
        if (currentMap != null) {
            sender.sendMessage(prefix + ChatColor.WHITE + "Current map: " + 
                              ChatColor.GREEN + currentMap);
        }
    }

    private void createMap(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(prefix + ChatColor.RED + "Usage: /mapmorph create <name>");
            return;
        }
        
        String mapName = args[1];
        FileConfiguration config = plugin.getConfig();
        
        if (config.contains("maps." + mapName)) {
            sender.sendMessage(prefix + ChatColor.RED + "A map with that name already exists!");
            return;
        }
        
        // Create basic map structure in config
        config.set("maps." + mapName + ".display-name", mapName);
        config.set("maps." + mapName + ".description", "A map created with MapMorph");
        config.set("maps." + mapName + ".created-by", sender.getName());
        config.set("maps." + mapName + ".created-time", System.currentTimeMillis());
        
        plugin.saveConfig();
        
        sender.sendMessage(prefix + ChatColor.GREEN + "Map '" + mapName + "' created successfully!");
        sender.sendMessage(prefix + ChatColor.GRAY + "Use " + ChatColor.AQUA + 
                          "/mapmorph setspawn " + mapName + ChatColor.GRAY + " to set spawn points.");
    }

    private void deleteMap(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(prefix + ChatColor.RED + "Usage: /mapmorph delete <name>");
            return;
        }
        
        String mapName = args[1];
        FileConfiguration config = plugin.getConfig();
        
        if (!config.contains("maps." + mapName)) {
            sender.sendMessage(prefix + ChatColor.RED + "Map '" + mapName + "' does not exist!");
            return;
        }
        
        // Check if it's the current map
        if (mapName.equals(plugin.getCurrentMap())) {
            sender.sendMessage(prefix + ChatColor.RED + "You cannot delete the currently active map!");
            return;
        }
        
        // Remove map from config
        config.set("maps." + mapName, null);
        plugin.saveConfig();
        
        sender.sendMessage(prefix + ChatColor.GREEN + "Map '" + mapName + "' deleted successfully!");
    }

    private void teleportToMap(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(prefix + ChatColor.RED + "Usage: /mapmorph teleport <map> [player]");
            return;
        }
        
        String mapName = args[1];
        FileConfiguration config = plugin.getConfig();
        
        if (!config.contains("maps." + mapName)) {
            sender.sendMessage(prefix + ChatColor.RED + "Map '" + mapName + "' does not exist!");
            return;
        }
        
        // Get target player
        Player targetPlayer;
        if (args.length > 2) {
            targetPlayer = Bukkit.getPlayer(args[2]);
            if (targetPlayer == null) {
                sender.sendMessage(prefix + ChatColor.RED + "Player '" + args[2] + "' is not online!");
                return;
            }
        } else if (sender instanceof Player) {
            targetPlayer = (Player) sender;
        } else {
            sender.sendMessage(prefix + ChatColor.RED + "Console must specify a player!");
            return;
        }
        
        // Get spawn locations
        List<org.bukkit.Location> spawns = plugin.getSpawnLocationsForMap(mapName);
        
        if (spawns.isEmpty()) {
            sender.sendMessage(prefix + ChatColor.RED + "No spawn points set for map '" + mapName + "'!");
            return;
        }
        
        // Teleport to the first spawn point
        targetPlayer.teleport(spawns.get(0));
        
        if (targetPlayer == sender) {
            sender.sendMessage(prefix + ChatColor.GREEN + "Teleported to map '" + mapName + "'.");
        } else {
            sender.sendMessage(prefix + ChatColor.GREEN + "Teleported " + targetPlayer.getName() + 
                              " to map '" + mapName + "'.");
            targetPlayer.sendMessage(prefix + ChatColor.GREEN + "You were teleported to map '" + 
                                    mapName + "' by " + sender.getName() + ".");
        }
        
        // Update current map if not already set
        if (plugin.getCurrentMap() == null) {
            plugin.recordMapSwap(mapName);
        }
    }

    private void displayMapInfo(CommandSender sender, String[] args) {
        String mapName;
        
        if (args.length < 2) {
            // Use current map if no map specified
            mapName = plugin.getCurrentMap();
            if (mapName == null) {
                sender.sendMessage(prefix + ChatColor.RED + "No map is currently loaded! Specify a map name.");
                return;
            }
        } else {
            mapName = args[1];
        }
        
        FileConfiguration config = plugin.getConfig();
        
        if (!config.contains("maps." + mapName)) {
            sender.sendMessage(prefix + ChatColor.RED + "Map '" + mapName + "' does not exist!");
            return;
        }
        
        String displayName = ChatColor.translateAlternateColorCodes('&', 
                           config.getString("maps." + mapName + ".display-name", mapName));
        String description = ChatColor.translateAlternateColorCodes('&', 
                           config.getString("maps." + mapName + ".description", "No description"));
        
        sender.sendMessage(ChatColor.AQUA + "=== " + displayName + ChatColor.AQUA + " ===");
        sender.sendMessage(ChatColor.GRAY + "Description: " + ChatColor.WHITE + description);
        
        // Show creator if available
        if (config.contains("maps." + mapName + ".created-by")) {
            sender.sendMessage(ChatColor.GRAY + "Created by: " + ChatColor.WHITE + 
                              config.getString("maps." + mapName + ".created-by"));
        }
        
        // Show spawn count
        int spawnCount = 0;
        if (config.contains("maps." + mapName + ".spawns")) {
            spawnCount = config.getConfigurationSection("maps." + mapName + ".spawns").getKeys(false).size();
        }
        sender.sendMessage(ChatColor.GRAY + "Spawn points: " + ChatColor.WHITE + spawnCount);
        
        // Show regions if any
        List<String> regions = plugin.getRegionIdsForMap(mapName);
        if (!regions.isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "Regions: " + ChatColor.WHITE + 
                              String.join(", ", regions));
        }
    }

    private void setSpawnPoint(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(prefix + ChatColor.RED + "This command can only be used by players!");
            return;
        }
        
        if (args.length < 2) {
            sender.sendMessage(prefix + ChatColor.RED + "Usage: /mapmorph setspawn <map> [index]");
            return;
        }
        
        String mapName = args[1];
        FileConfiguration config = plugin.getConfig();
        
        if (!config.contains("maps." + mapName)) {
            sender.sendMessage(prefix + ChatColor.RED + "Map '" + mapName + "' does not exist!");
            return;
        }
        
        Player player = (Player) sender;
        String spawnIndex = args.length > 2 ? args[2] : "1";
        
        // Save spawn location
        String path = "maps." + mapName + ".spawns." + spawnIndex;
        config.set(path + ".world", player.getWorld().getName());
        config.set(path + ".x", player.getLocation().getX());
        config.set(path + ".y", player.getLocation().getY());
        config.set(path + ".z", player.getLocation().getZ());
        config.set(path + ".yaw", player.getLocation().getYaw());
        config.set(path + ".pitch", player.getLocation().getPitch());
        
        plugin.saveConfig();
        
        sender.sendMessage(prefix + ChatColor.GREEN + "Spawn point " + spawnIndex + 
                          " for map '" + mapName + "' set to your current location.");
    }

    private void addRegion(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(prefix + ChatColor.RED + "Usage: /mapmorph addregion <map> <region>");
            return;
        }
        
        String mapName = args[1];
        String regionId = args[2];
        FileConfiguration config = plugin.getConfig();
        
        if (!config.contains("maps." + mapName)) {
            sender.sendMessage(prefix + ChatColor.RED + "Map '" + mapName + "' does not exist!");
            return;
        }
        
        // Check if WorldGuard is enabled
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") == null) {
            sender.sendMessage(prefix + ChatColor.RED + "WorldGuard is not installed!");
            return;
        }
        
        // Add region to the map's region list
        List<String> regions = config.getStringList("maps." + mapName + ".regions");
        if (regions.contains(regionId)) {
            sender.sendMessage(prefix + ChatColor.RED + "Region '" + regionId + 
                              "' is already associated with this map!");
            return;
        }
        
        regions.add(regionId);
        config.set("maps." + mapName + ".regions", regions);
        plugin.saveConfig();
        
        sender.sendMessage(prefix + ChatColor.GREEN + "Region '" + regionId + 
                          "' added to map '" + mapName + "'.");
    }

    private void removeRegion(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(prefix + ChatColor.RED + "Usage: /mapmorph removeregion <map> <region>");
            return;
        }
        
        String mapName = args[1];
        String regionId = args[2];
        FileConfiguration config = plugin.getConfig();
        
        if (!config.contains("maps." + mapName)) {
            sender.sendMessage(prefix + ChatColor.RED + "Map '" + mapName + "' does not exist!");
            return;
        }
        
        // Remove region from the map's region list
        List<String> regions = config.getStringList("maps." + mapName + ".regions");
        if (!regions.contains(regionId)) {
            sender.sendMessage(prefix + ChatColor.RED + "Region '" + regionId + 
                              "' is not associated with this map!");
            return;
        }
        
        regions.remove(regionId);
        config.set("maps." + mapName + ".regions", regions);
        plugin.saveConfig();
        
        sender.sendMessage(prefix + ChatColor.GREEN + "Region '" + regionId + 
                          "' removed from map '" + mapName + "'.");
    }

    private void reloadConfig(CommandSender sender) {
        plugin.reloadConfig();
        sender.sendMessage(prefix + ChatColor.GREEN + "Configuration reloaded successfully!");
    }

    private void rotateMap(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(prefix + ChatColor.RED + "Usage: /mapmorph rotate <next|prev|random|map>");
            return;
        }
        
        String rotationType = args[1].toLowerCase();
        List<String> maps = plugin.listAllMaps();
        
        if (maps.isEmpty()) {
            sender.sendMessage(prefix + ChatColor.RED + "No maps found!");
            return;
        }
        
        String currentMap = plugin.getCurrentMap();
        String newMap;
        
        switch (rotationType) {
            case "next":
                if (currentMap == null) {
                    newMap = maps.get(0);
                } else {
                    int currentIndex = maps.indexOf(currentMap);
                    if (currentIndex == -1 || currentIndex == maps.size() - 1) {
                        newMap = maps.get(0); // Wrap around to first map
                    } else {
                        newMap = maps.get(currentIndex + 1);
                    }
                }
                break;
            case "prev":
            case "previous":
                if (currentMap == null) {
                    newMap = maps.get(maps.size() - 1);
                } else {
                    int currentIndex = maps.indexOf(currentMap);
                    if (currentIndex <= 0) {
                        newMap = maps.get(maps.size() - 1); // Wrap to last map
                    } else {
                        newMap = maps.get(currentIndex - 1);
                    }
                }
                break;
            case "random":
                if (maps.size() == 1) {
                    newMap = maps.get(0);
                } else {
                    // Get a random map that's not the current one
                    do {
                        newMap = maps.get((int) (Math.random() * maps.size()));
                    } while (newMap.equals(currentMap) && maps.size() > 1);
                }
                break;
            default:
                // Assume it's a map name
                if (!maps.contains(rotationType)) {
                    sender.sendMessage(prefix + ChatColor.RED + "Map '" + rotationType + "' does not exist!");
                    return;
                }
                newMap = rotationType;
                break;
        }
        
        plugin.recordMapSwap(newMap);
        
        // In a full implementation, you would teleport all players, reset regions, etc.
        sender.sendMessage(prefix + ChatColor.GREEN + "Rotated to map '" + newMap + "'.");
        
        // Broadcast to all players if enabled in config
        if (plugin.getConfig().getBoolean("rotation.broadcast", true)) {
            Bukkit.broadcastMessage(prefix + ChatColor.GREEN + "Map changed to: " + 
                                   ChatColor.AQUA + newMap);
        }
    }

    private void rollbackMap(CommandSender sender) {
        String previousMap = plugin.rollbackToPreviousMap();
        
        if (previousMap == null) {
            sender.sendMessage(prefix + ChatColor.RED + "No previous map to roll back to!");
            return;
        }
        
        // In a full implementation, you would teleport all players, reset regions, etc.
        sender.sendMessage(prefix + ChatColor.GREEN + "Rolled back to map '" + previousMap + "'.");
        
        // Broadcast to all players if enabled in config
        if (plugin.getConfig().getBoolean("rotation.broadcast", true)) {
            Bukkit.broadcastMessage(prefix + ChatColor.GREEN + "Map rolled back to: " + 
                                   ChatColor.AQUA + previousMap);
        }
    }

    private void displayVersion(CommandSender sender) {
        sender.sendMessage(ChatColor.AQUA + "MapMorph " + ChatColor.WHITE + 
                           plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.GRAY + "Author: " + ChatColor.WHITE + 
                           "Kingdragoncat");
        sender.sendMessage(ChatColor.GRAY + "Website: " + ChatColor.WHITE + 
                           "https://github.com/Kingdragoncat/MapMorph");
    }
}

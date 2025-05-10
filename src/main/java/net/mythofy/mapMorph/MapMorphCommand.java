package net.mythofy.mapMorph;

import net.mythofy.mapMorph.api.MapMorphAPI;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MapMorphCommand implements CommandExecutor {

    private final MapMorph plugin;

    public MapMorphCommand(MapMorph plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§aMapMorph plugin is running! Use /mapmorph help for commands.");
            return true;
        }

        if (args[0].equalsIgnoreCase("setspawn")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cOnly players can use this command.");
                return true;
            }
            if (args.length < 3) {
                sender.sendMessage("§cUsage: /mapmorph setspawn <map> <index>");
                return true;
            }
            String mapName = args[1];
            int index;
            try {
                index = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage("§cIndex must be a number.");
                return true;
            }
            Player player = (Player) sender;
            Location loc = player.getLocation();

            // Save location to config: maps.<mapName>.spawns.<index>
            String path = "maps." + mapName + ".spawns." + index;
            plugin.getConfig().set(path + ".world", loc.getWorld().getName());
            plugin.getConfig().set(path + ".x", loc.getX());
            plugin.getConfig().set(path + ".y", loc.getY());
            plugin.getConfig().set(path + ".z", loc.getZ());
            plugin.getConfig().set(path + ".yaw", loc.getYaw());
            plugin.getConfig().set(path + ".pitch", loc.getPitch());
            plugin.saveConfig();

            sender.sendMessage("§aSet spawn point #" + index + " for map '" + mapName + "' at your current location.");
            return true;
        }

        if (args[0].equalsIgnoreCase("setregion")) {
            if (args.length < 3) {
                sender.sendMessage("§cUsage: /mapmorph setregion <map> <regionid>");
                return true;
            }
            String mapName = args[1];
            String regionId = args[2];

            // Add regionId to maps.<mapName>.regions list in config
            String path = "maps." + mapName + ".regions";
            List<String> regions = plugin.getConfig().getStringList(path);
            if (!regions.contains(regionId)) {
                regions.add(regionId);
                plugin.getConfig().set(path, regions);
                plugin.saveConfig();
                sender.sendMessage("§aLinked region '" + regionId + "' to map '" + mapName + "'.");
            } else {
                sender.sendMessage("§eRegion '" + regionId + "' is already linked to map '" + mapName + "'.");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("list")) {
            List<String> maps = new java.util.ArrayList<>();
            if (plugin.getConfig().contains("maps")) {
                maps = new java.util.ArrayList<>(plugin.getConfig().getConfigurationSection("maps").getKeys(false));
            }
            
            if (maps.isEmpty()) {
                sender.sendMessage("§eNo maps are defined.");
            } else {
                sender.sendMessage("§aAvailable maps: §f" + String.join(", ", maps));
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("preview")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cOnly players can use this command.");
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage("§cUsage: /mapmorph preview <map> [x] [y] [z]");
                return true;
            }
            String mapName = args[1];
            File schematicFile = new File(plugin.getMapsFolder(), mapName + ".schem");
            if (!schematicFile.exists()) {
                sender.sendMessage("§cSchematic file for map '" + mapName + "' not found.");
                return true;
            }
            Player player = (Player) sender;
            Location pasteLoc;
            if (args.length >= 5) {
                try {
                    double x = Double.parseDouble(args[2]);
                    double y = Double.parseDouble(args[3]);
                    double z = Double.parseDouble(args[4]);
                    pasteLoc = new Location(player.getWorld(), x, y, z);
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cInvalid coordinates.");
                    return true;
                }
            } else {
                pasteLoc = player.getLocation();
            }

            sender.sendMessage("§aPreviewing schematic for map '" + mapName + "' at " +
                    pasteLoc.getBlockX() + ", " + pasteLoc.getBlockY() + ", " + pasteLoc.getBlockZ() +
                    ". This will auto-undo in 30 seconds.");

            // Paste schematic and schedule undo
            FaweSchematicPreviewer.previewSchematic(plugin, schematicFile, pasteLoc, player, 30);

            return true;
        }

        if (args[0].equalsIgnoreCase("validate")) {
            if (args.length < 2) {
                sender.sendMessage("§cUsage: /mapmorph validate <map>");
                return true;
            }
            String mapName = args[1];
            MapValidator validator = new MapValidator(plugin);
            boolean valid = validator.isValid(mapName);
            String msg = validator.getValidationMessage(mapName);
            if (valid) {
                sender.sendMessage("§aMap '" + mapName + "' is valid!");
            } else {
                sender.sendMessage("§cMap '" + mapName + "' is invalid: " + msg);
            }
            return true;
        }
        
        if (args[0].equalsIgnoreCase("load") || args[0].equalsIgnoreCase("switch")) {
            if (args.length < 2) {
                sender.sendMessage("§cUsage: /mapmorph load <map> [countdown-seconds]");
                return true;
            }
            
            String mapName = args[1];
            int countdown = 0;
            
            if (args.length >= 3) {
                try {
                    countdown = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cCountdown must be a number of seconds.");
                    return true;
                }
            }
            
            // Use our API to switch the map
            sender.sendMessage("§6Starting map switch to '" + mapName + "'" + 
                    (countdown > 0 ? " with a " + countdown + " second countdown..." : "..."));
                    
            CompletableFuture<Boolean> future = MapMorphAPI.switchMap(mapName, countdown);
            future.thenAccept(success -> {
                if (success) {
                    sender.sendMessage("§aSuccessfully switched to map '" + mapName + "'!");
                    
                    // Teleport all players to spawn points
                    MapMorphAPI.teleportAllPlayersToSpawn();
                } else {
                    sender.sendMessage("§cFailed to switch to map '" + mapName + "'. Check console for errors.");
                }
            });
            
            return true;
        }
        
        if (args[0].equalsIgnoreCase("rollback") || args[0].equalsIgnoreCase("previous")) {
            String previousMap = plugin.rollbackToPreviousMap();
            if (previousMap != null) {
                sender.sendMessage("§6Rolling back to previous map: '" + previousMap + "'...");
                
                // Use the API to perform the actual switch
                CompletableFuture<Boolean> future = MapMorphAPI.switchMap(previousMap, 0);
                future.thenAccept(success -> {
                    if (success) {
                        sender.sendMessage("§aSuccessfully rolled back to map '" + previousMap + "'!");
                        MapMorphAPI.teleportAllPlayersToSpawn();
                    } else {
                        sender.sendMessage("§cFailed to roll back to map '" + previousMap + "'. Check console for errors.");
                    }
                });
            } else {
                sender.sendMessage("§cNo previous map to roll back to!");
            }
            return true;
        }
        
        if (args[0].equalsIgnoreCase("tp") || args[0].equalsIgnoreCase("teleport")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cOnly players can use this command.");
                return true;
            }
            
            Player player = (Player) sender;
            boolean success = MapMorphAPI.teleportPlayerToSpawn(player);
            
            if (success) {
                player.sendMessage("§aTeleported to a spawn point.");
            } else {
                player.sendMessage("§cNo valid spawn points found for the current map.");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("help")) {
            sender.sendMessage("§e=== MapMorph Commands ===");
            sender.sendMessage("§6/mapmorph §7- Show basic plugin info");
            sender.sendMessage("§6/mapmorph list §7- List all available maps");
            sender.sendMessage("§6/mapmorph validate <map> §7- Check if a map is valid");
            sender.sendMessage("§6/mapmorph preview <map> [x y z] §7- Preview a map schematic");
            sender.sendMessage("§6/mapmorph load <map> [countdown] §7- Switch to a different map");
            sender.sendMessage("§6/mapmorph rollback §7- Switch to the previous map");
            sender.sendMessage("§6/mapmorph tp §7- Teleport to a spawn point");
            sender.sendMessage("§6/mapmorph setspawn <map> <index> §7- Set a spawn point");
            sender.sendMessage("§6/mapmorph setregion <map> <regionid> §7- Link a WorldGuard region");
            return true;
        }

        sender.sendMessage("§cUnknown subcommand. Use /mapmorph help for commands.");
        return true;
    }
}
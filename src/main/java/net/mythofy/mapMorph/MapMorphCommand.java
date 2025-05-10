package net.mythofy.mapMorph;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.List;

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

        sender.sendMessage("§cUnknown subcommand. Use /mapmorph help for commands.");
        return true;
    }
}

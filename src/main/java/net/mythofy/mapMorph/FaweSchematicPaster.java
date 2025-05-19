package net.mythofy.mapMorph;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class FaweSchematicPaster {

    /**
     * Pastes a schematic asynchronously using WorldEdit, with safety and optimization.
     *
     * @param plugin      The plugin instance (for scheduling callbacks)
     * @param schematic   The schematic file to paste
     * @param worldName   The world to paste into
     * @param origin      The origin location for the paste
     * @param onComplete  Callback to run after paste is complete
     */
    public static void pasteSchematicAsync(Plugin plugin, File schematic, String worldName, Location origin, Consumer<Boolean> onComplete) {
        // Get default countdown from config
        int defaultCountdown = plugin.getConfig().getInt("schematics.default-countdown", 5);
        
        // Call the more advanced method with parameters from config
        pasteSchematicWithCountdown(plugin, schematic, worldName, origin, null, defaultCountdown, onComplete);
    }

    /**
     * Pastes a schematic asynchronously using WorldEdit, with safety, countdown, and fade-to-black.
     *
     * @param plugin      The plugin instance (for scheduling callbacks)
     * @param schematic   The schematic file to paste
     * @param worldName   The world to paste into
     * @param origin      The origin location for the paste
     * @param safeLoc     The location to teleport players to (if null, uses world spawn)
     * @param countdown   Seconds to wait before pasting (0 for instant)
     * @param onComplete  Callback to run after paste is complete
     */
    public static void pasteSchematicWithCountdown(Plugin plugin, File schematic, String worldName, Location origin, Location safeLoc, int countdown, Consumer<Boolean> onComplete) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (FileInputStream fis = new FileInputStream(schematic)) {
                var format = ClipboardFormats.findByFile(schematic);
                if (format == null) throw new IllegalArgumentException("Unknown schematic format: " + schematic.getName());
                ClipboardReader reader = format.getReader(fis);
                Clipboard clipboard = reader.read();

                org.bukkit.World bukkitWorld = Bukkit.getWorld(worldName);
                if (bukkitWorld == null) throw new IllegalArgumentException("Bukkit world not found: " + worldName);

                // Calculate schematic region bounds
                BlockVector3 min = clipboard.getRegion().getMinimumPoint().add(origin.getBlockX(), origin.getBlockY(), origin.getBlockZ());
                BlockVector3 max = clipboard.getRegion().getMaximumPoint().add(origin.getBlockX(), origin.getBlockY(), origin.getBlockZ());

                List<Player> playersInRegion = bukkitWorld.getPlayers().stream()
                        .filter(p -> isInRegion(p.getLocation(), min, max))
                        .collect(Collectors.toCollection(ArrayList::new));

                // Run countdown on main thread
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (countdown > 0) {
                        for (Player player : playersInRegion) {
                            player.sendTitle("§eMap changing soon!", "§fTeleporting in " + countdown + " seconds...", 10, 70, 20);
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                        }
                    }
                    runCountdown(plugin, playersInRegion, countdown, () -> {
                        // Check if fade effect is enabled in config
                        boolean useFadeEffect = true; // default
                        if (plugin instanceof MapMorph) {
                            useFadeEffect = ((MapMorph) plugin).getConfig().getBoolean("schematics.use-fade-effect", true);
                        }
                        
                        if (useFadeEffect) {
                            // Fade-to-black (blindness) for 2 seconds
                            for (Player player : playersInRegion) {
                                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 1, false, false, false));
                            }
                        }
                    
                        // Check if teleport is enabled in config
                        boolean teleportPlayers = true; // default
                        if (plugin instanceof MapMorph) {
                            teleportPlayers = ((MapMorph) plugin).getConfig().getBoolean("schematics.teleport-players", true);
                        }
                        
                        if (teleportPlayers) {
                            // Teleport players to safe location
                            Location safe = safeLoc != null ? safeLoc : bukkitWorld.getSpawnLocation();
                            
                            // Get message from config if possible
                            String teleportMessage = "§eYou were moved to safety for a map update!";
                            if (plugin instanceof MapMorph) {
                                teleportMessage = ((MapMorph) plugin).getConfig().getString("schematics.transition-message", teleportMessage)
                                        .replace("&", "§"); // Convert color codes
                            }
                            
                            for (Player player : playersInRegion) {
                                player.teleport(safe);
                                player.sendMessage(teleportMessage);
                            }
                        }

                        // Check if entity clearing is enabled in config
                        boolean clearEntities = true; // default
                        if (plugin instanceof MapMorph) {
                            clearEntities = ((MapMorph) plugin).getConfig().getBoolean("schematics.clear-entities", true);
                        }
                        
                        if (clearEntities) {
                            // Remove dropped items in the region
                            List<Entity> items = bukkitWorld.getEntities().stream()
                                    .filter(e -> e instanceof Item && isInRegion(e.getLocation(), min, max))
                                    .collect(Collectors.toCollection(ArrayList::new));
                            for (Entity item : items) {
                                item.remove();
                            }
                        }

                        // Now paste the schematic on the main thread
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            boolean success = false;
                            try {
                                // Create WorldEdit session
                                EditSession editSession = WorldEdit.getInstance().newEditSession(
                                        BukkitAdapter.adapt(bukkitWorld)
                                );
                                
                                // Paste the schematic
                                BlockVector3 to = BlockVector3.at(origin.getBlockX(), origin.getBlockY(), origin.getBlockZ());
                                ClipboardHolder holder = new ClipboardHolder(clipboard);
                                
                                Operation operation = holder
                                        .createPaste(editSession)
                                        .to(to)
                                        .ignoreAirBlocks(false)
                                        .build();
                                
                                Operations.complete(operation);
                                editSession.close();
                                success = true;
                            } catch (Exception e) {
                                plugin.getLogger().log(Level.SEVERE, "Failed to paste schematic: " + schematic.getName(), e);
                            }
                            boolean finalSuccess = success;
                            onComplete.accept(finalSuccess);
                        });
                    });
                });
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to prepare schematic: " + schematic.getName(), e);
                Bukkit.getScheduler().runTask(plugin, () -> onComplete.accept(false));
            }
        });
    }

    private static void runCountdown(Plugin plugin, List<Player> players, int seconds, Runnable onFinish) {
        if (seconds <= 0) {
            onFinish.run();
            return;
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            int next = seconds - 1;
            
            // Get message format from config if possible
            String messageFormat = "§eMap changing in §c{time}§e...";
            if (plugin.getConfig().contains("rotation.announce-format")) {
                messageFormat = plugin.getConfig().getString("rotation.announce-format", messageFormat)
                        .replace("&", "§"); // Convert color codes
            }
            
            String message = messageFormat.replace("{time}", String.valueOf(seconds));
            
            for (Player player : players) {
                player.sendActionBar(message);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            }
            if (next > 0) {
                runCountdown(plugin, players, next, onFinish);
            } else {
                onFinish.run();
            }
        }, 20L); // 20 ticks = 1 second
    }

    /**
     * Checks if a location is within a cuboid region defined by min and max BlockVector3.
     */
    private static boolean isInRegion(Location loc, BlockVector3 min, BlockVector3 max) {
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        return x >= min.getBlockX() && x <= max.getBlockX()
            && y >= min.getBlockY() && y <= max.getBlockY()
            && z >= min.getBlockZ() && z <= max.getBlockZ();
    }
}

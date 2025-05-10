package net.mythofy.mapMorph;

import com.fastasyncworldedit.core.FaweAPI;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
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
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class FaweSchematicPaster {

    /**
     * Pastes a schematic asynchronously using FAWE, with safety and optimization.
     *
     * @param plugin      The plugin instance (for scheduling callbacks)
     * @param schematic   The schematic file to paste
     * @param worldName   The world to paste into
     * @param origin      The origin location for the paste
     * @param onComplete  Callback to run after paste is complete
     */
    public static void pasteSchematicAsync(Plugin plugin, File schematic, String worldName, Location origin, Consumer<Boolean> onComplete) {
        // Call the more advanced method with default parameters
        pasteSchematicWithCountdown(plugin, schematic, worldName, origin, null, 0, onComplete);
    }

    /**
     * Pastes a schematic asynchronously using FAWE, with safety, countdown, and fade-to-black.
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
                ClipboardFormat format = ClipboardFormat.findByFile(schematic);
                if (format == null) throw new IllegalArgumentException("Unknown schematic format: " + schematic.getName());
                ClipboardReader reader = format.getReader(fis);
                Clipboard clipboard = reader.read();

                World weWorld = FaweAPI.getWorld(worldName);
                if (weWorld == null) throw new IllegalArgumentException("World not found: " + worldName);

                // Calculate schematic region bounds
                BlockVector3 min = clipboard.getRegion().getMinimumPoint().add(origin.getBlockX(), origin.getBlockY(), origin.getBlockZ());
                BlockVector3 max = clipboard.getRegion().getMaximumPoint().add(origin.getBlockX(), origin.getBlockY(), origin.getBlockZ());

                org.bukkit.World bukkitWorld = Bukkit.getWorld(worldName);
                if (bukkitWorld == null) throw new IllegalArgumentException("Bukkit world not found: " + worldName);

                List<Player> playersInRegion = bukkitWorld.getPlayers().stream()
                        .filter(p -> isInRegion(p.getLocation(), min, max))
                        .collect(Collectors.toList());

                // Run countdown on main thread
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (countdown > 0) {
                        for (Player player : playersInRegion) {
                            player.sendTitle("§eMap changing soon!", "§fTeleporting in " + countdown + " seconds...", 10, 70, 20);
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                        }
                    }
                    runCountdown(plugin, playersInRegion, countdown, () -> {
                        // Fade-to-black (blindness) for 2 seconds
                        for (Player player : playersInRegion) {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 1, false, false, false));
                        }

                        // Teleport players to safe location
                        Location safe = safeLoc != null ? safeLoc : bukkitWorld.getSpawnLocation();
                        for (Player player : playersInRegion) {
                            player.teleport(safe);
                            player.sendMessage("§eYou were moved to safety for a map update!");
                        }

                        // Remove dropped items in the region
                        List<Entity> items = bukkitWorld.getEntities().stream()
                                .filter(e -> e instanceof Item && isInRegion(e.getLocation(), min, max))
                                .collect(Collectors.toList());
                        for (Entity item : items) {
                            item.remove();
                        }

                        // Now paste the schematic asynchronously
                        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                            boolean success = false;
                            try (EditSession editSession = FaweAPI.getEditSessionBuilder(weWorld).fastmode(true).build()) {
                                BlockVector3 to = BlockVector3.at(origin.getBlockX(), origin.getBlockY(), origin.getBlockZ());
                                ClipboardHolder holder = new ClipboardHolder(clipboard);
                                editSession.setReorderMode(EditSession.ReorderMode.MULTI_STAGE);
                                editSession.enableQueue();
                                editSession.paste(holder, to, false, false, null);
                                editSession.flushQueue();
                                success = true;
                            } catch (Exception e) {
                                plugin.getLogger().log(Level.SEVERE, "Failed to paste schematic: " + schematic.getName(), e);
                            }
                            boolean finalSuccess = success;
                            Bukkit.getScheduler().runTask(plugin, () -> onComplete.accept(finalSuccess));
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
            for (Player player : players) {
                player.sendActionBar("§eMap changing in §c" + seconds + "§e...");
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

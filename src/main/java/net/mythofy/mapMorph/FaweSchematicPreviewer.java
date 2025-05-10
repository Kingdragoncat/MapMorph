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
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Provides schematic preview functionality using WorldEdit.
 * This creates a temporary paste that will be automatically reverted after a set time.
 */
public class FaweSchematicPreviewer {
    
    // Store sessions for undoing later
    private static final Map<UUID, EditSession> playerSessions = new HashMap<>();

    /**
     * Previews a schematic by temporarily pasting it and then undoing.
     *
     * @param plugin      The plugin instance
     * @param schematic   The schematic file to preview
     * @param origin      The location to paste at
     * @param player      The player previewing the schematic
     * @param seconds     How long to show the preview before undoing
     */
    public static void previewSchematic(Plugin plugin, File schematic, Location origin, Player player, int seconds) {
        // Remove any existing preview for this player
        clearExistingPreview(player);
        
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (FileInputStream fis = new FileInputStream(schematic)) {
                var format = ClipboardFormats.findByFile(schematic);
                if (format == null) {
                    player.sendMessage("§cUnknown schematic format: " + schematic.getName());
                    return;
                }
                
                ClipboardReader reader = format.getReader(fis);
                Clipboard clipboard = reader.read();
                
                // Run on main thread to be safe with WorldEdit API
                Bukkit.getScheduler().runTask(plugin, () -> {
                    EditSession editSession = null;
                    try {
                        // Create a temporary edit session
                        editSession = WorldEdit.getInstance().newEditSession(
                                BukkitAdapter.adapt(origin.getWorld())
                        );
                        
                        // Paste the schematic
                        BlockVector3 to = BlockVector3.at(origin.getBlockX(), origin.getBlockY(), origin.getBlockZ());
                        ClipboardHolder holder = new ClipboardHolder(clipboard);
                        
                        Operation operation = holder
                                .createPaste(editSession)
                                .to(to)
                                .ignoreAirBlocks(true)
                                .build();
                        
                        Operations.complete(operation);
                        
                        // Store the session for undoing later
                        final EditSession finalSession = editSession;
                        playerSessions.put(player.getUniqueId(), finalSession);
                        
                        // Schedule undo after specified seconds
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            clearExistingPreview(player);
                        }, seconds * 20L);
                        
                        player.sendMessage("§aPreview created! It will be removed in " + seconds + " seconds.");
                    } catch (Exception e) {
                        player.sendMessage("§cError creating preview: " + e.getMessage());
                        plugin.getLogger().log(Level.SEVERE, "Error creating schematic preview", e);
                        
                        if (editSession != null) {
                            editSession.close();
                        }
                    }
                });
            } catch (Exception e) {
                player.sendMessage("§cFailed to read schematic: " + e.getMessage());
                plugin.getLogger().log(Level.SEVERE, "Failed to read schematic: " + schematic.getName(), e);
            }
        });
    }
    
    /**
     * Clears any existing preview for a player
     */
    private static void clearExistingPreview(Player player) {
        EditSession session = playerSessions.remove(player.getUniqueId());
        if (session != null) {
            try {
                // Undo the changes on the main thread
                Bukkit.getScheduler().runTask(player.getServer().getPluginManager().getPlugins()[0], () -> {
                    try {
                        session.undo(session);
                        player.sendMessage("§ePreview has been removed.");
                    } catch (Exception e) {
                        player.getServer().getLogger().log(Level.WARNING, "Failed to undo preview", e);
                    } finally {
                        session.close();
                    }
                });
            } catch (Exception e) {
                player.getServer().getLogger().log(Level.WARNING, "Failed to schedule undo task", e);
            }
        }
    }
}
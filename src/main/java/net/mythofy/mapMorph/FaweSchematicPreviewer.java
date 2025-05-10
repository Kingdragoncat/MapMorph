package net.mythofy.mapMorph;

import com.fastasyncworldedit.core.FaweAPI;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileInputStream;
import java.util.logging.Level;

public class FaweSchematicPreviewer {

    /**
     * Paste a schematic for preview and auto-undo after delaySeconds.
     */
    public static void previewSchematic(Plugin plugin, File schematic, Location origin, Player player, int delaySeconds) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (FileInputStream fis = new FileInputStream(schematic)) {
                ClipboardFormat format = ClipboardFormat.findByFile(schematic);
                if (format == null) throw new IllegalArgumentException("Unknown schematic format: " + schematic.getName());
                ClipboardReader reader = format.getReader(fis);
                Clipboard clipboard = reader.read();

                World weWorld = FaweAPI.getWorld(origin.getWorld().getName());
                if (weWorld == null) throw new IllegalArgumentException("World not found: " + origin.getWorld().getName());

                try (EditSession editSession = FaweAPI.getEditSessionBuilder(weWorld).fastmode(true).build()) {
                    BlockVector3 to = BlockVector3.at(origin.getBlockX(), origin.getBlockY(), origin.getBlockZ());
                    ClipboardHolder holder = new ClipboardHolder(clipboard);
                    editSession.setReorderMode(EditSession.ReorderMode.MULTI_STAGE);
                    editSession.enableQueue();
                    editSession.paste(holder, to, false, false, null);
                    editSession.flushQueue();

                    // Schedule undo after delay
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        try {
                            editSession.undo(editSession);
                            player.sendMessage("§ePreview for '" + schematic.getName() + "' has been undone.");
                        } catch (WorldEditException e) {
                            plugin.getLogger().log(Level.SEVERE, "Failed to undo schematic preview.", e);
                        }
                    }, delaySeconds * 20L);
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to preview schematic: " + schematic.getName(), e);
                player.sendMessage("§cFailed to preview schematic: " + e.getMessage());
            }
        });
    }
}
package net.mythofy.mapMorph;

import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.stream.Collectors;

public class AsyncCleanupUtil {

    /**
     * Removes dropped items and mobs in the specified cuboid region.
     * @param plugin The plugin instance
     * @param worldName The world name
     * @param min The minimum corner of the region
     * @param max The maximum corner of the region
     */
    public static void cleanupRegionAsync(Plugin plugin, String worldName, BlockVector3 min, BlockVector3 max) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            World world = Bukkit.getWorld(worldName);
            if (world == null) return;

            List<Entity> toRemove = world.getEntities().stream()
                    .filter(e -> isInRegion(e.getLocation(), min, max) &&
                            (e instanceof Item || (e instanceof LivingEntity && !(e instanceof Player))))
                    .collect(Collectors.toList());

            Bukkit.getScheduler().runTask(plugin, () -> {
                for (Entity entity : toRemove) {
                    entity.remove();
                }
            });
        });
    }

    private static boolean isInRegion(Location loc, BlockVector3 min, BlockVector3 max) {
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        return x >= min.getBlockX() && x <= max.getBlockX()
                && y >= min.getBlockY() && y <= max.getBlockY()
                && z >= min.getBlockZ() && z <= max.getBlockZ();
    }
}
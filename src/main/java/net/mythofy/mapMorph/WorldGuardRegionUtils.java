package net.mythofy.mapMorph;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WorldGuardRegionUtils {

    /**
     * Enables only the regions associated with the given map in the specified world.
     * Optionally disables/removes all other regions.
     *
     * @param worldName The Bukkit world name.
     * @param regionIds The list of region IDs to enable.
     */
    public static void activateRegionsForMap(String worldName, List<String> regionIds) {
        World bukkitWorld = Bukkit.getWorld(worldName);
        if (bukkitWorld == null) return;

        RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(bukkitWorld));
        if (regionManager == null) return;

        Set<String> toKeep = new HashSet<>(regionIds);

        // Remove regions not in the list
        for (String regionId : regionManager.getRegions().keySet()) {
            if (!toKeep.contains(regionId)) {
                regionManager.removeRegion(regionId);
            }
        }

        // Optionally, you could re-import or re-enable regions here if needed
        // For now, this just ensures only the desired regions remain
    }

    /**
     * Utility to check if a region exists in the world.
     */
    public static boolean regionExists(String worldName, String regionId) {
        World bukkitWorld = Bukkit.getWorld(worldName);
        if (bukkitWorld == null) return false;
        RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(bukkitWorld));
        if (regionManager == null) return false;
        return regionManager.hasRegion(regionId);
    }
}
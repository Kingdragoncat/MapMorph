package net.mythofy.mapMorph;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MapRotationManager {

    private final JavaPlugin plugin;
    private BukkitTask rotationTask;
    private List<String> rotationMaps;
    private int intervalMinutes;
    private AtomicInteger currentIndex = new AtomicInteger(0);

    public MapRotationManager(JavaPlugin plugin) {
        this.plugin = plugin;
        reloadConfig();
    }

    public void reloadConfig() {
        this.rotationMaps = plugin.getConfig().getStringList("rotation.maps");
        this.intervalMinutes = plugin.getConfig().getInt("rotation.interval_minutes", 10);
        this.currentIndex.set(0);
    }

    public void startRotation() {
        if (rotationTask != null && !rotationTask.isCancelled()) {
            rotationTask.cancel();
        }
        if (rotationMaps == null || rotationMaps.isEmpty()) {
            plugin.getLogger().warning("No maps set for rotation!");
            return;
        }
        rotationTask = Bukkit.getScheduler().runTaskTimer(plugin, this::rotateToNextMap, 0L, intervalMinutes * 60L * 20L);
        plugin.getLogger().info("Map rotation started. Interval: " + intervalMinutes + " minutes.");
    }

    public void stopRotation() {
        if (rotationTask != null) {
            rotationTask.cancel();
            rotationTask = null;
            plugin.getLogger().info("Map rotation stopped.");
        }
    }

    public void rotateToNextMap() {
        if (rotationMaps == null || rotationMaps.isEmpty()) return;
        int idx = currentIndex.getAndUpdate(i -> (i + 1) % rotationMaps.size());
        String nextMap = rotationMaps.get(idx);
        plugin.getLogger().info("Rotating to map: " + nextMap);
        // TODO: Call your map swap logic here, e.g. plugin.swapToMap(nextMap);
    }

    public boolean isRunning() {
        return rotationTask != null && !rotationTask.isCancelled();
    }
}
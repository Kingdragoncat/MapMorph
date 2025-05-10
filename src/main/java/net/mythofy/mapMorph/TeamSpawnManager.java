package net.mythofy.mapMorph;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class TeamSpawnManager {

    private final JavaPlugin plugin;

    public TeamSpawnManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Get all team names for a map from config.
     */
    public Set<String> getTeamsForMap(String mapName) {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("maps." + mapName + ".teams");
        if (section == null) return Collections.emptySet();
        return section.getKeys(false);
    }

    /**
     * Get all spawn locations for a team on a map.
     */
    public List<Location> getSpawnsForTeam(String mapName, String team) {
        List<Location> spawns = new ArrayList<>();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("maps." + mapName + ".teams." + team + ".spawns");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                String path = "maps." + mapName + ".teams." + team + ".spawns." + key;
                String world = plugin.getConfig().getString(path + ".world");
                double x = plugin.getConfig().getDouble(path + ".x");
                double y = plugin.getConfig().getDouble(path + ".y");
                double z = plugin.getConfig().getDouble(path + ".z");
                float yaw = (float) plugin.getConfig().getDouble(path + ".yaw");
                float pitch = (float) plugin.getConfig().getDouble(path + ".pitch");
                World bukkitWorld = Bukkit.getWorld(world);
                if (bukkitWorld != null) {
                    spawns.add(new Location(bukkitWorld, x, y, z, yaw, pitch));
                }
            }
        }
        return spawns;
    }

    /**
     * Assign players to teams as evenly as possible.
     */
    public Map<UUID, String> autoAssignTeams(Collection<? extends Player> players, Set<String> teams) {
        Map<UUID, String> assignments = new HashMap<>();
        List<String> teamList = new ArrayList<>(teams);
        int teamCount = teamList.size();
        int i = 0;
        for (Player player : players) {
            String team = teamList.get(i % teamCount);
            assignments.put(player.getUniqueId(), team);
            i++;
        }
        return assignments;
    }

    /**
     * Teleport players to their team's spawn points (round-robin).
     */
    public void teleportTeamsToSpawns(String mapName, Map<UUID, String> assignments) {
        Map<String, Integer> teamSpawnIndex = new HashMap<>();
        for (Map.Entry<UUID, String> entry : assignments.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null) continue;
            String team = entry.getValue();
            List<Location> spawns = getSpawnsForTeam(mapName, team);
            if (spawns.isEmpty()) continue;
            int idx = teamSpawnIndex.getOrDefault(team, 0);
            Location spawn = spawns.get(idx % spawns.size());
            player.teleport(spawn);
            teamSpawnIndex.put(team, idx + 1);
            player.sendMessage("§aTeleported to " + team + " spawn for map " + mapName + "!");
        }
    }
}
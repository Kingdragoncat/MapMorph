package net.mythofy.mapMorph;

import org.bukkit.Location;
import java.io.File;
import java.util.List;

public class SchematicMetadata {
    private final String name;
    private final File file;
    private List<Location> teleportPoints;
    private List<String> worldGuardRegionIds;

    public SchematicMetadata(String name, File file) {
        this.name = name;
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public File getFile() {
        return file;
    }

    public List<Location> getTeleportPoints() {
        return teleportPoints;
    }

    public void setTeleportPoints(List<Location> teleportPoints) {
        this.teleportPoints = teleportPoints;
    }

    public List<String> getWorldGuardRegionIds() {
        return worldGuardRegionIds;
    }

    public void setWorldGuardRegionIds(List<String> worldGuardRegionIds) {
        this.worldGuardRegionIds = worldGuardRegionIds;
    }
}
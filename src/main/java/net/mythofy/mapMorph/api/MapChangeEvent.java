package net.mythofy.mapMorph.api;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event that is fired when a map is changed.
 * Other plugins can listen to this event to respond to map changes.
 */
public class MapChangeEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final String mapName;

    /**
     * Creates a new MapChangeEvent.
     *
     * @param mapName The name of the new map
     */
    public MapChangeEvent(String mapName) {
        this.mapName = mapName;
    }

    /**
     * Gets the name of the new map.
     *
     * @return The map name
     */
    public String getMapName() {
        return mapName;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Required by Bukkit's event system.
     * 
     * @return The handler list for this event
     */
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
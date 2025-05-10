package net.mythofy.mapMorph;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MapMorphPreSwapEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final String fromMap;
    private final String toMap;
    private boolean cancelled = false;

    public MapMorphPreSwapEvent(String fromMap, String toMap) {
        this.fromMap = fromMap;
        this.toMap = toMap;
    }

    public String getFromMap() {
        return fromMap;
    }

    public String getToMap() {
        return toMap;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
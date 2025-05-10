package net.mythofy.mapMorph;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MapMorphPostSwapEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final String fromMap;
    private final String toMap;

    public MapMorphPostSwapEvent(String fromMap, String toMap) {
        this.fromMap = fromMap;
        this.toMap = toMap;
    }

    public String getFromMap() {
        return fromMap;
    }

    public String getToMap() {
        return toMap;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
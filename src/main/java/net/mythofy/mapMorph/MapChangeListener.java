package net.mythofy.mapMorph;

import net.mythofy.mapMorph.api.MapChangeCallback;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;

/**
 * Manages callbacks for map change events.
 */
public class MapChangeListener {
    
    private final List<MapChangeCallback> callbacks = new ArrayList<>();
    
    /**
     * Registers a callback to be notified on map changes.
     * 
     * @param callback The callback to register
     */
    public void registerCallback(MapChangeCallback callback) {
        if (!callbacks.contains(callback)) {
            callbacks.add(callback);
        }
    }
    
    /**
     * Unregisters a callback.
     * 
     * @param callback The callback to unregister
     */
    public void unregisterCallback(MapChangeCallback callback) {
        callbacks.remove(callback);
    }
    
    /**
     * Notifies all registered callbacks that the map has changed.
     * 
     * @param newMapName The name of the new map
     */
    public void notifyCallbacks(String newMapName) {
        for (MapChangeCallback callback : new ArrayList<>(callbacks)) {
            try {
                callback.onMapChange(newMapName);
            } catch (Exception e) {
                // Prevent one bad callback from breaking others
                Bukkit.getLogger().warning("Error in map change callback: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Gets the number of registered callbacks.
     *
     * @return The count of callbacks
     */
    public int getCallbackCount() {
        return callbacks.size();
    }
    
    /**
     * Clears all registered callbacks.
     * Useful when shutting down the plugin.
     */
    public void clearCallbacks() {
        callbacks.clear();
    }
}

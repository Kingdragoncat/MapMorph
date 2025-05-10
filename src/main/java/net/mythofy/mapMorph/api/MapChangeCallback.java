package net.mythofy.mapMorph.api;

/**
 * Functional interface for map change callbacks.
 * Implement this to receive notifications when the map changes.
 */
@FunctionalInterface
public interface MapChangeCallback {
    
    /**
     * Called when the map changes.
     * 
     * @param newMapName The name of the new map
     */
    void onMapChange(String newMapName);
}

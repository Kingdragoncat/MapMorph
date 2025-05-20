package com.mapmorph.plugin;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class CoreListener implements Listener {
    
    private final MapMorphPlugin plugin;
    
    public CoreListener(MapMorphPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Quick fail check before doing expensive operations
        ItemStack item = event.getItem();
        if (item == null || !isMapItem(item)) {
            return;
        }
        
        // Process map interaction
        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("Player " + event.getPlayer().getName() + " interacted with a map item");
        }
        
        // Handle map interaction logic here
    }
    
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Only process if necessary based on config settings
        if (!plugin.getConfigManager().getConfig().getBoolean("send-welcome-message", false)) {
            return;
        }
        
        // Send welcome message
    }
    
    private boolean isMapItem(ItemStack item) {
        // Efficient check for map items
        return item.getType().name().contains("MAP");
    }
}

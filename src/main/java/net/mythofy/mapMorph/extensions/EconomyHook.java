package net.mythofy.mapMorph.extensions;

import net.mythofy.mapMorph.MapMorph;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Provides integration with economy plugins through Vault.
 * Supports map-specific rewards and currency management.
 */
public class EconomyHook {
    
    private final MapMorph plugin;
    private final Logger logger;
    private boolean vaultEnabled = false;
    private Object economy = null; // Not directly using Vault's Economy to avoid hard dependency
    
    // Storage for map-specific rewards
    private final Map<String, Map<String, Double>> mapRewards = new HashMap<>();
    
    /**
     * Creates a new economy hook.
     * 
     * @param plugin The MapMorph plugin instance
     */
    public EconomyHook(MapMorph plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        setupEconomy();
    }
    
    /**
     * Sets up the economy hook with Vault.
     */
    private void setupEconomy() {
        // Check if economy features are enabled in config
        boolean economyEnabled = plugin.getConfig().getBoolean("economy.enabled", true);
        if (!economyEnabled) {
            logger.info("Economy features disabled in config");
            return;
        }
        
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            try {
                RegisteredServiceProvider<?> economyProvider = Bukkit.getServicesManager()
                        .getRegistration(Class.forName("net.milkbowl.vault.economy.Economy"));
                
                if (economyProvider != null) {
                    economy = economyProvider.getProvider();
                    vaultEnabled = true;
                    logger.info("Successfully hooked into Vault economy");
                    
                    // Load default rewards from config
                    loadDefaultRewards();
                } else {
                    logger.warning("Vault found, but no economy plugin detected");
                }
            } catch (ClassNotFoundException e) {
                logger.warning("Vault found, but economy class not found: " + e.getMessage());
            } catch (Exception e) {
                logger.warning("Error hooking into Vault economy: " + e.getMessage());
            }
        } else {
            logger.info("Vault not found, economy features disabled");
        }
    }
    
    /**
     * Loads default rewards from configuration
     */
    private void loadDefaultRewards() {
        if (plugin.getConfig().isConfigurationSection("economy.default-rewards")) {
            ConfigurationSection rewardsSection = plugin.getConfig().getConfigurationSection("economy.default-rewards");
            for (String key : rewardsSection.getKeys(false)) {
                double amount = rewardsSection.getDouble(key);
                // Register default rewards for all maps
                for (String mapName : plugin.listAllMaps()) {
                    // Check for map multiplier
                    double multiplier = plugin.getConfig().getDouble("economy.map-multipliers." + mapName, 1.0);
                    registerReward(mapName, key, amount * multiplier);
                }
            }
            logger.info("Loaded default rewards from config");
        }
    }
    
    /**
     * Checks if an economy plugin is available.
     * 
     * @return true if economy is available
     */
    public boolean isEconomyAvailable() {
        return vaultEnabled && economy != null;
    }
    
    /**
     * Awards currency to a player.
     * 
     * @param player The player to award currency to
     * @param amount The amount to award
     * @param reason A description of why the currency was awarded
     * @return true if successful, false if economy is not available
     */
    public boolean awardCurrency(Player player, double amount, String reason) {
        if (!isEconomyAvailable() || player == null) {
            return false;
        }
        
        // Check if rewards are enabled in config
        if (!plugin.getConfig().getBoolean("economy.award-rewards", true)) {
            return false;
        }
        
        try {
            // Using reflection to avoid direct Vault dependency
            Class<?> economyClass = economy.getClass();
            economyClass.getMethod("depositPlayer", Player.class, double.class)
                    .invoke(economy, player, amount);
            
            // Get currency symbol from config
            String currencySymbol = plugin.getConfig().getString("economy.currency-symbol", "$");
            
            // Check if rewards should be announced
            if (plugin.getConfig().getBoolean("economy.announce-rewards", true)) {
                // Get the format from config
                String format = plugin.getConfig().getString("economy.reward-format", 
                        "&a+{amount}{currency} &7- {reason}");
                
                // Replace placeholders in the format
                String message = format
                        .replace("{amount}", String.format("%.2f", amount))
                        .replace("{currency}", currencySymbol)
                        .replace("{reason}", reason)
                        .replace("&", "§"); // Convert color codes
                
                player.sendMessage(message);
            }
            
            return true;
        } catch (Exception e) {
            logger.warning("Failed to award currency to " + player.getName() + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Registers a reward for a specific action on a specific map.
     * 
     * @param mapName The map name
     * @param actionName The action name
     * @param amount The reward amount
     */
    public void registerReward(String mapName, String actionName, double amount) {
        mapRewards.computeIfAbsent(mapName, k -> new HashMap<>()).put(actionName, amount);
    }
    
    /**
     * Triggers a registered reward for a player.
     * 
     * @param player The player to reward
     * @param mapName The map name
     * @param actionName The action name
     * @return true if the reward was found and applied
     */
    public boolean triggerReward(Player player, String mapName, String actionName) {
        Map<String, Double> rewards = mapRewards.get(mapName);
        if (rewards == null) {
            return false;
        }
        
        Double amount = rewards.get(actionName);
        if (amount == null) {
            return false;
        }
        
        return awardCurrency(player, amount, "Reward for " + actionName + " on " + mapName);
    }
    
    /**
     * Gets the amount for a specific reward.
     * 
     * @param mapName The map name
     * @param actionName The action name
     * @return The reward amount, or 0 if not found
     */
    public double getRewardAmount(String mapName, String actionName) {
        Map<String, Double> rewards = mapRewards.get(mapName);
        if (rewards == null) {
            return 0;
        }
        
        return rewards.getOrDefault(actionName, 0.0);
    }
}

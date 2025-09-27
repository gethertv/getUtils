package dev.gether.getutils.inventory;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.java.JavaPlugin;

public class GetInventory implements Listener {

    @Getter private static GetInventory instance;
    @Getter private static JavaPlugin pluginInstance;

    private GetInventory() {}

    /**
     * Singleton pattern - zapobiega wielokrotnemu rejestrowaniu listenerów
     */
    public static void initialize(JavaPlugin plugin) {
        if (instance == null) {
            instance = new GetInventory();
            pluginInstance = plugin;
            plugin.getServer().getPluginManager().registerEvents(instance, plugin);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof AbstractInventoryHolder abstractHolder) {
            // Sprawdź czy GUI nie zostało już zamknięte
            if (abstractHolder.isClosed()) {
                event.setCancelled(true);
                return;
            }

            event.setCancelled(abstractHolder.isCancelClicks());

            try {
                abstractHolder.handleClick(event);
            } catch (Exception e) {
                // Log błąd ale nie crashuj serwera
                if (pluginInstance != null) {
                    pluginInstance.getLogger().warning("Error handling inventory click: " + e.getMessage());
                }
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        InventoryHolder holder = inventory.getHolder();

        if (holder instanceof AbstractInventoryHolder abstractHolder) {
            try {
                abstractHolder.close();
            } catch (Exception e) {
                if (pluginInstance != null) {
                    pluginInstance.getLogger().warning("Error during inventory cleanup: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Wyczyść instancję (do użycia przy wyłączaniu pluginu)
     */
    public static void cleanup() {
        if (instance != null) {
            Bukkit.getOnlinePlayers().forEach(player ->  {
                if(player.getOpenInventory().getTopInventory().getHolder() instanceof AbstractInventoryHolder abstractInventoryHolder) {
                    player.closeInventory();
                }
            });
            instance = null;
            pluginInstance = null;
        }
    }
}
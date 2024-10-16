package dev.gether.getutils.models.inventory;

import dev.gether.getutils.utils.MessageUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.java.JavaPlugin;

public class GetInventory implements Listener {

    public GetInventory(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof AbstractInventoryHolder abstractInventoryHolder) {
            event.setCancelled(abstractInventoryHolder.isCancelClicks());
            abstractInventoryHolder.handleClick(event);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        InventoryHolder holder = inventory.getHolder();
        if (holder instanceof AbstractInventoryHolder abstractInventoryHolder) {
            abstractInventoryHolder.close();
        }
    }
}

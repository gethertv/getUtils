package dev.gether.getutils.selector;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class SelectorListener implements Listener {
    private final SelectorManager manager;

    public SelectorListener(SelectorManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || !item.isSimilar(manager.getSelectorItem())) {
            return;
        }

        event.setCancelled(true);

        if (event.getClickedBlock() == null) {
            return;
        }

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            manager.setFirstPoint(player, event.getClickedBlock().getLocation());
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            manager.setSecondPoint(player, event.getClickedBlock().getLocation());
        }
    }
}
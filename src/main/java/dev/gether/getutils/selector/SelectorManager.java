package dev.gether.getutils.selector;

import dev.gether.getutils.builder.ItemStackBuilder;
import dev.gether.getutils.utils.MessageUtil;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SelectorManager {
    Map<UUID, RegionSelection> selections = new HashMap<>();
    ItemStack selectorItem;

    public SelectorManager(JavaPlugin plugin) {
        this.selectorItem = createSelectorItem();
        plugin.getServer().getPluginManager().registerEvents(new SelectorListener(this), plugin);
    }

    public SelectorManager(JavaPlugin plugin, ItemStack selectorItem) {
        this.selectorItem = selectorItem;
        plugin.getServer().getPluginManager().registerEvents(new SelectorListener(this), plugin);
    }

    private ItemStack createSelectorItem() {
        return ItemStackBuilder.of(Material.BLAZE_ROD)
                .name("&e&lRegion Selector")
                .lore(new ArrayList<>(List.of(
                        "&7Left-click to set the first point",
                        "&7Right-click to set the second point"
                )))
                .glow(true)
                .build();
    }

    public void setFirstPoint(Player player, org.bukkit.Location location) {
        selections.compute(player.getUniqueId(), (uuid, selection) -> {
            if (selection == null) {
                selection = new RegionSelection(player.getUniqueId(), location, null);
            } else {
                selection = new RegionSelection(player.getUniqueId(), location, selection.secondPoint());
            }
            MessageUtil.sendMessage(player, "&aFirst point set at " + formatLocation(location));
            return selection;
        });
    }

    public void setSecondPoint(Player player, org.bukkit.Location location) {
        selections.compute(player.getUniqueId(), (uuid, selection) -> {
            if (selection == null) {
                selection = new RegionSelection(player.getUniqueId(), null, location);
            } else {
                selection = new RegionSelection(player.getUniqueId(), selection.firstPoint(), location);
            }
            MessageUtil.sendMessage(player, "&aSecond point set at " + formatLocation(location));
            return selection;
        });
    }

    public RegionSelection getSelection(Player player) {
        return selections.get(player.getUniqueId());
    }

    public void clearSelection(Player player) {
        selections.remove(player.getUniqueId());
        MessageUtil.sendMessage(player, "&cYour region selection has been cleared.");
    }

    public ItemStack getSelectorItem() {
        return selectorItem.clone();
    }

    private String formatLocation(org.bukkit.Location loc) {
        return String.format("(%.0f, %.0f, %.0f)", loc.getX(), loc.getY(), loc.getZ());
    }
}
package dev.gether.getutils.inventory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.gether.getutils.inventory.item.DynamicItem;
import dev.gether.getutils.utils.ColorFixer;
import lombok.*;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InventoryConfig {

    private InventoryType inventoryType;
    private int size;
    private String title;
    private int refreshInterval = -1;
    private boolean cancelClicks = true;

    private List<DynamicItem> decorations;

}
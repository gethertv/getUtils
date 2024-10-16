package dev.gether.getutils.models.inventory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.gether.getutils.utils.ColorFixer;
import lombok.*;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InventoryConfig {

    String title;
    int size;
    int refreshInterval = -1;
    boolean cancelClicks = true;

    List<DynamicItem> decorations;

    @JsonIgnore
    public Inventory createInventory(AbstractInventoryHolder holder) {
        Inventory inventory = Bukkit.createInventory(holder, size, ColorFixer.addColors(title));

        if(decorations != null) {
            decorations.forEach(decorationItem -> {
                ItemStack itemStack = holder.processPlaceholders(decorationItem.getProcessedItem());
                decorationItem.getSlots().forEach(slot -> {
                    inventory.setItem(slot, itemStack);
                });
            });
        }
        return inventory;
    }

}
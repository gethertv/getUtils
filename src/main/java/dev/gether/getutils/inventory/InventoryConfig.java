package dev.gether.getutils.inventory;

import dev.gether.getutils.inventory.item.DynamicItem;
import lombok.*;
import org.bukkit.event.inventory.InventoryType;

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
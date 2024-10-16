package dev.gether.getutils.models.inventory;

import dev.gether.getutils.models.Item;
import lombok.*;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StaticItem {

    Item item;
    int slot;

}

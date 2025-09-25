package dev.gether.getutils.inventory.item;

import dev.gether.getutils.models.Item;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StaticItem {

    boolean enabled;
    Item item;
    int slot;

}

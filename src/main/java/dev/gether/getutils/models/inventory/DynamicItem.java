package dev.gether.getutils.models.inventory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.gether.getutils.models.Item;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DynamicItem {
    Item item;
    List<Integer> slots;
    Map<String, Supplier<String>> placeholders = new HashMap<>();

    public void addPlaceholder(String key, Supplier<String> valueProvider) {
        placeholders.put(key, valueProvider);
    }

    public void updatePlaceholder(String key, Supplier<String> valueProvider) {
        placeholders.replace(key, valueProvider);
    }

    @JsonIgnore
    public ItemStack getProcessedItem() {
        ItemStack processedItem = item.getItemStack();
        ItemMeta meta = processedItem.getItemMeta();
        if (meta != null) {
            if (meta.hasDisplayName()) {
                meta.setDisplayName(replacePlaceholders(meta.getDisplayName()));
            }
            if (meta.hasLore()) {
                List<String> lore = meta.getLore();
                List<String> newLore = new ArrayList<>();
                for (String line : lore) {
                    newLore.add(replacePlaceholders(line));
                }
                meta.setLore(newLore);
            }
            processedItem.setItemMeta(meta);
        }
        return processedItem;
    }

    @JsonIgnore
    private String replacePlaceholders(String text) {
        if(placeholders==null) return text;
        for (Map.Entry<String, Supplier<String>> entry : placeholders.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            String value = entry.getValue().get();
            text = text.replace(placeholder, value);
        }
        return text;
    }
}
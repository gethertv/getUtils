package dev.gether.getutils.models.inventory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.Serializable;
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
public class DynamicItem implements Serializable {
    private ItemStack itemStack;
    private List<Integer> slots;
    private Map<String, Supplier<String>> placeholders = new HashMap<>();

    public void addPlaceholder(String key, Supplier<String> valueProvider) {
        placeholders.put(key, valueProvider);
    }

    public void updatePlaceholder(String key, Supplier<String> valueProvider) {
        placeholders.replace(key, valueProvider);
    }

    @JsonIgnore
    public ItemStack getProcessedItem() {
        ItemStack processedItem = itemStack.clone();
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
        for (Map.Entry<String, Supplier<String>> entry : placeholders.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            String value = entry.getValue().get();
            text = text.replace(placeholder, value);
        }
        return text;
    }
}
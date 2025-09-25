package dev.gether.getutils.inventory.item;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.gether.getutils.models.Item;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DynamicItem {
    boolean enabled;
    Item item;
    List<Integer> slots;
    Map<String, Supplier<String>> placeholders;

    volatile boolean disposed = false;

    // Lazy initialization dla placeholders
    private Map<String, Supplier<String>> getPlaceholders() {
        if (placeholders == null) {
            placeholders = new ConcurrentHashMap<>();
        }
        return placeholders;
    }

    public void addPlaceholder(String key, Supplier<String> valueProvider) {
        if (disposed || key == null || valueProvider == null) return;
        getPlaceholders().put(key, valueProvider);
    }

    public void updatePlaceholder(String key, Supplier<String> valueProvider) {
        if (disposed || key == null || valueProvider == null) return;
        if (placeholders != null) {
            placeholders.replace(key, valueProvider);
        }
    }

    public void removePlaceholder(String key) {
        if (disposed || key == null || placeholders == null) return;
        placeholders.remove(key);
    }

    @JsonIgnore
    public ItemStack getProcessedItem() {
        if (disposed || item == null) return null;

        try {
            ItemStack processedItem = item.getItemStack();
            if (processedItem == null) return null;

            ItemMeta meta = processedItem.getItemMeta();
            if (meta != null) {
                if (meta.hasDisplayName()) {
                    meta.setDisplayName(replacePlaceholders(meta.getDisplayName()));
                }
                if (meta.hasLore()) {
                    List<String> lore = meta.getLore();
                    if (lore != null) {
                        List<String> newLore = new ArrayList<>();
                        for (String line : lore) {
                            newLore.add(replacePlaceholders(line));
                        }
                        meta.setLore(newLore);
                    }
                }
                processedItem.setItemMeta(meta);
            }
            return processedItem;
        } catch (Exception e) {
            // Zwróć podstawowy item w przypadku błędu
            return item != null ? item.getItemStack() : null;
        }
    }

    @JsonIgnore
    private String replacePlaceholders(String text) {
        if (disposed || text == null || placeholders == null || placeholders.isEmpty()) {
            return text;
        }

        String result = text;
        try {
            for (Map.Entry<String, Supplier<String>> entry : placeholders.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    String placeholder = "{" + entry.getKey() + "}";
                    try {
                        String value = entry.getValue().get();
                        if (value != null) {
                            result = result.replace(placeholder, value);
                        }
                    } catch (Exception e) {
                        // Ignoruj błędy pojedynczych placeholderów
                    }
                }
            }
        } catch (Exception e) {
            // W przypadku błędu zwróć oryginalny tekst
            return text;
        }

        return result;
    }

    /**
     * Czyści wszystkie referencje - zapobiega memory leakom
     */
    public void dispose() {
        if (disposed) return;

        disposed = true;

        // Wyczyść placeholdery
        if (placeholders != null) {
            placeholders.clear();
            placeholders = null;
        }

        // Wyczyść sloty
        if (slots != null) {
            slots.clear();
            slots = null;
        }

        // Null item reference
        item = null;
    }

    /**
     * Klonuj DynamicItem (przydatne dla kopii)
     */
    public DynamicItem clone() {
        if (disposed) return null;

        DynamicItem cloned = new DynamicItem();
        cloned.item = this.item; // Item może być współdzielony
        cloned.slots = this.slots != null ? new ArrayList<>(this.slots) : null;

        // Skopiuj placeholdery
        if (this.placeholders != null) {
            cloned.placeholders = new ConcurrentHashMap<>(this.placeholders);
        }

        return cloned;
    }

    /**
     * Sprawdź czy item jest prawidłowy
     */
    public boolean isValid() {
        return !disposed && item != null && slots != null && !slots.isEmpty();
    }
}
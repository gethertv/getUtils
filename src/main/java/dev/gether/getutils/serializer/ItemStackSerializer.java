package dev.gether.getutils.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import dev.gether.getutils.models.PersistentData;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemStackSerializer extends StdSerializer<ItemStack> {

    public ItemStackSerializer() {
        super(ItemStack.class);
    }

    @Override
    public void serialize(ItemStack itemStack, JsonGenerator gen, SerializerProvider provider) throws IOException {
        Map<String, Object> serializedItem = new HashMap<>();

        serializedItem.put("type", itemStack.getType().name());
        serializedItem.put("amount", itemStack.getAmount());

        if (itemStack.hasItemMeta()) {
            ItemMeta meta = itemStack.getItemMeta();
            Map<String, Object> metaMap = new HashMap<>();

            if (meta.hasDisplayName()) {
                metaMap.put("display-name", meta.getDisplayName());
            }

            if (meta.hasLore()) {
                metaMap.put("lore", meta.getLore());
            }

            if (meta.hasEnchants()) {
                Map<String, Integer> enchants = meta.getEnchants().entrySet().stream()
                        .collect(Collectors.toMap(e -> e.getKey().getKey().toString(), Map.Entry::getValue));
                metaMap.put("enchants", enchants);
            }

            if (!meta.getItemFlags().isEmpty()) {
                metaMap.put("item-flags", meta.getItemFlags().stream()
                        .map(Enum::name)
                        .collect(Collectors.toList()));
            }

            if (meta.hasCustomModelData()) {
                metaMap.put("custom-model-data", meta.getCustomModelData());
            }

            metaMap.put("unbreakable", meta.isUnbreakable());

            if (meta instanceof Damageable) {
                metaMap.put("damage", ((Damageable) meta).getDamage());
            }

            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (!container.isEmpty()) {
                Map<String, Map<String, Object>> persistentData = new HashMap<>();
                for (NamespacedKey key : container.getKeys()) {
                    for (PersistentDataType<?, ?> type : PersistentData.TYPES) {
                        if (container.has(key, type)) {
                            Object value = container.get(key, type);
                            Map<String, Object> entry = new HashMap<>();
                            entry.put("type", getTypeName(type));
                            entry.put("value", value);
                            persistentData.put(key.toString(), entry);
                            break;
                        }
                    }
                }
                if (!persistentData.isEmpty()) {
                    metaMap.put("persistent-data", persistentData);
                }
            }

            serializedItem.put("meta", metaMap);
        }

        gen.writeObject(serializedItem);
    }

    private String getTypeName(PersistentDataType<?, ?> type) {
        if (type == PersistentDataType.BYTE) return "BYTE";
        if (type == PersistentDataType.SHORT) return "SHORT";
        if (type == PersistentDataType.INTEGER) return "INTEGER";
        if (type == PersistentDataType.LONG) return "LONG";
        if (type == PersistentDataType.FLOAT) return "FLOAT";
        if (type == PersistentDataType.DOUBLE) return "DOUBLE";
        if (type == PersistentDataType.STRING) return "STRING";
        if (type == PersistentDataType.BYTE_ARRAY) return "BYTE_ARRAY";
        if (type == PersistentDataType.INTEGER_ARRAY) return "INTEGER_ARRAY";
        if (type == PersistentDataType.LONG_ARRAY) return "LONG_ARRAY";
        if (type == PersistentDataType.TAG_CONTAINER) return "TAG_CONTAINER";
        if (type == PersistentDataType.TAG_CONTAINER_ARRAY) return "TAG_CONTAINER_ARRAY";
        return "UNKNOWN";
    }
}
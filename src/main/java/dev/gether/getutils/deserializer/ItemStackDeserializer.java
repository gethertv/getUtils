package dev.gether.getutils.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import dev.gether.getutils.models.PersistentData;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ItemStackDeserializer extends StdDeserializer<ItemStack> {




    public ItemStackDeserializer() {
        super(ItemStack.class);
    }

    @Override
    public ItemStack deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        Material material = Material.valueOf(node.get("type").asText());
        int amount = node.get("amount").asInt();

        ItemStack itemStack = new ItemStack(material, amount);

        if (node.has("meta")) {
            JsonNode metaNode = node.get("meta");
            ItemMeta meta = itemStack.getItemMeta();

            if (metaNode.has("display-name")) {
                meta.setDisplayName(metaNode.get("display-name").asText());
            }

            if (metaNode.has("unbreakable")) {
                meta.setUnbreakable(metaNode.get("unbreakable").asBoolean());
            }

            if (metaNode.has("lore")) {
                List<String> lore = new ArrayList<>();
                metaNode.get("lore").elements().forEachRemaining(loreNode -> lore.add(loreNode.asText()));
                meta.setLore(lore);
            }

            if (metaNode.has("enchants")) {
                JsonNode enchantNode = metaNode.get("enchants");
                enchantNode.fields().forEachRemaining(entry -> {
                    Enchantment enchantment = Enchantment.getByKey(NamespacedKey.fromString(entry.getKey()));
                    if (enchantment != null) {
                        meta.addEnchant(enchantment, entry.getValue().asInt(), true);
                    }
                });
            }

            if (metaNode.has("item-flags")) {
                metaNode.get("item-flags").elements().forEachRemaining(flagNode ->
                        meta.addItemFlags(ItemFlag.valueOf(flagNode.asText())));
            }

            if (metaNode.has("custom-model-data")) {
                meta.setCustomModelData(metaNode.get("custom-model-data").asInt());
            }

            if (metaNode.has("damage") && meta instanceof Damageable) {
                ((Damageable) meta).setDamage(metaNode.get("damage").asInt());
            }

            if (metaNode.has("persistent-data")) {
                JsonNode persistentDataNode = metaNode.get("persistent-data");
                persistentDataNode.fields().forEachRemaining(entry -> {
                    NamespacedKey key = NamespacedKey.fromString(entry.getKey());
                    if (key != null) {
                        JsonNode valueNode = entry.getValue();
                        String typeName = valueNode.get("type").asText();
                        JsonNode value = valueNode.get("value");
                        setDataContainerValue(meta.getPersistentDataContainer(), key, typeName, value);
                    }
                });
            }

            itemStack.setItemMeta(meta);
        }

        return itemStack;
    }


    @SuppressWarnings("unchecked")
    private void setDataContainerValue(PersistentDataContainer container, NamespacedKey key,
                                       String typeName, JsonNode valueNode) {
        switch (typeName) {
            case "BYTE":
                container.set(key, PersistentDataType.BYTE, (byte) valueNode.intValue());
                break;
            case "SHORT":
                container.set(key, PersistentDataType.SHORT, (short) valueNode.intValue());
                break;
            case "INTEGER":
                container.set(key, PersistentDataType.INTEGER, valueNode.intValue());
                break;
            case "LONG":
                container.set(key, PersistentDataType.LONG, valueNode.longValue());
                break;
            case "FLOAT":
                container.set(key, PersistentDataType.FLOAT, (float) valueNode.doubleValue());
                break;
            case "DOUBLE":
                container.set(key, PersistentDataType.DOUBLE, valueNode.doubleValue());
                break;
            case "STRING":
                container.set(key, PersistentDataType.STRING, valueNode.textValue());
                break;
            case "BYTE_ARRAY":
                // TODO: BYTE_ARRAY
                break;
            case "INTEGER_ARRAY":
                // TODO: INTEGER_ARRAY
                break;
            case "LONG_ARRAY":
                // TODO: LONG_ARRAY
                break;
            case "TAG_CONTAINER":
                // TODO: TAG_CONTAINER
                break;
            case "TAG_CONTAINER_ARRAY":
                // TODO: TAG_CONTAINER_ARRAY
                break;
        }
    }


}
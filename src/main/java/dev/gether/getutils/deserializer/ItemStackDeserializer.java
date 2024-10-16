package dev.gether.getutils.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;


public class ItemStackDeserializer extends JsonDeserializer<ItemStack> {

    @Override
    public ItemStack deserialize(JsonParser json, DeserializationContext ctxt) throws IOException {
        JsonNode node = json.getCodec().readTree(json);
        Map<String, Object> itemMap = new LinkedHashMap<>();
        itemMap.put("==", "org.bukkit.inventory.ItemStack");
        // convert the node to map and put to itemMap
        itemMap.putAll(jsonNodeToMap(node));

        YamlConfiguration craftConfig = new YamlConfiguration();
        craftConfig.set("_", itemMap);
        try {
            craftConfig.loadFromString(craftConfig.saveToString());
        } catch (InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
        return craftConfig.getItemStack("_");
    }

    public Map<String, Object> jsonNodeToMap(JsonNode jsonNode) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> result = mapper.convertValue(jsonNode, Map.class);
        return result;
    }
}
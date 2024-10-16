package dev.gether.getutils.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.util.Map;

public class ItemStackSerializer extends StdSerializer<ItemStack> {

    public ItemStackSerializer() {
        super(ItemStack.class);
    }

    @Override
    public void serialize(ItemStack itemStack, JsonGenerator gen, SerializerProvider provider) throws IOException {
        // serialize itemstack to map
        //Map<String, Object> serializedItemStack = itemStack.serialize();
        YamlConfiguration craftConfig = new YamlConfiguration();
        craftConfig.set("_", itemStack);

        Map<String, Map<String, Object>> root = new Yaml().load(craftConfig.saveToString());
        Map<String, Object> itemMap = root.get("_");

        itemMap.remove("==");

        gen.writeObject(itemMap);
    }

}
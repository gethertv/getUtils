package dev.gether.getutils.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

import java.io.IOException;

public class EnchantmentDeserializer extends JsonDeserializer<Enchantment> {

    @Override
    public Enchantment deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String enchantmentName = p.getValueAsString();

        String lowerCaseName = enchantmentName.toLowerCase();
        NamespacedKey key = NamespacedKey.minecraft(lowerCaseName);

        Enchantment enchantment = Enchantment.getByKey(key);

        if (enchantment == null) {
            throw new IOException("Unknown enchantment: " + enchantmentName);
        }

        return enchantment;
    }
}
package dev.gether.getutils.deserializer;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

import java.io.IOException;

public class EnchantmentKeyDeserializer extends KeyDeserializer {

    @Override
    public Enchantment deserializeKey(String key, DeserializationContext ctxt) throws IOException {
        return Enchantment.getByKey(NamespacedKey.fromString(key));
    }
}

package dev.gether.getutils.deserializer;

import com.fasterxml.jackson.databind.KeyDeserializer;
import org.bukkit.potion.PotionEffectType;

import java.io.IOException;

public class PotionEffectTypeKeyDeserializer extends KeyDeserializer {
    @Override
    public Object deserializeKey(String key, com.fasterxml.jackson.databind.DeserializationContext ctxt) throws IOException {
        return PotionEffectType.getByName(key); // Convert the string key to PotionEffectType
    }
}

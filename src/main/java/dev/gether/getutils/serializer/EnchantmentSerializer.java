package dev.gether.getutils.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.bukkit.enchantments.Enchantment;

import java.io.IOException;

public class EnchantmentSerializer extends JsonSerializer<Enchantment> {

    @Override
    public void serialize(Enchantment enchantment, JsonGenerator gen, SerializerProvider provider) throws IOException {
        String key = enchantment.getKey().getKey();
        String formattedKey = key.toUpperCase();
        gen.writeString(formattedKey);
    }
}
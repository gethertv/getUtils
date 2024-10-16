package dev.gether.getutils.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.bukkit.enchantments.Enchantment;

import java.io.IOException;

public class EnchantmentKeySerializer extends StdSerializer<Enchantment> {

    public EnchantmentKeySerializer() {
        super(Enchantment.class);
    }

    @Override
    public void serialize(Enchantment enchantment, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeFieldName(enchantment.getKey().toString());
    }
}

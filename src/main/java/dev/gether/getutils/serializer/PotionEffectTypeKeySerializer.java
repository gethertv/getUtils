package dev.gether.getutils.serializer;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.bukkit.potion.PotionEffectType;

import java.io.IOException;

public class PotionEffectTypeKeySerializer extends JsonSerializer<PotionEffectType> {
    @Override
    public void serialize(PotionEffectType value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeFieldName(value.getName()); // Zapisuje nazwę efektu jako klucz JSON
    }
}

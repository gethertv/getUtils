package dev.gether.getutils.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.IOException;

public class PotionEffectSerializer extends StdSerializer<PotionEffect> {

    public PotionEffectSerializer() {
        super(PotionEffect.class);
    }

    @Override
    public void serialize(PotionEffect potionEffect, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("amplifier", potionEffect.getAmplifier());
        gen.writeNumberField("duration", potionEffect.getDuration());
        gen.writeStringField("type", potionEffect.getType().getName());
        gen.writeEndObject();
    }
}
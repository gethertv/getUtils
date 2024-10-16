package dev.gether.getutils.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.IOException;

public class PotionEffectDeserializer extends JsonDeserializer<PotionEffect> {

    @Override
    public PotionEffect deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        int amplifier = node.get("amplifier").asInt();
        int duration = node.get("duration").asInt();
        PotionEffectType type = PotionEffectType.getByName(node.get("type").asText());
        return new PotionEffect(type, duration, amplifier);
    }
}
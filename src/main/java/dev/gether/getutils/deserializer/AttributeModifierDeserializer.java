package dev.gether.getutils.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;

import java.io.IOException;
import java.util.UUID;

public class AttributeModifierDeserializer extends StdDeserializer<AttributeModifier> {

    public AttributeModifierDeserializer() {
        super(AttributeModifier.class);
    }

    @Override
    public AttributeModifier deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        String name = node.get("name").asText();
        double amount = node.get("amount").asDouble();
        String slot = node.get("slot").asText();
        AttributeModifier.Operation operation = AttributeModifier.Operation.valueOf(node.get("operation").asText());

        return new AttributeModifier(UUID.randomUUID(), name, amount, operation, EquipmentSlot.valueOf(slot.toUpperCase()));
    }
}
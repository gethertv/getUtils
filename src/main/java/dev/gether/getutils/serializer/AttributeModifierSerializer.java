package dev.gether.getutils.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.bukkit.attribute.AttributeModifier;

import java.io.IOException;

public class AttributeModifierSerializer extends StdSerializer<AttributeModifier> {

    public AttributeModifierSerializer() {
        super(AttributeModifier.class);
    }

    @Override
    public void serialize(AttributeModifier modifier, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("name", modifier.getName());
        gen.writeNumberField("amount", modifier.getAmount());
        gen.writeStringField("operation", modifier.getOperation().name());
        gen.writeStringField("slot", modifier.getSlot().name());
        gen.writeEndObject();
    }
}
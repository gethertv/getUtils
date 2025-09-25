package dev.gether.getutils.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.bukkit.Sound;
import java.io.IOException;

public class SoundSerializer extends JsonSerializer<Sound> {
    @Override
    public void serialize(Sound sound, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (sound == null) {
            jsonGenerator.writeNull();
            return;
        }
        try {
            String soundName = sound.name();
            jsonGenerator.writeString(soundName);
        } catch (Throwable e) {
            try {
                jsonGenerator.writeString(Sound.BLOCK_ANVIL_USE.name());
            } catch (Throwable fallbackError) {
                jsonGenerator.writeString("BLOCK_ANVIL_USE");
            }
        }
    }
}
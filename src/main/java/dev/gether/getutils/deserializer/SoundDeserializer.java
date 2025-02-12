package dev.gether.getutils.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.bukkit.Sound;
import java.io.IOException;

public class SoundDeserializer extends JsonDeserializer<Sound> {
    @Override
    public Sound deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        return Sound.valueOf(jsonParser.getValueAsString());
    }
}

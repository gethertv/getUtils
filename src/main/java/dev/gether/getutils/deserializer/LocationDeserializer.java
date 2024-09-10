package dev.gether.getutils.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.bukkit.Location;

import java.io.IOException;
import java.util.Map;

public class LocationDeserializer extends JsonDeserializer<Location> {

    @Override
    public Location deserialize(JsonParser json, DeserializationContext ctxt) throws IOException {
        // read json to map
        Map<String, Object> itemMap = json.readValueAs(Map.class);
        // and map convert to location
        return Location.deserialize(itemMap);
    }


}
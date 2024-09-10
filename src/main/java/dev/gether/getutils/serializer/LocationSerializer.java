package dev.gether.getutils.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.bukkit.Location;

import java.io.IOException;
import java.util.Map;

public class LocationSerializer extends StdSerializer<Location> {

    public LocationSerializer() {
        super(Location.class);
    }

    @Override
    public void serialize(Location value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        // serialize location to map
        Map<String, Object> serializedItemStack = value.serialize();
        // serialize the actual data
        gen.writeObject(serializedItemStack);
    }
}
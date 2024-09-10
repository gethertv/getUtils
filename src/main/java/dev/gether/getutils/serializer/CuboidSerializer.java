package dev.gether.getutils.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import dev.gether.getutils.models.Cuboid;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CuboidSerializer extends StdSerializer<Cuboid> {

    public CuboidSerializer() {
        super(Cuboid.class);
    }

    @Override
    public void serialize(Cuboid cuboid, JsonGenerator gen, SerializerProvider provider) throws IOException {
        Map<String, Object> itemMap = new HashMap<>();
        itemMap.put("world", cuboid.getWorldName());
        itemMap.put("min-x", cuboid.getMinX());
        itemMap.put("min-y", cuboid.getMinY());
        itemMap.put("min-z", cuboid.getMinZ());
        itemMap.put("max-x", cuboid.getMaxX());
        itemMap.put("max-y", cuboid.getMaxY());
        itemMap.put("max-z", cuboid.getMaxZ());
        gen.writeObject(itemMap);
    }
}

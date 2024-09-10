package dev.gether.getutils.deserializer;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import dev.gether.getutils.models.Cuboid;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.IOException;

public class CuboidDeserializer extends JsonDeserializer<Cuboid> {


    @Override
    public Cuboid deserialize(JsonParser json, DeserializationContext ctxt) throws IOException, JacksonException {
        JsonNode node = json.getCodec().readTree(json);

        World world = Bukkit.getWorld(node.get("world").asText());

        double minX = node.get("min-x").asDouble();
        double minY = node.get("min-y").asDouble();
        double minZ = node.get("min-z").asDouble();

        double maxX = node.get("max-x").asDouble();
        double maxY = node.get("max-y").asDouble();
        double maxZ = node.get("max-z").asDouble();

        Location minLoc = new Location(world, minX, minY, minZ);
        Location maxLoc = new Location(world, maxX, maxY, maxZ);

        return new Cuboid(minLoc, maxLoc);
    }
}

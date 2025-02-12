package dev.gether.getutils.models.location;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.Serializable;

@Setter
@Getter
public class LocationData implements Serializable {

    private String worldName;
    private double x;
    private double y;
    private double z;

    public LocationData(Location location) {
        worldName = location.getWorld().getName();
        x = location.getX();
        y = location.getY();
        z = location.getZ();
    }

    public Location getLocation() {
        World world = Bukkit.getWorld(worldName);
        if(world == null) {
            throw new RuntimeException("World not exists");
        }
        return new Location(world, x, y, z);
    }
}

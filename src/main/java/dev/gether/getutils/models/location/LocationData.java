package dev.gether.getutils.models.location;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.Serializable;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LocationData implements Serializable {

    private String proxyName;
    private String worldName;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;

    public LocationData(String proxyName, Location location) {
        this(location);
        this.proxyName = proxyName;
    }

    public LocationData(Location location) {
        worldName = location.getWorld().getName();
        x = location.getX();
        y = location.getY();
        z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();

    }

    public Location getLocation() {
        World world = Bukkit.getWorld(worldName);
        if(world == null) {
            throw new RuntimeException("World not exists");
        }
        return new Location(world, x, y, z, yaw, pitch);
    }
}

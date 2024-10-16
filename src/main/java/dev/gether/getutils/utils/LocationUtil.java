package dev.gether.getutils.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Location;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LocationUtil {

    /**
     * Calculates a new location at a specified distance behind a given location.
     * The new location is offset 1 block upwards relative to the original.
     *
     * @param location The original location
     * @param distance The distance by which the new location should be offset behind the original
     * @return The new location
     */
    public static Location getDirection(Location location, double distance) {
        Location result = location.clone();
        double yawRadians = Math.toRadians(location.getYaw() - 180.0);
        double x = Math.cos(yawRadians) * distance;
        double z = Math.sin(yawRadians) * distance;
        result.add(x, 1.0, z);
        result.setDirection(location.getDirection());
        return result;
    }


}

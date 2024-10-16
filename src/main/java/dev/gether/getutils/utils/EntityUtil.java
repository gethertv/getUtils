package dev.gether.getutils.utils;

import dev.gether.getutils.Valid;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EntityUtil {

    /**
     * Finds all entities of a specified type within a given range from a central location.
     *
     * @param <T> The type of LivingEntity to search for
     * @param center The central Location to search around
     * @param range The radius of the spherical search area
     * @param entityClass The Class object representing the type of entity to find
     * @return A list of entities of the specified type within the range, sorted by distance from the center
     *
     * @throws IllegalArgumentException if center is null, range is negative, or entityClass is null
     */
    public static <T extends LivingEntity> List<T> findNearbyEntities(Location center, double range, Class<T> entityClass) {
        // Validate input parameters
        Valid.checkNotNull(center, "Center location cannot be null");
        Valid.checkBoolean(range >= 0, "Range must be non-negative");
        Valid.checkNotNull(entityClass, "Entity class cannot be null");

        final List<T> found = new ArrayList<>();

        for (final Entity nearby : getNearbyEntities(center, range)) {
            if (nearby instanceof LivingEntity && entityClass.isAssignableFrom(nearby.getClass())) {
                found.add((T) nearby);
            }
        }

        // Sort the list by distance from the center
        found.sort((first, second) -> Double.compare(first.getLocation().distance(center), second.getLocation().distance(center)));

        return found;
    }

    /**
     * Return nearby entities in a location
     *
     * @param location
     * @param radius
     * @return
     */
    public static Collection<Entity> getNearbyEntities(final Location location, final double radius) {
        Valid.checkNotNull(location, "Location cannot be null");
        Valid.checkNotNull(location.getWorld(), "World cannot be null");

        try {
            return location.getWorld().getNearbyEntities(location, radius, radius, radius);
        } catch (final Throwable t) {
            final List<Entity> found = new ArrayList<>();

            for (final Entity nearby : location.getWorld().getEntities())
                if (nearby.getLocation().distance(location) <= radius)
                    found.add(nearby);

            return found;
        }
    }


}

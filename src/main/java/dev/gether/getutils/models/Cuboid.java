package dev.gether.getutils.models;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a cuboid region in a Minecraft world.
 * This class provides methods for manipulating and querying the cuboid.
 */
@Getter
@Setter
public class Cuboid implements Serializable {

    private String worldName;
    private int minX, maxX, minY, maxY, minZ, maxZ;

    /**
     * Constructs a Cuboid from two locations.
     *
     * @param loc1 The first corner location
     * @param loc2 The second corner location
     */
    public Cuboid(Location loc1, Location loc2) {
        this(loc1.getWorld(), loc1.getBlockX(), loc1.getBlockY(), loc1.getBlockZ(),
             loc2.getBlockX(), loc2.getBlockY(), loc2.getBlockZ());
    }

    /**
     * Constructs a Cuboid from world and coordinates.
     *
     * @param world The world
     * @param x1 First X coordinate
     * @param y1 First Y coordinate
     * @param z1 First Z coordinate
     * @param x2 Second X coordinate
     * @param y2 Second Y coordinate
     * @param z2 Second Z coordinate
     */
    public Cuboid(World world, int x1, int y1, int z1, int x2, int y2, int z2) {
        this.worldName = world.getName();
        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.minZ = Math.min(z1, z2);
        this.maxX = Math.max(x1, x2);
        this.maxY = Math.max(y1, y2);
        this.maxZ = Math.max(z1, z2);
    }

    /**
     * Gets all blocks within the cuboid.
     *
     * @return List of BlockData for all blocks in the cuboid
     */
    public List<BlockData> getBlocks() {
        List<BlockData> blocks = new ArrayList<>();
        World world = Bukkit.getWorld(worldName);
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block blockAt = world.getBlockAt(x, y, z);
                    blocks.add(new BlockData(blockAt));
                }
            }
        }
        return blocks;
    }

    /**
     * Clears all blocks within the cuboid, setting them to air.
     */
    public void clearCuboid() {
        World world = Bukkit.getWorld(worldName);
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getType() != Material.AIR) {
                        block.setType(Material.AIR);
                    }
                }
            }
        }
    }

    /**
     * Gets all non-air blocks within the cuboid.
     *
     * @return List of non-air blocks in the cuboid
     */
    public List<Block> getBlockCuboidWithoutAIR() {
        List<Block> blocks = new ArrayList<>();
        World world = Bukkit.getWorld(worldName);
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block blockAt = world.getBlockAt(x, y, z);
                    if (blockAt.getType() != Material.AIR) {
                        blocks.add(blockAt);
                    }
                }
            }
        }
        return blocks;
    }

    /**
     * Checks if this cuboid fully contains another cuboid.
     *
     * @param cuboid The cuboid to check
     * @return true if this cuboid contains the other cuboid, false otherwise
     */
    public boolean contains(Cuboid cuboid) {
        return worldName.equals(cuboid.getWorldName()) &&
               cuboid.getMinX() >= minX && cuboid.getMaxX() <= maxX &&
               cuboid.getMinY() >= minY && cuboid.getMaxY() <= maxY &&
               cuboid.getMinZ() >= minZ && cuboid.getMaxZ() <= maxZ;
    }

    /**
     * Checks if this cuboid contains a specific location.
     *
     * @param location The location to check
     * @return true if the location is within this cuboid, false otherwise
     */
    public boolean contains(Location location) {
        return worldName.equals(location.getWorld().getName()) &&
               contains(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    /**
     * Checks if this cuboid contains specific coordinates.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return true if the coordinates are within this cuboid, false otherwise
     */
    public boolean contains(int x, int y, int z) {
        return x >= minX && x <= maxX &&
               y >= minY && y <= maxY &&
               z >= minZ && z <= maxZ;
    }

    /**
     * Checks if this cuboid overlaps with another cuboid.
     *
     * @param cuboid The cuboid to check for overlap
     * @return true if the cuboids overlap, false otherwise
     */
    public boolean overlaps(Cuboid cuboid) {
        if (!worldName.equals(cuboid.getWorldName())) {
            return false;
        }
        return !(cuboid.getMinX() > maxX || cuboid.getMaxX() < minX ||
                 cuboid.getMinY() > maxY || cuboid.getMaxY() < minY ||
                 cuboid.getMinZ() > maxZ || cuboid.getMaxZ() < minZ);
    }

    /**
     * Constructs a Cuboid centered around a location with given dimensions.
     *
     * @param center The center location
     * @param radius The radius in X and Z directions
     * @param up The distance up from the center
     * @param down The distance down from the center
     */
    public Cuboid(Location center, int radius, int up, int down) {
        this.worldName = center.getWorld().getName();
        int centerX = center.getBlockX();
        int centerY = center.getBlockY();
        int centerZ = center.getBlockZ();

        this.minX = centerX - radius;
        this.maxX = centerX + radius;
        this.minY = centerY - down;
        this.maxY = centerY + up;
        this.minZ = centerZ - radius;
        this.maxZ = centerZ + radius;
    }

    /**
     * Gets the center location of this cuboid.
     *
     * @return The center Location
     */
    public Location getCenter() {
        World world = Bukkit.getWorld(worldName);
        return new Location(world, (minX + maxX) / 2.0, (minY + maxY) / 2.0, (minZ + maxZ) / 2.0);
    }

    /**
     * Gets all players currently inside this cuboid.
     *
     * @return List of players inside the cuboid
     */
    public List<Player> getPlayersInside() {
        List<Player> playersInside = new ArrayList<>();
        World world = Bukkit.getWorld(this.worldName);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld().equals(world) && contains(player.getLocation())) {
                playersInside.add(player);
            }
        }
        return playersInside;
    }

    /**
     * Gets the highest non-air block at the center of this cuboid.
     *
     * @return Location of the highest center block
     */
    public Location getHighestCenterBlock() {
        World world = Bukkit.getWorld(worldName);
        int centerX = (this.minX + this.maxX) / 2;
        int centerZ = (this.minZ + this.maxZ) / 2;
        Block highestBlockAt = world.getHighestBlockAt(centerX, centerZ);
        return highestBlockAt.getLocation().clone().add(0, 1, 0);
    }

    /**
     * Updates the cuboid dimensions around its center.
     *
     * @param radius The new radius in X and Z directions
     * @param up The new distance up from the center
     * @param down The new distance down from the center
     */
    public void updateCuboid(int radius, int up, int down) {
        int centerX = (minX + maxX) / 2;
        int centerY = (minY + maxY) / 2;
        int centerZ = (minZ + maxZ) / 2;

        this.minX = centerX - radius;
        this.maxX = centerX + radius;
        this.minY = centerY - down;
        this.maxY = centerY + up;
        this.minZ = centerZ - radius;
        this.maxZ = centerZ + radius;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Cuboid)) return false;
        Cuboid other = (Cuboid) obj;
        return worldName.equals(other.worldName) &&
               minX == other.minX && minY == other.minY && minZ == other.minZ &&
               maxX == other.maxX && maxY == other.maxY && maxZ == other.maxZ;
    }

    @Override
    public String toString() {
        return "Cuboid[world:" + worldName +
               ", minX:" + minX + ", minY:" + minY + ", minZ:" + minZ +
               ", maxX:" + maxX + ", maxY:" + maxY + ", maxZ:" + maxZ + "]";
    }
}
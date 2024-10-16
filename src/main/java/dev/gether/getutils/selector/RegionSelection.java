package dev.gether.getutils.selector;

import org.bukkit.Location;

import java.util.UUID;

public record RegionSelection(UUID playerUuid, Location firstPoint, Location secondPoint) {
    public boolean isComplete() {
        return firstPoint != null && secondPoint != null;
    }
}

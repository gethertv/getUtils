package dev.gether.getutils.utils;

import dev.gether.getutils.models.particles.DustOptions;
import dev.gether.getutils.models.particles.ParticleConfig;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;

public class ParticlesUtil {

    public static void spawnParticles(Entity entity, ParticleConfig particleConfig) {
        spawnParticles(entity.getLocation(), particleConfig);
    }

    public static void spawnParticles(Location location, ParticleConfig particleConfig) {
        Particle.DustOptions dustOptions = null;

        if (particleConfig.getParticle() == Particle.REDSTONE && particleConfig.getDustOptions() != null) {
            DustOptions customDustOptions = particleConfig.getDustOptions();
            dustOptions = new Particle.DustOptions(
                    Color.fromRGB(customDustOptions.getRed(), customDustOptions.getGreen(), customDustOptions.getBlue()),
                    customDustOptions.getSize());
        }

        location.getWorld().spawnParticle(particleConfig.getParticle(),
                location,
                particleConfig.getCount(),
                particleConfig.getOffSetX(),
                particleConfig.getOffSetY(),
                particleConfig.getOffSetZ(),
                particleConfig.getExtra(),
                dustOptions);
    }
}

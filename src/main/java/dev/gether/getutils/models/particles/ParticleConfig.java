package dev.gether.getutils.models.particles;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Particle;

@Getter
@Setter
@Builder
public class ParticleConfig {
    private boolean enable;
    private Particle particle;
    private int count = 15;
    private double offSetX = 0.1;
    private double offSetY = 0.1;
    private double offSetZ = 0.1;
    private double extra = 0.01;
    private DustOptions dustOptions;

    public ParticleConfig() {}

    public ParticleConfig(boolean enable, Particle particle, int count, double offSetX, double offSetY, double offSetZ, double extra, DustOptions dustOptions) {
        this.enable = enable;
        this.particle = particle;
        this.count = count;
        this.offSetX = offSetX;
        this.offSetY = offSetY;
        this.offSetZ = offSetZ;
        this.extra = extra;
        this.dustOptions = dustOptions;
    }
}

package dev.gether.getutils.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PotionEffectConfig {
    private String potionName;
    private int seconds;
    private int level;
    public PotionEffectConfig() {}
    public PotionEffectConfig(String potionName, int seconds, int level) {
        this.potionName = potionName;
        this.seconds = seconds;
        this.level = level;
    }
}

package dev.gether.getutils.models.sound;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Sound;

@Getter
@Setter
@Builder
public class SoundConfig {
    private boolean enable;
    private Sound sound;

    public SoundConfig() {}

    public SoundConfig(boolean enable, Sound sound) {
        this.enable = enable;
        this.sound = sound;
    }
}

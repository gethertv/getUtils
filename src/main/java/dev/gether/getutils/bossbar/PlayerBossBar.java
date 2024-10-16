package dev.gether.getutils.bossbar;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PlayerBossBar implements Serializable {
    String message;
    BarColor barColor;
    BarStyle barStyle;
    CountingType countingType;
    int durationSeconds;
}
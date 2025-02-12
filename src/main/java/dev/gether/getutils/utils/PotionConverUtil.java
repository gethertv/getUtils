package dev.gether.getutils.utils;

import dev.gether.getutils.models.PotionEffectConfig;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class PotionConverUtil {

    public static List<PotionEffect> getPotionEffectFromConfig(List<PotionEffectConfig> potionEffectConfigs) {
        List<PotionEffect> potionEffects = new ArrayList<>();
        potionEffectConfigs.forEach(potion -> {
            PotionEffectType potionEffectType = PotionEffectType.getByName(potion.getPotionName());
            if(potionEffectType == null) {
                MessageUtil.logMessage(ConsoleColor.RED, "The potion effect name '" + potion.getPotionName() + "' does not exist!");
                return;
            }
            potionEffects.add(new PotionEffect(potionEffectType, potion.getSeconds() * 20, potion.getLevel() - 1));
        });
        return potionEffects;
    }

    public static List<PotionEffectType> getPotionEffectByName(List<String> potionsName) {
        List<PotionEffectType> potionEffects = new ArrayList<>();
        potionsName.forEach(potion -> {
            PotionEffectType potionEffectType = PotionEffectType.getByName(potion);
            if(potionEffectType == null) {
                MessageUtil.logMessage(ConsoleColor.RED, "The potion effect name '" + potion + "' does not exist!");
            }
            potionEffects.add(potionEffectType);
        });

        return potionEffects;
    }

}

package dev.gether.getutils.adapter;

import com.fasterxml.jackson.databind.module.SimpleModule;
import dev.gether.getutils.deserializer.*;
import dev.gether.getutils.models.Cuboid;
import dev.gether.getutils.serializer.*;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public class BukkitSerializer implements ServerSerializer {
    @Override
    public void registerSerializers(SimpleModule module) {
        module.addSerializer(ItemStack.class, new ItemStackSerializer());
        module.addSerializer(Location.class, new LocationSerializer());
        module.addSerializer(Cuboid.class, new CuboidSerializer());
        module.addSerializer(AttributeModifier.class, new AttributeModifierSerializer());
        module.addSerializer(PotionEffect.class, new PotionEffectSerializer());
        module.addSerializer(Sound.class, new SoundSerializer());
        module.addKeySerializer(Enchantment.class, new EnchantmentKeySerializer());
    }

    @Override
    public void registerDeserializers(SimpleModule module) {
        module.addDeserializer(ItemStack.class, new ItemStackDeserializer());
        module.addDeserializer(Location.class, new LocationDeserializer());
        module.addDeserializer(Cuboid.class, new CuboidDeserializer());
        module.addDeserializer(AttributeModifier.class, new AttributeModifierDeserializer());
        module.addDeserializer(PotionEffect.class, new PotionEffectDeserializer());
        module.addKeyDeserializer(Enchantment.class, new EnchantmentKeyDeserializer());
        module.addDeserializer(Sound.class, new SoundDeserializer());
    }
}

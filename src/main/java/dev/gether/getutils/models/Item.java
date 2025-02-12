package dev.gether.getutils.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import dev.gether.getutils.builder.ItemStackBuilder;
import dev.gether.getutils.deserializer.PotionEffectTypeKeyDeserializer;
import dev.gether.getutils.serializer.PotionEffectTypeKeySerializer;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Item {
    int amount;
    Material material;
    String name;
    List<String> lore;
    String base64;

    Map<Enchantment, Integer> enchantments;
    boolean unbreakable;
    boolean glow;
    int modelData;

    Map<Attribute, List<AttributeModifier>> attributeModifiers;
    List<ItemFlag> itemFlags;

    PotionType potionType;
    boolean extended;
    boolean upgraded;

    @JsonIgnore
    public ItemStack getItemStack() {
        ItemStackBuilder builder = ItemStackBuilder
                .of(material)
                .amount(amount)
                .name(name)
                .lore(lore)
                .glow(glow)
                .skullTexture(base64)
                .unbreakable(unbreakable)
                .modelData(modelData)
                .enchantments(enchantments)
                .attributeModifiers(attributeModifiers)
                .addItemFlags(itemFlags == null ? new ItemFlag[0] : itemFlags.toArray(ItemFlag[]::new));

        if (potionType != null) {
            builder.potionData(potionType, extended, upgraded);
        }

        return builder.build();
    }
}

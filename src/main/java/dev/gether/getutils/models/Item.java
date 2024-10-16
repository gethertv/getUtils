package dev.gether.getutils.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.gether.getutils.builder.ItemStackBuilder;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

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

    @JsonIgnore
    public ItemStack getItemStack() {
        return ItemStackBuilder
                .of(material)
                .amount(amount)
                .skullTexture(base64)
                .name(name)
                .lore(lore)
                .glow(glow)
                .unbreakable(unbreakable)
                .modelData(modelData)
                .enchantments(enchantments)
                .attributeModifiers(attributeModifiers)
                .addItemFlags(itemFlags == null ? new ItemFlag[0] : itemFlags.toArray(ItemFlag[]::new))
                .build();
    }

}

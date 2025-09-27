package dev.gether.getutils.models;

import dev.gether.getutils.builder.ItemStackBuilder;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.bukkit.Color;
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

    // Color support - HEX format (e.g., "#FF0000" for red)
    String hexColor;        // For leather armor dyeing
    String potionHexColor;  // For potion colors
    String fireworkHexColor; // For firework colors

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

        // Add color support
        if (hexColor != null && !hexColor.isEmpty()) {
            builder.color(hexColor);
        }

        if (potionHexColor != null && !potionHexColor.isEmpty()) {
            builder.potionColor(potionHexColor);
        }

        if (fireworkHexColor != null && !fireworkHexColor.isEmpty()) {
            builder.fireworkColor(fireworkHexColor);
        }

        if (potionType != null) {
            builder.potionData(potionType, extended, upgraded);
        }

        return builder.build();
    }

    /**
     * Helper method to convert HEX color to Bukkit Color
     *
     * @param hex HEX color string (e.g., "#FF0000" or "FF0000")
     * @return Bukkit Color object
     */
    public static Color hexToColor(String hex) {
        if (hex == null || hex.isEmpty()) {
            return null;
        }

        // Remove # if present
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }

        // Validate hex format
        if (hex.length() != 6) {
            throw new IllegalArgumentException("Invalid HEX color format. Expected 6 characters (e.g., 'FF0000')");
        }

        try {
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);

            return Color.fromRGB(r, g, b);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid HEX color format: " + hex, e);
        }
    }
}
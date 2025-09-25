package dev.gether.getutils.builder;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import dev.gether.getutils.Valid;
import dev.gether.getutils.utils.ColorFixer;
import dev.gether.getutils.utils.ConsoleColor;
import dev.gether.getutils.utils.MessageUtil;
import dev.gether.getutils.utils.ServerVersionUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class ItemStackBuilder {

    private ItemStack itemStack;
    private ItemMeta itemMeta;
    private Map<Attribute, List<AttributeModifier>> attributeModifiers = new HashMap<>();
    private List<ItemFlag> itemFlags = new ArrayList<>();

    private PotionType potionType;
    private boolean extended;
    private boolean upgraded;

    private ItemStackBuilder(Material material) {
        Valid.checkNotNull(material, "Material cannot be null");
        this.itemStack = new ItemStack(material);
        this.itemMeta = itemStack.getItemMeta();
    }

    /**
     * Creates a new ItemBuilder for the specified material.
     *
     * @param material The material of the item
     * @return A new ItemBuilder instance
     */
    public static ItemStackBuilder of(Material material) {
        return new ItemStackBuilder(material);
    }

    /**
     * Sets the display name of the item.
     *
     * @param name The name to set
     * @return This ItemBuilder instance
     */
    public ItemStackBuilder name(String name) {
        if(name == null)
            return this;

        itemMeta.setDisplayName(ColorFixer.addColors(name));
        return this;
    }

    /**
     * Sets the amount of the item.
     *
     * @param amount The amount to set
     * @return This ItemBuilder instance
     */
    public ItemStackBuilder amount(int amount) {
        itemStack.setAmount(Math.max(1, amount));
        return this;
    }

    /**
     * Adds attribute modifiers to the item.
     *
     * @param attributeModifiers A map of attributes and their modifiers
     * @return This ItemBuilder instance
     */
    public ItemStackBuilder attributeModifiers(Map<Attribute, List<AttributeModifier>> attributeModifiers) {
        if(attributeModifiers != null) {
            this.attributeModifiers.putAll(attributeModifiers);
        }
        return this;
    }

    /**
     * Adds a single attribute modifier to the item.
     *
     * @param attribute The attribute to modify
     * @param modifier The modifier to apply
     * @return This ItemBuilder instance
     */
    public ItemStackBuilder addAttributeModifier(Attribute attribute, AttributeModifier modifier) {
        if(attribute == null || modifier == null)
            return this;

        this.attributeModifiers.computeIfAbsent(attribute, k -> new ArrayList<>()).add(modifier);
        return this;
    }

    /**
     * Adds item flags to the item.
     *
     * @param flags The flags to add
     * @return This ItemBuilder instance
     */
    public ItemStackBuilder addItemFlags(ItemFlag... flags) {
        if(flags != null) {
            this.itemFlags.addAll(Arrays.asList(flags));
        }
        return this;
    }

    /**
     * Sets the lore of the item.
     *
     * @param lore The lore to set
     * @return This ItemBuilder instance
     */
    public ItemStackBuilder lore(List<String> lore) {
        if(lore != null) {
            itemMeta.setLore(ColorFixer.addColors(new ArrayList<>(lore)));
        }
        return this;
    }

    /**
     * Adds a glowing effect to the item.
     *
     * @param glow Whether the item should glow
     * @return This ItemBuilder instance
     */
    public ItemStackBuilder glow(boolean glow) {
        if (glow) {
            Enchantment enchantment;
            try {
                enchantment = Enchantment.DURABILITY;
            } catch (NoSuchFieldError e) {
                enchantment = Enchantment.getByName("UNBREAKING");
                if (enchantment == null) {
                    throw new IllegalStateException("Could not find appropriate enchantment for glow effect");
                }
            }
            itemStack.setItemMeta(itemMeta);
            itemStack.addUnsafeEnchantment(enchantment, 1);
            itemMeta = itemStack.getItemMeta();
            addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        return this;
    }

    /**
     * Sets the item as unbreakable.
     *
     * @param unbreakable Whether the item should be unbreakable
     * @return This ItemBuilder instance
     */
    public ItemStackBuilder unbreakable(boolean unbreakable) {
        itemMeta.setUnbreakable(unbreakable);
        if (unbreakable) {
            addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        }
        return this;
    }

    /**
     * Sets the custom model data of the item.
     *
     * @param modelData The custom model data to set
     * @return This ItemBuilder instance
     */
    public ItemStackBuilder modelData(int modelData) {
        itemMeta.setCustomModelData(modelData);
        return this;
    }

    /**
     * Adds enchantments to the item.
     *
     * @param enchantments A map of enchantments and their levels
     * @return This ItemBuilder instance
     */
    public ItemStackBuilder enchantments(Map<Enchantment, Integer> enchantments) {
        if (enchantments != null && !enchantments.isEmpty()) {
            itemStack.setItemMeta(itemMeta);
            enchantments.forEach((enchantment, level) -> itemStack.addUnsafeEnchantment(enchantment, level));
            itemMeta = itemStack.getItemMeta();
        }
        return this;
    }

    /**
     * Sets the skull texture for player heads.
     *
     * @param base64 The Base64 encoded texture string
     * @return This ItemBuilder instance
     */
    public ItemStackBuilder skullTexture(String base64) {
        if (itemMeta instanceof SkullMeta skullMeta && base64 != null && !base64.isEmpty()) {
            UUID hashAsId = new UUID(base64.hashCode(), base64.hashCode());
            if (ServerVersionUtil.isNewHeadApiSupported()) {
                applyNewSkullTexture(skullMeta, base64, hashAsId);
            } else {
                applyLegacySkullTexture(base64, hashAsId);
            }
        }
        return this;
    }

    /**
     * Sets the color of leather armor using HEX color code.
     *
     * @param hexColor HEX color string (e.g., "#FF0000" or "FF0000")
     * @return This ItemBuilder instance
     */
    public ItemStackBuilder color(String hexColor) {
        if (itemMeta instanceof LeatherArmorMeta leatherMeta && hexColor != null && !hexColor.isEmpty()) {
            try {
                Color color = hexToColor(hexColor);
                leatherMeta.setColor(color);
            } catch (IllegalArgumentException e) {
                MessageUtil.logMessage(ConsoleColor.RED, "[getUtils] Invalid HEX color format: " + hexColor + " - " + e.getMessage());
            }
        }
        return this;
    }

    /**
     * Sets the color of a potion using HEX color code.
     *
     * @param hexColor HEX color string (e.g., "#FF0000" or "FF0000")
     * @return This ItemBuilder instance
     */
    public ItemStackBuilder potionColor(String hexColor) {
        if (itemMeta instanceof PotionMeta potionMeta && hexColor != null && !hexColor.isEmpty()) {
            try {
                Color color = hexToColor(hexColor);
                potionMeta.setColor(color);
            } catch (IllegalArgumentException e) {
                MessageUtil.logMessage(ConsoleColor.RED, "[getUtils] Invalid HEX color format for potion: " + hexColor + " - " + e.getMessage());
            }
        }
        return this;
    }

    /**
     * Sets the color of firework using HEX color code.
     *
     * @param hexColor HEX color string (e.g., "#FF0000" or "FF0000")
     * @return This ItemBuilder instance
     */
    public ItemStackBuilder fireworkColor(String hexColor) {
        if (itemMeta instanceof FireworkMeta fireworkMeta && hexColor != null && !hexColor.isEmpty()) {
            try {
                Color color = hexToColor(hexColor);
                // Note: Firework colors are more complex and might require additional configuration
                // This is a basic implementation - you may need to expand based on your needs
                MessageUtil.logMessage(ConsoleColor.YELLOW, "[getUtils] Firework color setting requires additional implementation for full functionality");
            } catch (IllegalArgumentException e) {
                MessageUtil.logMessage(ConsoleColor.RED, "[getUtils] Invalid HEX color format for firework: " + hexColor + " - " + e.getMessage());
            }
        }
        return this;
    }

    /**
     * Helper method to convert HEX color to Bukkit Color
     *
     * @param hex HEX color string (e.g., "#FF0000" or "FF0000")
     * @return Bukkit Color object
     */
    private Color hexToColor(String hex) {
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

    private void applyNewSkullTexture(SkullMeta skullMeta, String base64, UUID hashAsId) {
        try {
            // Use reflection to access new API methods
            Class<?> bukkitClass = Class.forName("org.bukkit.Bukkit");
            Method createProfileMethod = bukkitClass.getMethod("createPlayerProfile", UUID.class);
            Object playerProfile = createProfileMethod.invoke(null, hashAsId);

            Class<?> playerProfileClass = Class.forName("org.bukkit.profile.PlayerProfile");
            Method getTexturesMethod = playerProfileClass.getMethod("getTextures");
            Object playerTextures = getTexturesMethod.invoke(playerProfile);

            Class<?> playerTexturesClass = Class.forName("org.bukkit.profile.PlayerTextures");
            Method setSkinMethod = playerTexturesClass.getMethod("setSkin", URL.class);

            URL skinUrl = getUrlFromBase64(base64);
            setSkinMethod.invoke(playerTextures, skinUrl);

            Class<?> skullMetaClass = Class.forName("org.bukkit.inventory.meta.SkullMeta");
            Method setOwnerProfileMethod = skullMetaClass.getMethod("setOwnerProfile", playerProfileClass);
            setOwnerProfileMethod.invoke(skullMeta, playerProfile);

        } catch (Exception e) {
            MessageUtil.logMessage(ConsoleColor.RED, "[getUtils] Failed to set new skull texture: " + e.getMessage());
        }
    }

    public URL getUrlFromBase64(String base64) throws MalformedURLException {
        String decoded = new String(Base64.getDecoder().decode(base64));
        return new URL(decoded.substring("{\"textures\":{\"SKIN\":{\"url\":\"".length(), decoded.length() - "\"}}}".length()));
    }

    private void applyLegacySkullTexture(String base64, UUID hashAsId) {
        try {
            GameProfile profile = new GameProfile(hashAsId, "gether.dev");
            profile.getProperties().put("textures", new Property("textures", base64));
            Method method = itemMeta.getClass().getDeclaredMethod("setProfile", GameProfile.class);
            method.setAccessible(true);
            method.invoke(itemMeta, profile);
        } catch (Exception e) {
            MessageUtil.logMessage(ConsoleColor.RED, "[getUtils] Failed to set legacy skull texture: " + e.getMessage());
            String nbt = "{SkullOwner:{Id:\"" + hashAsId + "\",Properties:{textures:[{Value:\"" + base64 + "\"}]}}}";
            itemStack = Bukkit.getUnsafe().modifyItemStack(itemStack, nbt);
            itemMeta = itemStack.getItemMeta();
        }
    }

    public ItemStackBuilder potionData(PotionType type, boolean extended, boolean upgraded) {
        if (itemMeta instanceof PotionMeta potionMeta) {
            this.potionType = type;
            this.extended = extended;
            this.upgraded = upgraded;
        }
        return this;
    }

    /**
     * Builds the final ItemStack with all applied properties.
     *
     * @return The constructed ItemStack
     */
    public ItemStack build() {
        if (itemMeta == null) {
            return itemStack; // Jeśli meta nie istnieje, zwracamy surowy ItemStack
        }

        if (itemMeta instanceof PotionMeta potionMeta && potionType != null) {
            if (extended && upgraded) {
                throw new IllegalArgumentException("Potion cannot be both extended and upgraded.");
            }

            potionMeta.setBasePotionData(new PotionData(potionType, extended, upgraded));
            itemStack.setItemMeta(potionMeta);
        }

        if (attributeModifiers != null) {
            attributeModifiers.forEach((attribute, modifiers) -> {
                if (modifiers != null) {
                    modifiers.forEach(modifier -> itemMeta.addAttributeModifier(attribute, modifier));
                }
            });
        }

        if (!itemFlags.isEmpty()) {
            itemMeta.addItemFlags(itemFlags.toArray(new ItemFlag[0]));
        }

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }
}
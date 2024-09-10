package dev.gether.getutils.builder;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import dev.gether.getutils.Valid;
import dev.gether.getutils.utils.ColorFixer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Method;
import java.util.*;

public class ItemBuilder {

    private ItemStack itemStack;
    private ItemMeta itemMeta;
    private Map<Attribute, List<AttributeModifier>> attributeModifiers = new HashMap<>();
    private List<ItemFlag> itemFlags = new ArrayList<>();

    private ItemBuilder(Material material) {
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
    public static ItemBuilder of(Material material) {
        return new ItemBuilder(material);
    }

    /**
     * Sets the display name of the item.
     *
     * @param name The name to set
     * @return This ItemBuilder instance
     */
    public ItemBuilder name(String name) {
        Valid.checkNotNull(name, "Name cannot be null");
        itemMeta.setDisplayName(ColorFixer.addColors(name));
        return this;
    }

    /**
     * Sets the amount of the item.
     *
     * @param amount The amount to set
     * @return This ItemBuilder instance
     */
    public ItemBuilder amount(int amount) {
        itemStack.setAmount(Math.max(1, amount));
        return this;
    }

    /**
     * Adds attribute modifiers to the item.
     *
     * @param attributeModifiers A map of attributes and their modifiers
     * @return This ItemBuilder instance
     */
    public ItemBuilder attributeModifiers(Map<Attribute, List<AttributeModifier>> attributeModifiers) {
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
    public ItemBuilder addAttributeModifier(Attribute attribute, AttributeModifier modifier) {
        Valid.checkNotNull(attribute, "Attribute cannot be null");
        Valid.checkNotNull(modifier, "AttributeModifier cannot be null");
        this.attributeModifiers.computeIfAbsent(attribute, k -> new ArrayList<>()).add(modifier);
        return this;
    }

    /**
     * Adds item flags to the item.
     *
     * @param flags The flags to add
     * @return This ItemBuilder instance
     */
    public ItemBuilder addItemFlags(ItemFlag... flags) {
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
    public ItemBuilder lore(List<String> lore) {
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
    public ItemBuilder glow(boolean glow) {
        if (glow) {
            itemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
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
    public ItemBuilder unbreakable(boolean unbreakable) {
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
    public ItemBuilder modelData(int modelData) {
        itemMeta.setCustomModelData(modelData);
        return this;
    }

    /**
     * Adds enchantments to the item.
     *
     * @param enchantments A map of enchantments and their levels
     * @return This ItemBuilder instance
     */
    public ItemBuilder enchantments(Map<Enchantment, Integer> enchantments) {
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
    public ItemBuilder skullTexture(String base64) {
        if (itemMeta instanceof SkullMeta skullMeta && base64 != null && !base64.isEmpty()) {
            UUID hashAsId = new UUID(base64.hashCode(), base64.hashCode());
            GameProfile profile = new GameProfile(hashAsId, "gether.dev");
            profile.getProperties().put("textures", new Property("textures", base64));

            try {
                Method method = skullMeta.getClass().getDeclaredMethod("setProfile", GameProfile.class);
                method.setAccessible(true);
                method.invoke(skullMeta, profile);
            } catch (Exception e) {
                Bukkit.getLogger().warning("Failed to set skull texture: " + e.getMessage());
                itemStack = Bukkit.getUnsafe().modifyItemStack(itemStack,
                        "{SkullOwner:{Id:\"" + hashAsId + "\",Properties:{textures:[{Value:\"" + base64 + "\"}]}}}");
                itemMeta = itemStack.getItemMeta();
            }
        }
        return this;
    }

    /**
     * Builds the final ItemStack with all applied properties.
     *
     * @return The constructed ItemStack
     */
    public ItemStack build() {
        attributeModifiers.forEach((attribute, modifiers) ->
                modifiers.forEach(modifier -> itemMeta.addAttributeModifier(attribute, modifier)));

        itemMeta.addItemFlags(itemFlags.toArray(new ItemFlag[0]));

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
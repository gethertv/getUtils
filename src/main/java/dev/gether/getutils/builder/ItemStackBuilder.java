package dev.gether.getutils.builder;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import dev.gether.getutils.Valid;
import dev.gether.getutils.utils.ColorFixer;
import dev.gether.getutils.utils.ConsoleColor;
import dev.gether.getutils.utils.MessageUtil;
import dev.gether.getutils.utils.ServerVersionUtil;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class ItemStackBuilder {

    private ItemStack itemStack;
    private ItemMeta itemMeta;
    private Map<Attribute, List<AttributeModifier>> attributeModifiers = new HashMap<>();
    private List<ItemFlag> itemFlags = new ArrayList<>();

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
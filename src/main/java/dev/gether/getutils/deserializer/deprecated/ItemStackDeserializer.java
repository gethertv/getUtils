package dev.gether.getutils.deserializer.deprecated;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import dev.gether.getutils.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Deprecated
public class ItemStackDeserializer extends StdDeserializer<ItemStack> {

    public ItemStackDeserializer() {
        super(ItemStack.class);
    }

    @Override
    public ItemStack deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        Material type = Material.valueOf(node.get("type").asText());
        int amount = node.get("amount").asInt();
        ItemStack itemStack = new ItemStack(type, amount);

        if (node.has("meta")) {
            ItemMeta meta = itemStack.getItemMeta();
            deserializeItemMeta(node.get("meta"), meta, itemStack);
            itemStack.setItemMeta(meta);
        }

        return itemStack;
    }

    private void deserializeItemMeta(JsonNode metaNode, ItemMeta meta, ItemStack itemStack) {
        deserializeDisplayName(metaNode, meta);
        deserializeLore(metaNode, meta);
        deserializeEnchants(metaNode, meta);
        deserializeItemFlags(metaNode, meta);
        deserializeUnbreakable(metaNode, meta);
        deserializeCustomModelData(metaNode, meta);
        deserializeDamage(metaNode, meta);
        deserializeSkullOwner(metaNode, meta, itemStack);
        deserializePotionEffects(metaNode, meta);
    }

    private void deserializeDisplayName(JsonNode metaNode, ItemMeta meta) {
        if (metaNode.has("display-name")) {
            meta.setDisplayName(ColorFixer.addColors(metaNode.get("display-name").asText()));
        }
    }

    private void deserializeLore(JsonNode metaNode, ItemMeta meta) {
        if (metaNode.has("lore")) {
            List<String> lore = new ArrayList<>();
            metaNode.get("lore").elements().forEachRemaining(loreNode -> lore.add(loreNode.asText()));
            meta.setLore(ColorFixer.addColors(lore));
        }
    }

    private void deserializeEnchants(JsonNode metaNode, ItemMeta meta) {
        if (metaNode.has("enchants")) {
            JsonNode enchantNode = metaNode.get("enchants");
            enchantNode.fields().forEachRemaining(entry -> {
                Enchantment enchantment = Enchantment.getByKey(NamespacedKey.fromString(entry.getKey()));
                if (enchantment != null) {
                    meta.addEnchant(enchantment, entry.getValue().asInt(), true);
                }
            });
        }
    }

    private void deserializeItemFlags(JsonNode metaNode, ItemMeta meta) {
        if (metaNode.has("item-flags")) {
            metaNode.get("item-flags").forEach(flagNode ->
                    meta.addItemFlags(ItemFlag.valueOf(flagNode.asText())));
        }
    }

    private void deserializeUnbreakable(JsonNode metaNode, ItemMeta meta) {
        if (metaNode.has("unbreakable")) {
            meta.setUnbreakable(metaNode.get("unbreakable").asBoolean());
        }
    }

    private void deserializeCustomModelData(JsonNode metaNode, ItemMeta meta) {
        if (metaNode.has("custom-model-data")) {
            meta.setCustomModelData(metaNode.get("custom-model-data").asInt());
        }
    }

    private void deserializeDamage(JsonNode metaNode, ItemMeta meta) {
        if (metaNode.has("damage") && meta instanceof Damageable) {
            int damage = metaNode.get("damage").asInt();
            if(damage > 0) {
                ((Damageable) meta).setDamage(damage);
            }
        }
    }

    private void deserializeSkullOwner(JsonNode metaNode, ItemMeta meta, ItemStack itemStack) {
        if (metaNode.has("skull-owner") && meta instanceof SkullMeta) {
            JsonNode skullOwnerNode = metaNode.get("skull-owner");
            String base64Texture = skullOwnerNode.get("textures").asText();
            UUID uuid = UUID.fromString(skullOwnerNode.get("uuid").asText());

            if (ServerVersionUtil.isNewHeadApiSupported()) {
                deserializeNewSkullMeta((SkullMeta) meta, base64Texture, uuid);
            } else {
                deserializeOldSkullMeta((SkullMeta) meta, itemStack, base64Texture, uuid, skullOwnerNode.get("name").asText());
            }
        }
    }

    private void deserializeNewSkullMeta(SkullMeta skullMeta, String base64Texture, UUID uuid) {
        try {
            skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
            UUID hashAsId = new UUID(base64Texture.hashCode(), base64Texture.hashCode());
            applySkullTexture(skullMeta, base64Texture, hashAsId);
        } catch (Exception e) {
            MessageUtil.logMessage(ConsoleColor.RED, "Error deserializing new skull meta: " + e.getMessage());
        }
    }

    private void deserializeOldSkullMeta(SkullMeta skullMeta, ItemStack itemStack, String base64Texture, UUID uuid, String name) {
        try {
            GameProfile profile = new GameProfile(uuid, name);
            profile.getProperties().put("textures", new Property("textures", base64Texture));

            ReflectionUtils.invoke(skullMeta, "setProfile", profile);
        } catch (Exception e) {
            MessageUtil.logMessage(ConsoleColor.RED, "Error deserializing old skull meta: " + e.getMessage());
            try {
                String nbt = "{SkullOwner:{Id:\"" + uuid + "\",Properties:{textures:[{Value:\"" + base64Texture + "\"}]}}}";
                itemStack = Bukkit.getUnsafe().modifyItemStack(itemStack, nbt);
                skullMeta = (SkullMeta) itemStack.getItemMeta();
            } catch (Exception unsafeException) {
                MessageUtil.logMessage(ConsoleColor.RED, "Failed to apply unsafe method: " + unsafeException.getMessage());
            }
        }
    }

    private void deserializePotionEffects(JsonNode metaNode, ItemMeta meta) {
        if (meta instanceof PotionMeta && metaNode.has("potion")) {
            PotionMeta potionMeta = (PotionMeta) meta;
            JsonNode potionNode = metaNode.get("potion");

            try {
                PotionType baseType = PotionType.valueOf(potionNode.get("base-effect").asText());
                boolean isExtended = potionNode.get("is-extended").asBoolean();
                boolean hasUpgrade = potionNode.get("has-upgrade").asBoolean();
                potionMeta.setBasePotionData(new PotionData(baseType, isExtended, hasUpgrade));
            } catch (IllegalArgumentException e) {
                MessageUtil.logMessage(ConsoleColor.RED, "Invalid base potion effect: " + potionNode.get("base-effect").asText());
            }

            if (potionNode.has("custom-effects")) {
                potionNode.get("custom-effects").forEach(effectNode -> {
                    try {
                        String effectName = effectNode.get("effect").asText();
                        PotionEffectType effectType = PotionEffectType.getByName(effectName);
                        if (effectType == null) {
                            throw new IllegalArgumentException("Unknown potion effect type: " + effectName);
                        }
                        int amplifier = effectNode.get("amplifier").asInt();
                        int duration = effectNode.get("duration").asInt();
                        potionMeta.addCustomEffect(new PotionEffect(effectType, duration, amplifier), true);
                    } catch (Exception e) {
                        MessageUtil.logMessage(ConsoleColor.RED, "Error deserializing custom potion effect: " + e.getMessage());
                    }
                });
            }
        }
    }

    private void applySkullTexture(SkullMeta skullMeta, String base64Texture, UUID uuid) {
        try {
            Class<?> bukkitClass = Class.forName("org.bukkit.Bukkit");
            Method createProfileMethod = bukkitClass.getMethod("createPlayerProfile", UUID.class);
            Object playerProfile = createProfileMethod.invoke(null, uuid);

            Class<?> playerProfileClass = Class.forName("org.bukkit.profile.PlayerProfile");
            Method getTexturesMethod = playerProfileClass.getMethod("getTextures");
            Object playerTextures = getTexturesMethod.invoke(playerProfile);

            Class<?> playerTexturesClass = Class.forName("org.bukkit.profile.PlayerTextures");
            Method setSkinMethod = playerTexturesClass.getMethod("setSkin", URL.class);
            URL skinUrl = getUrlFromBase64(base64Texture);
            setSkinMethod.invoke(playerTextures, skinUrl);

            Class<?> skullMetaClass = Class.forName("org.bukkit.inventory.meta.SkullMeta");
            Method setOwnerProfileMethod = skullMetaClass.getMethod("setOwnerProfile", playerProfileClass);
            setOwnerProfileMethod.invoke(skullMeta, playerProfile);
        } catch (Exception e) {
            MessageUtil.logMessage(ConsoleColor.RED, "Error applying skull texture: " + e.getMessage());
        }
    }


    private URL getUrlFromBase64(String base64) throws Exception {
        String decoded = new String(java.util.Base64.getDecoder().decode(base64));
        String url = decoded.substring(decoded.indexOf("\"textures\":{\"SKIN\":{\"url\":\"") + 27, decoded.indexOf("\"}}}"));
        return new URL(url);
    }
}
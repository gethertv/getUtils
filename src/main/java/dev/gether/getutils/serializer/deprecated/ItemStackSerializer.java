package dev.gether.getutils.serializer.deprecated;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import dev.gether.getutils.utils.ConsoleColor;
import dev.gether.getutils.utils.MessageUtil;
import dev.gether.getutils.utils.ReflectionUtils;
import dev.gether.getutils.utils.ServerVersionUtil;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionEffect;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Deprecated
public class ItemStackSerializer extends StdSerializer<ItemStack> {

    public ItemStackSerializer() {
        super(ItemStack.class);
    }

    @Override
    public void serialize(ItemStack itemStack, JsonGenerator gen, SerializerProvider provider) throws IOException {
        Map<String, Object> serializedItem = new HashMap<>();
        serializeBasicItemData(itemStack, serializedItem);

        if (itemStack.hasItemMeta()) {
            Map<String, Object> metaMap = serializeItemMeta(itemStack.getItemMeta());
            serializedItem.put("meta", metaMap);
        }

        gen.writeObject(serializedItem);
    }

    private void serializeBasicItemData(ItemStack itemStack, Map<String, Object> serializedItem) {
        serializedItem.put("type", itemStack.getType().name());
        serializedItem.put("amount", itemStack.getAmount());
    }

    private Map<String, Object> serializeItemMeta(ItemMeta meta) {
        Map<String, Object> metaMap = new HashMap<>();

        serializeCommonMetaData(meta, metaMap);
        serializeEnchantments(meta, metaMap);
        serializeItemFlags(meta, metaMap);
        serializeCustomModelData(meta, metaMap);
        serializeDamage(meta, metaMap);

        if (meta instanceof SkullMeta) {
            serializeSkullMeta((SkullMeta) meta, metaMap);
        }

        if (meta instanceof PotionMeta) {
            serializePotionMeta((PotionMeta) meta, metaMap);
        }

        return metaMap;
    }

    private void serializeCommonMetaData(ItemMeta meta, Map<String, Object> metaMap) {
        if (meta.hasDisplayName()) metaMap.put("display-name", meta.getDisplayName());
        if (meta.hasLore()) metaMap.put("lore", meta.getLore());
        metaMap.put("unbreakable", meta.isUnbreakable());
    }

    private void serializeEnchantments(ItemMeta meta, Map<String, Object> metaMap) {
        if (meta.hasEnchants()) {
            Map<String, Integer> enchants = meta.getEnchants().entrySet().stream()
                    .collect(Collectors.toMap(e -> e.getKey().getKey().toString(), Map.Entry::getValue));
            metaMap.put("enchants", enchants);
        }
    }

    private void serializeItemFlags(ItemMeta meta, Map<String, Object> metaMap) {
        if (!meta.getItemFlags().isEmpty()) {
            metaMap.put("item-flags", meta.getItemFlags().stream().map(Enum::name).collect(Collectors.toList()));
        }
    }

    private void serializeCustomModelData(ItemMeta meta, Map<String, Object> metaMap) {
        if (meta.hasCustomModelData()) metaMap.put("custom-model-data", meta.getCustomModelData());
    }

    private void serializeDamage(ItemMeta meta, Map<String, Object> metaMap) {
        if (meta instanceof Damageable) metaMap.put("damage", ((Damageable) meta).getDamage());
    }

    private void serializeSkullMeta(SkullMeta skullMeta, Map<String, Object> metaMap) {
        if (ServerVersionUtil.isNewHeadApiSupported()) {
            serializeNewSkullMeta(skullMeta, metaMap);
        } else {
            serializeOldSkullMeta(skullMeta, metaMap);
        }
    }

    private void serializeNewSkullMeta(SkullMeta skullMeta, Map<String, Object> metaMap) {
        try {
            Object playerProfile = getOwnerProfile(skullMeta);
            if (playerProfile != null) {
                Map<String, Object> skullInfo = new HashMap<>();
                skullInfo.put("textures", getTextureValue(playerProfile));
                skullInfo.put("name", ReflectionUtils.invoke(playerProfile, "getName"));
                skullInfo.put("uuid", ReflectionUtils.invoke(playerProfile, "getUniqueId").toString());

                URL skinURL = (URL) ReflectionUtils.invoke(ReflectionUtils.invoke(playerProfile, "getTextures"), "getSkin");
                if (skinURL != null) {
                    metaMap.put("skull-owner", skullInfo);
                }
            }
        } catch (Exception e) {
            MessageUtil.logMessage(ConsoleColor.RED, "Error serializing new skull meta: " + e.getMessage());
            MessageUtil.logMessage(ConsoleColor.RED, "Stack trace: " + Arrays.toString(e.getStackTrace()));
        }
    }

    private void serializeOldSkullMeta(SkullMeta skullMeta, Map<String, Object> metaMap) {
        try {
            Object profile = ReflectionUtils.getFieldValue(skullMeta, "profile");
            if (profile != null) {
                Map<String, Object> skullInfo = new HashMap<>();
                skullInfo.put("uuid", ReflectionUtils.invoke(profile, "getId").toString());
                skullInfo.put("name", ReflectionUtils.invoke(profile, "getName"));

                Collection<?> properties = (Collection<?>) ReflectionUtils.invoke(ReflectionUtils.invoke(profile, "getProperties"), "get", "textures");
                if (!properties.isEmpty()) {
                    Object textures = properties.iterator().next();
                    skullInfo.put("textures", ReflectionUtils.invoke(textures, "getValue"));
                }

                metaMap.put("skull-owner", skullInfo);
            }
        } catch (Exception e) {
            MessageUtil.logMessage(ConsoleColor.RED, "Error serializing old skull meta: " + e.getMessage());
            MessageUtil.logMessage(ConsoleColor.RED, "Stack trace: " + Arrays.toString(e.getStackTrace()));
        }
    }

    public String getTextureValue(Object playerProfile) {
        try {
            Object textureProperty = ReflectionUtils.invoke(playerProfile, "getProperty", "textures");

            if (textureProperty != null) {
                return (String) ReflectionUtils.getFieldValue(textureProperty, "value");
            }
        } catch (Exception e) {
            MessageUtil.logMessage(ConsoleColor.RED, "Error retrieving texture value: " + e.getMessage());
            ReflectionUtils.debugObject(playerProfile);
        }
        return null;
    }

    private void serializePotionMeta(PotionMeta potionMeta, Map<String, Object> metaMap) {
        Map<String, Object> potionMap = new HashMap<>();
        potionMap.put("base-effect", potionMeta.getBasePotionData().getType().toString());
        potionMap.put("is-extended", potionMeta.getBasePotionData().isExtended());
        potionMap.put("has-upgrade", potionMeta.getBasePotionData().isUpgraded());

        List<Map<String, Object>> customEffects = potionMeta.getCustomEffects().stream()
                .map(this::serializePotionEffect)
                .collect(Collectors.toList());
        potionMap.put("custom-effects", customEffects);

        metaMap.put("potion", potionMap);
    }

    private Map<String, Object> serializePotionEffect(PotionEffect effect) {
        Map<String, Object> effectMap = new HashMap<>();
        effectMap.put("effect", effect.getType().getName());
        effectMap.put("amplifier", effect.getAmplifier());
        effectMap.put("duration", effect.getDuration());
        return effectMap;
    }

    private Object getOwnerProfile(SkullMeta skullMeta) throws Exception {
        Class<?> skullMetaClass = Class.forName("org.bukkit.inventory.meta.SkullMeta");
        Method getOwnerProfileMethod = skullMetaClass.getMethod("getOwnerProfile");
        return getOwnerProfileMethod.invoke(skullMeta);
    }

}
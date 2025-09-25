package dev.gether.getutils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import dev.gether.getutils.deserializer.*;
import dev.gether.getutils.models.Cuboid;
import dev.gether.getutils.serializer.*;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public class ObjectMapperSingleton {
    private static final ObjectMapper instance = createObjectMapper();

    public static ObjectMapper getInstance() {
        return instance;
    }

    /**
     * Creates and configures an ObjectMapper for YAML serialization and deserialization.
     *
     * @return A configured ObjectMapper instance.
     */
    private static ObjectMapper createObjectMapper() {
        YAMLFactory yamlFactory = new YAMLFactory();
        yamlFactory.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER);

        yamlFactory.configure(YAMLGenerator.Feature.MINIMIZE_QUOTES, true);
        yamlFactory.configure(YAMLGenerator.Feature.SPLIT_LINES, false);

        ObjectMapper mapper = new ObjectMapper(yamlFactory);

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        mapper.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, false);
        mapper.configure(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS, false);

        mapper.configure(JsonParser.Feature.USE_FAST_BIG_NUMBER_PARSER, false);
        mapper.configure(JsonParser.Feature.STRICT_DUPLICATE_DETECTION, true);

        StreamReadConstraints constraints = StreamReadConstraints.builder()
                .maxNestingDepth(1000)
                .maxNumberLength(100)
                .maxStringLength(100_000)
                .maxNameLength(1000)
                .build();

        yamlFactory.setStreamReadConstraints(constraints);

        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.findAndRegisterModules();

        SimpleModule module = new SimpleModule();
        registerSerializers(module);
        registerDeserializers(module);

        mapper.registerModule(module);
        return mapper;
    }


    /**
     * Registers custom serializers with the provided SimpleModule.
     *
     * @param module The SimpleModule to register serializers with.
     */
    private static void registerSerializers(SimpleModule module) {
        //module.addSerializer(ItemStack.class, new ItemStackSerializer());
        module.addSerializer(ItemStack.class, new ItemStackSerializer());
        module.addSerializer(Location.class, new LocationSerializer());
        module.addSerializer(Cuboid.class, new CuboidSerializer());
        module.addSerializer(AttributeModifier.class, new AttributeModifierSerializer());
        module.addSerializer(PotionEffect.class, new PotionEffectSerializer());
        module.addSerializer(Sound.class, new SoundSerializer());
        module.addSerializer(Sound.class, new SoundSerializer());

        module.addSerializer(Enchantment.class, new EnchantmentSerializer());
        module.addKeySerializer(Enchantment.class, new EnchantmentKeySerializer());
    }

    /**
     * Registers custom deserializers with the provided SimpleModule.
     *
     * @param module The SimpleModule to register deserializers with.
     */
    private static void registerDeserializers(SimpleModule module) {
        //module.addDeserializer(ItemStack.class, new ItemStackDeserializer());
        module.addDeserializer(ItemStack.class, new ItemStackDeserializer());
        module.addDeserializer(Location.class, new LocationDeserializer());
        module.addDeserializer(Cuboid.class, new CuboidDeserializer());
        module.addDeserializer(AttributeModifier.class, new AttributeModifierDeserializer());
        module.addDeserializer(PotionEffect.class, new PotionEffectDeserializer());
        module.addKeyDeserializer(Enchantment.class, new EnchantmentKeyDeserializer());
        module.addDeserializer(Sound.class, new SoundDeserializer());
        module.addDeserializer(Enchantment.class, new EnchantmentDeserializer());
    }
}
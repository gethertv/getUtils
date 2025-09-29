package dev.gether.getutils;

import dev.gether.getutils.annotation.Comment;
import dev.gether.getutils.annotation.YamlIgnore;
import dev.gether.getutils.utils.MessageUtil;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.NamespacedKey;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Logger;

@Getter
public class GetConfig {
    private static final Logger logger = Logger.getLogger(GetConfig.class.getName());

    private final Yaml yaml;
    private File file;
    private URL url;
    private String content;
    private boolean isLoading = false;
    private boolean isSaving = false;

    public GetConfig() {
        this.yaml = createYaml();
    }

    private Yaml createYaml() {
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dumperOptions.setPrettyFlow(true);
        dumperOptions.setIndent(2);

        // Force empty collections to use block style
        dumperOptions.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
        dumperOptions.setWidth(Integer.MAX_VALUE); // Don't wrap lines

        LoaderOptions loaderOptions = new LoaderOptions();

        CustomRepresenter representer = new CustomRepresenter(dumperOptions);
        CustomConstructor constructor = new CustomConstructor(loaderOptions);

        return new Yaml(constructor, representer, dumperOptions, loaderOptions);
    }


    private class CustomRepresenter extends Representer {
        public CustomRepresenter(DumperOptions options) {
            super(options);

            this.multiRepresenters.put(Location.class, new RepresentLocation());
            this.multiRepresenters.put(ItemStack.class, new RepresentItemStack());
            this.multiRepresenters.put(PotionEffect.class, new RepresentPotionEffect());
            this.multiRepresenters.put(AttributeModifier.class, new RepresentAttributeModifier());
            this.multiRepresenters.put(Enchantment.class, new RepresentEnchantment());
            this.multiRepresenters.put(Sound.class, new RepresentSound());

            this.multiRepresenters.put(int[].class, new RepresentIntArray());
            this.multiRepresenters.put(long[].class, new RepresentLongArray());
            this.multiRepresenters.put(double[].class, new RepresentDoubleArray());
            this.multiRepresenters.put(String[].class, new RepresentStringArray());
            this.multiRepresenters.put(Object[].class, new RepresentObjectArray());

            this.multiRepresenters.put(List.class, new RepresentList());
            this.multiRepresenters.put(Set.class, new RepresentSet());

            this.multiRepresenters.put(Object.class, new RepresentCustomObject());
        }

        private class RepresentIntArray implements Represent {
            public Node representData(Object data) {
                int[] array = (int[]) data;
                List<Integer> list = new ArrayList<>();
                for (int value : array) {
                    list.add(value);
                }
                if (list.isEmpty()) {
                    return representSequence(Tag.SEQ, list, DumperOptions.FlowStyle.FLOW);
                } else {
                    return representSequence(Tag.SEQ, list, DumperOptions.FlowStyle.BLOCK);
                }
            }
        }

        private class RepresentLongArray implements Represent {
            public Node representData(Object data) {
                long[] array = (long[]) data;
                List<Long> list = new ArrayList<>();
                for (long value : array) {
                    list.add(value);
                }
                return representSequence(Tag.SEQ, list, list.isEmpty() ? DumperOptions.FlowStyle.FLOW : DumperOptions.FlowStyle.BLOCK);
            }
        }

        private class RepresentDoubleArray implements Represent {
            public Node representData(Object data) {
                double[] array = (double[]) data;
                List<Double> list = new ArrayList<>();
                for (double value : array) {
                    list.add(value);
                }
                return representSequence(Tag.SEQ, list, list.isEmpty() ? DumperOptions.FlowStyle.FLOW : DumperOptions.FlowStyle.BLOCK);
            }
        }

        private class RepresentStringArray implements Represent {
            public Node representData(Object data) {
                String[] array = (String[]) data;
                List<String> list = Arrays.asList(array);
                return representSequence(Tag.SEQ, list, list.isEmpty() ? DumperOptions.FlowStyle.FLOW : DumperOptions.FlowStyle.BLOCK);
            }
        }

        private class RepresentObjectArray implements Represent {
            public Node representData(Object data) {
                Object[] array = (Object[]) data;
                List<Object> list = Arrays.asList(array);
                return representSequence(Tag.SEQ, list, list.isEmpty() ? DumperOptions.FlowStyle.FLOW : DumperOptions.FlowStyle.BLOCK);
            }
        }

        private class RepresentList implements Represent {
            public Node representData(Object data) {
                List<?> list = (List<?>) data;
                if (list.isEmpty()) {
                    return representSequence(getTag(data.getClass(), Tag.SEQ), list, DumperOptions.FlowStyle.FLOW);
                } else {
                    return representSequence(getTag(data.getClass(), Tag.SEQ), list, DumperOptions.FlowStyle.BLOCK);
                }
            }
        }

        private class RepresentSet implements Represent {
            public Node representData(Object data) {
                Set<?> set = (Set<?>) data;
                List<Object> list = new ArrayList<>(set);
                if (list.isEmpty()) {
                    return representSequence(getTag(data.getClass(), Tag.SEQ), list, DumperOptions.FlowStyle.FLOW);
                } else {
                    return representSequence(getTag(data.getClass(), Tag.SEQ), list, DumperOptions.FlowStyle.BLOCK);
                }
            }
        }

        private class RepresentLocation implements Represent {
            public Node representData(Object data) {
                return represent(((Location) data).serialize());
            }
        }

        private class RepresentItemStack implements Represent {
            public Node representData(Object data) {
                return represent(((ItemStack) data).serialize());
            }
        }

        private class RepresentPotionEffect implements Represent {
            public Node representData(Object data) {
                return represent(((PotionEffect) data).serialize());
            }
        }

        private class RepresentAttributeModifier implements Represent {
            public Node representData(Object data) {
                return represent(((AttributeModifier) data).serialize());
            }
        }

        private class RepresentEnchantment implements Represent {
            public Node representData(Object data) {
                return representScalar(Tag.STR, ((Enchantment) data).getKey().getKey());
            }
        }

        private class RepresentSound implements Represent {
            public Node representData(Object data) {
                return representScalar(Tag.STR, ((Sound) data).name());
            }
        }

        private class RepresentCustomObject implements Represent {
            public Node representData(Object data) {
                if (isCustomObject(data)) {
                    try {
                        Map<String, Object> objectMap = objectToMap(data);
                        return represent(objectMap);
                    } catch (Exception e) {
                        return representScalar(Tag.STR, data.toString());
                    }
                }
                return null;
            }
        }
    }

    private static class CustomConstructor extends Constructor {
        public CustomConstructor(LoaderOptions loaderOptions) {
            super(loaderOptions);
        }
    }

    public void setFile(File file) {
        this.file = file;
        this.url = null;
        if (!file.exists()) {
            try {
                Files.createDirectories(file.getParentFile().toPath());
                Files.createFile(file.toPath());
            } catch (IOException e) {
                throw new RuntimeException("Failed to create file", e);
            }
        }
    }

    public void setUrl(String urlString) {
        try {
            this.url = new URL(urlString);
            this.file = null;
        } catch (IOException e) {
            throw new RuntimeException("Invalid URL: " + urlString, e);
        }
    }

    public void save() {
        if (isSaving) return;

        try {
            isSaving = true;

            Map<String, Object> dataMap = objectToMap(this);
            String yamlContent = yaml.dump(dataMap);
            String processedYaml = insertComments(yamlContent);

            if (file != null) {
                Files.write(file.toPath(), processedYaml.getBytes(StandardCharsets.UTF_8));
            } else if (url != null) {
                saveToUrl(processedYaml);
            } else {
                this.content = processedYaml;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to save configuration", e);
        } finally {
            isSaving = false;
        }
    }

    public void load() {
        if (isLoading) return;

        boolean wasEmpty = false;

        try {
            isLoading = true;
            String yamlContent = null;

            if (file != null) {
                if (!file.exists() || Files.size(file.toPath()) == 0) {
                    wasEmpty = true;
                } else {
                    yamlContent = Files.readString(file.toPath(), StandardCharsets.UTF_8);
                }
            } else if (url != null) {
                yamlContent = loadFromUrl();
                wasEmpty = (yamlContent == null || yamlContent.trim().isEmpty());
            } else if (content != null) {
                yamlContent = content;
            }

            if (yamlContent != null && !yamlContent.trim().isEmpty()) {
                Object data = yaml.load(yamlContent);

                if (data instanceof Map) {
                    mapToObject((Map<String, Object>) data, this);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to load configuration", e);
        } finally {
            isLoading = false;
        }

        if (wasEmpty) {
            save();
        }
    }

    private Map<String, Object> objectToMap(Object obj) throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();

        for (Field field : obj.getClass().getDeclaredFields()) {
            if (shouldSkipField(field)) continue;

            field.setAccessible(true);
            Object value = field.get(obj);

            if (value != null) {
                result.put(field.getName(), serializeValue(value));
            }
        }

        return result;
    }

    private void mapToObject(Map<String, Object> data, Object target) throws Exception {
        for (Field field : target.getClass().getDeclaredFields()) {
            if (shouldSkipField(field)) continue;

            String fieldName = field.getName();
            if (!data.containsKey(fieldName)) continue;

            field.setAccessible(true);
            Object value = data.get(fieldName);
            Object convertedValue = deserializeValue(value, field.getType(), field.getGenericType());

            field.set(target, convertedValue);
        }
    }

    @SuppressWarnings("unchecked")
    private Object serializeValue(Object value) throws Exception {
        if (value == null) return null;

        // Array types - convert to lists
        if (value instanceof int[]) {
            int[] array = (int[]) value;
            List<Integer> list = new ArrayList<>();
            for (int val : array) {
                list.add(val);
            }
            return list;
        } else if (value instanceof long[]) {
            long[] array = (long[]) value;
            List<Long> list = new ArrayList<>();
            for (long val : array) {
                list.add(val);
            }
            return list;
        } else if (value instanceof double[]) {
            double[] array = (double[]) value;
            List<Double> list = new ArrayList<>();
            for (double val : array) {
                list.add(val);
            }
            return list;
        } else if (value instanceof String[]) {
            return Arrays.asList((String[]) value);
        } else if (value instanceof Object[]) {
            Object[] array = (Object[]) value;
            List<Object> list = new ArrayList<>();
            for (Object val : array) {
                list.add(serializeValue(val)); // Recursively serialize
            }
            return list;
        }

        // Bukkit types
        else if (value instanceof Location) {
            return ((Location) value).serialize();
        } else if (value instanceof ItemStack) {
            return ((ItemStack) value).serialize();
        } else if (value instanceof PotionEffect) {
            return ((PotionEffect) value).serialize();
        } else if (value instanceof AttributeModifier) {
            return ((AttributeModifier) value).serialize();
        } else if (value instanceof Enchantment) {
            return ((Enchantment) value).getKey().getKey();
        } else if (value instanceof Sound) {
            return ((Sound) value).name();
        } else if (value instanceof Enum) {
            return ((Enum<?>) value).name();
        } else if (value instanceof Collection) {
            List<Object> list = new ArrayList<>();
            for (Object item : (Collection<?>) value) {
                list.add(serializeValue(item));
            }
            return list;
        } else if (value instanceof Map) {
            Map<Object, Object> map = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                Object key = entry.getKey();
                map.put(key, serializeValue(entry.getValue()));
            }
            return map;
        } else if (isCustomObject(value)) {
            return objectToMap(value);
        }

        return value;
    }

    @SuppressWarnings("unchecked")
    private Object deserializeValue(Object value, Class<?> targetType, Type genericType) throws Exception {
        if (value == null) return null;

        // Array types - convert lists back to arrays
        if (targetType == int[].class && value instanceof List) {
            List<Number> list = (List<Number>) value;
            return list.stream().mapToInt(Number::intValue).toArray();
        } else if (targetType == long[].class && value instanceof List) {
            List<Number> list = (List<Number>) value;
            return list.stream().mapToLong(Number::longValue).toArray();
        } else if (targetType == double[].class && value instanceof List) {
            List<Number> list = (List<Number>) value;
            return list.stream().mapToDouble(Number::doubleValue).toArray();
        } else if (targetType == String[].class && value instanceof List) {
            List<String> list = (List<String>) value;
            return list.toArray(new String[0]);
        } else if (targetType.isArray() && value instanceof List) {
            List<Object> list = (List<Object>) value;
            Class<?> componentType = targetType.getComponentType();
            Object array = java.lang.reflect.Array.newInstance(componentType, list.size());
            for (int i = 0; i < list.size(); i++) {
                java.lang.reflect.Array.set(array, i, deserializeValue(list.get(i), componentType, null));
            }
            return array;
        }
        // Bukkit types
        else if (targetType == Location.class && value instanceof Map) {
            return Location.deserialize((Map<String, Object>) value);
        } else if (targetType == ItemStack.class && value instanceof Map) {
            return ItemStack.deserialize((Map<String, Object>) value);
        } else if (targetType == PotionEffect.class && value instanceof Map) {
            return new PotionEffect((Map<String, Object>) value);
        } else if (targetType == AttributeModifier.class && value instanceof Map) {
            return deserializeAttributeModifier((Map<String, Object>) value);
        } else if (targetType == Enchantment.class && value instanceof String) {
            return Enchantment.getByKey(NamespacedKey.minecraft((String) value));
        } else if (targetType == Sound.class && value instanceof String) {
            return Sound.valueOf((String) value);
        } else if (targetType.isEnum() && value instanceof String) {
            return Enum.valueOf((Class<Enum>) targetType, (String) value);
        }
        // Collections (List, Set)
        else if (value instanceof List) {
            List<?> sourceList = (List<?>) value;

            if (Set.class.isAssignableFrom(targetType)) {
                Set<Object> resultSet = new LinkedHashSet<>();

                Class<?> elementType = Object.class;
                if (genericType instanceof ParameterizedType) {
                    ParameterizedType paramType = (ParameterizedType) genericType;
                    Type[] typeArgs = paramType.getActualTypeArguments();
                    if (typeArgs.length > 0 && typeArgs[0] instanceof Class) {
                        elementType = (Class<?>) typeArgs[0];
                    }
                }

                for (Object item : sourceList) {
                    if (item instanceof Map && isCustomClass(elementType)) {
                        Object instance = elementType.getDeclaredConstructor().newInstance();
                        mapToObject((Map<String, Object>) item, instance);
                        resultSet.add(instance);
                    } else {
                        resultSet.add(deserializeValue(item, elementType, null));
                    }
                }
                return resultSet;
            } else if (List.class.isAssignableFrom(targetType)) {
                List<Object> resultList = new ArrayList<>();

                Class<?> elementType = Object.class;
                if (genericType instanceof ParameterizedType) {
                    ParameterizedType paramType = (ParameterizedType) genericType;
                    Type[] typeArgs = paramType.getActualTypeArguments();
                    if (typeArgs.length > 0 && typeArgs[0] instanceof Class) {
                        elementType = (Class<?>) typeArgs[0];
                    }
                }

                for (Object item : sourceList) {
                    if (item instanceof Map && isCustomClass(elementType)) {
                        Object instance = elementType.getDeclaredConstructor().newInstance();
                        mapToObject((Map<String, Object>) item, instance);
                        resultList.add(instance);
                    } else {
                        resultList.add(deserializeValue(item, elementType, null));
                    }
                }
                return resultList;
            } else if (Collection.class.isAssignableFrom(targetType)) {
                return new ArrayList<>(sourceList);
            }
        }
        // Maps - PRZENIESIONE TUTAJ, NA ODPOWIEDNI POZIOM
        else if (value instanceof Map && Map.class.isAssignableFrom(targetType)) {
            Map<Object, Object> sourceMap = (Map<Object, Object>) value;
            Map<Object, Object> resultMap = new LinkedHashMap<>();

            Class<?> keyType = Object.class;
            Class<?> valueType = Object.class;

            if (genericType instanceof ParameterizedType) {
                ParameterizedType paramType = (ParameterizedType) genericType;
                Type[] typeArgs = paramType.getActualTypeArguments();
                MessageUtil.broadcast("DEBUG: Found " + typeArgs.length + " type args");
                if (typeArgs.length >= 2) {
                    MessageUtil.broadcast("DEBUG: keyType: " + typeArgs[0]);
                    MessageUtil.broadcast("DEBUG: valueType: " + typeArgs[1]);
                    if (typeArgs[0] instanceof Class) keyType = (Class<?>) typeArgs[0];
                    if (typeArgs[1] instanceof Class) valueType = (Class<?>) typeArgs[1];
                }
            }

            for (Map.Entry<?, ?> entry : sourceMap.entrySet()) {
                Object deserializedKey = deserializeValue(entry.getKey(), keyType, null);
                Object deserializedValue = deserializeValue(entry.getValue(), valueType, null);
                resultMap.put(deserializedKey, deserializedValue);
            }

            return resultMap;
        }
        // Custom objects
        else if (value instanceof Map && isCustomClass(targetType)) {
            Object instance = targetType.getDeclaredConstructor().newInstance();
            mapToObject((Map<String, Object>) value, instance);
            return instance;
        }

        // Basic type conversion
        if (targetType == String.class) {
            return value.toString();
        } else if (targetType == Integer.class || targetType == int.class) {
            return value instanceof Number ? ((Number) value).intValue() : Integer.parseInt(value.toString());
        } else if (targetType == Double.class || targetType == double.class) {
            return value instanceof Number ? ((Number) value).doubleValue() : Double.parseDouble(value.toString());
        } else if (targetType == Boolean.class || targetType == boolean.class) {
            return value instanceof Boolean ? value : Boolean.parseBoolean(value.toString());
        }

        return value;
    }

    @SuppressWarnings("unchecked")
    private Object deserializeValue(Object value, Class<?> targetType) throws Exception {
        return deserializeValue(value, targetType, null);
    }

    private AttributeModifier deserializeAttributeModifier(Map<String, Object> data) {
        String name = (String) data.get("name");
        double amount = ((Number) data.get("amount")).doubleValue();
        String operationStr = (String) data.get("operation");
        String slotStr = (String) data.get("slot");


        AttributeModifier.Operation operation = AttributeModifier.Operation.valueOf(operationStr);
        org.bukkit.inventory.EquipmentSlot slot = org.bukkit.inventory.EquipmentSlot.valueOf(slotStr.toUpperCase());

        return new AttributeModifier(UUID.randomUUID(), name, amount, operation, slot);
    }

    private boolean isCustomObject(Object value) {
        String className = value.getClass().getName();
        return !className.startsWith("java.") &&
                !className.startsWith("javax.") &&
                !className.startsWith("org.bukkit.") &&
                !value.getClass().isPrimitive() &&
                !value.getClass().isEnum();
    }

    private boolean isCustomClass(Class<?> clazz) {
        String className = clazz.getName();
        return !className.startsWith("java.") &&
                !className.startsWith("javax.") &&
                !className.startsWith("org.bukkit.") &&
                !clazz.isPrimitive() &&
                !clazz.isEnum();
    }

    private String insertComments(String yamlString) {
        // First fix empty lists format
        yamlString = fixEmptyListsFormat(yamlString);

        Map<String, String[]> comments = new LinkedHashMap<>();
        for (Field field : this.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Comment.class)) {
                Comment comment = field.getAnnotation(Comment.class);
                comments.put(field.getName(), comment.value());
            }
        }

        if (comments.isEmpty()) return yamlString;

        StringBuilder sb = new StringBuilder();
        String[] lines = yamlString.split("\n");

        for (String line : lines) {
            for (Map.Entry<String, String[]> entry : comments.entrySet()) {
                if (line.startsWith(entry.getKey() + ":")) {
                    for (String commentLine : entry.getValue()) {
                        sb.append("# ").append(commentLine).append("\n");
                    }
                    break;
                }
            }
            sb.append(line).append("\n");
        }

        return sb.toString();
    }

    private String fixEmptyListsFormat(String yamlString) {
        // empty arrays that span multiple lines
        yamlString = yamlString.replaceAll(":\\s*\\[\\s*\\n\\s*\\]", ": []");

        // pattern: "key: [\n    ]" -> "key: []"
        yamlString = yamlString.replaceAll(":\\s*\\[\\s*\\n\\s*\\]", ": []");

        // pattern where there's whitespace between brackets
        yamlString = yamlString.replaceAll(":\\s*\\[\\s+\\]", ": []");

        // any [ followed by whitespace and newlines ending with ]
        yamlString = yamlString.replaceAll("(?m)^(\\s*\\w+):\\s*\\[\\s*\\n\\s*\\]", "$1: []");

        String[] lines = yamlString.split("\n");
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            if (line.matches(".*:\\s*\\[\\s*$")) {
                // Look ahead for closing bracket
                int j = i + 1;
                boolean foundClosing = false;
                while (j < lines.length && lines[j].matches("\\s*")) {
                    j++;
                }
                if (j < lines.length && lines[j].matches("\\s*\\]\\s*")) {
                    result.append(line.replaceAll(":\\s*\\[\\s*$", ": []")).append("\n");
                    i = j;
                    foundClosing = true;
                }
                if (!foundClosing) {
                    result.append(line).append("\n");
                }
            } else {
                result.append(line).append("\n");
            }
        }

        return result.toString();
    }

    private boolean shouldSkipField(Field field) {
        return field.isSynthetic() ||
                java.lang.reflect.Modifier.isStatic(field.getModifiers()) ||
                java.lang.reflect.Modifier.isTransient(field.getModifiers()) ||
                field.isAnnotationPresent(YamlIgnore.class) ||
                field.getName().equals("yaml") ||
                field.getName().equals("file") ||
                field.getName().equals("url") ||
                field.getName().equals("content") ||
                field.getName().equals("isLoading") ||
                field.getName().equals("isSaving");
    }

    private String loadFromUrl() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        try (InputStream is = connection.getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private void saveToUrl(String content) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/yaml");

        try (OutputStream os = connection.getOutputStream()) {
            os.write(content.getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP error code: " + responseCode);
        }
    }

}
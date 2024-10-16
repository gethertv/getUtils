package dev.gether.getutils.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReflectionUtils {

    private static final String VERSION;
    private static final String NMS_PATH;
    private static final String CRAFTBUKKIT_PATH;

    static {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        VERSION = getServerVersion(packageName);
        NMS_PATH = "net.minecraft.server." + VERSION + ".";
        CRAFTBUKKIT_PATH = "org.bukkit.craftbukkit." + VERSION + ".";
    }

    private static String getServerVersion(String packageName) {
        try {
            return packageName.substring(packageName.lastIndexOf('.') + 1);
        } catch (Exception e) {
            MessageUtil.logMessage(ConsoleColor.RED, "Failed to determine server version. Using default. Error: " + e.getMessage());
            return "";
        }
    }

    /**
     * Gets an NMS class by its name.
     *
     * @param name The name of the NMS class
     * @return The NMS class or null if not found
     */
    public static Class<?> getNMSClass(String name) {
        try {
            return Class.forName(NMS_PATH + name);
        } catch (ClassNotFoundException e) {
            MessageUtil.logMessage(ConsoleColor.RED, "Failed to get NMS class: " + name + ". Error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Gets a CraftBukkit class by its name.
     *
     * @param name The name of the CraftBukkit class
     * @return The CraftBukkit class or null if not found
     */
    public static Class<?> getCraftBukkitClass(String name) {
        try {
            return Class.forName(CRAFTBUKKIT_PATH + name);
        } catch (ClassNotFoundException e) {
            MessageUtil.logMessage(ConsoleColor.RED, "Failed to get CraftBukkit class: " + name + ". Error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Gets a method from a given class.
     *
     * @param clazz          The class to get the method from
     * @param methodName     The name of the method
     * @param parameterTypes The parameter types of the method
     * @return The method or null if not found
     */
    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            return clazz.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            MessageUtil.logMessage(ConsoleColor.RED, "Failed to get method: " + methodName + " from class: " + clazz.getName() + ". Error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Gets a field from a given class.
     *
     * @param clazz     The class to get the field from
     * @param fieldName The name of the field
     * @return The field or null if not found
     */
    public static Field getField(Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            MessageUtil.logMessage(ConsoleColor.RED, "Failed to get field: " + fieldName + " from class: " + clazz.getName() + ". Error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Gets a constructor from a given class.
     *
     * @param clazz          The class to get the constructor from
     * @param parameterTypes The parameter types of the constructor
     * @return The constructor or null if not found
     */
    public static Constructor<?> getConstructor(Class<?> clazz, Class<?>... parameterTypes) {
        try {
            return clazz.getConstructor(parameterTypes);
        } catch (NoSuchMethodException e) {
            MessageUtil.logMessage(ConsoleColor.RED, "Failed to get constructor from class: " + clazz.getName() + ". Error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Sends a packet to a player.
     *
     * @param player The player to send the packet to
     * @param packet The packet to send
     */
    public static void sendPacket(Player player, Object packet) {
        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
            Method sendPacket = playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet"));
            sendPacket.invoke(playerConnection, packet);
        } catch (Exception e) {
            MessageUtil.logMessage(ConsoleColor.RED, "Failed to send packet to player: " + player.getName() + ". Error: " + e.getMessage());
        }
    }

    /**
     * Provides a detailed debug output for any given object, including its fields and methods.
     * This method uses reflection to inspect the object's structure and content and logs the information to the console.
     *
     * @param obj The object to debug
     */
    public static void debugObject(Object obj) {
        if (obj == null) {
            MessageUtil.logMessage(ConsoleColor.YELLOW, "Debug: Object is null");
            return;
        }

        Class<?> clazz = obj.getClass();
        MessageUtil.logMessage(ConsoleColor.CYAN, "Debugging object of class: " + clazz.getName());

        // Debug fields
        MessageUtil.logMessage(ConsoleColor.BLUE, "\nFields:");
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                MessageUtil.logMessage(ConsoleColor.GREEN, String.format("- %s (%s): %s",
                        field.getName(),
                        field.getType().getSimpleName(),
                        Modifier.toString(field.getModifiers())));
                MessageUtil.logMessage(ConsoleColor.WHITE, String.format("  Value: %s", field.get(obj)));
            } catch (IllegalAccessException e) {
                MessageUtil.logMessage(ConsoleColor.RED, String.format("- %s: Unable to access (Reason: %s)", field.getName(), e.getMessage()));
            }
        }

        // Debug methods
        MessageUtil.logMessage(ConsoleColor.BLUE, "\nMethods:");
        for (Method method : clazz.getDeclaredMethods()) {
            method.setAccessible(true);
            MessageUtil.logMessage(ConsoleColor.GREEN, String.format("- %s%s: %s",
                    method.getName(),
                    formatParameters(method.getParameterTypes()),
                    method.getReturnType().getSimpleName()));
            MessageUtil.logMessage(ConsoleColor.WHITE, String.format("  Modifiers: %s", Modifier.toString(method.getModifiers())));

            // Try to invoke parameter-less, non-static methods
            if (method.getParameterCount() == 0 && !Modifier.isStatic(method.getModifiers())) {
                try {
                    Object result = method.invoke(obj);
                    MessageUtil.logMessage(ConsoleColor.CYAN, String.format("  Invocation result: %s", result));
                } catch (Exception e) {
                    MessageUtil.logMessage(ConsoleColor.RED, String.format("  Unable to invoke method (Reason: %s)", e.getMessage()));
                }
            }
        }

        // Debug superclass
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null && superClass != Object.class) {
            MessageUtil.logMessage(ConsoleColor.YELLOW, String.format("\nSuperclass: %s", superClass.getName()));
        }

        // Debug interfaces
        Class<?>[] interfaces = clazz.getInterfaces();
        if (interfaces.length > 0) {
            MessageUtil.logMessage(ConsoleColor.BLUE, "\nImplemented Interfaces:");
            for (Class<?> iface : interfaces) {
                MessageUtil.logMessage(ConsoleColor.GREEN, String.format("- %s", iface.getName()));
            }
        }
    }

    /**
     * Formats an array of parameter types into a readable string.
     *
     * @param parameterTypes Array of parameter types
     * @return Formatted string of parameter types
     */
    private static String formatParameters(Class<?>[] parameterTypes) {
        return Arrays.stream(parameterTypes)
                .map(Class::getSimpleName)
                .collect(Collectors.joining(", ", "(", ")"));
    }


    /**
     * Invokes a method on an object using reflection.
     *
     * @param obj The object on which to invoke the method
     * @param methodName The name of the method to invoke
     * @param args The arguments to pass to the method
     * @return The result of the method invocation
     * @throws Exception If the method cannot be found or invoked
     */
    public static Object invoke(Object obj, String methodName, Object... args) throws Exception {
        Class<?>[] argTypes = Arrays.stream(args)
                .map(arg -> arg == null ? Object.class : arg.getClass())
                .toArray(Class<?>[]::new);
        Method method = obj.getClass().getMethod(methodName, argTypes);
        return method.invoke(obj, args);
    }


    /**
     * Gets the value of a field from an object using reflection.
     *
     * @param obj The object from which to get the field value
     * @param fieldName The name of the field
     * @return The value of the field
     * @throws Exception If the field cannot be found or accessed
     */
    public static Object getFieldValue(Object obj, String fieldName) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }

    /**
     * Sets the value of a field in an object using reflection.
     *
     * @param obj The object in which to set the field value
     * @param fieldName The name of the field
     * @param value The value to set
     * @throws Exception If the field cannot be found or accessed
     */
    public static void setFieldValue(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    /**
     * Invokes a static method using reflection.
     *
     * @param clazz The class containing the static method
     * @param methodName The name of the static method
     * @param args The arguments to pass to the method
     * @return The result of the method invocation
     * @throws Exception If the method cannot be found or invoked
     */
    public static Object invokeStatic(Class<?> clazz, String methodName, Object... args) throws Exception {
        Class<?>[] argTypes = Arrays.stream(args)
                .map(arg -> arg == null ? Object.class : arg.getClass())
                .toArray(Class<?>[]::new);
        Method method = clazz.getMethod(methodName, argTypes);
        return method.invoke(null, args);
    }

    /**
     * Gets the value of a static field using reflection.
     *
     * @param clazz The class containing the static field
     * @param fieldName The name of the static field
     * @return The value of the static field
     * @throws Exception If the field cannot be found or accessed
     */
    public static Object getStaticFieldValue(Class<?> clazz, String fieldName) throws Exception {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(null);
    }

}
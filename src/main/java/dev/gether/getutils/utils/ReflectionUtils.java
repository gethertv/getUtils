package dev.gether.getutils.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionUtils {

    private static final String VERSION = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    private static final String NMS_PATH = "net.minecraft.server." + VERSION + ".";
    private static final String CRAFTBUKKIT_PATH = "org.bukkit.craftbukkit." + VERSION + ".";

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
            e.printStackTrace();
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
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets a method from a given class.
     *
     * @param clazz The class to get the method from
     * @param methodName The name of the method
     * @param parameterTypes The parameter types of the method
     * @return The method or null if not found
     */
    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            return clazz.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets a field from a given class.
     *
     * @param clazz The class to get the field from
     * @param fieldName The name of the field
     * @return The field or null if not found
     */
    public static Field getField(Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets a constructor from a given class.
     *
     * @param clazz The class to get the constructor from
     * @param parameterTypes The parameter types of the constructor
     * @return The constructor or null if not found
     */
    public static Constructor<?> getConstructor(Class<?> clazz, Class<?>... parameterTypes) {
        try {
            return clazz.getConstructor(parameterTypes);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }
}
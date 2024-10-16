package dev.gether.getutils.selector;


import org.bukkit.plugin.java.JavaPlugin;

public class SelectorRegion {
    private static SelectorManager manager;

    private SelectorRegion() {}

    public static void init(JavaPlugin plugin) {
        if (manager != null) {
            throw new IllegalStateException("SelectorRegion already initialized");
        }
        manager = new SelectorManager(plugin);
    }

    public static SelectorManager getManager() {
        if (manager == null) {
            throw new IllegalStateException("SelectorRegion not initialized. Call init() first.");
        }
        return manager;
    }
}

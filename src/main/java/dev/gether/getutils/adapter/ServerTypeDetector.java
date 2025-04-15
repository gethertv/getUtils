package dev.gether.getutils.adapter;

public class ServerTypeDetector {
    private static final String BUKKIT_CLASS = "org.bukkit.Bukkit";
    private static final String VELOCITY_CLASS = "com.velocitypowered.api.proxy.ProxyServer";
    
    public static ServerType detectServerType() {
        try {
            Class.forName(BUKKIT_CLASS);
            return ServerType.BUKKIT;
        } catch (ClassNotFoundException e) {
            return ServerType.VELOCITY;
        }
    }
}
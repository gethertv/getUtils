package dev.gether.getutils.adapter;

public class ServerSerializerFactory {
    public static ServerSerializer createSerializer() {
        ServerType type = ServerTypeDetector.detectServerType();
        return switch (type) {
            case BUKKIT -> new BukkitSerializer();
            default -> new VelocitySerializer();
        };
    }
}

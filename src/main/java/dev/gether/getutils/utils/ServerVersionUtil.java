package dev.gether.getutils.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ServerVersionUtil {

    /**
     * Checks if the server is running on version 1.20 or higher.
     * This method is used to determine if the new Head API is available.
     *
     * @return true if the server version is 1.20 or higher, false otherwise
     */
    public static boolean isNewHeadApiSupported() {
        String version = Bukkit.getBukkitVersion();
        String[] versionParts = version.split("-")[0].split("\\.");

        try {
            int major = Integer.parseInt(versionParts[0]);
            int minor = Integer.parseInt(versionParts[1]);

            return (major == 1 && minor >= 20);
        } catch (NumberFormatException e) {
            MessageUtil.logMessage(ConsoleColor.RED, "Failed to parse server version: " + version);
            return false;
        }
    }


}


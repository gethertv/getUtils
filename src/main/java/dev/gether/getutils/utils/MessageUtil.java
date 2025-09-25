package dev.gether.getutils.utils;

import dev.gether.getutils.models.TitleMessage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MessageUtil {


    // logger
    private static final Logger LOG = Bukkit.getLogger();

    /**
     * Logs a colored message to the console.
     *
     * @param consoleColor The color code for the console message.
     * @param message The message to be logged.
     */
    public static void logMessage(String consoleColor, String message) {
        LOG.info(consoleColor + message + ConsoleColor.RESET);
    }


    public static void sendMessage(UUID playerUUID, String message) {
        Player player = Bukkit.getPlayer(playerUUID);
        if(player == null) return;

        sendMessage(player, message);
    }

    /**
     * Sends a colored message to a player.
     *
     * @param player The player to receive the message.
     * @param message The message to be sent.
     */
    public static void sendMessage(Player player, String message) {
        if(message == null || message.isEmpty() || message.equalsIgnoreCase("none")) return;

        player.sendMessage(ColorFixer.addColors(message));
    }

    /**
     * Sends a colored message to a command sender.
     *
     * @param sender The command sender to receive the message.
     * @param message The message to be sent.
     */
    public static void sendMessage(CommandSender sender, String message) {
        if(message == null || message.isEmpty() || message.equalsIgnoreCase("none")) return;

        sender.sendMessage(ColorFixer.addColors(message));
    }

    /**
     * Sends a list of colored messages to a command sender.
     *
     * @param sender The command sender to receive the messages.
     * @param messages The list of messages to be sent.
     */
    public static void sendMessage(CommandSender sender, List<String> messages) {
        if (messages.isEmpty()) {
            return;
        }
        sender.sendMessage(ColorFixer.addColors(String.join("\n", messages)));
    }

    /**
     * Broadcasts a colored message to all online players and the console.
     *
     * @param message The message to be broadcast.
     */
    public static void broadcast(String message) {
        if(message == null || message.isEmpty() || message.equalsIgnoreCase("none")) return;

        String coloredMessage = ColorFixer.addColors(message);
        Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(coloredMessage));
        Bukkit.getConsoleSender().sendMessage(coloredMessage);
    }

    /**
     * Broadcasts a message to all online players and the console without color processing.
     *
     * @param message The message to be broadcast without color processing.
     */
    public static void broadcastNoneColor(String message) {
        Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(message));
        Bukkit.getConsoleSender().sendMessage(message);
    }

    /**
     * Broadcasts a title message to all online players.
     *
     * @param titleMessage The TitleMessage object containing title information.
     */
    public static void broadcastTitle(TitleMessage titleMessage) {
        if (titleMessage == null || !titleMessage.isEnabled()) {
            return;
        }

        String title = ColorFixer.addColors(titleMessage.getTitle());
        String subtitle = ColorFixer.addColors(titleMessage.getSubtitle());
        int fadeIn = titleMessage.getFadeIn();
        int stay = titleMessage.getStay();
        int fadeOut = titleMessage.getFadeOut();

        Bukkit.getOnlinePlayers().forEach(player -> 
            player.sendTitle(title, subtitle, fadeIn, stay, fadeOut)
        );
    }

    /**
     * Broadcasts a title message to all online players with variable replacement.
     *
     * @param titleMessage The TitleMessage object containing title information.
     * @param variables A map of variables to be replaced in the title and subtitle.
     */
    public static void broadcastTitle(TitleMessage titleMessage, Map<String, String> variables) {
        if (titleMessage == null || !titleMessage.isEnabled()) {
            return;
        }

        String title = titleMessage.getTitle();
        String subtitle = titleMessage.getSubtitle();

        for (Map.Entry<String, String> entry : variables.entrySet()) {
            title = title.replace(entry.getKey(), entry.getValue());
            subtitle = subtitle.replace(entry.getKey(), entry.getValue());
        }

        final String finalTitle = ColorFixer.addColors(title);
        final String finalSubtitle = ColorFixer.addColors(subtitle);
        int fadeIn = titleMessage.getFadeIn();
        int stay = titleMessage.getStay();
        int fadeOut = titleMessage.getFadeOut();

        Bukkit.getOnlinePlayers().forEach(player -> 
            player.sendTitle(finalTitle, finalSubtitle, fadeIn, stay, fadeOut)
        );
    }
}
package dev.gether.getutils.utils;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import dev.gether.getutils.models.TitleMessage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class VelocityMessageUtil {

    private static ProxyServer proxyServer;
    private static Logger logger;
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    public static void init(ProxyServer server, Logger log) {
        proxyServer = server;
        logger = log;
    }

    /**
     * Logs a colored message to the console.
     *
     * @param consoleColor The color code for the console message (ignored in Velocity).
     * @param message The message to be logged.
     */
    public static void logMessage(String consoleColor, String message) {
        logger.info(message);
    }

    /**
     * Sends a colored message to a player.
     *
     * @param player The Velocity player to receive the message.
     * @param message The message to be sent.
     */
    public static void sendMessage(Player player, String message) {
        if(message == null || message.isEmpty() || message.equalsIgnoreCase("none")) return;

        Component component = miniMessage.deserialize(message);
        player.sendMessage(component);
    }

    /**
     * Sends a colored message to a command sender.
     *
     * @param sender The command source to receive the message.
     * @param message The message to be sent.
     */
    public static void sendMessage(com.velocitypowered.api.command.CommandSource sender, String message) {
        if(message == null || message.isEmpty() || message.equalsIgnoreCase("none")) return;

        Component component = miniMessage.deserialize(message);
        sender.sendMessage(component);
    }

    /**
     * Sends a list of colored messages to a command sender.
     *
     * @param sender The command source to receive the messages.
     * @param messages The list of messages to be sent.
     */
    public static void sendMessage(com.velocitypowered.api.command.CommandSource sender, List<String> messages) {
        if (messages.isEmpty()) return;

        Component component = miniMessage.deserialize(String.join("\n", messages));
        sender.sendMessage(component);
    }

    /**
     * Broadcasts a colored message to all online players and the console.
     *
     * @param message The message to be broadcast.
     */
    public static void broadcast(String message) {
        if(message == null || message.isEmpty() || message.equalsIgnoreCase("none")) return;

        Component component = miniMessage.deserialize(message);
        proxyServer.getAllPlayers().forEach(player -> player.sendMessage(component));
        proxyServer.getConsoleCommandSource().sendMessage(component);
    }

    /**
     * Broadcasts a message to all online players and the console without color processing.
     *
     * @param message The message to be broadcast.
     */
    public static void broadcastNoneColor(String message) {
        Component component = Component.text(message);
        proxyServer.getAllPlayers().forEach(player -> player.sendMessage(component));
        proxyServer.getConsoleCommandSource().sendMessage(component);
    }

    /**
     * Broadcasts a title message to all online players.
     *
     * @param titleMessage The TitleMessage object containing title information.
     */
    public static void broadcastTitle(TitleMessage titleMessage) {
        if (titleMessage == null || !titleMessage.isEnabled()) return;

        Component title = miniMessage.deserialize(titleMessage.getTitle());
        Component subtitle = miniMessage.deserialize(titleMessage.getSubtitle());

        Title.Times times = Title.Times.of(
            Duration.ofMillis(titleMessage.getFadeIn() * 50L),
            Duration.ofMillis(titleMessage.getStay() * 50L),
            Duration.ofMillis(titleMessage.getFadeOut() * 50L)
        );

        Title titleToShow = Title.title(title, subtitle, times);
        proxyServer.getAllPlayers().forEach(player -> player.showTitle(titleToShow));
    }

    /**
     * Broadcasts a title message to all online players with variable replacement.
     *
     * @param titleMessage The TitleMessage object containing title information.
     * @param variables A map of variables to be replaced in the title and subtitle.
     */
    public static void broadcastTitle(TitleMessage titleMessage, Map<String, String> variables) {
        if (titleMessage == null || !titleMessage.isEnabled()) return;

        String title = titleMessage.getTitle();
        String subtitle = titleMessage.getSubtitle();

        for (Map.Entry<String, String> entry : variables.entrySet()) {
            title = title.replace(entry.getKey(), entry.getValue());
            subtitle = subtitle.replace(entry.getKey(), entry.getValue());
        }

        Component titleComponent = miniMessage.deserialize(title);
        Component subtitleComponent = miniMessage.deserialize(subtitle);

        Title.Times times = Title.Times.of(
            Duration.ofMillis(titleMessage.getFadeIn() * 50L),
            Duration.ofMillis(titleMessage.getStay() * 50L),
            Duration.ofMillis(titleMessage.getFadeOut() * 50L)
        );

        Title titleToShow = Title.title(titleComponent, subtitleComponent, times);
        proxyServer.getAllPlayers().forEach(player -> player.showTitle(titleToShow));
    }

    /**
     * Broadcasts a message to players on a specific server.
     *
     * @param server The server to broadcast to.
     * @param message The message to broadcast.
     */
    public static void broadcastToServer(RegisteredServer server, String message) {
        if(message == null || message.isEmpty() || message.equalsIgnoreCase("none")) return;

        Component component = miniMessage.deserialize(message);
        server.getPlayersConnected().forEach(player -> player.sendMessage(component));
    }

    /**
     * Sends an action bar message to a player.
     *
     * @param player The player to receive the action bar message
     * @param message The message to be displayed (supports MiniMessage format)
     */
    public static void sendActionBar(Player player, String message) {
        if(message == null || message.isEmpty() || message.equalsIgnoreCase("none")) return;

        Component component = miniMessage.deserialize(message);
        player.sendActionBar(component);
    }

    /**
     * Sends a title to a specific player.
     *
     * @param player The player to receive the title
     * @param title The title text (supports MiniMessage format)
     * @param subtitle The subtitle text (supports MiniMessage format)
     * @param fadeIn Fade in time in ticks
     * @param stay Stay time in ticks
     * @param fadeOut Fade out time in ticks
     */
    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        if(title == null && subtitle == null) return;

        Component titleComponent = title != null ? miniMessage.deserialize(title) : Component.empty();
        Component subtitleComponent = subtitle != null ? miniMessage.deserialize(subtitle) : Component.empty();

        Title.Times times = Title.Times.of(
                Duration.ofMillis(fadeIn * 50L),
                Duration.ofMillis(stay * 50L),
                Duration.ofMillis(fadeOut * 50L)
        );

        Title titleToShow = Title.title(titleComponent, subtitleComponent, times);
        player.showTitle(titleToShow);
    }

    /**
     * Broadcasts an action bar message to all online players.
     *
     * @param message The message to be broadcast (supports MiniMessage format)
     */
    public static void actionBar(String message) {
        if(message == null || message.isEmpty() || message.equalsIgnoreCase("none")) return;

        Component component = miniMessage.deserialize(message);
        proxyServer.getAllPlayers().forEach(player -> player.sendActionBar(component));
    }

    /**
     * Broadcasts an action bar message to players on a specific server.
     *
     * @param server The server to broadcast to
     * @param message The message to broadcast (supports MiniMessage format)
     */
    public static void actionBar(RegisteredServer server, String message) {
        if(message == null || message.isEmpty() || message.equalsIgnoreCase("none")) return;

        Component component = miniMessage.deserialize(message);
        server.getPlayersConnected().forEach(player -> player.sendActionBar(component));
    }

}
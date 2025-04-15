package dev.gether.getutils.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public final class ComponentUtils {

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    /**
     * Creates a Component from text with MiniMessage color support
     *
     * @param text Text with MiniMessage formatting
     * @return Formatted component
     */
    public static Component asComponent(String text) {
        if (text == null || text.isEmpty()) return Component.empty();
        return miniMessage.deserialize(text);
    }

    /**
     * Creates empty component
     *
     * @return Empty component
     */
    public static Component empty() {
        return Component.empty();
    }

    /**
     * Creates Title.Times for title display
     *
     * @param fadeIn Fade in time in ticks
     * @param stay Stay time in ticks
     * @param fadeOut Fade out time in ticks
     * @return Title.Times instance
     */
    public static Title.Times titleTimes(int fadeIn, int stay, int fadeOut) {
        return Title.Times.of(
                Duration.ofMillis(fadeIn * 50L),
                Duration.ofMillis(stay * 50L),
                Duration.ofMillis(fadeOut * 50L)
        );
    }

    /**
     * Creates a Title with components
     *
     * @param title Title component
     * @param subtitle Subtitle component
     * @param times Title times
     * @return Title instance
     */
    public static Title createTitle(Component title, Component subtitle, Title.Times times) {
        return Title.title(title, subtitle, times);
    }

    /**
     * Creates a Title with text (supports MiniMessage)
     *
     * @param title Title text
     * @param subtitle Subtitle text
     * @param fadeIn Fade in time in ticks
     * @param stay Stay time in ticks
     * @param fadeOut Fade out time in ticks
     * @return Title instance
     */
    public static Title createTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        return Title.title(
                asComponent(title),
                asComponent(subtitle),
                titleTimes(fadeIn, stay, fadeOut)
        );
    }

    /**
     * Joins multiple objects into one component. Accepts Components and Strings.
     *
     * @param objects Objects to join (Components or Strings)
     * @return Combined component
     */
    public static Component join(Object... objects) {
        if (objects == null || objects.length == 0) return Component.empty();

        TextComponent.Builder builder = Component.text();
        for (Object obj : objects) {
            if (obj != null) {
                if (obj instanceof Component) {
                    builder.append((Component) obj);
                } else {
                    builder.append(asComponent(obj.toString()));
                }
            }
        }
        return builder.build();
    }

    /**
     * Creates an error message component
     *
     * @param message Error message text
     * @return Error component
     */
    public static Component error(String message) {
        return asComponent("<red>" + message + "</red>");
    }

    /**
     * Creates a success message component
     *
     * @param message Success message text
     * @return Success component
     */
    public static Component success(String message) {
        return asComponent("<green>" + message + "</green>");
    }

    /**
     * Creates a warning message component
     *
     * @param message Warning message text
     * @return Warning component
     */
    public static Component warning(String message) {
        return asComponent("<yellow>" + message + "</yellow>");
    }

    /**
     * Creates an info message component
     *
     * @param message Info message text
     * @return Info component
     */
    public static Component info(String message) {
        return asComponent("<gray>" + message + "</gray>");
    }
}
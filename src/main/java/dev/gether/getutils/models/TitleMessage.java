package dev.gether.getutils.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TitleMessage {

    /**
     * Determines if the title message is enabled and should be displayed.
     */
    private boolean enabled;

    /**
     * The main title text to be displayed.
     */
    private String title;

    /**
     * The subtitle text to be displayed below the main title.
     */
    private String subtitle;

    /**
     * The time in ticks for the title to fade in.
     */
    private int fadeIn;

    /**
     * The time in ticks for the title to stay on screen.
     */
    private int stay;

    /**
     * The time in ticks for the title to fade out.
     */
    private int fadeOut;

    /**
     * Sends this title message to a specific player.
     *
     * @param player The player to send the title message to.
     */
    public void sendTo(Player player) {
        if (!enabled) {
            return;
        }
        player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }

    /**
     * Creates a copy of this TitleMessage with replaced placeholders.
     *
     * @param placeholders A map of placeholder keys to their replacement values.
     * @return A new TitleMessage instance with placeholders replaced.
     */
    public TitleMessage replacePlaceholders(java.util.Map<String, String> placeholders) {
        String newTitle = this.title;
        String newSubtitle = this.subtitle;

        for (java.util.Map.Entry<String, String> entry : placeholders.entrySet()) {
            newTitle = newTitle.replace(entry.getKey(), entry.getValue());
            newSubtitle = newSubtitle.replace(entry.getKey(), entry.getValue());
        }

        return new TitleMessage(this.enabled, newTitle, newSubtitle, this.fadeIn, this.stay, this.fadeOut);
    }

    /**
     * Builds a new TitleMessage with default values for fade-in, stay, and fade-out times.
     *
     * @param title The main title text.
     * @param subtitle The subtitle text.
     * @return A new TitleMessage instance with default timing values.
     */
    public static TitleMessage of(String title, String subtitle) {
        return TitleMessage.builder()
                .enabled(true)
                .title(title)
                .subtitle(subtitle)
                .fadeIn(10)
                .stay(70)
                .fadeOut(20)
                .build();
    }
}
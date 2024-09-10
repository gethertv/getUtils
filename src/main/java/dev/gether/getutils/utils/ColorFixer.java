package dev.gether.getutils.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.ChatColor;

import java.awt.Color;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ColorFixer {

    private static final Pattern GRADIENT_PATTERN = Pattern.compile("<(#[A-Fa-f0-9]{3}(?:[A-Fa-f0-9]{3})?)>(.*?)</(#[A-Fa-f0-9]{3}(?:[A-Fa-f0-9]{3})?)>");
    private static final Pattern BOLD_PATTERN = Pattern.compile("<b>(.*?)</b>");
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("#[A-Fa-f0-9]{6}");
    private static final Pattern BRACKET_HEX_COLOR_PATTERN = Pattern.compile("\\{#([A-Fa-f0-9]{6})}");

    /**
     * Applies color codes and gradients to a list of strings.
     *
     * @param input List of strings to process
     * @return Processed list of strings with colors applied
     */
    public static List<String> addColors(List<String> input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        input.replaceAll(ColorFixer::addColors);
        return input;
    }

    /**
     * Applies color codes and gradients to a single string.
     *
     * @param input String to process
     * @return Processed string with colors applied
     */
    public static String addColors(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        input = applyGradients(input);
        input = applyHexColors(input);
        input = applyBracketHexColors(input);
        input = applyBoldTags(input);

        // Finally, translate standard color codes
        return ChatColor.translateAlternateColorCodes('&', input);
    }

    /**
     * Applies a gradient effect to a string.
     *
     * @param input String to apply gradient to
     * @param startColor Starting color of the gradient
     * @param endColor Ending color of the gradient
     * @return String with gradient effect applied
     */
    public static String applyGradient(String input, Color startColor, Color endColor) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        int length = input.length();
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < length; i++) {
            double ratio = (double) i / (length - 1);
            int red = (int) (startColor.getRed() * (1 - ratio) + endColor.getRed() * ratio);
            int green = (int) (startColor.getGreen() * (1 - ratio) + endColor.getGreen() * ratio);
            int blue = (int) (startColor.getBlue() * (1 - ratio) + endColor.getBlue() * ratio);

            String hexColor = String.format("#%02x%02x%02x", red, green, blue);
            result.append(translateHexColorCodes(hexColor)).append(input.charAt(i));
        }

        return result.toString();
    }

    private static String applyGradients(String input) {
        Matcher gradientMatcher = GRADIENT_PATTERN.matcher(input);
        StringBuilder gradientResult = new StringBuilder();

        while (gradientMatcher.find()) {
            String startColorHex = gradientMatcher.group(1);
            String text = gradientMatcher.group(2);
            String endColorHex = gradientMatcher.group(3);

            Color startColor = parseHexColor(startColorHex);
            Color endColor = parseHexColor(endColorHex);
            String gradientText = applyGradient(text, startColor, endColor);

            gradientMatcher.appendReplacement(gradientResult, Matcher.quoteReplacement(gradientText));
        }
        gradientMatcher.appendTail(gradientResult);
        return gradientResult.toString();
    }

    private static String applyHexColors(String input) {
        Matcher hexMatcher = HEX_COLOR_PATTERN.matcher(input);
        StringBuilder hexResult = new StringBuilder();

        while (hexMatcher.find()) {
            String hexColor = hexMatcher.group();
            String replacement = translateHexColorCodes(hexColor);
            hexMatcher.appendReplacement(hexResult, replacement);
        }
        hexMatcher.appendTail(hexResult);
        return hexResult.toString();
    }

    private static String applyBracketHexColors(String input) {
        Matcher bracketHexMatcher = BRACKET_HEX_COLOR_PATTERN.matcher(input);
        StringBuilder bracketHexResult = new StringBuilder();

        while (bracketHexMatcher.find()) {
            String hexColor = "#" + bracketHexMatcher.group(1);
            String replacement = translateHexColorCodes(hexColor);
            bracketHexMatcher.appendReplacement(bracketHexResult, replacement);
        }
        bracketHexMatcher.appendTail(bracketHexResult);
        return bracketHexResult.toString();
    }

    private static String applyBoldTags(String input) {
        Matcher boldMatcher = BOLD_PATTERN.matcher(input);
        StringBuilder boldResult = new StringBuilder();

        while (boldMatcher.find()) {
            String boldText = ChatColor.BOLD + boldMatcher.group(1) + ChatColor.RESET;
            boldMatcher.appendReplacement(boldResult, boldText);
        }
        boldMatcher.appendTail(boldResult);
        return boldResult.toString();
    }

    private static Color parseHexColor(String hexColor) {
        if (hexColor.length() == 4) {
            hexColor = "#" + hexColor.charAt(1) + hexColor.charAt(1)
                    + hexColor.charAt(2) + hexColor.charAt(2)
                    + hexColor.charAt(3) + hexColor.charAt(3);
        }
        return Color.decode(hexColor);
    }

    private static String translateHexColorCodes(String hexColor) {
        StringBuilder result = new StringBuilder("§x");
        for (int i = 1; i < hexColor.length(); i++) {
            result.append("§").append(hexColor.charAt(i));
        }
        return result.toString();
    }
}
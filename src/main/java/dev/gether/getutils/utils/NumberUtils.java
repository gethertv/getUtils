package dev.gether.getutils.utils;

import dev.gether.getutils.Valid;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NumberUtils {

    private static final int[] VALUES = {
            1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1
    };

    private static final String[] SYMBOLS = {
            "M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"
    };

    /**
     * Converts an integer to a Roman numeral.
     *
     * @param num the integer to convert
     * @return the Roman numeral representation
     * @throws IllegalArgumentException if the number is less than or equal to zero
     */
    public static String intToRoman(int num) {
        // Validate that the input is a positive integer
        if(num <= 0) return "";

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < VALUES.length; i++) {
            while (num >= VALUES[i]) {
                num -= VALUES[i];
                sb.append(SYMBOLS[i]);
            }
        }

        return sb.toString();
    }

    /**
     * Checks if the string can be parsed as an integer.
     *
     * @param str the string to check
     * @return true if the string is a valid integer, false otherwise
     */
    public static boolean isInt(String str) {
        Valid.checkNotNull(str, "Input cannot be null");
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Checks if the string can be parsed as a double.
     *
     * @param str the string to check
     * @return true if the string is a valid double, false otherwise
     */
    public static boolean isDouble(String str) {
        Valid.checkNotNull(str, "Input cannot be null");
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Checks if the string can be parsed as a float.
     *
     * @param str the string to check
     * @return true if the string is a valid float, false otherwise
     */
    public static boolean isFloat(String str) {
        Valid.checkNotNull(str, "Input cannot be null");
        try {
            Float.parseFloat(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}

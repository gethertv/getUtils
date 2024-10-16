package dev.gether.getutils;

public final class Valid {

    private Valid() {}

    /**
     * Checks if the given object is not null.
     *
     * @param object The object to check
     * @param errorMessage The error message to use if the object is null
     * @throws IllegalArgumentException if the object is null
     */
    public static void checkNotNull(Object object, String errorMessage) {
        if (object == null) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    /**
     * Checks if the given boolean condition is true.
     *
     * @param condition The condition to check
     * @param errorMessage The error message to use if the condition is false
     * @throws IllegalArgumentException if the condition is false
     */
    public static void checkBoolean(boolean condition, String errorMessage) {
        if (!condition) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    /**
     * Checks if the given string can be parsed as a double.
     *
     * @param input The string to check
     * @param errorMessage The error message to use if the string is not a valid double
     * @throws IllegalArgumentException if the string cannot be parsed as a double
     */
    public static void checkDouble(String input, String errorMessage) {
        try {
            Double.parseDouble(input);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    /**
     * Checks if the given string can be parsed as an int.
     *
     * @param input The string to check
     * @param errorMessage The error message to use if the string is not a valid int
     * @throws IllegalArgumentException if the string cannot be parsed as an int
     */
    public static void checkInt(String input, String errorMessage) {
        try {
            Integer.parseInt(input);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(errorMessage);
        }
    }


}

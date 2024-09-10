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


}

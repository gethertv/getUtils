package dev.gether.getutils.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.File;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileUtil {

    /**
     * Deletes a directory and all its contents, including files and subdirectories.
     *
     * @param directory the directory to be deleted.
     * @return true if the directory and its contents were successfully deleted, false otherwise.
     * @throws IllegalArgumentException if the specified directory does not exist or is not a directory.
     *                                   This exception will also be thrown if an error occurs during deletion.
     *
     */
    public static boolean deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        return directory.delete();
    }
}

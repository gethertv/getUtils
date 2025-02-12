package dev.gether.getutils.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseCallbackUtil {
    private static final Logger LOGGER = Logger.getLogger(DatabaseCallbackUtil.class.getName());
    private static final ExecutorService DB_EXECUTOR = Executors.newFixedThreadPool(3);
    private static Plugin plugin;

    /**
     * Initializes the DatabaseCallbackUtil with a plugin instance.
     * This should be called when your plugin enables.
     *
     * @param plugin Your plugin instance
     */
    public static void init(Plugin plugin) {
        DatabaseCallbackUtil.plugin = plugin;
    }

    /**
     * Executes a database operation asynchronously with success and error callbacks.
     *
     * @param <T> The type of data being returned from the database
     * @param databaseOperation The database operation to execute
     * @param successCallback Callback to handle successful operation
     * @param errorCallback Callback to handle errors
     * @return BukkitTask representing the async task
     */
    public static <T> BukkitTask executeAsync(DatabaseOperation<T> databaseOperation,
                                              Consumer<T> successCallback,
                                              Consumer<Exception> errorCallback) {
        return Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                CompletableFuture<T> future = CompletableFuture.supplyAsync(() -> {
                    try {
                        return databaseOperation.execute();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, DB_EXECUTOR);

                T result = future.get();

                Bukkit.getScheduler().runTask(plugin, () -> {
                    try {
                        successCallback.accept(result);
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Error in success callback", e);
                    }
                });
            } catch (Exception e) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    try {
                        errorCallback.accept(e);
                    } catch (Exception callbackError) {
                        LOGGER.log(Level.SEVERE, "Error in error callback", callbackError);
                    }
                });
            }
        });
    }

    /**
     * Executes a database operation asynchronously with only a success callback.
     *
     * @param <T> The type of data being returned from the database
     * @param databaseOperation The database operation to execute
     * @param successCallback Callback to handle successful operation
     * @return BukkitTask representing the async task
     */
    public static <T> BukkitTask executeAsync(DatabaseOperation<T> databaseOperation,
                                              Consumer<T> successCallback) {
        return executeAsync(databaseOperation, successCallback,
                e -> LOGGER.log(Level.SEVERE, "Database operation failed", e));
    }

    /**
     * Executes a void database operation asynchronously with success and error callbacks.
     *
     * @param databaseOperation The void database operation to execute
     * @param successCallback Callback to handle successful operation
     * @param errorCallback Callback to handle errors
     * @return BukkitTask representing the async task
     */
    public static BukkitTask executeVoidAsync(VoidDatabaseOperation databaseOperation,
                                              Runnable successCallback,
                                              Consumer<Exception> errorCallback) {
        return Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        databaseOperation.execute();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, DB_EXECUTOR);

                future.get();

                Bukkit.getScheduler().runTask(plugin, () -> {
                    try {
                        successCallback.run();
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Error in success callback", e);
                    }
                });
            } catch (Exception e) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    try {
                        errorCallback.accept(e);
                    } catch (Exception callbackError) {
                        LOGGER.log(Level.SEVERE, "Error in error callback", callbackError);
                    }
                });
            }
        });
    }

    /**
     * Functional interface for database operations that return a value.
     *
     * @param <T> The type of data being returned from the database
     */
    @FunctionalInterface
    public interface DatabaseOperation<T> {
        T execute() throws Exception;
    }

    /**
     * Functional interface for database operations that don't return a value.
     */
    @FunctionalInterface
    public interface VoidDatabaseOperation {
        void execute() throws Exception;
    }

    /**
     * Shuts down the executor service.
     * This should be called when your plugin disables.
     */
    public static void shutdown() {
        DB_EXECUTOR.shutdown();
    }
}
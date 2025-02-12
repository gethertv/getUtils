package dev.gether.getutils.bossbar;

import dev.gether.getutils.utils.ColorFixer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BossBarManager {
    JavaPlugin plugin;
    Map<UUID, Map<String, ActiveBossBar>> activeBossBars;
    Map<String, Function<Player, String>> placeholders;

    public BossBarManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.activeBossBars = new ConcurrentHashMap<>();
        this.placeholders = new ConcurrentHashMap<>();
        initDefaultPlaceholders();
    }

    private void initDefaultPlaceholders() {
        addPlaceholder("time", player -> String.valueOf(System.currentTimeMillis() / 1000));
        addPlaceholder("player", Player::getName);
        addPlaceholder("online", player -> String.valueOf(Bukkit.getOnlinePlayers().size()));
    }

    public void addPlaceholder(String placeholder, Function<Player, String> resolver) {
        placeholders.put(placeholder, resolver);
    }

    private String resolvePlaceholders(String message, Player player) {
        for (Map.Entry<String, Function<Player, String>> entry : placeholders.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            String value = entry.getValue().apply(player);
            message = message.replace(placeholder, value);
        }
        return ColorFixer.addColors(message);
    }

    public void sendBossBar(Player player, PlayerBossBar playerBossBar) {
        UUID playerUUID = player.getUniqueId();
        String id = UUID.randomUUID().toString();

        BossBar bossBar = createBossBar(player, playerBossBar);
        ActiveBossBar activeBossBar = new ActiveBossBar(id, playerBossBar, bossBar);
        activeBossBars.computeIfAbsent(playerUUID, k -> new ConcurrentHashMap<>()).put(id, activeBossBar);

        updateBossBarPositions(player);
        scheduleBossBarTasks(player, activeBossBar);
    }

    private BossBar createBossBar(Player player, PlayerBossBar playerBossBar) {
        String resolvedMessage = resolvePlaceholders(playerBossBar.getMessage(), player);
        BossBar bossBar = Bukkit.createBossBar(resolvedMessage, playerBossBar.getBarColor(), playerBossBar.getBarStyle());
        bossBar.setProgress(1.0);
        bossBar.addPlayer(player);
        return bossBar;
    }

    private void scheduleBossBarTasks(Player player, ActiveBossBar activeBossBar) {
        PlayerBossBar playerBossBar = activeBossBar.getPlayerBossBar();
        if (playerBossBar.getCountingType() != CountingType.SOLID) {
            startCounting(player, activeBossBar);
        } else {
            scheduleSolidBossBarRemoval(player, activeBossBar);
        }
        scheduleRegularUpdates(player, activeBossBar);
    }

    private void startCounting(Player player, ActiveBossBar activeBossBar) {
        UUID playerUUID = player.getUniqueId();
        PlayerBossBar playerBossBar = activeBossBar.getPlayerBossBar();
        BossBar bossBar = activeBossBar.getBossBar();
        final int totalTicks = playerBossBar.getDurationSeconds() * 10;

        BukkitTask task = new BukkitRunnable() {
            int ticksLeft = totalTicks;

            @Override
            public void run() {
                if (shouldStopCounting(player, playerUUID, ticksLeft)) {
                    removeBossBar(player, activeBossBar.getId());
                    this.cancel();
                    return;
                }

                updateBossBarProgress(bossBar, playerBossBar.getCountingType(), ticksLeft, totalTicks);
                ticksLeft--;
            }
        }.runTaskTimer(plugin, 0L, 2L);

        activeBossBar.setTask(task);
    }

    private boolean shouldStopCounting(Player player, UUID playerUUID, int ticksLeft) {
        return ticksLeft <= 0 || !player.isOnline() || !activeBossBars.containsKey(playerUUID);
    }

    private void updateBossBarProgress(BossBar bossBar, CountingType countingType, int ticksLeft, int totalTicks) {
        double progress = countingType == CountingType.COUNTDOWN
                ? (double) ticksLeft / totalTicks
                : 1 - ((double) ticksLeft / totalTicks);
        bossBar.setProgress(progress);
    }

    private void scheduleSolidBossBarRemoval(Player player, ActiveBossBar activeBossBar) {
        // Don't schedule removal if duration is -1 (permanent)
        if (activeBossBar.getPlayerBossBar().getDurationSeconds() == -1) {
            return;
        }

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                removeBossBar(player, activeBossBar.getId());
            }
        }.runTaskLater(plugin, activeBossBar.getPlayerBossBar().getDurationSeconds() * 20L);
        activeBossBar.setTask(task);
    }

    private void scheduleRegularUpdates(Player player, ActiveBossBar activeBossBar) {
        BukkitTask updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !activeBossBars.containsKey(player.getUniqueId())) {
                    this.cancel();
                    return;
                }
                updateBossBarMessage(player, activeBossBar);
            }
        }.runTaskTimer(plugin, 2L, 2L);
        activeBossBar.setUpdateTask(updateTask);
    }

    public void sendBossBarToAll(PlayerBossBar playerBossBar) {
        Bukkit.getOnlinePlayers().forEach(player -> sendBossBar(player, playerBossBar));
    }

    public void removeBossBar(Player player, String bossBarId) {
        UUID playerUUID = player.getUniqueId();
        Map<String, ActiveBossBar> playerBossBars = activeBossBars.get(playerUUID);
        if (playerBossBars != null) {
            ActiveBossBar activeBossBar = playerBossBars.remove(bossBarId);
            if (activeBossBar != null) {
                cleanupActiveBossBar(player, activeBossBar);
            }
            if (playerBossBars.isEmpty()) {
                activeBossBars.remove(playerUUID);
            } else {
                updateBossBarPositions(player);
            }
        }
    }

    private void cleanupActiveBossBar(Player player, ActiveBossBar activeBossBar) {
        activeBossBar.getBossBar().removePlayer(player);
        Optional.ofNullable(activeBossBar.getTask()).ifPresent(BukkitTask::cancel);
        Optional.ofNullable(activeBossBar.getUpdateTask()).ifPresent(BukkitTask::cancel);
    }

    public void removeAllBossBars(Player player) {
        UUID playerUUID = player.getUniqueId();
        Map<String, ActiveBossBar> playerBossBars = activeBossBars.remove(playerUUID);
        if (playerBossBars != null) {
            playerBossBars.values().forEach(activeBossBar -> cleanupActiveBossBar(player, activeBossBar));
        }
    }

    public void removeAllBossBars() {
        new HashSet<>(activeBossBars.keySet()).forEach(playerUUID -> {
            Player player = Bukkit.getPlayer(playerUUID);
            if (player != null) {
                removeAllBossBars(player);
            }
        });
        activeBossBars.clear();
    }

    private void updateBossBarMessage(Player player, ActiveBossBar activeBossBar) {
        String resolvedMessage = resolvePlaceholders(activeBossBar.getPlayerBossBar().getMessage(), player);
        activeBossBar.getBossBar().setTitle(resolvedMessage);
    }

    private void updateBossBarPositions(Player player) {
        UUID playerUUID = player.getUniqueId();
        Map<String, ActiveBossBar> playerBossBars = activeBossBars.get(playerUUID);
        if (playerBossBars != null) {
            List<ActiveBossBar> sortedBossBars = new ArrayList<>(playerBossBars.values());
            sortedBossBars.sort(Comparator.comparing(ab -> ab.getPlayerBossBar().getMessage()));
            sortedBossBars.forEach(activeBossBar -> {
                BossBar bossBar = activeBossBar.getBossBar();
                bossBar.setVisible(false);
                bossBar.setVisible(true);
            });
        }
    }

    public void updateBossBarProgress(PlayerBossBar playerBossBar, double progress) {
        updateActiveBossBars(playerBossBar, activeBossBar -> activeBossBar.getBossBar().setProgress(progress));
    }

    public void updateBossBarColor(PlayerBossBar playerBossBar, BarColor barColor) {
        updateActiveBossBars(playerBossBar, activeBossBar -> activeBossBar.getBossBar().setColor(barColor));
    }

    public void updateBossBarMessage(PlayerBossBar playerBossBar) {
        activeBossBars.forEach((playerUUID, bossBars) -> {
            Player player = Bukkit.getPlayer(playerUUID);
            if (player != null && player.isOnline()) {
                bossBars.values().stream()
                        .filter(activeBossBar -> activeBossBar.getPlayerBossBar().equals(playerBossBar))
                        .forEach(activeBossBar -> {
                            String resolvedMessage = resolvePlaceholders(playerBossBar.getMessage(), player);
                            activeBossBar.getBossBar().setTitle(resolvedMessage);
                        });
            }
        });
    }

    private void updateActiveBossBars(PlayerBossBar playerBossBar, java.util.function.Consumer<ActiveBossBar> updateAction) {
        activeBossBars.values().forEach(playerBossBars ->
                playerBossBars.values().stream()
                        .filter(activeBossBar -> activeBossBar.getPlayerBossBar().equals(playerBossBar))
                        .forEach(updateAction));
    }

    @FieldDefaults(level = AccessLevel.PRIVATE)
    @Getter
    @Setter
    private static class ActiveBossBar {
        final String id;
        final PlayerBossBar playerBossBar;
        final BossBar bossBar;
        BukkitTask task;
        BukkitTask updateTask;

        public ActiveBossBar(String id, PlayerBossBar playerBossBar, BossBar bossBar) {
            this.id = id;
            this.playerBossBar = playerBossBar;
            this.bossBar = bossBar;
        }
    }
}
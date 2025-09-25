package dev.gether.getutils.inventory;

import dev.gether.getutils.inventory.item.DynamicItem;
import dev.gether.getutils.inventory.item.StaticItem;
import dev.gether.getutils.utils.ColorFixer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@FieldDefaults(level = AccessLevel.PROTECTED)
@Getter
public abstract class AbstractInventoryHolder<T extends JavaPlugin> implements InventoryHolder {
    final T plugin;
    final Player player;
    Inventory inventory;
    final Map<Integer, Consumer<InventoryClickEvent>> slotActions;
    BukkitTask refreshTask;
    boolean cancelClicks;
    long refreshInterval = -1;
    boolean hookPlaceholder;
    volatile boolean closed = false;
    InventoryConfig inventoryConfig;

    public AbstractInventoryHolder(T plugin, Player player, InventoryConfig inventoryConfig) {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            this.hookPlaceholder = true;
        }
        this.plugin = plugin;
        this.player = player;
        this.cancelClicks = inventoryConfig.isCancelClicks();
        if(inventoryConfig.getInventoryType() == null || inventoryConfig.getInventoryType() == InventoryType.CHEST) {
            this.inventory = Bukkit.createInventory(this, inventoryConfig.getSize(), ColorFixer.addColors(inventoryConfig.getTitle()));
        } else {
            this.inventory = Bukkit.createInventory(this, inventoryConfig.getInventoryType(), ColorFixer.addColors(inventoryConfig.getTitle()));
        }
        this.slotActions = new ConcurrentHashMap<>();
        this.refreshInterval = inventoryConfig.getRefreshInterval();
        this.inventoryConfig = inventoryConfig;
    }

    protected void initializeItems() {
        if(inventoryConfig.getDecorations() != null && !inventoryConfig.getDecorations().isEmpty()) {
            inventoryConfig.getDecorations().forEach(this::setItem);
        }

    }

    private void setItem(DynamicItem dynamicItem) {
        if (closed || dynamicItem == null || dynamicItem.getItem() == null) return;
        if(dynamicItem.getSlots() == null || dynamicItem.getSlots().isEmpty()) return;

        ItemStack itemStack = dynamicItem.getItem().getItemStack();
        dynamicItem.getSlots().forEach(slot -> {
            inventory.setItem(slot, processPlaceholders(itemStack));
        });
    }

    /**
     * Wewnętrzna metoda która automatycznie czyści akcje przed inicjalizacją
     */
    private void safeInitializeItems() {
        if (closed) return;

        // Automatyczne czyszczenie - nie musisz o tym pamiętać!
        slotActions.clear();

        try {
            initializeItems();
        } catch (Exception e) {
            plugin.getLogger().warning("Error during items initialization: " + e.getMessage());
            // W przypadku błędu też wyczyść akcje
            slotActions.clear();
        }
    }

    public void setItem(int slot, ItemStack item) {
        if (closed) return; // Zabezpieczenie przed operacjami na zamkniętym GUI
        inventory.setItem(slot, processPlaceholders(item));
    }

    public void addItem(DynamicItem dynamicItem) {
        if (closed || dynamicItem == null || !dynamicItem.isEnabled()) return;

        ItemStack processedItem = processPlaceholders(dynamicItem.getProcessedItem());
        for (int slot : dynamicItem.getSlots()) {
            inventory.setItem(slot, processedItem);
        }
    }

    public void removeItem(int slot) {
        if (closed) return;

        inventory.clear(slot);
        slotActions.remove(slot);
    }

    public void clearInventory() {
        if (closed) return;

        inventory.clear();
        slotActions.clear();
    }

    public void handleClick(InventoryClickEvent event) {
        if (closed) return;

        int slot = event.getSlot();
        Consumer<InventoryClickEvent> action = slotActions.get(slot);
        if (action != null) {
            try {
                action.accept(event);
            } catch (Exception e) {
                // Log błąd i nie crashuj serwera
                plugin.getLogger().warning("Error handling inventory click: " + e.getMessage());
            }
        }
    }

    public void setItem(int slot, ItemStack item, Consumer<InventoryClickEvent> action) {
        if (closed) return;

        inventory.setItem(slot, processPlaceholders(item));
        if (action != null) {
            slotActions.put(slot, action);
        }
    }

    public void setItem(StaticItem staticItem) {
        if (closed || staticItem == null || staticItem.getItem() == null || !staticItem.isEnabled()) return;

        inventory.setItem(staticItem.getSlot(), processPlaceholders(staticItem.getItem().getItemStack()));
    }

    public void setItem(StaticItem staticItem, Consumer<InventoryClickEvent> action) {
        if (closed || staticItem == null || !staticItem.isEnabled()) return;

        setItem(staticItem);
        if (action != null) {
            slotActions.put(staticItem.getSlot(), action);
        }
    }

    public void open() {
        if (closed) return;

        // Automatycznie zainicjalizuj items przy otwieraniu
        safeInitializeItems();

        player.openInventory(inventory);
        startRefreshTask();
    }

    protected ItemStack processPlaceholders(ItemStack item) {
        if (item == null || closed) return null;
        if (!hookPlaceholder) return item;

        ItemStack processedItem = item.clone();
        return Optional.ofNullable(processedItem.getItemMeta())
                .map(meta -> {
                    if (meta.hasDisplayName()) {
                        meta.setDisplayName(setPlaceholders(meta.getDisplayName()));
                    }
                    if (meta.hasLore()) {
                        List<String> lore = meta.getLore().stream()
                                .map(this::setPlaceholders)
                                .collect(Collectors.toList());
                        meta.setLore(lore);
                    }
                    processedItem.setItemMeta(meta);
                    return processedItem;
                })
                .orElse(processedItem);
    }

    protected String setPlaceholders(String text) {
        if (closed || text == null) return text;
        if (hookPlaceholder && player != null && player.isOnline()) {
            return PlaceholderAPI.setPlaceholders(player, text);
        }
        return text;
    }

    protected void startRefreshTask() {
        if (closed || refreshInterval <= 0) return;

        // Zatrzymaj poprzedni task jeśli istnieje
        stopRefreshTask();

        refreshTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            // Sprawdź czy GUI nie zostało zamknięte i gracz jest online
            if (!closed && player.isOnline()) {
                refresh();
            } else {
                // Automatycznie zamknij jeśli gracz się rozłączył
                cleanup();
            }
        }, refreshInterval, refreshInterval);
    }

    protected void stopRefreshTask() {
        if (refreshTask != null && !refreshTask.isCancelled()) {
            refreshTask.cancel();
            refreshTask = null;
        }
    }

    public void cleanup() {
        if (closed) return; // Zapobiegaj wielokrotnemu wywołaniu

        closed = true; // Ustaw flagę zamknięcia

        // Zatrzymaj task odświeżania
        stopRefreshTask();

        // Wyczyść wszystkie akcje
        if (slotActions != null) {
            slotActions.clear();
        }

        // Wyczyść inventory
        if (inventory != null) {
            inventory.clear();
        }

        // Null referencje dla GC (opcjonalne, ale pomocne)
        inventory = null;
    }

    /**
     * Publiczna metoda do manualnego zamknięcia GUI
     * (nie override - InventoryHolder nie ma takiej metody)
     */
    public void close() {
        cleanup();
    }

    protected void refresh() {
        if (closed || !player.isOnline()) {
            cleanup(); // Automatyczne zamknięcie jeśli gracz offline
            return;
        }

        try {
            // Używamy bezpiecznej metody która automatycznie czyści akcje
            safeInitializeItems();

            // Aktualizuj inventory gracza
            player.updateInventory();
        } catch (Exception e) {
            // Log błąd i zamknij GUI w przypadku problemów
            plugin.getLogger().warning("Error during inventory refresh: " + e.getMessage());
            cleanup();
        }
    }

    /**
     * Bezpieczne dodawanie akcji - automatycznie sprawdza czy GUI nie jest zamknięte
     */
    public void addSlotAction(int slot, Consumer<InventoryClickEvent> action) {
        if (closed || action == null) return;
        slotActions.put(slot, action);
    }

    /**
     * Bezpieczne usuwanie akcji
     */
    public void removeSlotAction(int slot) {
        if (closed) return;
        slotActions.remove(slot);
    }

    /**
     * Metoda do manualnego odświeżenia GUI (automatycznie czyści akcje)
     */
    public void forceRefresh() {
        if (!closed) {
            safeInitializeItems();
            if (player.isOnline()) {
                player.updateInventory();
            }
        }
    }

    /**
     * Async refresh - bezpieczne odświeżenie w następnym ticku
     * Przydatne gdy chcesz odświeżyć z poziomu event handlera
     */
    public void refreshAsync() {
        if (closed || !player.isOnline()) return;

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!closed && player.isOnline()) {
                forceRefresh();
            }
        });
    }

    /**
     * Refresh z opóźnieniem (w tickach)
     */
    public void refreshDelayed(long delayTicks) {
        if (closed || !player.isOnline()) return;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!closed && player.isOnline()) {
                forceRefresh();
            }
        }, delayTicks);
    }
}
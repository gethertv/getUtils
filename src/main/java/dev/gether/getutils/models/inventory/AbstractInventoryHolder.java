    package dev.gether.getutils.models.inventory;

    import dev.gether.getutils.utils.ColorFixer;
    import lombok.AccessLevel;
    import lombok.Getter;
    import lombok.experimental.FieldDefaults;
    import me.clip.placeholderapi.PlaceholderAPI;
    import org.bukkit.Bukkit;
    import org.bukkit.entity.Player;
    import org.bukkit.event.EventHandler;
    import org.bukkit.event.HandlerList;
    import org.bukkit.event.Listener;
    import org.bukkit.event.inventory.InventoryClickEvent;
    import org.bukkit.event.inventory.InventoryCloseEvent;
    import org.bukkit.inventory.Inventory;
    import org.bukkit.inventory.InventoryHolder;
    import org.bukkit.inventory.ItemStack;
    import org.bukkit.plugin.Plugin;
    import org.bukkit.scheduler.BukkitTask;

    import java.util.*;
    import java.util.function.Consumer;
    import java.util.stream.Collectors;

    @FieldDefaults(level = AccessLevel.PROTECTED)
    @Getter
    public abstract class AbstractInventoryHolder implements InventoryHolder {
        final Player player;
        Inventory inventory;
        final Map<Integer, Consumer<InventoryClickEvent>> slotActions;
        final Plugin plugin;
        BukkitTask refreshTask;
        boolean cancelClicks;
        long refreshInterval = -1;
        boolean hookPlaceholder;

        public AbstractInventoryHolder(Plugin plugin, Player player, InventoryConfig inventoryConfig) {
            if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                this.hookPlaceholder = true;
            }
            this.plugin = plugin;
            this.player = player;
            this.cancelClicks = inventoryConfig.isCancelClicks();
            this.inventory = inventoryConfig.createInventory(this);
            this.slotActions = new HashMap<>();
            this.refreshInterval = inventoryConfig.getRefreshInterval();

        }

        protected abstract void initializeItems();

        public void setItem(int slot, ItemStack item) {
            inventory.setItem(slot, processPlaceholders(item));
        }

        public void addItem(DynamicItem dynamicItem) {
            ItemStack processedItem = processPlaceholders(dynamicItem.getProcessedItem());
            for (int slot : dynamicItem.getSlots()) {
                inventory.setItem(slot, processedItem);
            }
        }

        public void removeItem(int slot) {
            inventory.clear(slot);
            slotActions.remove(slot);
        }

        public void clearInventory() {
            inventory.clear();
            slotActions.clear();
        }

        public void handleClick(InventoryClickEvent event) {
            int slot = event.getSlot();
            if (slotActions.containsKey(slot)) {
                slotActions.get(slot).accept(event);
            }
        }

        public void setItem(int slot, ItemStack item, Consumer<InventoryClickEvent> action) {
            inventory.setItem(slot, item);
            if (action != null) {
                slotActions.put(slot, action);
            }
        }

        public void setItem(StaticItem staticItem) {
            inventory.setItem(staticItem.slot, staticItem.getItem().getItemStack());
        }

        public void setItem(StaticItem staticItem, Consumer<InventoryClickEvent> action) {
            setItem(staticItem);
            if (action != null) {
                slotActions.put(staticItem.slot, action);
            }
        }

        public void open() {
            player.openInventory(inventory);
            startRefreshTask();
        }

        public void close() {
            stopRefreshTask();
        }

        protected ItemStack processPlaceholders(ItemStack item) {
            if (item == null) return null;
            if(!hookPlaceholder)
                return item;

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
            if (hookPlaceholder) {
                return PlaceholderAPI.setPlaceholders(player, text);
            }
            return text;
        }

        protected void startRefreshTask() {
            if (refreshInterval > 0) {
                refreshTask = Bukkit.getScheduler().runTaskTimer(plugin, this::refresh, refreshInterval, refreshInterval);
            }
        }

        protected void stopRefreshTask() {
            if (refreshTask != null) {
                refreshTask.cancel();
                refreshTask = null;
            }
        }

        protected void refresh() {
            initializeItems();
            player.updateInventory();
        }
    }
package dev.gether.getutils.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PlayerUtil {


    /**
     * Adds items to the player's inventory. If the inventory is full,
     * remaining items are dropped on the ground near the player.
     *
     * @param player The player to whose inventory items will be added
     * @param items The items to add
     */
    public static void addItems(Player player, ItemStack... items) {
        Map<Integer, ItemStack> notAdded = player.getInventory().addItem(items);

        if (!notAdded.isEmpty()) {
            Location dropLocation = player.getLocation();
            for (ItemStack item : notAdded.values()) {
                player.getWorld().dropItemNaturally(dropLocation, item);
            }
        }
    }

    /**
     * Removes a specified amount of items from a player's inventory.
     * This method searches through the player's entire inventory, including the off-hand slot,
     * and removes the specified items until the desired amount is reached or the inventory is exhausted.
     *
     * @param player    The player whose inventory will be modified
     * @param itemStack The ItemStack representing the type of item to remove
     * @param amount    The number of items to remove
     * @return The number of items that were actually removed
     *
     * @throws IllegalArgumentException if player is null, amount is negative, or itemStack is null
     */
    public static int removeItems(Player player, ItemStack itemStack, int amount) {
        if (player == null) throw new IllegalArgumentException("Player cannot be null");
        if (itemStack == null) throw new IllegalArgumentException("ItemStack cannot be null");
        if (amount < 0) throw new IllegalArgumentException("Amount cannot be negative");

        int removedAmount = 0;
        ItemStack[] contents = player.getInventory().getContents();

        for (int i = 0; i < contents.length && removedAmount < amount; i++) {
            ItemStack is = contents[i];
            if (is != null && is.isSimilar(itemStack)) {
                int currentAmount = Math.min(is.getAmount(), amount - removedAmount);
                removedAmount += currentAmount;

                if (is.getAmount() == currentAmount) {
                    player.getInventory().setItem(i, null);
                } else {
                    is.setAmount(is.getAmount() - currentAmount);
                }
            }
        }

        // Check off-hand separately
        ItemStack offHandItem = player.getInventory().getItemInOffHand();
        if (offHandItem.isSimilar(itemStack) && removedAmount < amount) {
            int currentAmount = Math.min(offHandItem.getAmount(), amount - removedAmount);
            removedAmount += currentAmount;

            if (offHandItem.getAmount() == currentAmount) {
                player.getInventory().setItemInOffHand(null);
            } else {
                offHandItem.setAmount(offHandItem.getAmount() - currentAmount);
            }
        }

        player.updateInventory();
        return removedAmount;
    }

    /**
     * Counts the number of items in a player's inventory that match the specified ItemStack.
     * This method searches through the player's entire inventory, including the off-hand slot.
     *
     * @param player    The player whose inventory will be searched
     * @param itemStack The ItemStack representing the type of item to count
     * @return The total number of matching items found in the player's inventory
     *
     * @throws IllegalArgumentException if player is null or itemStack is null
     */
    public static int countItems(Player player, ItemStack itemStack) {
        if (player == null) throw new IllegalArgumentException("Player cannot be null");
        if (itemStack == null) throw new IllegalArgumentException("ItemStack cannot be null");

        int totalCount = 0;
        PlayerInventory inventory = player.getInventory();

        // Count items in main inventory
        for (ItemStack is : inventory.getContents()) {
            if (is != null && is.isSimilar(itemStack)) {
                totalCount += is.getAmount();
            }
        }

        // Count items in off-hand
        ItemStack offHandItem = inventory.getItemInOffHand();
        if (offHandItem.isSimilar(itemStack)) {
            totalCount += offHandItem.getAmount();
        }

        return totalCount;
    }


    /**
     * Counts items in a player's inventory based on a NamespacedKey.
     * This method searches through the player's entire inventory, including the off-hand slot.
     *
     * @param player The player whose inventory will be searched
     * @param key The NamespacedKey to identify the items
     * @return The total number of matching items found in the player's inventory
     *
     * @throws IllegalArgumentException if player is null or key is null
     */
    public static int countItemsByKey(Player player, NamespacedKey key) {
        if (player == null) throw new IllegalArgumentException("Player cannot be null");
        if (key == null) throw new IllegalArgumentException("NamespacedKey cannot be null");

        int totalCount = 0;
        PlayerInventory inventory = player.getInventory();

        // Count items in main inventory
        for (ItemStack item : inventory.getContents()) {
            if (item != null && hasNamespacedKey(item, key)) {
                totalCount += item.getAmount();
            }
        }

        // Count item in off-hand
        ItemStack offHandItem = inventory.getItemInOffHand();
        if (offHandItem != null && hasNamespacedKey(offHandItem, key)) {
            totalCount += offHandItem.getAmount();
        }

        return totalCount;
    }

    /**
     * Removes a specified amount of items from a player's inventory based on a NamespacedKey.
     * This method searches through the player's entire inventory, including the off-hand slot,
     * and removes the specified items until the desired amount is reached or the inventory is exhausted.
     *
     * @param player The player whose inventory will be modified
     * @param key The NamespacedKey to identify the items to remove
     * @param amount The number of items to remove
     * @return The number of items that were actually removed
     *
     * @throws IllegalArgumentException if player is null, key is null, or amount is negative
     */
    public static int removeItemsByKey(Player player, NamespacedKey key, int amount) {
        if (player == null) throw new IllegalArgumentException("Player cannot be null");
        if (key == null) throw new IllegalArgumentException("NamespacedKey cannot be null");
        if (amount < 0) throw new IllegalArgumentException("Amount cannot be negative");

        int removedAmount = 0;
        PlayerInventory inventory = player.getInventory();

        for (int i = 0; i < inventory.getSize() && removedAmount < amount; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && hasNamespacedKey(item, key)) {
                int toRemove = Math.min(item.getAmount(), amount - removedAmount);
                removedAmount += toRemove;
                if (toRemove == item.getAmount()) {
                    inventory.setItem(i, null);
                } else {
                    item.setAmount(item.getAmount() - toRemove);
                }
            }
        }

        if (removedAmount < amount) {
            ItemStack offHandItem = inventory.getItemInOffHand();
            if (offHandItem != null && hasNamespacedKey(offHandItem, key)) {
                int toRemove = Math.min(offHandItem.getAmount(), amount - removedAmount);
                removedAmount += toRemove;
                if (toRemove == offHandItem.getAmount()) {
                    inventory.setItemInOffHand(null);
                } else {
                    offHandItem.setAmount(offHandItem.getAmount() - toRemove);
                }
            }
        }

        if (removedAmount > 0) {
            player.updateInventory();
        }

        return removedAmount;
    }

    private static boolean hasNamespacedKey(ItemStack item, NamespacedKey key) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(key, PersistentDataType.STRING);
    }



}

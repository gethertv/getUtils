package dev.gether.getutils.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

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

}

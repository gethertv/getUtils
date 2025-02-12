package dev.gether.getutils.utils;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;

public class ItemUtil {

    public static boolean sameItem(ItemStack item1, ItemStack item2) {
        if (item1.getType() != item2.getType()) {
            return false;
        }
        ItemMeta meta1 = item1.getItemMeta();
        ItemMeta meta2 = item2.getItemMeta();

        if ((meta1 != null && meta2 != null) &&
                (!Objects.equals(meta1.getDisplayName(), meta2.getDisplayName()))) {
            return false;
        }

        if (!Objects.equals(meta1.getLore(), meta2.getLore())) {
            return false;
        }

        if (!Objects.equals(item1.getEnchantments(), item2.getEnchantments())) {
            return false;
        }
        if (!Objects.equals(meta1.getItemFlags(), meta2.getItemFlags())) {
            return false;
        }
        if (meta1.isUnbreakable() != meta2.isUnbreakable()) {
            return false;
        }
        return true;
    }
    public static boolean sameItemName(ItemStack item1, ItemStack item2) {
        if (item1.getType() != item2.getType()) {
            return false;
        }
        ItemMeta meta1 = item1.getItemMeta();
        ItemMeta meta2 = item2.getItemMeta();

        if ((meta1 != null && meta2 != null) &&
                (!Objects.equals(meta1.getDisplayName(), meta2.getDisplayName()))) {
            return false;
        }
        if (!Objects.equals(item1.getEnchantments(), item2.getEnchantments())) {
            return false;
        }
        if (!Objects.equals(meta1.getItemFlags(), meta2.getItemFlags())) {
            return false;
        }
        if (meta1.isUnbreakable() != meta2.isUnbreakable()) {
            return false;
        }
        return true;
    }

    public static int removeItemReturnSlot(Player player, ItemStack itemStack, int amount) {
        int remove = amount;
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack current = player.getInventory().getItem(i);
            if (current == null) {
                continue;
            }

            if (current.isSimilar(itemStack)) {
                int currentAmount = current.getAmount();
                if (currentAmount >= remove) {
                    current.setAmount(currentAmount - remove);
                    return i; // Return the slot number where we removed the item
                } else {
                    player.getInventory().setItem(i, null);
                    remove -= currentAmount;
                    return i; // Return the last slot we removed from
                }
            }
        }
        return -1; // Return -1 if no item was removed
    }

    public static void removeItem(Player player, ItemStack itemStack, int amount) {
        int remove = amount;
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack current = player.getInventory().getItem(i);
            if (current == null) {
                continue;
            }

            if (current.isSimilar(itemStack)) {
                int currentAmount = current.getAmount();
                if (currentAmount >= remove) {
                    current.setAmount(currentAmount - remove);
                    break;
                } else {
                    player.getInventory().setItem(i, null);
                    remove -= currentAmount;
                }
            }
        }
    }

    public static int calcItem(Player player, ItemStack calcStack)
    {
        int amount = 0;
        for(ItemStack itemStack : player.getInventory())
        {
            if(itemStack==null || itemStack.getType()== Material.AIR)
                continue;

            if(itemStack.isSimilar(calcStack))
                amount+=itemStack.getAmount();
        }

        return amount;
    }

    public static boolean hasCurrentAmount(Player player, ItemStack itemStack, int needAmount)
    {
        int amount = calcItem(player, itemStack);
        return amount >= needAmount;
    }



}

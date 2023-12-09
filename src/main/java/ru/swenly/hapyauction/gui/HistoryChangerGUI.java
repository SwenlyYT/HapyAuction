package ru.swenly.hapyauction.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.swenly.hapyauction.HapyAuction;
import ru.swenly.hapyauction.config.ConfigSystem;
import ru.swenly.hapyauction.config.PlacedHolders;
import ru.swenly.hapyauction.utils.AuctionUtils;
import ru.swenly.hapyauction.utils.HashMapUtils;
import ru.swenly.hapyauction.utils.YamlUtils;

import java.io.File;
import java.util.*;

public class HistoryChangerGUI implements Listener {
    public static File plugin_path = HapyAuction.getPlugin(HapyAuction.class).getDataFolder();
    public static HashMap<String, Inventory> invMap = new HashMap<>();
    public static FileConfiguration config;

    public static void CreateGUI(Player player) {
        try {
            Inventory inventory;
            inventory = Bukkit.createInventory(null, 9, "История слотов");

            inventory.setItem(2, createGuiItem(Material.IRON_INGOT, 1, "§aПроданные слоты", "§eСлоты, которые вы продали"));

            inventory.setItem(4, createGuiItem(Material.CLOCK, 1, "§aИстекшие слоты", "§eСлоты, которые вы выставили,", "§eно их срок продажи истек"));

            inventory.setItem(6, createGuiItem(Material.GOLD_INGOT, 1, "§aКупленные слоты", "§eСлоты, которые вы купили"));

            invMap.put(player.getName(), inventory);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    // Nice little method to create a gui item with a custom name, and description
    public static ItemStack createGuiItem(final Material material, Integer amount, final String name, final String... lore) {
        final ItemStack item = new ItemStack(material, amount);
        final ItemMeta meta = item.getItemMeta();

        // Set the name of the item
        if (!name.equals("")) {
            meta.setDisplayName(name);
        }

        // Set the lore of the item
        meta.setLore(Arrays.asList(lore));

        item.setItemMeta(meta);

        return item;
    }

    public static void openInventory(final HumanEntity ent) {
        Inventory inventory = invMap.get(ent.getName());

        ent.openInventory(inventory);
    }

    // Check for clicks on items
    @EventHandler()
    public void onInventoryClick(final InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Inventory inventory = e.getInventory();

        if (!e.getView().getTitle().equals("История слотов")) return;
        e.setCancelled(true);

        if (e.getRawSlot() == 2) {
            SalesHistoryGUI.CreateGUI(player);
            SalesHistoryGUI.openInventory(player);
        } else if (e.getRawSlot() == 4) {
            ExpiredHistoryGUI.CreateGUI(player);
            ExpiredHistoryGUI.openInventory(player);
        } else if (e.getRawSlot() == 6) {
            PurchasesHistoryGUI.CreateGUI(player);
            PurchasesHistoryGUI.openInventory(player);
        }
    }

    // Cancel dragging in our inventory
    @EventHandler
    public void onInventoryDrag(final InventoryDragEvent e) {
        Player player = (Player) e.getWhoClicked();
        Inventory inventory = e.getInventory();

        if (inventory.equals(invMap.get(player.getName()))) {
            e.setCancelled(true);
        }
    }
}

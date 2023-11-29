package ru.swenly.hapyauction.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.swenly.hapyauction.HapyAuction;
import ru.swenly.hapyauction.config.ConfigSystem;
import ru.swenly.hapyauction.config.PlacedHolders;
import ru.swenly.hapyauction.utils.AuctionUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ConfirmGUI implements Listener {
    public static File plugin_path = HapyAuction.getPlugin(HapyAuction.class).getDataFolder();
    public static FileConfiguration config;
    public static HashMap<String, String> confirmsMap = new HashMap<>();
    public static HashMap<String, String> categoriesMap = new HashMap<>();
    public static HashMap<String, Inventory> invMap = new HashMap<>();
    public static HashMap<String, ConfigurationSection> slotMap = new HashMap<>();

    public static void CreateGui(Player player) {
        Inventory inventory;

        // Create a new inventory, with no owner (as this isn't a real inventory), a size of nine, called example
        inventory = Bukkit.createInventory(player, 27, "Подтверждение");
        invMap.put(player.getName(), inventory);

        // Put the items into the inventory
        initializeItems();
    }

    // You can call this whenever you want to put the items in
    public static void initializeItems() {
        // inv.addItem(createGuiItem(Material.DIAMOND_SWORD, "Example Sword", "§aFirst line of the lore", "§bSecond line of the lore"));
        // inv.addItem(createGuiItem(Material.IRON_HELMET, "§bExample Helmet", "§aFirst line of the lore", "§bSecond line of the lore"));
    }

    // Nice little method to create a gui item with a custom name, and description
    public static ItemStack createGuiItem(final Material material, final String name, final String... lore) {
        final ItemStack item = new ItemStack(material, 1);
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

    // You can open the inventory with this
    public static void openInventory(final HumanEntity ent, ConfigurationSection itemData, String category, ItemStack itemStack, Boolean cancelBuy, String fromType, Boolean oneItem) throws IOException, InvalidConfigurationException {
        ItemStack green_glass_pane = createGuiItem(Material.GREEN_STAINED_GLASS_PANE, "§aКупить предмет");
        Inventory inventory = invMap.get(ent.getName());

        if (fromType.equals("sales")) {
            green_glass_pane = createGuiItem(Material.GREEN_STAINED_GLASS_PANE, "§aОтменить продажу слота");
        }
        else if (fromType.equals("expired")) {
            green_glass_pane = createGuiItem(Material.GREEN_STAINED_GLASS_PANE, "§aЗабрать истекший слот");
        }

        inventory.setItem(0, green_glass_pane);
        inventory.setItem(1, green_glass_pane);
        inventory.setItem(2, green_glass_pane);
        inventory.setItem(9, green_glass_pane);
        inventory.setItem(10, green_glass_pane);
        inventory.setItem(11, green_glass_pane);
        inventory.setItem(18, green_glass_pane);
        inventory.setItem(19, green_glass_pane);
        inventory.setItem(20, green_glass_pane);

        ItemStack red_glass_pane = createGuiItem(Material.RED_STAINED_GLASS_PANE, "§cОтмена покупки");
        if (fromType.equals("sales")) {
            red_glass_pane = createGuiItem(Material.RED_STAINED_GLASS_PANE, "§cОставить слот в аукционе");
        }
        else if (fromType.equals("expired")) {
            red_glass_pane = createGuiItem(Material.RED_STAINED_GLASS_PANE, "§cОставить слот в истекших");
        }

        inventory.setItem(6, red_glass_pane);
        inventory.setItem(7, red_glass_pane);
        inventory.setItem(8, red_glass_pane);
        inventory.setItem(15, red_glass_pane);
        inventory.setItem(16, red_glass_pane);
        inventory.setItem(17, red_glass_pane);
        inventory.setItem(24, red_glass_pane);
        inventory.setItem(25, red_glass_pane);
        inventory.setItem(26, red_glass_pane);

        ItemStack aqua_glass_pane = createGuiItem(Material.CYAN_STAINED_GLASS_PANE, "§f ");

        inventory.setItem(3, aqua_glass_pane);
        inventory.setItem(4, aqua_glass_pane);
        inventory.setItem(5, aqua_glass_pane);
        inventory.setItem(12, aqua_glass_pane);
        inventory.setItem(14, aqua_glass_pane);
        inventory.setItem(21, aqua_glass_pane);
        inventory.setItem(22, aqua_glass_pane);
        inventory.setItem(23, aqua_glass_pane);

        if (!oneItem) {
            inventory.setItem(13, itemStack);
        }
        else {
            ItemStack oneItemStack = itemStack.clone();
            oneItemStack.setAmount(1);

            inventory.setItem(13, oneItemStack);
        }

        confirmsMap.put(ent.getName(), itemData + ":" + cancelBuy + ":" + fromType + ":" + oneItem);
        categoriesMap.put(ent.getName(), category);
        invMap.put(ent.getName(), inventory);

        String fileType = "categories";

        if (fromType.equals("expired")) {
            fileType = "expired" + "/" + ent.getName();
        }

        slotMap.put(ent.getName(), itemData);

        ent.openInventory(inventory);
    }

    // Check for clicks on items
    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        Inventory inventory = invMap.get(e.getWhoClicked().getName());
        if (!e.getInventory().equals(inventory)) return;

        e.setCancelled(true);

        final ItemStack clickedItem = e.getCurrentItem();

        // verify current item is not null
        if (clickedItem == null || clickedItem.getType().isAir()) return;

        final Player p = (Player) e.getWhoClicked();
        if (clickedItem.getType() == Material.RED_STAINED_GLASS_PANE) {
            p.closeInventory();
            AuctionItemsGUI.openInventory(p, false);

            categoriesMap.remove(p.getName());
            confirmsMap.remove(p.getName());

            AuctionItemsGUI.openInventory(p, false);
        }

        if (clickedItem.getType() == Material.GREEN_STAINED_GLASS_PANE) {
            String confirm = confirmsMap.get(p.getName());
            String category = categoriesMap.get(p.getName());

            ConfigurationSection itemData = slotMap.get(p.getName());
            Boolean cancelBuy = Boolean.parseBoolean(confirm.split(":", -1)[1]);
            String fromType = confirm.split(":", -1)[2];
            Boolean oneItem = Boolean.parseBoolean(confirm.split(":", -1)[3]);

            if (fromType.equals("auction") || fromType.equals("sales")) {
                AuctionUtils.buyItem(p, itemData, category, cancelBuy, fromType, oneItem);
            }
            else if (fromType.equals("expired")) {
                AuctionUtils.buyItem(p, itemData, category, cancelBuy, fromType, oneItem);
            }

            categoriesMap.remove(p.getName());
            confirmsMap.remove(p.getName());

            AuctionItemsGUI.openInventory(p, false);
        }

        invMap.put(e.getWhoClicked().getName(), inventory);
    }

    // Cancel dragging in our inventory
    @EventHandler
    public void onInventoryDrag(final InventoryDragEvent e) {
        Inventory inventory = invMap.get(e.getWhoClicked().getName());

        if (e.getInventory().equals(inventory)) {
            e.setCancelled(true);
        }
    }
}

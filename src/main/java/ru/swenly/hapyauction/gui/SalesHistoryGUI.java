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

public class SalesHistoryGUI implements Listener {
    public static File plugin_path = HapyAuction.getPlugin(HapyAuction.class).getDataFolder();
    public static HashMap<String, Integer> pagesMap = new HashMap<>();
    public static HashMap<String, String> categoriesMap = new HashMap<>();
    public static HashMap<String, String> sortingMap = new HashMap<>();
    public static HashMap<String, Inventory> invMap = new HashMap<>();
    public static FileConfiguration config;

    public static void CreateGUI(Player player) {
        // Create a new inventory, with no owner (as this isn't a real inventory), a size of nine, called example

        // Put the items into the inventory
        initializeItems(player);
    }

    // You can call this whenever you want to put the items in
    public static void initializeItems(Player player) {
        // inventory.addItem(createGuiItem(Material.DIAMOND_SWORD, "Example Sword", "§aFirst line of the lore", "§bSecond line of the lore"));
        // inventory.addItem(createGuiItem(Material.IRON_HELMET, "§bExample Helmet", "§aFirst line of the lore", "§bSecond line of the lore"));

        try {
            Inventory inventory;
            inventory = Bukkit.createInventory(null, 54, "Проданные слоты");

            inventory.setItem(45, createGuiItem(Material.CHEST, 1, "§aНазад", "§eВернуться обратно в аукцион"));
            inventory.setItem(48, createGuiItem(Material.ARROW, 1, "§7Предыдущая страница"));
            inventory.setItem(49, createGuiItem(Material.EMERALD, 1, "§cОбновить"));
            inventory.setItem(50, createGuiItem(Material.ARROW, 1, "§7Следующая страница"));
            inventory.setItem(52, createGuiItem(Material.REDSTONE_TORCH, 1, "§aСортировка", "§6- Сначала новые", "§e- Сначала старые", "§e- Сначала дешевые", "§e- Сначала дорогие"));
            inventory.setItem(53, createGuiItem(Material.KNOWLEDGE_BOOK, 1, "§aКатегории", "§6- Все предметы", "§e- Броня", "§e- Оружие", "§e- Инструменты", "§e- Еда", "§e- Зелья", "§e- Блоки", "§e- Другое"));

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

        pagesMap.putIfAbsent(ent.getName(), 1);

        categoriesMap.putIfAbsent(ent.getName(), "Все предметы");
        sortingMap.putIfAbsent(ent.getName(), "Сначала новые");

        String category = categoriesMap.get(ent.getName());
        String sorting = sortingMap.get(ent.getName());

        if (sorting.equals("Сначала новые")) {
            inventory.setItem(52, createGuiItem(Material.REDSTONE_TORCH, 1, "§aСортировка", "§e§6- Сначала новые", "§e- Сначала старые", "§e- Сначала дешевые", "§e- Сначала дорогие"));
        }
        else if (sorting.equals("Сначала старые")) {
            inventory.setItem(52, createGuiItem(Material.REDSTONE_TORCH, 1, "§aСортировка", "§e- Сначала новые", "§e§6- Сначала старые", "§e- Сначала дешевые", "§e- Сначала дорогие"));
        }
        else if (sorting.equals("Сначала дешевые")) {
            inventory.setItem(52, createGuiItem(Material.REDSTONE_TORCH, 1, "§aСортировка", "§e- Сначала новые", "§e- Сначала старые", "§e§6- Сначала дешевые", "§e- Сначала дорогие"));
        }
        else if (sorting.equals("Сначала дорогие")) {
            inventory.setItem(52, createGuiItem(Material.REDSTONE_TORCH, 1, "§aСортировка", "§e- Сначала новые", "§e- Сначала старые", "§e- Сначала дешевые", "§e§6- Сначала дорогие"));
        }

        if (category.equals("Все предметы")) {
            inventory.setItem(53, createGuiItem(Material.KNOWLEDGE_BOOK, 1, "§aКатегории", "§e§6- Все предметы", "§e- Броня", "§e- Оружие", "§e- Инструменты", "§e- Еда", "§e- Зелья", "§e- Блоки", "§e- Другое"));
        }
        else if (category.equals("Броня")) {
            inventory.setItem(53, createGuiItem(Material.KNOWLEDGE_BOOK, 1, "§aКатегории", "§e- Все предметы", "§e§6- Броня", "§e- Оружие", "§e- Инструменты", "§e- Еда", "§e- Зелья", "§e- Блоки", "§e- Другое"));
        }
        else if (category.equals("Оружие")) {
            inventory.setItem(53, createGuiItem(Material.KNOWLEDGE_BOOK, 1, "§aКатегории", "§e- Все предметы", "§e- Броня", "§e§6- Оружие", "§e- Инструменты", "§e- Еда", "§e- Зелья", "§e- Блоки", "§e- Другое"));
        }
        else if (category.equals("Инструменты")) {
            inventory.setItem(53, createGuiItem(Material.KNOWLEDGE_BOOK, 1, "§aКатегории", "§e- Все предметы", "§e- Броня", "§e- Оружие", "§e§6- Инструменты", "§e- Еда", "§e- Зелья", "§e- Блоки", "§e- Другое"));
        }
        else if (category.equals("Еда")) {
            inventory.setItem(53, createGuiItem(Material.KNOWLEDGE_BOOK, 1, "§aКатегории", "§e- Все предметы", "§e- Броня", "§e- Оружие", "§e- Инструменты", "§e§6- Еда", "§e- Зелья", "§e- Блоки", "§e- Другое"));
        }
        else if (category.equals("Зелья")) {
            inventory.setItem(53, createGuiItem(Material.KNOWLEDGE_BOOK, 1, "§aКатегории", "§e- Все предметы", "§e- Броня", "§e- Оружие", "§e- Инструменты", "§e- Еда", "§e§6- Зелья", "§e- Блоки", "§e- Другое"));
        }
        else if (category.equals("Блоки")) {
            inventory.setItem(53, createGuiItem(Material.KNOWLEDGE_BOOK, 1, "§aКатегории", "§e- Все предметы", "§e- Броня", "§e- Оружие", "§e- Инструменты", "§e- Еда", "§e- Зелья", "§e§6- Блоки", "§e- Другое"));
        }
        else if (category.equals("Другое")) {
            inventory.setItem(53, createGuiItem(Material.KNOWLEDGE_BOOK, 1, "§aКатегории", "§e- Все предметы", "§e- Броня", "§e- Оружие", "§e- Инструменты", "§e- Еда", "§e- Зелья", "§e- Блоки", "§e§6- Другое"));
        }

        try {
            YamlConfiguration sortedYamlConfiguration = new YamlConfiguration();
            Map<String, Object> itemsMap = AuctionUtils.getPlayerSalesItems((OfflinePlayer) ent, HapyAuction.categoriesMap.get(category));

            ConfigurationSection items = HashMapUtils.mapToSection(itemsMap);
            ConfigurationSection items_sorted = sortedYamlConfiguration.createSection("Items");

            if (sorting.equals("Сначала новые")) {
                Map<String, Object> sortedItemsMap = HashMapUtils.reverseKeys(itemsMap);

                items_sorted = HashMapUtils.mapToSection(sortedItemsMap);
            }

            else if (sorting.equals("Сначала старые")) {
                items_sorted = items;
                // COPY OF SECTION
            }

            else if (sorting.equals("Сначала дешевые")) {
                Map<String, Object> sortedItemsMap = YamlUtils.sortingMapByKey(itemsMap, "Price", false);

                items_sorted = HashMapUtils.mapToSection(sortedItemsMap);
            }

            else if (sorting.equals("Сначала дорогие")) {
                Map<String, Object> sortedItemsMap = YamlUtils.sortingMapByKey(itemsMap, "Price", true);

                items_sorted = HashMapUtils.mapToSection(sortedItemsMap);
            }

            config = ConfigSystem.getConfig();

            int i = 0;
            for (Map.Entry<String, Object> entry : items_sorted.getValues(false).entrySet()) {
                ConfigurationSection itemInfo = (ConfigurationSection) entry.getValue();
                Integer itemPosInGUI = ((i + 1) + (45 * pagesMap.get(ent.getName())) - 45);

                String price = itemInfo.get("Price").toString();
                try {
                    String[] priceSplit = price.split("\\.", -1);
                    if (priceSplit[1].length() > 2) {
                        price = priceSplit[0] + "." + priceSplit[1].substring(0, 2);
                    }
                }
                catch (Exception ignored) { }

                ItemStack itemStack = itemInfo.getItemStack("Item");

                String one_item_price = "" + ((itemInfo.getDouble("Price") + 0.0) / itemStack.getAmount());
                try {
                    String[] priceSplit = one_item_price.split("\\.", -1);
                    if (priceSplit[1].length() >= 2) {
                        one_item_price = priceSplit[0] + "." + priceSplit[1].substring(0, 2);
                    }
                }
                catch (Exception ignored) { }

                String seller = itemInfo.get("Seller").toString();
                String buyer = itemInfo.get("Buyer").toString();

                String date = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(itemInfo.getLong("Timestamp") + 172800 * 1000);
                String buy_date = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(itemInfo.getLong("Buy_Timestamp") + 172800 * 1000);

                PlacedHolders.addPlaceholder("%item_price%", price);
                PlacedHolders.addPlaceholder("%one_item_price%", one_item_price);
                PlacedHolders.addPlaceholder("%item_seller%", seller);
                PlacedHolders.addPlaceholder("%item_buyer%", buyer);
                PlacedHolders.addPlaceholder("%item_expire_date%", date);
                PlacedHolders.addPlaceholder("%item_buy_date%", buy_date);

                ItemMeta itemMeta = itemStack.getItemMeta();
                List<String> lore = new ArrayList<>();

                try {
                    List<String> item_info = (List<String>) config.get("sell_item_info");
                    for (String line : item_info) {
                        lore.add(PlacedHolders.formatText(line));
                    }
                }
                catch (Exception exception) {
                    lore = Arrays.asList("§r", "§8------------------", "§r§eЦена: §6" + price, "§r§eПродавец: §6" + seller, "§r§eИстекает в: §6" + date, "§8------------------");
                    exception.printStackTrace();
                }

                itemMeta.setLore(lore);
                itemStack.setItemMeta(itemMeta);

                inventory.setItem(i, itemStack);

                i++;
                if (i >= 45) {
                    break;
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        ent.openInventory(inventory);
        invMap.put(ent.getName(), inventory);
    }

    // Check for clicks on items
    @EventHandler()
    public void onInventoryClick(final InventoryClickEvent e) {
        System.out.println(e.getView().getTitle());

        if (!e.getView().getTitle().equals("Проданные слоты")) return;
        e.setCancelled(true);

        final ItemStack clickedItem = e.getCurrentItem();

        // verify current item is not null
        if (clickedItem == null || clickedItem.getType().isAir()) return;

        final Player p = (Player) e.getWhoClicked();

        if (e.getRawSlot() >= 45) {
            if (e.getRawSlot() == 48) {
                if (pagesMap.get(p.getName()) > 1) {
                    pagesMap.replace(p.getName(), pagesMap.get(p.getName()) - 1);
                }
            }
            if (e.getRawSlot() == 50) {
                if (e.getInventory().getItem(44).getType() != Material.AIR){
                    pagesMap.replace(p.getName(), pagesMap.get(p.getName()) + 1);
                }
            }

            if (e.getRawSlot() == 52) {
                if (e.getClick().isLeftClick()) {
                    String sortingNow = sortingMap.get(p.getName());
                    Boolean sortingFound = false;
                    String sortingNext = "Сначала новые";

                    for (String key : HapyAuction.sortingList) {
                        if (sortingFound) {
                            sortingNext = key;
                            break;
                        }

                        if (key.equals(sortingNow)) {
                            sortingFound = true;
                        }
                    }

                    sortingMap.replace(p.getName(), sortingNow, sortingNext);
                }

                else if (e.getClick().isRightClick()) {
                    String sortingNow = sortingMap.get(p.getName());
                    String sortingPrev = "Сначала дорогие";

                    for (String key : HapyAuction.sortingList) {
                        if (key.equals(sortingNow)) {
                            break;
                        }

                        sortingPrev = key;
                    }

                    sortingMap.replace(p.getName(), sortingNow, sortingPrev);
                }
            }

            if (e.getRawSlot() == 53) {
                if (e.getClick().isLeftClick()) {
                    String categoryNow = categoriesMap.get(p.getName());
                    Boolean categoryFound = false;
                    String categoryNext = "Все предметы";

                    for (String key : HapyAuction.categoriesMap.keySet()) {
                        if (categoryFound) {
                            categoryNext = key;
                            break;
                        }

                        if (key.equals(categoryNow)) {
                            categoryFound = true;
                        }
                    }

                    categoriesMap.replace(p.getName(), categoryNow, categoryNext);
                }

                else if (e.getClick().isRightClick()) {
                    String categoryNow = categoriesMap.get(p.getName());
                    String categoryPrev = "Другое";

                    for (String key : HapyAuction.categoriesMap.keySet()) {
                        if (key.equals(categoryNow)) {
                            break;
                        }

                        categoryPrev = key;
                    }

                    categoriesMap.replace(p.getName(), categoryNow, categoryPrev);
                }
            }

            SalesHistoryGUI.CreateGUI(p);
            SalesHistoryGUI.openInventory(p);

            if (e.getRawSlot() == 45) {
                AuctionItemsGUI.openInventory(p, true);
            }
        }
    }

    // Cancel dragging in our inventory
    @EventHandler
    public void onInventoryDrag(final InventoryDragEvent e) {
        if (e.getView().getTitle().equals("Проданные слоты")) {
            e.setCancelled(true);
        }
    }

    // Deleting page of player
    @EventHandler
    public void onInventoryClose(final InventoryCloseEvent e) {
        if (e.getView().getTitle().equals("Проданные слоты")) {
            Boolean b = pagesMap.containsKey(e.getPlayer().getName());

            if (b != null && b) {
                pagesMap.remove(e.getPlayer().getName());
            }
        }
    }
}


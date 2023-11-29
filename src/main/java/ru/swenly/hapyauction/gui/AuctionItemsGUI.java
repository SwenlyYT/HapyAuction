package ru.swenly.hapyauction.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
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
import java.text.SimpleDateFormat;
import java.util.*;

public class AuctionItemsGUI implements Listener {
    public static File plugin_path = HapyAuction.getPlugin(HapyAuction.class).getDataFolder();
    public static HashMap<String, Integer> pagesMap = new HashMap<>();
    public static HashMap<String, String> categoriesMap = new HashMap<>();
    public static HashMap<String, String> sortingMap = new HashMap<>();
    public static HashMap<String, ArrayList<Object>> invMap = new HashMap<>();
    public static FileConfiguration config;

    // You can call this whenever you want to put the items in
    public static void initializeItems(Player player, Inventory inventory) {
        // inventory.addItem(createGuiItem(Material.DIAMOND_SWORD, "Example Sword", "§aFirst line of the lore", "§bSecond line of the lore"));
        // inventory.addItem(createGuiItem(Material.IRON_HELMET, "§bExample Helmet", "§aFirst line of the lore", "§bSecond line of the lore"));
        try {
            inventory.setItem(45, createGuiItem(Material.ENDER_CHEST, 1, "§aВаши предметы", "§eПредметы, выставленные вами,", "§eкоторые все еще активны"));
            inventory.setItem(46, createGuiItem(Material.CHEST, 1, "§aИстория слотов", "§eЖурнал ваших действий,", "§eсвязанных со слотами"));
            inventory.setItem(48, createGuiItem(Material.ARROW, 1, "§7Предыдущая страница"));
            inventory.setItem(49, createGuiItem(Material.EMERALD, 1, "§cОбновить"));
            inventory.setItem(50, createGuiItem(Material.ARROW, 1, "§7Следующая страница"));
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

//    public static void plusPos() {
//        for (Map.Entry<String, Inventory> entry : invMap.entrySet()) {
//            try {
//                String player = entry.getKey();
//
//                posMap.put(player, posMap.get(player) + 1);
//            }
//            catch (Exception ignored) { }
//        }
//    }

    // You can open the inventory with this
    public static void openInventory(final Player player, Boolean newAll) {
        // Create a new inventory, with no owner (as this isn't a real inventory), a size of nine, called example
        Inventory inventory;
        Map<Integer, ConfigurationSection> dataMap = new LinkedHashMap<>();

        try {
            Boolean b = pagesMap.containsKey(player.getName());
            Boolean b1 = categoriesMap.containsKey(player.getName());
            String category = "Все предметы";

            if (b1 != null && b1) {
                category = categoriesMap.get(player.getName());
            }

            YamlConfiguration yamlConfiguration = new YamlConfiguration();

            File file = new File(plugin_path + "/categories/" + HapyAuction.categoriesMap.get(category)  + ".yml");
            yamlConfiguration = new YamlConfiguration();
            yamlConfiguration.load(file);

            ConfigurationSection items = yamlConfiguration.getConfigurationSection("Items");

            Integer pagesCount;
            if ((items.getKeys(false).size() % 45) < 45) {
                pagesCount = items.getKeys(false).size() / 45 + 1;
            }
            else {
                pagesCount = items.getKeys(false).size() / 45;
            }

            if (b != null && b) {
                Integer page = pagesMap.get(player.getName());
                inventory = Bukkit.createInventory(player, 54, "Аукцион [" + page + "/" + pagesCount + "]");
            }
            else {
                inventory = Bukkit.createInventory(player, 54, "Аукцион [1/" + pagesCount + "]");
            }
        }
        catch (Exception exception) {
            inventory = Bukkit.createInventory(player, 54, "Аукцион");

            exception.printStackTrace();
        }
        // Put the items into the inventory
        initializeItems(player, inventory);

        pagesMap.putIfAbsent(player.getName(), 1);

        categoriesMap.putIfAbsent(player.getName(), "Все предметы");
        sortingMap.putIfAbsent(player.getName(), "Сначала новые");

        if (newAll) {
            pagesMap.put(player.getName(), 1);

            categoriesMap.put(player.getName(), "Все предметы");
            sortingMap.put(player.getName(), "Сначала новые");
        }

        String category = categoriesMap.get(player.getName());
        String sorting = sortingMap.get(player.getName());

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
            File file = new File(plugin_path + "/categories/" + HapyAuction.categoriesMap.get(category)  + ".yml");
            YamlConfiguration yamlConfiguration = new YamlConfiguration();
            yamlConfiguration.load(file);

            ConfigurationSection items = yamlConfiguration.getConfigurationSection("Items");
            Map<String, Object> itemsMap = items.getValues(false);
            Map<String, Object> sortedItemsMap = items.getValues(false);

            // if (sorting.equals("Сначала новые")) {
            //     int length = items.getKeys(false).size() - 1;
//
            //     for (int i = length; i >= 0; i--){
            //         try {
            //             items_sorted.add(items.get(i));
            //         } catch (Exception e) {
            //             e.printStackTrace();
            //         }
            //     }
            // }
//
            // else if (sorting.equals("Сначала старые")) {
            //     items_sorted = items;
            // }
//
            // else if (sorting.equals("Сначала дешевые")) {
            //     items_sorted = YamlUtils.sortJsonArrayByValue(items, "Price", true);
            // }
//
            // else if (sorting.equals("Сначала дорогие")) {
            //     items_sorted = YamlUtils.sortJsonArrayByValue(items, "Price", false);
            // }

            if (sorting.equals("Сначала новые")) {
                sortedItemsMap = HashMapUtils.reverseKeys(itemsMap);
            }

            else if (sorting.equals("Сначала старые")) {
                // COPY OF SECTION
                sortedItemsMap = itemsMap;
            }

            else if (sorting.equals("Сначала дешевые")) {
                sortedItemsMap = YamlUtils.sortingMapByKey(itemsMap, "Price", false);
            }

            else if (sorting.equals("Сначала дорогие")) {
                sortedItemsMap = YamlUtils.sortingMapByKey(itemsMap, "Price", true);
            }

            for (int i = 0; i < 45; i++) {
                Integer itemPosInGUI = ((i + 1) + (45 * pagesMap.get(player.getName())) - 45) - 1;
                ConfigurationSection itemInfo;

                try {
                   itemInfo = (ConfigurationSection) new ArrayList<>(sortedItemsMap.values()).get(itemPosInGUI);
                   dataMap.put(itemPosInGUI + 1, itemInfo);
                }
                catch (Exception ignored) {
                    break;
                }

                ItemStack itemStack = itemInfo.getItemStack("Item").clone();
                ItemMeta itemMeta = itemStack.getItemMeta();
                List<String> lore = new ArrayList<>();

                String price = itemInfo.get("Price").toString();
                try {
                    String[] priceSplit = price.split("\\.", -1);
                    if (priceSplit[1].length() > 2) {
                        price = priceSplit[0] + "." + priceSplit[1].substring(0, 2);
                    }
                }
                catch (Exception ignored) { }

                String one_item_price = (itemInfo.getInt("Price") / (itemStack.getAmount() + 0.0)) + "";

                try {
                    String[] priceSplit = one_item_price.split("\\.", -1);
                    if (priceSplit[1].length() > 2) {
                        one_item_price = priceSplit[0] + "." + priceSplit[1].substring(0, 2);
                    }
                }
                catch (Exception ignored) { }

                String seller = itemInfo.get("Seller").toString();
                String date = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(itemInfo.getLong("Timestamp") + 172800 * 1000);

                PlacedHolders.addPlaceholder("%item_price%", price);
                PlacedHolders.addPlaceholder("%one_item_price%", one_item_price);
                PlacedHolders.addPlaceholder("%item_seller%", seller);
                PlacedHolders.addPlaceholder("%item_expire_date%", date);

                config = ConfigSystem.getConfig();

                try {
                    List<String> item_info = (List<String>) config.get("item_info");
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
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        ArrayList<Object> newArrayList = new ArrayList<>();

        newArrayList.add(inventory);
        newArrayList.add(dataMap);

        invMap.put(player.getName(), newArrayList);
        player.openInventory(inventory);
    }

    // Check for clicks on items
    @EventHandler()
    public void onInventoryClick(final InventoryClickEvent e) {
        if (!e.getView().getTitle().startsWith("Аукцион")) return;
        e.setCancelled(true);

        final ItemStack clickedItem = e.getCurrentItem();

        // verify current item is not null
        if (clickedItem == null || clickedItem.getType().isAir()) return;

        final Player p = (Player) e.getWhoClicked();

        if (e.getRawSlot() >= 45) {
            switch (e.getRawSlot()) {
                case 48:
                    if (pagesMap.get(p.getName()) > 1) {
                        pagesMap.replace(p.getName(), pagesMap.get(p.getName()) - 1);
                    }
                    break;
                case 50:
                    if (e.getInventory().getItem(44).getType() != Material.AIR){
                        pagesMap.replace(p.getName(), pagesMap.get(p.getName()) + 1);
                    }
                    break;
                case 52:
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
                    break;
                case 53:
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

                    pagesMap.put(p.getName(), 1);
                    break;
            }

            openInventory(p, false);

            if (e.getRawSlot() == 45) {
                PlayerSalesGUI.openInventory(p);
            }
            else if (e.getRawSlot() == 46) {
                ExpiredHistoryGUI.CreateGUI(p);
                ExpiredHistoryGUI.openInventory(p);
            }
        }

        else {
            try {
                // Using slots click is a best option for your inventory click's
                Integer page = pagesMap.get(p.getName());
                Integer position = ((e.getRawSlot() + 1) + (45 * page - 45));

                String category = categoriesMap.get(p.getName());

                Boolean b = pagesMap.containsKey(p.getName());

                if (b != null && b) {
             /*
                ItemMeta itemMeta = clickedItem.getItemMeta();
                ArrayList<String> emptyLore = new ArrayList<String>();

                clickedItem.setType(Material.BARRIER);
                clickedItem.setAmount(1);
                itemMeta.setLore(emptyLore);
                itemMeta.setDisplayName("§r§cВы уже купили этот предмет!");
                clickedItem.setItemMeta(itemMeta);
                break;
            */

                    ConfigurationSection itemData = ((Map<Integer, ConfigurationSection>) (invMap.get(p.getName()).get(1))).get(position);

                    ConfirmGUI.CreateGui(p);
                    if (itemData.getString("Seller").equals(p.getName())) {
                        ConfirmGUI.openInventory(p, itemData, category, clickedItem,true, "sales", false);
                    }
                    else {
                        ConfirmGUI.openInventory(p, itemData, category, clickedItem,false, "auction", e.getClick().isRightClick());
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    // Cancel dragging in our inventory
    @EventHandler
    public void onInventoryDrag(final InventoryDragEvent e) {
        if (e.getView().getTitle().startsWith("Аукцион")) {
            e.setCancelled(true);
        }
    }

    // Deleting page of player
    @EventHandler
    public void onInventoryClose(final InventoryCloseEvent e) {
        if (e.getView().getTitle().startsWith("Аукцион")) {
            // Boolean b = pagesMap.containsKey(e.getPlayer().getName());
            //
            // if (b != null && b) {
            //     pagesMap.remove(e.getPlayer().getName());
            // }
            //
            // try {
            //     categoriesMap.remove(e.getPlayer().getName());
            // } catch (Exception exception) {
            //     exception.printStackTrace();
            // }
            //
            // sortingMap.remove(e.getPlayer().getName());

        }
    }
}


package ru.swenly.hapyauction.utils;

import com.earth2me.essentials.User;
import com.earth2me.essentials.api.Economy;
import com.earth2me.essentials.api.NoLoanPermittedException;
import com.earth2me.essentials.api.UserDoesNotExistException;
import net.ess3.api.MaxMoneyException;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import ru.swenly.hapyauction.HapyAuction;
import ru.swenly.hapyauction.config.ConfigSystem;
import ru.swenly.hapyauction.config.PlacedHolders;
import ru.swenly.hapyauction.gui.AuctionItemsGUI;

import java.io.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

public class AuctionUtils {
    public static File plugin_path = HapyAuction.getPlugin(HapyAuction.class).getDataFolder();
    public static JSONParser jsonParser = new JSONParser();
    public static String category;
    public static FileConfiguration config;

    public static String sellItem(Player player, Double price) throws UserDoesNotExistException, MaxMoneyException, NoLoanPermittedException {
        ItemStack itemInHand = player.getItemInHand();

        if (itemInHand.getType() == Material.AIR) {
            return "air";
        }

        config = ConfigSystem.getConfig();

        try {
            Double maxSlots = 0d;
            Map<String, Object> permsMap = new LinkedHashMap<>();

            int i = 1;
            for (PermissionAttachmentInfo permissionAttachmentInfo : player.getEffectivePermissions()) {
                if (permissionAttachmentInfo.getPermission().startsWith("hapyauction.sell.slots.")) {
                    permsMap.put(i + "", Double.parseDouble(permissionAttachmentInfo.getPermission().split("\\.", -1)[3]));
                    i++;
                }
            }

            permsMap = HashMapUtils.reverseKeys(HashMapUtils.sortingMapByDoubleValue(permsMap));

            try {
                maxSlots = (Double) permsMap.values().toArray()[0];
            }
            catch (Exception ignored) { }

            Map<String, Object> allSales = getPlayerSales((OfflinePlayer) player, "all_items");

            if (allSales.size() >= maxSlots) {
                PlacedHolders.addPlaceholder("%max_slots%", maxSlots);
                player.sendMessage(PlacedHolders.formatText(config.getString("max_slots_message")));
                return "max_slots";
            }
        }

        catch (Exception exception) {
            exception.printStackTrace();
        }

        try {
            if (price > config.getInt("max_price")) {
                PlacedHolders.addPlaceholder("%max_price%", config.getInt("max_price"));
                player.sendMessage(PlacedHolders.formatText(config.getString("max_price_message")));
                return "max_price";
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }

        try {
            if (price < config.getInt("min_price")) {
                PlacedHolders.addPlaceholder("%min_price%", config.getInt("min_price"));
                player.sendMessage(PlacedHolders.formatText(config.getString("min_price_message")));
                return "min_price";
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }

        if (player.getInventory().getItemInHand().equals(itemInHand)) {
            player.setItemInHand(null);
        }
        else {
            player.getInventory().removeItemAnySlot(itemInHand);
        }

        // Map<Enchantment, Integer> enchantments = itemInHand.getItemMeta().getEnchants();
        // List<PotionEffect> effects = new ArrayList<>();
        // JSONArray jsonEffects = new JSONArray();
//
        // try {
        //     PotionMeta potionMeta = (PotionMeta) itemInHand.getItemMeta();
        //     Potion potion = Potion.fromItemStack(itemInHand);
//
        //     effects = (List<PotionEffect>) potion.getEffects();
        //     String potionType = potionMeta.getBasePotionData().getType().getEffectType().getName();
//
        //     jsonEffects.add(potion.getType().getEffectType().getName());
        //     // Integer potionEffectDuration = potionEffect.getDuration();
        //     //
        //     // jsonEffects.add(potionEffectType.getName() + " " + potion.getLevel() + " " + potionEffectDuration);
//
        //     // player.sendMessage(((potion.getEffects()) + ""));
        //     // player.sendMessage(potionMeta.getBasePotionData().getType().getEffectType() + "");
        // }
        // catch (Exception exception) {
        //     exception.printStackTrace();
        // }

        // for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
        //     Enchantment enchantment = entry.getKey();
        //     Integer level = entry.getValue();
//
        //     jsonEnchantments.add(enchantment.getKey() + " " + level);
        // }
//
        // for (PotionEffect entry : effects) {
        //     String effect = entry.getType().getName();
//
        //     jsonEffects.add(effect);
        //     player.sendMessage(effect);
        // }

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        Long milliseconds = timestamp.getTime();

        try {
            String category = getCategory(itemInHand);
            String[] categoryFiles = {"all_items", HapyAuction.categoriesMap.get(category)};

            for (String categoryFile : categoryFiles) {
                File file = new File(plugin_path + "/categories/" + categoryFile + ".yml");

                YamlConfiguration yamlConfiguration = new YamlConfiguration();
                yamlConfiguration.load(file);

                ConfigurationSection configurationSection = yamlConfiguration.getConfigurationSection("Items").createSection(String.valueOf(yamlConfiguration.getConfigurationSection("Items").getKeys(false).size() + 1));

                configurationSection.set("Price", price);
                configurationSection.set("Seller", player.getName());
                configurationSection.set("Timestamp", milliseconds);

                Map<String, Object> itemMap = itemInHand.serialize();
                itemMap.put("==", "org.bukkit.inventory.ItemStack");
                configurationSection.createSection("Item", itemMap);

                FileWriter fileWriter = new FileWriter(file);
                BufferedWriter bw = new BufferedWriter(fileWriter);
                bw.write(yamlConfiguration.saveToString());
                bw.flush();
                bw.close();
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }


        try {
            PlacedHolders.addPlaceholder("%item_name%", itemInHand.getType().name().toUpperCase());
            PlacedHolders.addPlaceholder("%item_amount%", itemInHand.getAmount());
            PlacedHolders.addPlaceholder("%item_price%", price);

            player.sendMessage(PlacedHolders.formatText(config.getString("sold_message")));
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }

        return "ok";
    }

    public static void buyItem(Player player, ConfigurationSection itemData, String category, Boolean cancelSell, String fromType, Boolean oneItem) {
        try {
            config = ConfigSystem.getConfig();

            File file = new File(plugin_path + "/categories/" + HapyAuction.categoriesMap.get(category) + ".yml");
            if (fromType.equals("expired")) {
                file = new File(plugin_path + "/expired/" + player.getName() + "/" + HapyAuction.categoriesMap.get(category) + ".yml");
            }

            YamlConfiguration yamlConfiguration = new YamlConfiguration();
            YamlConfiguration sortedYamlConfiguration = new YamlConfiguration();
            yamlConfiguration.load(file);

            ConfigurationSection items = yamlConfiguration.getConfigurationSection("Items");
            ConfigurationSection items_sorted = sortedYamlConfiguration.createSection("Items");
            Map<String, Object> itemsMap = items.getValues(false);

//            if (cancelSell && fromType.equals("sales")) {
//                itemsMap = getPlayerSales(player, HapyAuction.categoriesMap.get(category));
//            }

//            Map<String, Object> sortedItemsMap = new LinkedHashMap<>();
//
//            if (sorting.equals("Сначала новые")) {
//                sortedItemsMap = HashMapUtils.reverseKeys(itemsMap);
//
//                items_sorted = HashMapUtils.mapToSection(sortedItemsMap);
//            }
//
//            else if (sorting.equals("Сначала старые")) {
//                sortedItemsMap = itemsMap;
//
//                items_sorted = items;
//                // COPY OF SECTION
//            }
//
//            else if (sorting.equals("Сначала дешевые")) {
//                sortedItemsMap = YamlUtils.sortingMapByKey(itemsMap, "Price", false);
//
//                items_sorted = HashMapUtils.mapToSection(sortedItemsMap);
//            }
//
//            else if (sorting.equals("Сначала дорогие")) {
//                sortedItemsMap = YamlUtils.sortingMapByKey(itemsMap, "Price", true);
//
//                items_sorted = HashMapUtils.mapToSection(sortedItemsMap);
//            }

            // if (sorting.equals("Сначала новые") || sorting.equals("Сначала дорогие")) {
            //     itemData = items_sorted.getConfigurationSection(sortedItemsMap.keySet().toArray()[sortedItemsMap.size() - position].toString());
            // }

            OfflinePlayer offlineSeller = Bukkit.getOfflinePlayer(itemData.get("Seller").toString());
            ItemStack oldItemStack = new ItemStack(itemData.getItemStack("Item"));
            ItemStack itemStack = new ItemStack(itemData.getItemStack("Item"));

            int amount = itemData.getItemStack("Item").getAmount();
            double price = itemData.getInt("Price");
            double oldPrice = itemData.getInt("Price");
            long timestamp = itemData.getLong("Timestamp");

            YamlConfiguration newYamlConfiguration = new YamlConfiguration();
            ConfigurationSection newItemData = newYamlConfiguration.createSection("Items").createSection(itemData.getName());

            newItemData.set("Item", itemStack);
            newItemData.set("Price", price);
            newItemData.set("Timestamp", timestamp);
            newItemData.set("Seller", offlineSeller.getName());

            if (oneItem) {
                price = (price + 0.0) / amount;
                oldPrice -= price;

                if (itemStack.getAmount() > 1) {
                    itemStack.setAmount(itemStack.getAmount() - 1);

                    newItemData.set("Price", oldPrice);
                    newItemData.set("Seller", offlineSeller.getName());
                    newItemData.set("Timestamp", timestamp);
                    newItemData.set("Item", itemStack);
                }
            }

            if (!cancelSell) {
                if (!Economy.hasEnough(player.getUniqueId(), new BigDecimal(price))) {
                    try {
                        player.sendMessage(PlacedHolders.formatText(config.getString("not_enough_message")));
                    }
                    catch (Exception exception) {
                        exception.printStackTrace();
                    }

                    return;
                }
            }

            try {
                yamlConfiguration = new YamlConfiguration();
                yamlConfiguration.load(file);
                items = yamlConfiguration.getConfigurationSection("Items");

                itemsMap = (LinkedHashMap<String, Object>) items.getValues(false);

                if (!HashMapUtils.containsValueInSectionByMap(itemsMap, itemData)) {
                    player.sendMessage(PlacedHolders.formatText(ConfigSystem.getConfig().getString("item_already_bought_message")));
                    return;
                }

                if (oneItem && oldItemStack.getAmount() > 1) {
                    items_sorted = HashMapUtils.replaceValueInSectionByMap(itemsMap, itemData, newItemData);
                }
                else {
                    items_sorted = HashMapUtils.deleteValueInSectionByMap(itemsMap, itemData);
                }

                // sortedItemsMap = YamlUtils.sortingMapByKey(itemsMap, "Timestamp", true);

                yamlConfiguration.set("Items", items_sorted);
                yamlConfiguration.save(file);
            }
            catch (Exception exception) {
                exception.printStackTrace();
            }

            if (!cancelSell) {
                Economy.subtract(player.getUniqueId(), new BigDecimal(price));
                try {
                    Economy.add(offlineSeller.getName(), new BigDecimal(price));
                }
                catch (Exception exception) {
                    exception.printStackTrace();
                }

                PlacedHolders.addPlaceholder("%item_name%", itemData.getItemStack("Item").getType().name().toUpperCase());

                if (oneItem) {
                    PlacedHolders.addPlaceholder("%item_amount%", 1);

                    try {
                        String[] priceSplit = ((price + 0.0) / (itemData.getItemStack("Item").getAmount()) + "").split("\\.");
                        PlacedHolders.addPlaceholder("%item_price%", price);
                    }
                    catch (Exception exception) {
                        PlacedHolders.addPlaceholder("%item_price%", price);
                        exception.printStackTrace();
                    }
                }
                else {
                    PlacedHolders.addPlaceholder("%item_amount%", itemStack.getAmount());
                    PlacedHolders.addPlaceholder("%item_price%", price);
                }

                PlacedHolders.addPlaceholder("%item_seller%", offlineSeller.getName());
                PlacedHolders.addPlaceholder("%item_buyer%", player.getName());

                try {
                    String message = PlacedHolders.formatText(config.getString("bought_message"));

                    player.sendMessage(message);
                }
                catch (Exception exception) {
                    exception.printStackTrace();
                }

                try {
                    if (offlineSeller.isOnline()) {
                        Player seller = (Player) offlineSeller;
                        String message = PlacedHolders.formatText(config.getString("other_bought_message"));

                        seller.sendMessage(message);
                    }
                }

                catch (Exception exception) {
                    exception.printStackTrace();
                }
            }

            else {
                try {
                    PlacedHolders.addPlaceholder("%item_name%", itemStack.getType().name().toUpperCase());

                    if (oneItem) {
                        PlacedHolders.addPlaceholder("%item_amount%", 1);

                        try {
                            String[] priceSplit = ((price + 0.0) / (itemData.getItemStack("Item").getAmount()) + "").split("\\.");
                            PlacedHolders.addPlaceholder("%item_price%", price);

                        }
                        catch (Exception exception) {
                            PlacedHolders.addPlaceholder("%item_price%", price);
                            exception.printStackTrace();
                        }
                    }
                    else {
                        PlacedHolders.addPlaceholder("%item_amount%", itemStack.getAmount());
                        PlacedHolders.addPlaceholder("%item_price%", price);
                    }


                    if (fromType.equals("expired")) {
                        player.sendMessage(PlacedHolders.formatText(config.getString("expired_message")));
                    }
                    else {
                        player.sendMessage(PlacedHolders.formatText(config.getString("canceled_message")));
                    }
                }
                catch (Exception exception) {
                    exception.printStackTrace();
                }
            }

            ItemStack itemStack1 = new ItemStack(itemStack);
            if (oneItem) {
                itemStack = new ItemStack(itemStack);
                itemStack.setAmount(1);
            }
            player.getInventory().addItem(itemStack);

            if (oneItem) {
                itemStack = itemStack1;
            }

            // Map<Enchantment, Integer> enchantmentsMap = new LinkedHashMap<>();
            // for (Object enchantment : (JSONArray) itemData.get("Enchantments")) {
            //     String enchantmentStr = enchantment.toString();
            //     enchantmentsMap.put(Enchantment.getByKey(NamespacedKey.fromString(enchantmentStr.split(" ", -1)[0])), Integer.parseInt(enchantmentStr.split(" ", -1)[1]));
            // }
            //
            // try {
            //     PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();
            //     for (Object effect : (JSONArray) itemData.get("Effects")) {
            //         player.sendMessage(effect.toString());
            //         String effectStr = effect.toString().split(" ", -1)[0];
            //         Integer level = Integer.parseInt(effect.toString().split(" ", -1)[1]);
            //         Integer duration = Integer.parseInt((effect.toString().split(" ", -1)[2]).split("\\.", -1)[0]);
//
            //         PotionEffect potionEffect = new PotionEffect(PotionEffectType.getByName(effectStr), duration, level);
//
            //         potionMeta.addCustomEffect(potionEffect, true);
            //     }
//
            //     itemStack.setItemMeta((ItemMeta) potionMeta);
//
            // } catch (Exception exception) {
            //     exception.printStackTrace();
            // }
//
            // itemStack.addEnchantments(enchantmentsMap);

            try {
                if (!HapyAuction.categoriesMap.get(getCategory(itemStack)).equals(HapyAuction.categoriesMap.get(category))) {
                    file = new File(plugin_path + "/categories/" + HapyAuction.categoriesMap.get(getCategory(itemStack)) + ".yml");
                    yamlConfiguration = new YamlConfiguration();
                    yamlConfiguration.load(file);
                    items = yamlConfiguration.getConfigurationSection("Items");

                    itemsMap = (LinkedHashMap<String, Object>) items.getValues(false);

                    if (oneItem && oldItemStack.getAmount() > 1) {
                        items_sorted = HashMapUtils.replaceValueInSectionByMap(itemsMap, itemData, newItemData);
                    }
                    else {
                        items_sorted = HashMapUtils.deleteValueInSectionByMap(itemsMap, itemData);
                    }

                    // sortedItemsMap = YamlUtils.sortingMapByKey(itemsMap, "Timestamp", true);

                    yamlConfiguration.set("Items", items_sorted);
                    yamlConfiguration.save(file);
                }
            }
            catch (Exception exception) {
                exception.printStackTrace();
            }

            try {
                file = new File(plugin_path + "/categories/" + "all_items.yml");
                yamlConfiguration = new YamlConfiguration();
                yamlConfiguration.load(file);
                items = yamlConfiguration.getConfigurationSection("Items");

                itemsMap = (LinkedHashMap<String, Object>) items.getValues(false);

                if (oneItem && oldItemStack.getAmount() > 1) {
                    items_sorted = HashMapUtils.replaceValueInSectionByMap(itemsMap, itemData, newItemData);
                }
                else {
                    items_sorted = HashMapUtils.deleteValueInSectionByMap(itemsMap, itemData);
                }

                // sortedItemsMap = YamlUtils.sortingMapByKey(itemsMap, "Timestamp", true);

                yamlConfiguration.set("Items", items_sorted);
                yamlConfiguration.save(file);
            }
            catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static ArrayList<JSONObject> getAllSalesFromPlayerWithJson(OfflinePlayer player) {
        ArrayList<JSONObject> allSales = new ArrayList<>();

        try {
            File all_items = new File(plugin_path + "/categories/" + "all_items.json");
            FileReader fileReader = new FileReader(all_items);
            JSONObject jsonObject = (JSONObject) jsonParser.parse(fileReader);
            JSONArray items = (JSONArray) jsonObject.get("Items");

            for (Object object : items) {
                 JSONObject itemJson = (JSONObject) object;

                 if (itemJson.get("Seller").toString().equals(player.getName())) {
                     allSales.add(itemJson);
                 }
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }

        return allSales;
    }

    public static Map<String, Object> getPlayerSales(OfflinePlayer player, String category) {
        Map<String, Object> allSales = new LinkedHashMap<>();

        try {
            File file = new File(plugin_path + "/categories/" + category + ".yml");
            YamlConfiguration yamlConfiguration = new YamlConfiguration();
            yamlConfiguration.load(file);

            Map<String, Object> items = yamlConfiguration.getConfigurationSection("Items").getValues(false);

            for (Map.Entry<String, Object> entry : items.entrySet()) {
                ConfigurationSection configurationSection = (ConfigurationSection) entry.getValue();

                if (configurationSection.getString("Seller").equals(player.getName())) {
                    allSales.put(entry.getKey(), configurationSection);
                }
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }

        return allSales;
    }

    public static Map<String, Object> getPlayerExpiredItems(OfflinePlayer player, String category) {
        Map<String, Object> allSales = new LinkedHashMap<>();

        try {
            File file = new File(plugin_path + "/expired/" + player.getName() + "/" + category + ".yml");
            YamlConfiguration yamlConfiguration = new YamlConfiguration();
            yamlConfiguration.load(file);

            Map<String, Object> items = yamlConfiguration.getConfigurationSection("Items").getValues(false);

            for (Map.Entry<String, Object> entry : items.entrySet()) {
                ConfigurationSection configurationSection = (ConfigurationSection) entry.getValue();

                if (configurationSection.getString("Seller").equals(player.getName())) {
                    allSales.put(entry.getKey(), configurationSection);
                }
            }
        } catch (Exception ignored) {}

        return allSales;
    }

    public static String getCategory(ItemStack itemStack) {
        FileConfiguration config = HapyAuction.config;
        category = "Другое";

        ConfigurationSection categories = config.getConfigurationSection("categories");
        String material = itemStack.getType().toString();

        if (((List<String>) categories.get("armor")).contains(material)) {
            category = "Броня";
        }
        else if (((List<String>) categories.get("weapons")).contains(material)) {
            category = "Оружие";
        }
        else if (((List<String>) categories.get("tools")).contains(material)) {
            category = "Инструменты";
        }
        else if (((List<String>) categories.get("potions")).contains(material)) {
            category = "Зелья";
        }

        if (itemStack.getType().isBlock()) {
            category = "Блоки";
        }
        else if (itemStack.getType().isEdible()) {
            category = "Еда";
        }

        return category;
    }

    public static ConfigurationSection getItemData(Integer position, String sorting, File file) throws IOException, InvalidConfigurationException {
        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        YamlConfiguration sortedYamlConfiguration = new YamlConfiguration();
        yamlConfiguration.load(file);

        ConfigurationSection items = yamlConfiguration.getConfigurationSection("Items");
        ConfigurationSection items_sorted = sortedYamlConfiguration.createSection("Items");
        Map<String, Object> itemsMap = items.getValues(false);

        Map<String, Object> sortedItemsMap = new LinkedHashMap<>();

        if (sorting.equals("Сначала новые")) {
            sortedItemsMap = HashMapUtils.reverseKeys(itemsMap);

            items_sorted = HashMapUtils.mapToSection(sortedItemsMap);
        }

        else if (sorting.equals("Сначала старые")) {
            sortedItemsMap = itemsMap;

            items_sorted = items;
            // COPY OF SECTION
        }

        else if (sorting.equals("Сначала дешевые")) {
            sortedItemsMap = YamlUtils.sortingMapByKey(itemsMap, "Price", false);

            items_sorted = HashMapUtils.mapToSection(sortedItemsMap);
        }

        else if (sorting.equals("Сначала дорогие")) {
            sortedItemsMap = YamlUtils.sortingMapByKey(itemsMap, "Price", true);

            items_sorted = HashMapUtils.mapToSection(sortedItemsMap);
        }

        ConfigurationSection itemData = items_sorted.getConfigurationSection(sortedItemsMap.keySet().toArray()[position - 1].toString());
        return itemData;
    }
}

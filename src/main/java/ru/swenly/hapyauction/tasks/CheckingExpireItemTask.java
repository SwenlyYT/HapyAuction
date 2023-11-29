package ru.swenly.hapyauction.tasks;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import ru.swenly.hapyauction.HapyAuction;
import ru.swenly.hapyauction.gui.AuctionItemsGUI;
import ru.swenly.hapyauction.utils.AuctionUtils;
import ru.swenly.hapyauction.utils.HashMapUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.LinkedHashMap;
import java.util.Map;

public class CheckingExpireItemTask extends BukkitRunnable {
    private final JavaPlugin plugin;

    public CheckingExpireItemTask(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        File plugin_path = plugin.getDataFolder();
        File file = new File(plugin_path + "/categories/" + "all_items.yml");
        Long nowTimestamp = System.currentTimeMillis();
        Integer needTime = 172800 * 1000;

        YamlConfiguration yamlConfiguration = new YamlConfiguration();

        try {
            yamlConfiguration.load(file);
        } catch (Exception exception) {
            exception.printStackTrace();
            return;
        }

        ConfigurationSection items = yamlConfiguration.getConfigurationSection("Items");
        ConfigurationSection items_sorted;

        Map<String, Object> itemsMap = items.getValues(false);
        for (Map.Entry<String, Object> entry : itemsMap.entrySet()) {
            String itemID = entry.getKey();
            ConfigurationSection itemData = (ConfigurationSection) entry.getValue();

            Long itemTimestamp = itemData.getLong("Timestamp");
            if ((itemTimestamp + needTime) <= nowTimestamp) {
                items_sorted = HashMapUtils.deleteValueInSectionByMap(itemsMap, itemData);

                OfflinePlayer itemSeller = Bukkit.getOfflinePlayer(itemData.get("Seller").toString());

                File expired_file = new File(plugin_path + "/expired/" + itemSeller.getName() + "/all_items.yml");

                try {
                    if (!expired_file.exists()) {
                        expired_file.getParentFile().mkdir();
                        expired_file.createNewFile();

                        for (String category : HapyAuction.categoriesMap.values()) {
                            File category_file = new File(plugin_path + "/expired/" + itemSeller.getName() + "/" + category + ".yml");

                            try {
                                if (!category_file.exists()) {
                                    category_file.getParentFile().mkdir();
                                    category_file.createNewFile();
                                }
                            } catch (Exception exception) {
                                System.out.println("Can't create " + category + ".yml");
                            }

                            YamlConfiguration categoryYamlConfiguration = new YamlConfiguration();
                            categoryYamlConfiguration.createSection("Items");

                            FileWriter fileWriter = new FileWriter(category_file);
                            BufferedWriter bw = new BufferedWriter(fileWriter);
                            bw.write(categoryYamlConfiguration.saveToString());
                            bw.flush();
                            bw.close();
                        }
                    }
                }
                catch (Exception exception) {
                    exception.printStackTrace();
                }

                try {
                    yamlConfiguration.set("Items", items_sorted);
                    yamlConfiguration.save(file);

                    File all_items_expired = new File(plugin_path + "/expired/" + itemSeller.getName() + "/all_items.yml");

                    yamlConfiguration = new YamlConfiguration();
                    yamlConfiguration.load(all_items_expired);

                    items = yamlConfiguration.getConfigurationSection("Items");

                    String strIndex = String.valueOf(items.getKeys(false).size() + 1);
                    items.set(strIndex, itemData);

                    yamlConfiguration.set("Items", items);
                    yamlConfiguration.save(all_items_expired);
                } catch (Exception exception) {
                    exception.printStackTrace();
                    return;
                }

                try {
                    ItemStack itemStack = itemData.getItemStack("Item");

                    file = new File(plugin_path + "/categories/" + HapyAuction.categoriesMap.get(AuctionUtils.getCategory(itemStack)) + ".yml");
                    yamlConfiguration = new YamlConfiguration();
                    yamlConfiguration.load(file);
                    items = yamlConfiguration.getConfigurationSection("Items");

                    itemsMap = (LinkedHashMap<String, Object>) items.getValues(false);
                    items_sorted = HashMapUtils.deleteValueInSectionByMap(itemsMap, itemData);
                    // sortedItemsMap = YamlUtils.sortingMapByKey(itemsMap, "Timestamp", true);

                    yamlConfiguration.set("Items", items_sorted);
                    yamlConfiguration.save(file);

                    file = new File(plugin_path + "/expired/" + itemSeller.getName() + "/" + HapyAuction.categoriesMap.get(AuctionUtils.getCategory(itemStack)) + ".yml");
                    yamlConfiguration = new YamlConfiguration();
                    yamlConfiguration.load(file);

                    items = yamlConfiguration.getConfigurationSection("Items");

                    String strIndex = String.valueOf(items.getKeys(false).size() + 1);
                    items.set(strIndex, itemData);

                    yamlConfiguration.set("Items", items);
                    yamlConfiguration.save(file);
                }
                catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        }
    }
}

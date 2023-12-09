package ru.swenly.hapyauction;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import ru.swenly.hapyauction.commands.AuctionCMD;
import ru.swenly.hapyauction.config.ConfigSystem;
import ru.swenly.hapyauction.config.PlacedHolders;
import ru.swenly.hapyauction.gui.*;
import ru.swenly.hapyauction.tasks.CheckingExpireItemTask;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

public final class HapyAuction extends JavaPlugin {
    public static File plugin_path;
    public static Map<String, String> categoriesMap = new LinkedHashMap<>();
    public static String[] sortingList = {"Сначала новые", "Сначала старые", "Сначала дешевые", "Сначала дорогие"};
    public static FileConfiguration config;
    public static BukkitTask expireTask;

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin_path = HapyAuction.getPlugin(HapyAuction.class).getDataFolder();

        // Commands
        getCommand("ah").setExecutor(new AuctionCMD());

        // Events
        Bukkit.getPluginManager().registerEvents(new AuctionItemsGUI(), this);
        Bukkit.getPluginManager().registerEvents(new ConfirmGUI(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerSalesGUI(), this);
        Bukkit.getPluginManager().registerEvents(new HistoryChangerGUI(), this);
        Bukkit.getPluginManager().registerEvents(new ExpiredHistoryGUI(), this);
        Bukkit.getPluginManager().registerEvents(new SalesHistoryGUI(), this);
        Bukkit.getPluginManager().registerEvents(new PurchasesHistoryGUI(), this);

        // Tasks
        expireTask = new CheckingExpireItemTask(this).runTaskTimer(this, 0L, 20L * 600); // 20L = 1 sec

        // Config
        this.saveDefaultConfig();
        config = this.getConfig();

        ConfigSystem.loadConfig(config);
        PlacedHolders.loadConfig(config);
        PlacedHolders.addPlaceholder("&", "§");

        // Categories
        categoriesMap.put("Все предметы", "all_items");
        categoriesMap.put("Броня", "armor");
        categoriesMap.put("Оружие", "weapons");
        categoriesMap.put("Инструменты", "tools");
        categoriesMap.put("Еда", "food");
        categoriesMap.put("Зелья", "potions");
        categoriesMap.put("Блоки", "blocks");
        categoriesMap.put("Другое", "others");

        for (String category : categoriesMap.values()) {
            File category_file = new File(plugin_path + "/categories/" + category + ".yml");

            if (!category_file.exists()) {
                category_file.getParentFile().mkdir();

                try {
                    category_file.createNewFile();

                    YamlConfiguration yamlConfiguration = new YamlConfiguration();
                    yamlConfiguration.createSection("Items");

                    FileWriter fileWriter = new FileWriter(category_file);
                    BufferedWriter bw = new BufferedWriter(fileWriter);
                    bw.write(yamlConfiguration.saveToString());
                    bw.flush();
                    bw.close();
                } catch (Exception exception) {
                    System.out.println("Can't create " + category + ".yml");
                }
            }
        }

        File expired_folder = new File(plugin_path + "/expired/");

        if (!expired_folder.exists()) {
            try {
                Files.createDirectories(Paths.get(plugin_path + "/expired/"));
            } catch (Exception exception) {
                System.out.println("Can't create expired folder");
            }
        }

        File history_folder = new File(plugin_path + "/history/");

        if (!history_folder.exists()) {
            try {
                Files.createDirectories(Paths.get(plugin_path + "/history/"));
            } catch (Exception exception) {
                System.out.println("Can't create expired folder");
            }
        }

        File sales_folder = new File(plugin_path + "/history/sales/");

        if (!sales_folder.exists()) {
            try {
                Files.createDirectories(Paths.get(plugin_path + "/history/sales/"));
            } catch (Exception exception) {
                System.out.println("Can't create sales folder");
            }
        }

        File buys_folder = new File(plugin_path + "/history/purchases/");

        if (!buys_folder.exists()) {
            try {
                Files.createDirectories(Paths.get(plugin_path + "/history/purchases/"));
            } catch (Exception exception) {
                System.out.println("Can't create buys folder");
            }
        }
    }

    @Override
    public void onDisable() {
        expireTask.cancel();
    }
}

package ru.swenly.hapyauction.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.swenly.hapyauction.HapyAuction;

import java.io.File;

public class ConfigSystem {
    public static FileConfiguration config;

    public static void loadConfig(FileConfiguration fileConfiguration) {
        config = fileConfiguration;
    }

    public static void reloadConfig() {
        File plugin_path = HapyAuction.getPlugin(HapyAuction.class).getDataFolder();

        config = new YamlConfiguration();

        try {
            config.load(new File(plugin_path + "/config.yml"));
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static FileConfiguration getConfig() {
        return config;
    }
}

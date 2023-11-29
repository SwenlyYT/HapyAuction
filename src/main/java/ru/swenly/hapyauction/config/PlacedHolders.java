package ru.swenly.hapyauction.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class PlacedHolders {
    public static FileConfiguration config;
    public static Map<String, String> placeholders = new HashMap<>();

    public static void loadConfig(FileConfiguration fileConfiguration) {
        config = fileConfiguration;
    }

    public static void addPlaceholder(String key, Object value) {
        placeholders.put(key, value.toString());
    }

    public static void clearPlaceholders() {
        placeholders.clear();
    }

    public static String formatText(String text) {
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            text = text.replace(entry.getKey(), entry.getValue());
        }

        text = text.replace("&", "§");
        return text;
    }
}

package ru.swenly.hapyauction.utils;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.Collections;

public class HashMapUtils {
    public static <T, E> Set<T> getKeysByValue(Map<T, E> map, E value) {
        Set<T> keys = new HashSet<T>();
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                keys.add(entry.getKey());
            }
        }

        return keys;
    }

    public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }

        return null;
    }

    public static Map<String, Object> reverseKeys(Map<String, Object> in) {
        Map<String, Object> out = new LinkedHashMap<String, Object>();
        List<String> keys = new ArrayList<>(in.keySet());
        List<Object> values = new ArrayList<>(in.values());

        for (int i = in.size() - 1; i >= 0; i--) {
            out.put(keys.get(i), values.get(i));
        }

        return out;
    }

    public static Boolean containsValueInSectionByMap(Map<String, Object> map, ConfigurationSection needValue) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            ConfigurationSection entrySection = (ConfigurationSection) entry.getValue();

            if (Objects.equals(entrySection.getString("Seller"), needValue.getString("Seller")) && Objects.equals(entrySection.getItemStack("Item"), needValue.getItemStack("Item")) && Objects.equals(entrySection.get("Timestamp"), needValue.get("Timestamp"))) {
                return true;
            }
        }

        return false;
    }

    public static ConfigurationSection deleteValueInSectionByMap(Map<String, Object> map, ConfigurationSection needValue) {
        Map<String, Object> newItems = new LinkedHashMap<>();
        Map<String, Object> finalItems = new LinkedHashMap<>();
        Object firstKey = map.keySet().toArray()[0];
        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        ConfigurationSection items = yamlConfiguration.createSection("Items");
        String keyForNeedValue = "";
        Integer i = 0;

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            ConfigurationSection entrySection = (ConfigurationSection) entry.getValue();

            if (Objects.equals(entrySection.getString("Seller"), needValue.getString("Seller")) && Objects.equals(entrySection.getItemStack("Item"), needValue.getItemStack("Item")) && Objects.equals(entrySection.get("Timestamp"), (needValue.get("Timestamp")))) {
                System.out.println(entry.getKey() + " is needed!");
                keyForNeedValue = entry.getKey();
            }
            else {
                System.out.println(entrySection.get("Timestamp") + " is not " + needValue);
                newItems.put(entry.getKey(), entrySection);
            }
        }

        for (i = 1; i <= newItems.size() + 1; i++) {
            ConfigurationSection section = (ConfigurationSection) newItems.get(i + "");

            if (Integer.parseInt(keyForNeedValue) > i) {
                items.set(i + "", section);
                System.out.println("Added " + i + " with value " + section);
            }
            else if (Integer.parseInt(keyForNeedValue) == i) System.out.println(i + " skipped!");
            else {
                items.set(i - 1 + "", section);
                System.out.println("Added " + (i - 1) + " with value " + section);
            }
        }

        System.out.println(finalItems.get("1"));
        return items;
    }

    public static ConfigurationSection replaceValueInSectionByMap(Map<String, Object> map, ConfigurationSection needValue, ConfigurationSection newValue) {
        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        ConfigurationSection items = yamlConfiguration.createSection("Items");
        Map<String, Object> newItems = new HashMap<>();
        String keyForNeedValue = (String) needValue.getParent().getKeys(false).toArray()[0];

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            ConfigurationSection entrySection = (ConfigurationSection) entry.getValue();

            if (entrySection.getString("Seller").equals(needValue.getString("Seller")) && entrySection.get("Timestamp").equals(needValue.get("Timestamp"))) {
                System.out.println(entry.getKey() + " is needed!");
                keyForNeedValue = entry.getKey();
            }
            else {
                newItems.put(entry.getKey(), entrySection);
            }
        }

        System.out.println(keyForNeedValue + " is key");
        for (int i = 1; i <= map.size() + 1; i++) {
            ConfigurationSection section = (ConfigurationSection) newItems.get(i + "");

            if (Integer.parseInt(keyForNeedValue) == i) {
                items.set(i + "", newValue);
            }
            else {
                items.set(i + "", section);
            }
        }

        return items;
    }

    public static ConfigurationSection mapToSection(Map<String, Object> map) {
        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        ConfigurationSection itemsSorted = yamlConfiguration.createSection("Items");

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            ConfigurationSection configurationSection = (ConfigurationSection) entry.getValue();
            ConfigurationSection entrySection = itemsSorted.createSection(entry.getKey());

            for (Map.Entry<String, Object> sortedEntry : configurationSection.getValues(false).entrySet()) {
                entrySection.addDefault(sortedEntry.getKey(), sortedEntry.getValue());
            }
        }

        return itemsSorted;
    }

    public static Map<String, Object> sortingMapByDoubleValue (Map<String, Object> in) {
        Map<String, Object> out = new LinkedHashMap<>();
        Set<String> keySet = in.keySet();
        List<String> keys = new ArrayList<>(keySet);

        Collections.sort(keys, new Comparator<String>() {

            public int compare(String a, String b) {
                Double valA = 0.0;
                Double valB = 0.0;

                try {
                    valA = (Double) in.get(a);
                    valB = (Double) in.get(b);
                } catch (Exception ignored) {
                    //do something
                }

                return valA.compareTo(valB);
            }
        });

        for (String key : keys) {
            out.put(key, in.get(key));
        }

        return out;
    }
}

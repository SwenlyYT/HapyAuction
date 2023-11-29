package ru.swenly.hapyauction.utils;

import org.bukkit.configuration.ConfigurationSection;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;

public class YamlUtils {
    // Not my
    public static JSONArray sortJsonArrayByValue(JSONArray jsonArray, String keyName, Boolean up) {
        JSONArray sortedJsonArray = new JSONArray();
        List<JSONObject> jsonList = new ArrayList<JSONObject>();
        for (int i = 0; i < jsonArray.size(); i++) {
            jsonList.add((JSONObject) jsonArray.get(i));
        }

        Collections.sort( jsonList, new Comparator<JSONObject>() {

            public int compare(JSONObject a, JSONObject b) {
                Double valA = 0.0;
                Double valB = 0.0;

                try {
                    valA = (Double) a.get(keyName);
                    valB = (Double) b.get(keyName);
                }
                catch (Exception ignored) {
                    //do something
                }

                if (up) {
                    return valA.compareTo(valB);
                }
                else {
                    return -valA.compareTo(valB);
                }
            }
        });

        for (int i = 0; i < jsonArray.size(); i++) {
            sortedJsonArray.add(jsonList.get(i));
        }

        return sortedJsonArray;
    }

    public static Map<String, Object> sortingMapByKey (Map<String, Object> in, String keyName, Boolean reverse) {
        Map<String, Object> out = new LinkedHashMap<>();
        Set<String> keySet = in.keySet();
        List<String> keys = new ArrayList<>(keySet);

        Collections.sort( keys, new Comparator<String>() {

            public int compare(String a, String b) {
                Double valA = 0.0;
                Double valB = 0.0;

                try {
                    valA = (Double) ((ConfigurationSection) in.get(a)).get(keyName);
                    valB = (Double) ((ConfigurationSection) in.get(b)).get(keyName);
                }
                catch (Exception ignored) {
                    //do something
                }

                return valA.compareTo(valB);
            }
        });

        for (String key : keys) {
            System.out.println("sorting with key " + key + " and with price: " + ((ConfigurationSection) in.get(key)).get(keyName));
            out.put(key, in.get(key));
        }

        if (reverse) {
            out = HashMapUtils.reverseKeys(out);
        }

        return out;
    }


}

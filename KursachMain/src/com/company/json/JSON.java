package com.company.json;

import java.util.HashMap;
import java.util.Map;

public class JSON {

    Map<String, String> json;

    public JSON() {
        this.json = new HashMap<>();
    }

    public JSON(String jsonData) {
        this.json = new HashMap<>();
        for (String tuple : jsonData.substring(1, jsonData.length() - 1).split(", ")) {
            final String[] data = tuple.split("=");
            json.put(data[0], data[1]);
        }
    }

    public void addTypeContent(String typeFieldData) {
        json.put(JSONConstants.Type, typeFieldData);
    }

    public void addBodyContent(String key, String value) {
        json.put(key, value);
    }

    public String getTypeValue() {
        return json.getOrDefault(JSONConstants.Type, null);
    }

    public String getValueForKey(String key) {
        return json.getOrDefault(key, null);
    }

    public String getStringRepresentation() {
        return json.toString();
    }
}
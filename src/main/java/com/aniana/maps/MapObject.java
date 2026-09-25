package com.aniana.maps;

import java.util.HashMap;
import java.util.Map;


public class MapObject {
    public String name   = "";
    public String type   = "";
    public int x, y, width, height;
    public Map<String, String> properties = new HashMap<>();

    public String getProp(String key) { return properties.getOrDefault(key, ""); }

    @Override
    public String toString() {
        return "MapObject{name='" + name + "', type='" + type + "', x=" + x + ", y=" + y + "}";
    }
}

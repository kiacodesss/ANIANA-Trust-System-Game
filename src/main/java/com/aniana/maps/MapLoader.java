package com.aniana.maps;

import com.aniana.core.GameWindow;
import com.aniana.utils.JsonParser;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class MapLoader {

    public static final int TILE_SIZE = GameWindow.TILE_SIZE;
    private static final int TILESET_MAX = 192; 

    public int width, height;
    public int[]     groundLayer;
    public int[]     collisionLayer;  
    public boolean[] collisionSolid;   
    public int[]     decorationLayer;
    public int[]     structureLayer;
    public List<MapObject> objects = new ArrayList<>();

   
    private int tilesheetFirstGid = 1;

    public void load(String resourcePath) {
        try {
            InputStream is = getClass().getResourceAsStream(resourcePath);
            if (is == null) throw new RuntimeException("Map not found: " + resourcePath);
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            is.close();

            Map<String, Object> root = JsonParser.asObject(JsonParser.parse(json));
            width  = JsonParser.asInt(root.get("width"));
            height = JsonParser.asInt(root.get("height"));

           
            tilesheetFirstGid = findTilesheetFirstGid(root);
            System.out.println("[MapLoader] " + resourcePath +
                    " -> tilesheet firstgid = " + tilesheetFirstGid);

           
            for (Object layerObj : JsonParser.asArray(root.get("layers"))) {
                Map<String, Object> layer = JsonParser.asObject(layerObj);
                String name = JsonParser.asString(layer.get("name"));
                String type = JsonParser.asString(layer.get("type"));

                if ("tilelayer".equals(type)) {
                    List<Object> rawData = JsonParser.asArray(layer.get("data"));
                    switch (name) {
                        case "Ground Layer"    -> groundLayer    = parseRenderData(rawData);
                        case "Collision Layer" -> {
                            collisionLayer = parseRenderData(rawData);
                            collisionSolid = parseSolidData(rawData);
                        }
                        case "Decoration Layer" -> decorationLayer = parseRenderData(rawData);
                        case "Structure Layer"  -> structureLayer  = parseRenderData(rawData);
                    }
                } else if ("objectgroup".equals(type)) {
                    Object objs = layer.get("objects");
                    if (objs != null) parseObjects(JsonParser.asArray(objs));
                }
            }
           
            int size = width * height;
            if (groundLayer     == null) groundLayer     = emptyLayer(size);
            if (collisionLayer  == null) collisionLayer  = emptyLayer(size);
            if (collisionSolid  == null) collisionSolid  = new boolean[size];
            if (decorationLayer == null) decorationLayer = emptyLayer(size);
            if (structureLayer  == null) structureLayer  = emptyLayer(size);

        } catch (Exception e) {
            throw new RuntimeException("Failed to load map: " + resourcePath, e);
        }
    }

    
    private static int[] emptyLayer(int size) {
        int[] arr = new int[size];
        java.util.Arrays.fill(arr, -1);
        return arr;
    }

  
    private int findTilesheetFirstGid(Map<String, Object> root) {
        if (!root.containsKey("tilesets")) return 1;
        List<Object> tilesets = JsonParser.asArray(root.get("tilesets"));
        int best = 1;
        boolean found = false;
        for (Object tsObj : tilesets) {
            Map<String, Object> ts = JsonParser.asObject(tsObj);
            String src = JsonParser.asString(ts.getOrDefault("source", ""));         
            String filename = src.contains("/") ? src.substring(src.lastIndexOf('/') + 1) : src;
            filename = filename.contains("\\") ? filename.substring(filename.lastIndexOf('\\') + 1) : filename;            
            if (filename.equals("tilesheet.tsx")) {
                int fgid = JsonParser.asInt(ts.get("firstgid"));
                if (!found || fgid > best) {
                    best  = fgid;
                    found = true;
                }
            }
        }
        if (!found) {
            System.err.println("[MapLoader] WARNING: tilesheet.tsx not found in tilesets, using firstgid=1");
        }
        return best;
    }

   
    private int[] parseRenderData(List<Object> arr) {
        int[] data = new int[arr.size()];
        for (int i = 0; i < arr.size(); i++) {
            int gid = JsonParser.asInt(arr.get(i));
            if (gid == 0 || gid < tilesheetFirstGid) {
                data[i] = -1; 
            } else {
                int idx = gid - tilesheetFirstGid; 
                data[i] = (idx >= 0 && idx < TILESET_MAX) ? idx : -1;
            }
        }
        return data;
    }

    
    private boolean[] parseSolidData(List<Object> arr) {
        boolean[] solid = new boolean[arr.size()];
        for (int i = 0; i < arr.size(); i++) {
            solid[i] = JsonParser.asInt(arr.get(i)) != 0;
        }
        return solid;
    }

    private void parseObjects(List<Object> arr) {
        if (arr == null) return;
        for (Object o : arr) {
            Map<String, Object> obj = JsonParser.asObject(o);
            MapObject mo = new MapObject();
            mo.name   = JsonParser.asString(obj.get("name"));
            mo.type   = JsonParser.asString(obj.getOrDefault("type", ""));
            mo.x      = (int) JsonParser.asDouble(obj.get("x"));
            mo.y      = (int) JsonParser.asDouble(obj.get("y"));
            mo.width  = obj.containsKey("width")  ? (int) JsonParser.asDouble(obj.get("width"))  : 32;
            mo.height = obj.containsKey("height") ? (int) JsonParser.asDouble(obj.get("height")) : 32;

            if (obj.containsKey("properties")) {
                for (Object p : JsonParser.asArray(obj.get("properties"))) {
                    Map<String, Object> prop = JsonParser.asObject(p);
                    mo.properties.put(JsonParser.asString(prop.get("name")),
                                      JsonParser.asString(prop.getOrDefault("value", "")));
                }
            }
            objects.add(mo);
        }
    }

    
    public boolean isSolid(int col, int row) {
        if (col < 0 || col >= width || row < 0 || row >= height) return true;
        if (collisionSolid == null) return false;
        return collisionSolid[row * width + col];
    }

    public MapObject findObject(String name) {
        return objects.stream().filter(o ->
                name.equals(o.name) || name.equals(o.properties.get("name"))
        ).findFirst().orElse(null);
    }

    public List<MapObject> findObjectsByType(String type) {
        return objects.stream().filter(o -> type.equals(o.type)).toList();
    }
}

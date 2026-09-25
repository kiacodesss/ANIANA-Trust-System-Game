package com.aniana.utils;

import java.util.*;


public class JsonParser {

    private final String src;
    private int pos = 0;

    public JsonParser(String json) {
        this.src = json;
    }

    public static Object parse(String json) {
        return new JsonParser(json.trim()).parseValue();
    }

    private Object parseValue() {
        skipWhitespace();
        char c = peek();
        if (c == '{') return parseObject();
        if (c == '[') return parseArray();
        if (c == '"') return parseString();
        if (c == 't' || c == 'f') return parseBoolean();
        if (c == 'n') { pos += 4; return null; }
        return parseNumber();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> asObject(Object o) { return (Map<String, Object>) o; }
    @SuppressWarnings("unchecked")
    public static List<Object> asArray(Object o) { return (List<Object>) o; }
    public static String asString(Object o) { return o == null ? "" : o.toString(); }
    public static int asInt(Object o) {
        if (o instanceof Number n) return n.intValue();
        return Integer.parseInt(o.toString());
    }
    public static double asDouble(Object o) {
        if (o instanceof Number n) return n.doubleValue();
        return Double.parseDouble(o.toString());
    }

    private Map<String, Object> parseObject() {
        consume('{');
        Map<String, Object> map = new LinkedHashMap<>();
        skipWhitespace();
        if (peek() == '}') { pos++; return map; }
        while (true) {
            skipWhitespace();
            String key = parseString();
            skipWhitespace();
            consume(':');
            skipWhitespace();
            Object val = parseValue();
            map.put(key, val);
            skipWhitespace();
            if (peek() == '}') { pos++; break; }
            consume(',');
        }
        return map;
    }

    private List<Object> parseArray() {
        consume('[');
        List<Object> list = new ArrayList<>();
        skipWhitespace();
        if (peek() == ']') { pos++; return list; }
        while (true) {
            skipWhitespace();
            list.add(parseValue());
            skipWhitespace();
            if (peek() == ']') { pos++; break; }
            consume(',');
        }
        return list;
    }

    private String parseString() {
        consume('"');
        StringBuilder sb = new StringBuilder();
        while (pos < src.length()) {
            char c = src.charAt(pos++);
            if (c == '"') break;
            if (c == '\\') {
                char esc = src.charAt(pos++);
                switch (esc) {
                    case 'n' -> sb.append('\n');
                    case 't' -> sb.append('\t');
                    case 'r' -> sb.append('\r');
                    case '"' -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    case '/' -> sb.append('/');
                    default  -> { sb.append('\\'); sb.append(esc); }
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private Number parseNumber() {
        int start = pos;
        if (peek() == '-') pos++;
        while (pos < src.length() && (Character.isDigit(src.charAt(pos)) || src.charAt(pos) == '.')) pos++;
        String num = src.substring(start, pos);
        if (num.contains(".")) return Double.parseDouble(num);
        return Long.parseLong(num);
    }

    private boolean parseBoolean() {
        if (src.startsWith("true", pos))  { pos += 4; return true; }
        if (src.startsWith("false", pos)) { pos += 5; return false; }
        throw new RuntimeException("Invalid boolean at " + pos);
    }

    private char peek() { return pos < src.length() ? src.charAt(pos) : 0; }
    private void consume(char c) {
        if (src.charAt(pos) != c) throw new RuntimeException("Expected '" + c + "' at " + pos + " but got '" + src.charAt(pos) + "'");
        pos++;
    }
    private void skipWhitespace() {
        while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) pos++;
    }
}

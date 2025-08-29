package ir.mrstudios.databasecloner.models;

import lombok.SneakyThrows;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YamlConfig {
    private final HashMap<String, Object> config;

    @SneakyThrows
    public YamlConfig(File file) {
        final Yaml yaml = new Yaml();
        final InputStream inputStream = new FileInputStream(file);
        this.config = yaml.load(inputStream);
    }

    @SuppressWarnings("unchecked")
    public Object get(String path) {
        Map<String, Object> currentMap = config;
        Object value = null;

        for (String key : path.split("\\.")) {
            value = currentMap.get(key);
            if (value instanceof Map) {
                currentMap = (Map<String, Object>) value;
            } else {
                break;
            }
        }

        return value;
    }

    public String getString(String path) {
        final Object value = get(path);
        return value != null ? value.toString() : null;
    }

    public Integer getInt(String path) {
        final Object value = get(path);
        return value instanceof Number ? ((Number) value).intValue() : null;
    }

    public Long getLong(String path) {
        Object value = get(path);
        return value instanceof Number ? ((Number) value).longValue() : null;
    }

    public Float getFloat(String path) {
        Object value = get(path);
        return value instanceof Number ? ((Number) value).floatValue() : null;
    }

    public Boolean getBoolean(String path) {
        Object value = get(path);
        return value instanceof Boolean ? (Boolean) value : null;
    }

    public List<?> getList(String path) {
        Object value = get(path);
        return value instanceof List ? (List<?>) value : null;
    }

    public Map<?, ?> getMap(String path) {
        Object value = get(path);
        return value instanceof Map ? (Map<?, ?>) value : null;
    }

    @SuppressWarnings("unchecked")
    public List<String> getStringList(String path) {
        Object value = get(path);
        if (!(value instanceof List<?> list)) return Collections.emptyList();
        if (list.isEmpty() || !(list.get(0) instanceof String)) return Collections.emptyList();
        return (List<String>) list;
    }
}


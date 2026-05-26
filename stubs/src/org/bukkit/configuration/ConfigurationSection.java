package org.bukkit.configuration;
import java.util.*;
public interface ConfigurationSection {
    Set<String> getKeys(boolean deep);
    String getString(String path);
    String getString(String path, String def);
    int getInt(String path);
    int getInt(String path, int def);
    double getDouble(String path);
    double getDouble(String path, double def);
    boolean getBoolean(String path);
    boolean getBoolean(String path, boolean def);
    List<String> getStringList(String path);
    List<Map<?,?>> getMapList(String path);
    ConfigurationSection getConfigurationSection(String path);
    ConfigurationSection createSection(String path);
    void set(String path, Object value);
    boolean contains(String path);
}

package org.bukkit.configuration.file;
import org.bukkit.configuration.ConfigurationSection;
import java.io.File;
import java.io.IOException;
import java.util.*;
public abstract class FileConfiguration implements ConfigurationSection {
    public void save(File f) throws IOException {}
    public void save(java.nio.file.Path p) throws IOException {}
    @Override public Set<String> getKeys(boolean deep){return Collections.emptySet();}
    @Override public String getString(String p){return null;}
    @Override public String getString(String p, String d){return d;}
    @Override public int getInt(String p){return 0;}
    @Override public int getInt(String p, int d){return d;}
    @Override public double getDouble(String p){return 0;}
    @Override public double getDouble(String p, double d){return d;}
    @Override public boolean getBoolean(String p){return false;}
    @Override public boolean getBoolean(String p, boolean d){return d;}
    @Override public List<String> getStringList(String p){return Collections.emptyList();}
    @Override public List<Map<?,?>> getMapList(String p){return Collections.emptyList();}
    @Override public ConfigurationSection getConfigurationSection(String p){return null;}
    @Override public ConfigurationSection createSection(String p){return null;}
    @Override public void set(String p, Object v){}
    @Override public boolean contains(String p){return false;}
}

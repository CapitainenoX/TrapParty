package org.bukkit.plugin;
import java.io.File;
import java.io.InputStream;
import java.util.logging.Logger;
import org.bukkit.Server;
public interface Plugin {
    String getName();
    File getDataFolder();
    Logger getLogger();
    boolean isEnabled();
    PluginDescriptionFile getDescription();
    Server getServer();
    org.bukkit.command.PluginCommand getCommand(String name);
    void saveDefaultConfig();
    void reloadConfig();
    org.bukkit.configuration.file.FileConfiguration getConfig();
    void saveConfig();
    void saveResource(String name, boolean replace);
    InputStream getResource(String name);
}

package org.bukkit.plugin.java;
import org.bukkit.plugin.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.command.PluginCommand;
import java.io.File;
import java.io.InputStream;
import java.util.logging.Logger;
import org.bukkit.Server;
public abstract class JavaPlugin implements Plugin {
    public void onEnable(){}
    public void onDisable(){}
    @Override public String getName(){return "";}
    @Override public File getDataFolder(){return new File(".");}
    @Override public Logger getLogger(){return Logger.getLogger("stub");}
    @Override public boolean isEnabled(){return true;}
    @Override public PluginDescriptionFile getDescription(){return new PluginDescriptionFile();}
    @Override public Server getServer(){return null;}
    @Override public PluginCommand getCommand(String name){return null;}
    @Override public void saveDefaultConfig(){}
    @Override public void reloadConfig(){}
    @Override public FileConfiguration getConfig(){return null;}
    @Override public void saveConfig(){}
    @Override public void saveResource(String name, boolean replace){}
    @Override public InputStream getResource(String name){return null;}
}

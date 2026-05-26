package org.bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import java.io.File;
import java.util.*;
public final class Bukkit {
    private Bukkit(){}
    public static Server getServer(){return null;}
    public static PluginManager getPluginManager(){return null;}
    public static ServicesManager getServicesManager(){return null;}
    public static BukkitScheduler getScheduler(){return null;}
    public static ScoreboardManager getScoreboardManager(){return null;}
    public static Player getPlayer(UUID uuid){return null;}
    public static Player getPlayer(String name){return null;}
    public static Player getPlayerExact(String name){return null;}
    public static OfflinePlayer getOfflinePlayer(UUID uuid){return null;}
    public static OfflinePlayer getOfflinePlayer(String name){return null;}
    public static Collection<? extends Player> getOnlinePlayers(){return Collections.emptyList();}
    public static World getWorld(String name){return null;}
    public static World getWorld(UUID uid){return null;}
    public static List<World> getWorlds(){return Collections.emptyList();}
    public static File getWorldContainer(){return null;}
    public static boolean unloadWorld(World w, boolean save){return false;}
    public static boolean unloadWorld(String name, boolean save){return false;}
    public static Inventory createInventory(InventoryHolder owner, int size, String title){return null;}
    public static String getBukkitVersion(){return "26.1.2-R0.1-SNAPSHOT";}
    public static BossBar createBossBar(String title, BarColor color, BarStyle style){return null;}
    public static void broadcastMessage(String msg){}
    public static Entity getEntity(UUID uuid){return null;}
}

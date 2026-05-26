package org.bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.entity.Entity;
import java.util.*;
public interface Server {
    PluginManager getPluginManager();
    ServicesManager getServicesManager();
    BukkitScheduler getScheduler();
    Player getPlayer(UUID uuid);
    Player getPlayerExact(String name);
    Collection<? extends Player> getOnlinePlayers();
    List<World> getWorlds();
    void broadcastMessage(String msg);
    Entity getEntity(UUID uuid);
}

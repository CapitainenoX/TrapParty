package org.bukkit.scheduler;
import org.bukkit.plugin.Plugin;
public abstract class BukkitRunnable implements Runnable {
    public BukkitTask runTask(Plugin plugin){return null;}
    public BukkitTask runTaskAsynchronously(Plugin plugin){return null;}
    public BukkitTask runTaskTimer(Plugin plugin, long delay, long period){return null;}
    public BukkitTask runTaskLater(Plugin plugin, long delay){return null;}
    public BukkitTask runTaskLaterAsynchronously(Plugin plugin, long delay){return null;}
    public void cancel(){}
}

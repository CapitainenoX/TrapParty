package org.bukkit.scheduler;
import org.bukkit.plugin.Plugin;
public interface BukkitScheduler {
    BukkitTask runTask(Plugin plugin, Runnable r);
    BukkitTask runTaskAsynchronously(Plugin plugin, Runnable r);
    BukkitTask runTaskLater(Plugin plugin, Runnable r, long delay);
    BukkitTask runTaskLaterAsynchronously(Plugin plugin, Runnable r, long delay);
    BukkitTask runTaskTimer(Plugin plugin, Runnable r, long delay, long period);
    BukkitTask runTaskTimerAsynchronously(Plugin plugin, Runnable r, long delay, long period);
}

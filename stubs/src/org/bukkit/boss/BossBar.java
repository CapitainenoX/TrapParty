package org.bukkit.boss;
import org.bukkit.entity.Player;
public interface BossBar {
    void setTitle(String t);
    void setColor(BarColor c);
    void setProgress(double p);
    void addPlayer(Player p);
    void removePlayer(Player p);
    void removeAll();
}

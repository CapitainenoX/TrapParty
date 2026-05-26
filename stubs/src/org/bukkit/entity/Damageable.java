package org.bukkit.entity;
public interface Damageable extends Entity {
    double getHealth();
    void setHealth(double v);
    double getMaxHealth();
    void setMaxHealth(double v);
    Player getKiller();
}

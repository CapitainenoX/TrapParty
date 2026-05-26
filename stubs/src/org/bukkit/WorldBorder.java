package org.bukkit;
public interface WorldBorder {
    void setCenter(Location loc);
    void setSize(double size);
    void setSize(double size, long seconds);
    void setDamageBuffer(double n);
    void setDamageAmount(double n);
    void setWarningDistance(int d);
}

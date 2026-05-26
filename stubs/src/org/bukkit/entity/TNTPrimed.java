package org.bukkit.entity;
public interface TNTPrimed extends Entity {
    void setFuseTicks(int t);
    void setYield(float y);
    void setSource(Entity src);
}

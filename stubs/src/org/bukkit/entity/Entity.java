package org.bukkit.entity;
import org.bukkit.Location;
import org.bukkit.World;
import java.util.*;
public interface Entity {
    UUID getUniqueId();
    Location getLocation();
    World getWorld();
    void remove();
    Collection<Entity> getNearbyEntities(double x, double y, double z);
    void setVelocity(org.bukkit.util.Vector v);
    org.bukkit.util.Vector getVelocity();
    void teleport(Location loc);
    void setFireTicks(int t);
}

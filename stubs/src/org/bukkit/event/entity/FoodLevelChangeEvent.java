package org.bukkit.event.entity;
import org.bukkit.event.Cancellable;
public class FoodLevelChangeEvent extends EntityEvent implements Cancellable {
    @Override public boolean isCancelled(){return false;}
    @Override public void setCancelled(boolean c){}
}

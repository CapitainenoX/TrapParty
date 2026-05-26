package org.bukkit.event.player;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
public class PlayerMoveEvent extends PlayerEvent implements Cancellable {
    public Location getFrom(){return null;}
    public Location getTo(){return null;}
    @Override public boolean isCancelled(){return false;}
    @Override public void setCancelled(boolean c){}
}

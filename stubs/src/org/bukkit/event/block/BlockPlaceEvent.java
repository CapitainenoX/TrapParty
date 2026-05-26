package org.bukkit.event.block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
public class BlockPlaceEvent extends BlockEvent implements Cancellable {
    public Player getPlayer(){return null;}
    @Override public boolean isCancelled(){return false;}
    @Override public void setCancelled(boolean c){}
}

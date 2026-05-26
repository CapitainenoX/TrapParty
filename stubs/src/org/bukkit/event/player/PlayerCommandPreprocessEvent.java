package org.bukkit.event.player;
import org.bukkit.event.Cancellable;
public class PlayerCommandPreprocessEvent extends PlayerEvent implements Cancellable {
    public String getMessage(){return "";}
    @Override public boolean isCancelled(){return false;}
    @Override public void setCancelled(boolean c){}
}

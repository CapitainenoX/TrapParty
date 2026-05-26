package org.bukkit.event.player;
import org.bukkit.event.Cancellable;
import org.bukkit.entity.Player;
import java.util.Set;
public class AsyncPlayerChatEvent extends PlayerEvent implements Cancellable {
    public Set<Player> getRecipients(){return java.util.Collections.emptySet();}
    public String getMessage(){return "";}
    @Override public boolean isCancelled(){return false;}
    @Override public void setCancelled(boolean c){}
}

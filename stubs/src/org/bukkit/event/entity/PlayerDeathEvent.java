package org.bukkit.event.entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.List;
public class PlayerDeathEvent extends EntityEvent {
    @Override public Player getEntity(){return null;}
    public void setKeepInventory(boolean b){}
    public void setKeepLevel(boolean b){}
    public List<ItemStack> getDrops(){return java.util.Collections.emptyList();}
    public void setDeathMessage(String msg){}
}

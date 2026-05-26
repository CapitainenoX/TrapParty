package org.bukkit.event.player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.Action;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.EquipmentSlot;
public class PlayerInteractEvent extends PlayerEvent implements Cancellable {
    public Action getAction(){return null;}
    public Block getClickedBlock(){return null;}
    public ItemStack getItem(){return null;}
    public EquipmentSlot getHand(){return null;}
    @Override public boolean isCancelled(){return false;}
    @Override public void setCancelled(boolean c){}
}

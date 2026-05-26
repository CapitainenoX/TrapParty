package org.bukkit.event.inventory;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.ItemStack;
public class InventoryClickEvent extends InventoryEvent implements Cancellable {
    public HumanEntity getWhoClicked(){return null;}
    public ItemStack getCurrentItem(){return null;}
    public int getRawSlot(){return 0;}
    @Override public boolean isCancelled(){return false;}
    @Override public void setCancelled(boolean c){}
}

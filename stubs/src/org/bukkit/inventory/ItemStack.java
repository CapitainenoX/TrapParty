package org.bukkit.inventory;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
public class ItemStack implements Cloneable {
    public ItemStack(Material mat){}
    public ItemStack(Material mat, int amount){}
    public Material getType(){return null;}
    public int getAmount(){return 0;}
    public void setAmount(int a){}
    public ItemMeta getItemMeta(){return null;}
    public void setItemMeta(ItemMeta meta){}
    @Override public ItemStack clone(){return this;}
}

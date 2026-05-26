package org.bukkit.inventory;
public interface Inventory {
    int getSize();
    ItemStack getItem(int slot);
    void setItem(int slot, ItemStack item);
    java.util.HashMap<Integer, ItemStack> addItem(ItemStack... items);
    void clear();
    void setContents(ItemStack[] items);
    ItemStack[] getContents();
    ItemStack[] getArmorContents();
    void setArmorContents(ItemStack[] items);
    InventoryHolder getHolder();
}

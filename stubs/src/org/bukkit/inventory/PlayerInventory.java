package org.bukkit.inventory;
public interface PlayerInventory extends Inventory {
    void setHelmet(ItemStack i);
    void setChestplate(ItemStack i);
    void setLeggings(ItemStack i);
    void setBoots(ItemStack i);
    void setItemInMainHand(ItemStack i);
    void setItemInOffHand(ItemStack i);
    ItemStack getItemInMainHand();
    ItemStack getItemInOffHand();
}

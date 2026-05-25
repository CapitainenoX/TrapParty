package fr.trapparty.shop;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ShopItem {
    private final Material material;
    private final int amount;
    private final int cost;
    private final boolean free;
    private final String potion; // type de potion ou null

    public ShopItem(Material material, int amount, int cost, boolean free, String potion) {
        this.material = material;
        this.amount = Math.max(1, amount);
        this.cost = Math.max(0, cost);
        this.free = free;
        this.potion = potion;
    }

    public Material getMaterial() { return material; }
    public int getAmount() { return amount; }
    public int getCost() { return cost; }
    public boolean isFree() { return free; }
    public String getPotion() { return potion; }

    public ItemStack toStack() {
        ItemStack it = new ItemStack(material, amount);
        if (potion != null && it.getItemMeta() instanceof org.bukkit.inventory.meta.PotionMeta pm) {
            try {
                org.bukkit.potion.PotionData data = new org.bukkit.potion.PotionData(
                        org.bukkit.potion.PotionType.valueOf(potion));
                pm.setBasePotionData(data);
                it.setItemMeta(pm);
            } catch (Throwable ignored) {}
        }
        return it;
    }
}

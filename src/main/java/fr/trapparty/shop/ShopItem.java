package fr.trapparty.shop;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

public class ShopItem {
    private final Material material;
    private final int amount;
    private final int cost;
    private final boolean free;
    private final String potion;

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
        if (potion != null && it.getItemMeta() instanceof PotionMeta pm) {
            PotionType type = resolvePotionType(potion);
            if (type != null) {
                // API moderne (1.20.5+) : setBasePotionType
                try {
                    pm.setBasePotionType(type);
                    it.setItemMeta(pm);
                    return it;
                } catch (Throwable ignored) {}
                // Fallback legacy : setBasePotionData (deprecated, removable)
                try {
                    @SuppressWarnings("deprecation")
                    org.bukkit.potion.PotionData data = new org.bukkit.potion.PotionData(type);
                    @SuppressWarnings("deprecation")
                    var x = pm; x.setBasePotionData(data);
                    it.setItemMeta(pm);
                } catch (Throwable ignored) {}
            }
        }
        return it;
    }

    private static PotionType resolvePotionType(String name) {
        if (name == null) return null;
        // Try registry first (1.20.5+ namespaced)
        try {
            PotionType pt = Registry.POTION.get(NamespacedKey.minecraft(name.toLowerCase()));
            if (pt != null) return pt;
        } catch (Throwable ignored) {}
        // Legacy valueOf
        try { return PotionType.valueOf(name.toUpperCase()); }
        catch (Throwable ignored) { return null; }
    }
}

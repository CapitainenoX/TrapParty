package org.bukkit.inventory.meta;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import java.util.List;
public interface ItemMeta extends PersistentDataHolder, Cloneable {
    void setDisplayName(String name);
    String getDisplayName();
    boolean hasDisplayName();
    void setLore(List<String> lore);
    List<String> getLore();
    boolean hasLore();
    void addEnchant(Enchantment e, int lvl, boolean ignoreLevelRestriction);
    void addItemFlags(ItemFlag... flags);
    void setUnbreakable(boolean b);
    void setCustomModelData(Integer data);
    int getCustomModelData();
    boolean hasCustomModelData();
    @Override PersistentDataContainer getPersistentDataContainer();
}

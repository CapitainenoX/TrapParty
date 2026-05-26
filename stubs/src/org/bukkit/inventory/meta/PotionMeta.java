package org.bukkit.inventory.meta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
public interface PotionMeta extends ItemMeta {
    void setBasePotionData(PotionData data);
    void setBasePotionType(PotionType type);
}

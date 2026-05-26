package org.bukkit.block;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
public interface Block {
    Material getType();
    void setType(Material m);
    Location getLocation();
    Block getRelative(int dx, int dy, int dz);
    Block getRelative(BlockFace face);
    BlockState getState();
    World getWorld();
    boolean breakNaturally(ItemStack tool);
}

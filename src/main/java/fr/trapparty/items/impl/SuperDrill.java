package fr.trapparty.items.impl;

import fr.trapparty.TrapPartyPlugin;
import fr.trapparty.items.SpecialItem;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

/**
 * Super Drill — clic droit sur un bloc : casse instantanément un rayon 3x3x3
 * autour. Les blocs sont récupérés dans l'inventaire (s'il y a la place).
 */
public class SuperDrill implements SpecialItem {

    @Override public String id() { return "super_drill"; }
    @Override public String displayName() { return "&b&l⚒ Super Foreuse"; }
    @Override public List<String> lore() {
        return List.of(
                "&7Casse instantanément",
                "&7une zone &b3x3x3&7 de blocs",
                "&7devant toi.",
                "&7Récupère les blocs cassés.");
    }
    @Override public String baseMaterial() { return "NETHERITE_PICKAXE|DIAMOND_PICKAXE|IRON_PICKAXE"; }
    @Override public int customModelData() { return 1001; }
    @Override public int maxUses() { return 6; }
    @Override public int cost() { return 60; }
    @Override public String shopCategory() { return "utility"; }

    @Override
    public boolean onUse(Player p, PlayerInteractEvent e) {
        TrapPartyPlugin plugin = TrapPartyPlugin.get();
        Block target = e.getClickedBlock();
        if (target == null) {
            // air → utilise le bloc visé à courte distance
            target = p.getTargetBlockExact(6);
            if (target == null) return false;
        }
        int broken = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    Block b = target.getRelative(dx, dy, dz);
                    if (b.getType() == Material.AIR || b.getType().isAir()) continue;
                    if (b.getType() == Material.BEDROCK) continue;
                    // ne casse pas les pièges placés par d'autres joueurs
                    if (plugin.traps().atBlock(b.getLocation()) != null) continue;
                    if (!b.getType().isSolid() && b.getType() != Material.WATER && b.getType() != Material.LAVA) continue;
                    if (b.breakNaturally(p.getInventory().getItemInMainHand())) broken++;
                }
            }
        }
        plugin.version().sound(p, "BLOCK_STONE_BREAK", 1f, 0.6f);
        plugin.version().particle(target.getLocation().add(0.5, 0.5, 0.5),
                "EXPLOSION_NORMAL", 12, 0.4, 0.4, 0.4, 0.1);
        return broken > 0;
    }
}

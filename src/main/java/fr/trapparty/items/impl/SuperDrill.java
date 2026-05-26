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
    @Override public int maxUses() { return 4; }
    @Override public int cost() { return 70; }
    @Override public String shopCategory() { return "utility"; }

    private static final java.util.Map<java.util.UUID, Long> lastUse = new java.util.concurrent.ConcurrentHashMap<>();

    @Override
    public boolean onUse(Player p, PlayerInteractEvent e) {
        TrapPartyPlugin plugin = TrapPartyPlugin.get();
        var game = plugin.games().forPlayer(p);
        if (game == null) return false;
        if (game.getState() == fr.trapparty.game.GameState.PREPARATION
                && !game.getArena().isBlockBreakAllowedInPrep()) {
            return false;
        }
        long cooldownMs = plugin.configs().drillCooldownMs();
        long now = System.currentTimeMillis();
        Long last = lastUse.get(p.getUniqueId());
        if (last != null && now - last < cooldownMs) {
            plugin.version().sendActionBar(p, "&7Foreuse en recharge... &c"
                    + ((cooldownMs - (now - last)) / 1000.0) + "s");
            return false;
        }
        lastUse.put(p.getUniqueId(), now);

        Block target = e.getClickedBlock();
        if (target == null) {
            target = p.getTargetBlockExact(6);
            if (target == null) return false;
        }
        int broken = 0;
        // 2x2x2 (8 blocs max) au lieu de 3x3x3 (27) — moins game-breaking
        for (int dx = 0; dx <= 1; dx++) {
            for (int dy = 0; dy <= 1; dy++) {
                for (int dz = 0; dz <= 1; dz++) {
                    Block b = target.getRelative(dx, dy, dz);
                    if (b.getType() == Material.AIR || b.getType().isAir()) continue;
                    if (b.getType() == Material.BEDROCK) continue;
                    if (plugin.traps().atBlock(b.getLocation()) != null) continue;
                    if (!b.getType().isSolid() && b.getType() != Material.WATER && b.getType() != Material.LAVA) continue;
                    if (b.breakNaturally(p.getInventory().getItemInMainHand())) broken++;
                }
            }
        }
        plugin.version().sound(p, "BLOCK_STONE_BREAK", 1f, 0.6f);
        plugin.version().particle(target.getLocation().add(0.5, 0.5, 0.5),
                "EXPLOSION", 12, 0.4, 0.4, 0.4, 0.1);
        return broken > 0;
    }
}

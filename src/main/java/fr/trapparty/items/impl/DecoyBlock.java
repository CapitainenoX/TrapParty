package fr.trapparty.items.impl;

import fr.trapparty.TrapPartyPlugin;
import fr.trapparty.items.SpecialItem;
import fr.trapparty.trap.Trap;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

/**
 * Decoy Block — clic droit : place un faux bloc qui explose
 * lorsqu'un autre joueur s'en approche à 2 blocs.
 */
public class DecoyBlock implements SpecialItem {

    @Override public String id() { return "decoy_block"; }
    @Override public String displayName() { return "&8&l✦ Faux Bloc"; }
    @Override public List<String> lore() {
        return List.of(
                "&7Place un faux bloc piégé.",
                "&7Explose au passage d'un",
                "&7adversaire à &c2 blocs&7.");
    }
    @Override public String baseMaterial() { return "GRAY_DYE"; }
    @Override public int customModelData() { return 1005; }
    @Override public int maxUses() { return 3; }
    @Override public int cost() { return 35; }
    @Override public String shopCategory() { return "traps"; }

    @Override
    public boolean onUse(Player p, PlayerInteractEvent e) {
        TrapPartyPlugin plugin = TrapPartyPlugin.get();
        Block target = e.getClickedBlock();
        if (target == null) target = p.getTargetBlockExact(6);
        if (target == null) return false;
        Block above = target.getRelative(0, 1, 0);
        if (above.getType() != Material.AIR && !above.getType().isAir()) return false;
        above.setType(Material.STONE);
        plugin.traps().register(
                plugin.games().forPlayer(p) != null ? plugin.games().forPlayer(p).getId() : "global",
                new Trap(p.getUniqueId(), Trap.Type.FAKE_BLOCK, above.getLocation()));
        // tick task vérifie si un joueur s'approche
        final Block hidden = above;
        new org.bukkit.scheduler.BukkitRunnable() {
            int ticks = 0;
            @Override public void run() {
                if (hidden.getType() == Material.AIR) { cancel(); return; }
                ticks += 4;
                if (ticks > 20 * 60 * 3) { cancel(); return; } // 3 minutes max
                for (var entity : hidden.getWorld().getNearbyEntities(
                        hidden.getLocation().add(0.5, 0.5, 0.5), 2, 2, 2)) {
                    if (!(entity instanceof Player other)) continue;
                    if (other.getUniqueId().equals(p.getUniqueId())) continue;
                    // boom
                    hidden.setType(Material.AIR);
                    TNTPrimed tnt = hidden.getWorld().spawn(hidden.getLocation().add(0.5, 0, 0.5), TNTPrimed.class);
                    tnt.setFuseTicks(0);
                    try { tnt.setSource(p); } catch (Throwable ignored) {}
                    cancel();
                    return;
                }
            }
        }.runTaskTimer(plugin, 20L, 4L);
        plugin.version().sound(p, "BLOCK_NOTE_BLOCK_BASS", 1f, 0.7f);
        return true;
    }
}

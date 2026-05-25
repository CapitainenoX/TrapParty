package fr.trapparty.items.impl;

import fr.trapparty.TrapPartyPlugin;
import fr.trapparty.items.SpecialItem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * Magnet Bomb — clic droit : aspire les joueurs à 6 blocs vers toi
 * puis amorce une TNT à ta position.
 */
public class MagnetBomb implements SpecialItem {

    @Override public String id() { return "magnet_bomb"; }
    @Override public String displayName() { return "&5&l⚛ Magnet Bomb"; }
    @Override public List<String> lore() {
        return List.of(
                "&7Aspire les ennemis &d6 blocs",
                "&7autour, puis amorce",
                "&7une &cTNT&7 à ta position.",
                "&8Usage unique.");
    }
    @Override public String baseMaterial() { return "AMETHYST_SHARD|PRISMARINE_SHARD"; }
    @Override public int customModelData() { return 1008; }
    @Override public int maxUses() { return 1; }
    @Override public int cost() { return 55; }
    @Override public String shopCategory() { return "traps"; }

    @Override
    public boolean onUse(Player p, PlayerInteractEvent e) {
        TrapPartyPlugin plugin = TrapPartyPlugin.get();
        Location at = p.getLocation();
        for (var entity : p.getNearbyEntities(6, 6, 6)) {
            if (!(entity instanceof Player other)) continue;
            Vector pull = at.toVector().subtract(other.getLocation().toVector()).normalize();
            pull.multiply(1.2);
            pull.setY(0.5);
            other.setVelocity(pull);
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!p.isOnline()) return;
            TNTPrimed tnt = p.getWorld().spawn(p.getLocation(), TNTPrimed.class);
            tnt.setFuseTicks(20);
            tnt.setYield(3.0f);
            try { tnt.setSource(p); } catch (Throwable ignored) {}
        }, 24L);
        plugin.version().sound(p, "BLOCK_AMETHYST_BLOCK_CHIME", 1f, 0.5f);
        return true;
    }
}

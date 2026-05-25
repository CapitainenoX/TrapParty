package fr.trapparty.items.impl;

import fr.trapparty.TrapPartyPlugin;
import fr.trapparty.items.SpecialItem;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * Wind Stomper — clic droit : explosion d'air qui te propulse vers le haut
 * et repousse violemment les joueurs à 4 blocs. 2 utilisations.
 */
public class WindStomper implements SpecialItem {

    @Override public String id() { return "wind_stomper"; }
    @Override public String displayName() { return "&3&l✦ Wind Stomper"; }
    @Override public List<String> lore() {
        return List.of(
                "&7Propulse-toi vers le haut",
                "&7et &brepousse&7 les joueurs proches.",
                "&7Rayon : &b4 blocs");
    }
    @Override public String baseMaterial() { return "WIND_CHARGE|FEATHER"; }
    @Override public int customModelData() { return 1004; }
    @Override public int maxUses() { return 2; }
    @Override public int cost() { return 30; }
    @Override public String shopCategory() { return "mobility"; }

    @Override
    public boolean onUse(Player p, PlayerInteractEvent e) {
        TrapPartyPlugin plugin = TrapPartyPlugin.get();
        // se propulse
        p.setVelocity(p.getVelocity().setY(1.6));
        // repousse les autres
        for (var entity : p.getNearbyEntities(4, 4, 4)) {
            if (!(entity instanceof Player other)) continue;
            if (other.equals(p)) continue;
            Vector knock = other.getLocation().toVector().subtract(p.getLocation().toVector()).normalize();
            knock.setY(0.9);
            other.setVelocity(knock.multiply(1.6));
        }
        plugin.version().sound(p, "ENTITY_BREEZE_SHOOT", 1f, 1f);
        plugin.version().particle(p.getLocation(), "CLOUD", 30, 1, 0.5, 1, 0.05);
        return true;
    }
}

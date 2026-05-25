package fr.trapparty.items.impl;

import fr.trapparty.TrapPartyPlugin;
import fr.trapparty.items.SpecialItem;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * Grappling Hook — clic droit : te projette vers le point visé (jusqu'à 25 blocs).
 */
public class GrapplingHook implements SpecialItem {

    @Override public String id() { return "grappling_hook"; }
    @Override public String displayName() { return "&e&l⚓ Grappling Hook"; }
    @Override public List<String> lore() {
        return List.of(
                "&7Te projette vers le",
                "&7point visé.",
                "&7Portée : &e25 blocs");
    }
    @Override public String baseMaterial() { return "FISHING_ROD"; }
    @Override public int customModelData() { return 1006; }
    @Override public int maxUses() { return 5; }
    @Override public int cost() { return 35; }
    @Override public String shopCategory() { return "mobility"; }

    @Override
    public boolean onUse(Player p, PlayerInteractEvent e) {
        TrapPartyPlugin plugin = TrapPartyPlugin.get();
        RayTraceResult hit = p.rayTraceBlocks(25.0);
        Location target = (hit != null && hit.getHitPosition() != null)
                ? hit.getHitPosition().toLocation(p.getWorld())
                : p.getEyeLocation().add(p.getEyeLocation().getDirection().multiply(25));

        Vector dir = target.toVector().subtract(p.getLocation().toVector());
        double dist = dir.length();
        if (dist < 2) return false;
        // velocity vector calibré
        Vector v = dir.normalize().multiply(Math.min(2.2, dist * 0.18));
        v.setY(Math.max(0.4, v.getY() + 0.3));
        p.setVelocity(v);
        p.setFallDistance(0f);
        plugin.version().sound(p, "ENTITY_FISHING_BOBBER_THROW", 1f, 1.4f);
        plugin.version().particle(p.getLocation(), "CRIT", 20, 0.3, 0.3, 0.3, 0.1);
        return true;
    }
}

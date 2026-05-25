package fr.trapparty.items.impl;

import fr.trapparty.TrapPartyPlugin;
import fr.trapparty.items.SpecialItem;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.RayTraceResult;

import java.util.List;

/**
 * Activateur à distance — clic droit : place une TNT amorcée à
 * l'endroit visé (jusqu'à 30 blocs). Usage unique par item.
 */
public class RemoteActivator implements SpecialItem {

    @Override public String id() { return "remote_activator"; }
    @Override public String displayName() { return "&c&l⚡ Activateur à distance"; }
    @Override public List<String> lore() {
        return List.of(
                "&7Place une &cTNT amorcée",
                "&7à l'endroit visé.",
                "&7Portée : &c30 blocs",
                "&8Usage unique.");
    }
    @Override public String baseMaterial() { return "COMPASS"; }
    @Override public int customModelData() { return 1002; }
    @Override public int maxUses() { return 1; }
    @Override public int cost() { return 45; }
    @Override public String shopCategory() { return "traps"; }

    @Override
    public boolean onUse(Player p, PlayerInteractEvent e) {
        TrapPartyPlugin plugin = TrapPartyPlugin.get();
        // raytrace pour trouver point d'impact
        RayTraceResult hit = p.rayTraceBlocks(30.0);
        Location target;
        if (hit != null && hit.getHitBlock() != null) {
            Block b = hit.getHitBlock();
            // place au-dessus du bloc touché si vise par dessous
            target = b.getLocation().add(0.5, 1.0, 0.5);
            if (hit.getHitBlockFace() != null) {
                target = b.getRelative(hit.getHitBlockFace()).getLocation().add(0.5, 0.0, 0.5);
            }
        } else {
            // vise dans le vide → 25 blocs devant
            target = p.getEyeLocation().add(p.getEyeLocation().getDirection().multiply(25));
        }
        if (target.getWorld() == null) return false;
        TNTPrimed tnt = target.getWorld().spawn(target, TNTPrimed.class);
        tnt.setFuseTicks(40);
        tnt.setYield(3.5f);
        try { tnt.setSource(p); } catch (Throwable ignored) {}
        plugin.version().sound(p, "ENTITY_TNT_PRIMED", 1f, 1.1f);
        return true;
    }
}

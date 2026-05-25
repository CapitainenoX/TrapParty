package fr.trapparty.listeners;

import fr.trapparty.TrapPartyPlugin;
import fr.trapparty.game.Game;
import fr.trapparty.trap.Trap;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Détecte le déclenchement de pièges et attribue les dégâts à l'owner.
 */
public class TrapListener implements Listener {

    private final TrapPartyPlugin plugin;

    public TrapListener(TrapPartyPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (e.getTo() == null) return;
        if (e.getFrom().getBlockX() == e.getTo().getBlockX()
                && e.getFrom().getBlockY() == e.getTo().getBlockY()
                && e.getFrom().getBlockZ() == e.getTo().getBlockZ()) return;
        Player p = e.getPlayer();
        Game g = plugin.games().forPlayer(p);
        if (g == null) return;

        Trap below = plugin.traps().atBlock(e.getTo().clone().add(0, -1, 0));
        Trap inside = plugin.traps().atBlock(e.getTo());
        Trap trap = inside != null ? inside : below;
        if (trap != null && !trap.getOwner().equals(p.getUniqueId())) {
            plugin.traps().noteTrigger(p.getUniqueId(), trap);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        Game g = plugin.games().forPlayer(p);
        if (g == null) return;
        // attribution piège via type de damage
        switch (e.getCause()) {
            case LAVA, FIRE, FIRE_TICK, HOT_FLOOR, FALL, CONTACT, ENTITY_EXPLOSION, BLOCK_EXPLOSION, SUFFOCATION -> {
                Trap nearby = nearbyTrap(p.getLocation());
                if (nearby != null && !nearby.getOwner().equals(p.getUniqueId())) {
                    plugin.traps().noteTrigger(p.getUniqueId(), nearby);
                }
            }
            default -> {}
        }
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent e) {
        Trap trap = nearbyTrap(e.getEntity().getLocation());
        if (trap == null) return;
        // tout joueur dans le rayon : note trigger
        for (var entity : e.getEntity().getNearbyEntities(5, 5, 5)) {
            if (entity instanceof Player p) {
                if (trap.getOwner().equals(p.getUniqueId())) continue;
                plugin.traps().noteTrigger(p.getUniqueId(), trap);
            }
        }
    }

    private Trap nearbyTrap(Location loc) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    Trap t = plugin.traps().atBlock(loc.clone().add(dx, dy, dz));
                    if (t != null) return t;
                }
            }
        }
        return null;
    }
}

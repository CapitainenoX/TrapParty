package fr.trapparty.events.impl;

import fr.trapparty.TrapPartyPlugin;
import fr.trapparty.events.GameEvent;
import fr.trapparty.game.Game;
import fr.trapparty.game.GamePlayer;
import fr.trapparty.util.LocationUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class MeteorEvent implements GameEvent {
    @Override public String id() { return "meteor"; }
    @Override public String displayName() { return "Météorites"; }

    @Override public void apply(Game game) {
        TrapPartyPlugin plugin = TrapPartyPlugin.get();
        if (game.world() == null) return;
        plugin.messages().broadcastPlayers(playersIn(game), "events.meteor", null);
        Location center = LocationUtil.parse(game.world(), game.getArena().getCenterDef());
        if (center == null) return;
        new org.bukkit.scheduler.BukkitRunnable() {
            int remaining = 8;
            @Override public void run() {
                if (remaining <= 0 || game.world() == null) { cancel(); return; }
                for (int i = 0; i < 2 && remaining > 0; i++) {
                    double dx = (Math.random() - 0.5) * 40;
                    double dz = (Math.random() - 0.5) * 40;
                    Location spawn = center.clone().add(dx, 40, dz);
                    Fireball fb = game.world().spawn(spawn, Fireball.class);
                    fb.setVelocity(new Vector(0, -1, 0));
                    fb.setYield(2.5f);
                    fb.setIsIncendiary(true);
                    remaining--;
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private static java.util.List<Player> playersIn(Game g) {
        java.util.List<Player> list = new java.util.ArrayList<>();
        for (GamePlayer gp : g.getPlayers()) {
            Player p = Bukkit.getPlayer(gp.getUuid());
            if (p != null) list.add(p);
        }
        return list;
    }
}

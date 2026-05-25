package fr.trapparty.events.impl;

import fr.trapparty.TrapPartyPlugin;
import fr.trapparty.events.GameEvent;
import fr.trapparty.game.Game;
import fr.trapparty.game.GamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Player;

public class StormEvent implements GameEvent {
    @Override public String id() { return "storm"; }
    @Override public String displayName() { return "Tempête"; }

    @Override public void apply(Game game) {
        TrapPartyPlugin plugin = TrapPartyPlugin.get();
        if (game.world() == null) return;
        game.world().setStorm(true);
        game.world().setThundering(true);
        game.world().setWeatherDuration(20 * 60);
        plugin.messages().broadcastPlayers(onlinePlayersIn(game), "events.storm", null);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // 2-3 éclairs aléatoires près de joueurs vivants
            int strikes = 3;
            for (GamePlayer gp : game.getPlayers()) {
                if (!gp.isAlive() || strikes <= 0) continue;
                Player p = Bukkit.getPlayer(gp.getUuid());
                if (p == null) continue;
                Location l = p.getLocation().clone().add(
                        (Math.random() - 0.5) * 10, 0, (Math.random() - 0.5) * 10);
                game.world().strikeLightningEffect(l);
                strikes--;
            }
        }, 40L);
    }

    private static Iterable<Player> onlinePlayersIn(Game g) {
        java.util.List<Player> list = new java.util.ArrayList<>();
        for (GamePlayer gp : g.getPlayers()) {
            Player p = Bukkit.getPlayer(gp.getUuid());
            if (p != null) list.add(p);
        }
        return list;
    }
}

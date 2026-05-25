package fr.trapparty.events.impl;

import fr.trapparty.TrapPartyPlugin;
import fr.trapparty.events.GameEvent;
import fr.trapparty.game.Game;
import fr.trapparty.game.GamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class NightFallEvent implements GameEvent {
    @Override public String id() { return "night_fall"; }
    @Override public String displayName() { return "Nuit"; }

    @Override public void apply(Game game) {
        TrapPartyPlugin plugin = TrapPartyPlugin.get();
        if (game.world() == null) return;
        game.world().setTime(18000);
        plugin.messages().broadcastPlayers(online(game), "events.night-fall", null);
    }

    private static java.util.List<Player> online(Game g) {
        java.util.List<Player> list = new java.util.ArrayList<>();
        for (GamePlayer gp : g.getPlayers()) {
            Player p = Bukkit.getPlayer(gp.getUuid());
            if (p != null) list.add(p);
        }
        return list;
    }
}

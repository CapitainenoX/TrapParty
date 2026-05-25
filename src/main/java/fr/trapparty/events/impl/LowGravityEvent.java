package fr.trapparty.events.impl;

import fr.trapparty.TrapPartyPlugin;
import fr.trapparty.events.GameEvent;
import fr.trapparty.game.Game;
import fr.trapparty.game.GamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class LowGravityEvent implements GameEvent {
    @Override public String id() { return "low_gravity"; }
    @Override public String displayName() { return "Faible gravité"; }

    @Override public void apply(Game game) {
        TrapPartyPlugin plugin = TrapPartyPlugin.get();
        PotionEffectType jump = PotionEffectType.getByName("JUMP_BOOST");
        if (jump == null) return;
        for (GamePlayer gp : game.getPlayers()) {
            Player p = Bukkit.getPlayer(gp.getUuid());
            if (p == null || !gp.isAlive()) continue;
            p.addPotionEffect(new PotionEffect(jump, 20 * 45, 4, true, false));
        }
        plugin.messages().broadcastPlayers(online(game), "events.low-gravity", null);
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

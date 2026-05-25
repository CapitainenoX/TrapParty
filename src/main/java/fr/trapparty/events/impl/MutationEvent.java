package fr.trapparty.events.impl;

import fr.trapparty.TrapPartyPlugin;
import fr.trapparty.events.GameEvent;
import fr.trapparty.game.Game;
import fr.trapparty.game.GamePlayer;
import fr.trapparty.util.RandomUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Map;

public class MutationEvent implements GameEvent {
    @Override public String id() { return "mutation"; }
    @Override public String displayName() { return "Mutation"; }

    @Override public void apply(Game game) {
        TrapPartyPlugin plugin = TrapPartyPlugin.get();
        List<String> options = List.of("SPEED", "JUMP_BOOST", "STRENGTH", "INVISIBILITY", "REGENERATION");
        String name = RandomUtil.pick(options);
        PotionEffectType type = fr.trapparty.util.EffectUtil.byName(name, "JUMP", "INCREASE_DAMAGE");
        if (type == null) return;
        for (GamePlayer gp : game.getPlayers()) {
            Player p = Bukkit.getPlayer(gp.getUuid());
            if (p == null || !gp.isAlive()) continue;
            p.addPotionEffect(new PotionEffect(type, 20 * 30, 1, true, false));
        }
        plugin.messages().broadcastPlayers(onlinePlayersIn(game), "events.mutation",
                Map.of("mutation", name.toLowerCase()));
    }

    private static java.util.List<Player> onlinePlayersIn(Game g) {
        java.util.List<Player> list = new java.util.ArrayList<>();
        for (GamePlayer gp : g.getPlayers()) {
            Player p = Bukkit.getPlayer(gp.getUuid());
            if (p != null) list.add(p);
        }
        return list;
    }
}

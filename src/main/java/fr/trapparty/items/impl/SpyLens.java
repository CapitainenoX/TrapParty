package fr.trapparty.items.impl;

import fr.trapparty.TrapPartyPlugin;
import fr.trapparty.game.Game;
import fr.trapparty.game.GamePlayer;
import fr.trapparty.items.SpecialItem;
import fr.trapparty.util.EffectUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

/**
 * Spy Lens — clic droit : applique Glowing 10s à tous les autres joueurs de
 * la partie (les rend visibles à travers les murs).
 */
public class SpyLens implements SpecialItem {

    @Override public String id() { return "spy_lens"; }
    @Override public String displayName() { return "&d&l✦ Lunette d'Espion"; }
    @Override public List<String> lore() {
        return List.of(
                "&7Marque tous les adversaires",
                "&7avec &dGlowing&7 pendant",
                "&d10 secondes&7.");
    }
    @Override public String baseMaterial() { return "SPYGLASS"; }
    @Override public int customModelData() { return 1007; }
    @Override public int maxUses() { return 2; }
    @Override public int cost() { return 40; }
    @Override public String shopCategory() { return "utility"; }

    @Override
    public boolean onUse(Player p, PlayerInteractEvent e) {
        TrapPartyPlugin plugin = TrapPartyPlugin.get();
        Game g = plugin.games().forPlayer(p);
        if (g == null) {
            plugin.messages().send(p, "generic.not-in-game");
            return false;
        }
        PotionEffectType glow = EffectUtil.byName("GLOWING");
        if (glow == null) return false;
        int count = 0;
        for (GamePlayer gp : g.getPlayers()) {
            if (gp.getUuid().equals(p.getUniqueId())) continue;
            if (!gp.isAlive()) continue;
            Player other = Bukkit.getPlayer(gp.getUuid());
            if (other == null) continue;
            other.addPotionEffect(new PotionEffect(glow, 20 * 10, 0, true, false));
            count++;
        }
        plugin.version().sendActionBar(p, "&d✦ §f" + count + " §dcibles marquées");
        plugin.version().sound(p, "ITEM_SPYGLASS_USE", 1f, 1.2f);
        return count > 0;
    }
}

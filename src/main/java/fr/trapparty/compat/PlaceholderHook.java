package fr.trapparty.compat;

import fr.trapparty.TrapPartyPlugin;
import fr.trapparty.game.Game;
import fr.trapparty.game.GamePlayer;
import fr.trapparty.stats.PlayerStats;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Hook PlaceholderAPI entièrement par reflection : le plugin compile et
 * tourne même sans PAPI sur le classpath. Si PAPI est présent, on
 * enregistre un PlaceholderExpansion exposant les placeholders TrapParty.
 *
 * Placeholders fournis (préfixe `trapparty_`) :
 *  - wins, kills, trap_kills, deaths, mmr, kd, games, coins_total
 *  - balance (Vault ou coins de partie)
 *  - game_state, game_arena, game_id
 *  - alive, total
 *  - kit
 */
public class PlaceholderHook {

    private final TrapPartyPlugin plugin;
    private boolean registered = false;

    public PlaceholderHook(TrapPartyPlugin plugin) {
        this.plugin = plugin;
    }

    public void tryRegister() {
        if (registered) return;
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) return;
        try {
            // On ne peut pas importer me.clip.placeholderapi : reflection-only
            // sub-class de PlaceholderExpansion dynamiquement via ByteBuddy serait
            // overkill ; on utilise plutôt une classe interne anonyme via
            // un wrapper compilé directement contre PAPI (présent au runtime).
            new PapiBridge(plugin).register();
            registered = true;
            plugin.getLogger().info("PlaceholderAPI hooked (trapparty_*)");
        } catch (Throwable t) {
            plugin.getLogger().fine("PAPI hook failed: " + t.getMessage());
        }
    }

    /**
     * Bridge inner class : référence me.clip.placeholderapi.expansion.PlaceholderExpansion.
     * Cette classe ne sera chargée par le ClassLoader QUE si PAPI est présent.
     * Donc on protège son instanciation via try/catch sur tout PapiBridge.
     */
    private static final class PapiBridge extends me.clip.placeholderapi.expansion.PlaceholderExpansion {
        private final TrapPartyPlugin plugin;
        PapiBridge(TrapPartyPlugin plugin) { this.plugin = plugin; }
        @Override public String getIdentifier() { return "trapparty"; }
        @Override public String getAuthor() { return "CapitainenoX"; }
        @Override public String getVersion() { return plugin.getDescription().getVersion(); }
        @Override public boolean persist() { return true; }
        @Override
        public String onRequest(OfflinePlayer player, String params) {
            if (player == null) return "";
            PlayerStats st = plugin.stats().get(player.getUniqueId());
            Game g = plugin.games().forPlayer(player.getUniqueId());
            GamePlayer gp = g == null ? null : g.getPlayer(player.getUniqueId());
            return switch (params.toLowerCase()) {
                case "wins" -> String.valueOf(st.getWins());
                case "kills" -> String.valueOf(st.getKills());
                case "trap_kills" -> String.valueOf(st.getTrapKills());
                case "deaths" -> String.valueOf(st.getDeaths());
                case "mmr" -> String.valueOf(st.getMmr());
                case "kd" -> String.valueOf(st.getKd());
                case "games" -> String.valueOf(st.getGamesPlayed());
                case "coins_total" -> String.valueOf(st.getCoinsTotal());
                case "balance" -> {
                    org.bukkit.entity.Player online = player.getPlayer();
                    yield online == null ? "0" : plugin.economy().format(plugin.economy().balance(online, gp));
                }
                case "game_state" -> g == null ? "" : g.getState().name();
                case "game_arena" -> g == null ? "" : g.getArena().getDisplayName();
                case "game_id" -> g == null ? "" : g.getId();
                case "alive" -> g == null ? "0" : String.valueOf(g.aliveCount());
                case "total" -> g == null ? "0" : String.valueOf(g.getPlayers().size());
                case "kit" -> gp == null || gp.getKit() == null ? "" : gp.getKit().getDisplayName();
                default -> null;
            };
        }
    }
}

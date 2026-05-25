package fr.trapparty.commands;

import fr.trapparty.TrapPartyPlugin;
import fr.trapparty.game.Game;
import fr.trapparty.stats.PlayerStats;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class TrapPartyCommand implements CommandExecutor, TabCompleter {

    private final TrapPartyPlugin plugin;

    public TrapPartyCommand(TrapPartyPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            help(sender);
            return true;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "join" -> {
                if (!(sender instanceof Player p)) { plugin.messages().send(sender, "generic.player-only"); return true; }
                if (args.length >= 2) {
                    var a = plugin.arenas().get(args[1]);
                    if (a == null) { sender.sendMessage("§cArène inconnue."); return true; }
                    var g = plugin.games().findOrCreate(a);
                    if (g == null) { sender.sendMessage("§cAucune partie disponible."); return true; }
                    plugin.games().join(p, g);
                } else {
                    plugin.games().joinAuto(p);
                }
            }
            case "leave", "quit" -> {
                if (!(sender instanceof Player p)) { plugin.messages().send(sender, "generic.player-only"); return true; }
                plugin.games().leave(p);
            }
            case "stats" -> {
                if (!(sender instanceof Player p)) { plugin.messages().send(sender, "generic.player-only"); return true; }
                PlayerStats st = plugin.stats().get(p.getUniqueId());
                p.sendMessage(plugin.messages().get("stats.header"));
                p.sendMessage(plugin.messages().get("stats.wins", Map.of("wins", String.valueOf(st.getWins()))));
                p.sendMessage(plugin.messages().get("stats.kills", Map.of("kills", String.valueOf(st.getKills()))));
                p.sendMessage(plugin.messages().get("stats.trap-kills", Map.of("trap_kills", String.valueOf(st.getTrapKills()))));
                p.sendMessage(plugin.messages().get("stats.games-played", Map.of("games", String.valueOf(st.getGamesPlayed()))));
                p.sendMessage(plugin.messages().get("stats.mmr", Map.of("mmr", String.valueOf(st.getMmr()))));
                p.sendMessage(plugin.messages().get("stats.kd", Map.of("kd", String.valueOf(st.getKd()))));
                // Affichage du solde (Vault ou coins de partie)
                var g = plugin.games().forPlayer(p);
                var gp = g != null ? g.getPlayer(p.getUniqueId()) : null;
                p.sendMessage("§7Solde : §e" + plugin.economy().format(plugin.economy().balance(p, gp)));
            }
            case "list" -> {
                sender.sendMessage("§6Parties en cours : §f" + plugin.games().all().size());
                for (Game g : plugin.games().all()) {
                    sender.sendMessage(" §8» §e" + g.getId() + " §7- §f" + g.getState()
                            + " §8(§a" + g.getPlayers().size() + "§7/§f" + g.getArena().getMaxPlayers() + "§8)");
                }
            }
            case "arenas" -> {
                sender.sendMessage("§6Arènes :");
                for (var a : plugin.arenas().all()) {
                    sender.sendMessage(" §8» §e" + a.getId() + " §7- " + a.getDisplayName());
                }
            }
            case "help" -> help(sender);
            default -> help(sender);
        }
        return true;
    }

    private void help(CommandSender s) {
        s.sendMessage("§6§lTrapParty §7- commandes :");
        s.sendMessage(" §e/tparty join [arène] §7- rejoindre");
        s.sendMessage(" §e/tparty leave §7- quitter");
        s.sendMessage(" §e/tparty stats §7- voir tes stats");
        s.sendMessage(" §e/tparty list §7- parties en cours");
        s.sendMessage(" §e/tparty arenas §7- liste des arènes");
        s.sendMessage(" §e/kit §7- choisir un kit");
        s.sendMessage(" §e/shop §7- ouvrir la boutique");
        s.sendMessage(" §e/spectate <arène> §7- spectate");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("join", "leave", "stats", "list", "arenas", "help");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("join")) {
            List<String> names = new ArrayList<>();
            for (var a : plugin.arenas().all()) names.add(a.getId());
            return names;
        }
        return Collections.emptyList();
    }
}

package fr.trapparty.commands;

import fr.trapparty.TrapPartyPlugin;
import fr.trapparty.game.Game;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;

public class AdminCommand implements CommandExecutor, TabCompleter {

    private final TrapPartyPlugin plugin;

    public AdminCommand(TrapPartyPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("trapparty.admin")) {
            plugin.messages().send(sender, "generic.no-permission");
            return true;
        }
        if (args.length == 0) { help(sender); return true; }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "reload" -> {
                long t = System.currentTimeMillis();
                plugin.reloadAll();
                plugin.messages().send(sender, "generic.reloaded",
                        Map.of("time", String.valueOf(System.currentTimeMillis() - t)));
            }
            case "create" -> {
                if (args.length < 3) { sender.sendMessage("§c/tpa create <arena> <templateWorld>"); return true; }
                if (plugin.arenas().createTemplate(args[1], args[2])) {
                    plugin.messages().send(sender, "admin.arena-created", Map.of("arena", args[1]));
                } else {
                    sender.sendMessage("§cArène déjà existante.");
                }
            }
            case "delete" -> {
                if (args.length < 2) { sender.sendMessage("§c/tpa delete <arena>"); return true; }
                if (plugin.arenas().delete(args[1])) {
                    plugin.messages().send(sender, "admin.arena-deleted", Map.of("arena", args[1]));
                } else sender.sendMessage("§cArène introuvable.");
            }
            case "setspawn" -> {
                if (!(sender instanceof Player p)) { plugin.messages().send(sender, "generic.player-only"); return true; }
                if (args.length < 2) { sender.sendMessage("§c/tpa setspawn <arena>"); return true; }
                var a = plugin.arenas().get(args[1]);
                if (a == null) { sender.sendMessage("§cArène inconnue."); return true; }
                var arenas = plugin.configs().arenas();
                List<String> spawns = arenas.getStringList("arenas." + args[1] + ".spawns");
                spawns.add(fr.trapparty.util.LocationUtil.serialize(p.getLocation()));
                arenas.set("arenas." + args[1] + ".spawns", spawns);
                plugin.configs().save("arenas.yml", arenas);
                plugin.arenas().reload();
                sender.sendMessage("§aSpawn ajouté (" + spawns.size() + " total).");
            }
            case "setcenter" -> {
                if (!(sender instanceof Player p)) { plugin.messages().send(sender, "generic.player-only"); return true; }
                if (args.length < 2) { sender.sendMessage("§c/tpa setcenter <arena>"); return true; }
                var a = plugin.arenas().get(args[1]);
                if (a == null) { sender.sendMessage("§cArène inconnue."); return true; }
                var arenas = plugin.configs().arenas();
                arenas.set("arenas." + args[1] + ".center", fr.trapparty.util.LocationUtil.serialize(p.getLocation()));
                plugin.configs().save("arenas.yml", arenas);
                plugin.arenas().reload();
                sender.sendMessage("§aCentre défini.");
            }
            case "forcestart" -> {
                if (args.length < 2) { sender.sendMessage("§c/tpa forcestart <gameId>"); return true; }
                Game g = plugin.games().get(args[1]);
                if (g == null) { sender.sendMessage("§cPartie inconnue."); return true; }
                g.forceStart();
                plugin.messages().send(sender, "admin.game-force-start", Map.of("arena", g.getArena().getId()));
            }
            case "forcestop" -> {
                if (args.length < 2) { sender.sendMessage("§c/tpa forcestop <gameId>"); return true; }
                Game g = plugin.games().get(args[1]);
                if (g == null) { sender.sendMessage("§cPartie inconnue."); return true; }
                g.forceEnd();
                plugin.messages().send(sender, "admin.game-force-end", Map.of("arena", g.getArena().getId()));
            }
            case "debug" -> {
                boolean dbg = !plugin.configs().debug();
                plugin.configs().root().set("settings.debug", dbg);
                plugin.saveConfig();
                plugin.messages().send(sender, dbg ? "admin.debug-on" : "admin.debug-off");
            }
            case "save" -> {
                plugin.stats().flush();
                sender.sendMessage("§aStats sauvegardées.");
            }
            default -> help(sender);
        }
        return true;
    }

    private void help(CommandSender s) {
        s.sendMessage("§6§lTrapParty Admin §7- commandes :");
        s.sendMessage(" §e/tpa reload §7- recharger config");
        s.sendMessage(" §e/tpa create <arena> <templateWorld>");
        s.sendMessage(" §e/tpa delete <arena>");
        s.sendMessage(" §e/tpa setspawn <arena>");
        s.sendMessage(" §e/tpa setcenter <arena>");
        s.sendMessage(" §e/tpa forcestart <gameId>");
        s.sendMessage(" §e/tpa forcestop <gameId>");
        s.sendMessage(" §e/tpa debug §7- toggle debug");
        s.sendMessage(" §e/tpa save §7- flush stats");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> sub = new ArrayList<>(List.of("reload", "create", "delete", "setspawn", "setcenter",
                    "forcestart", "forcestop", "debug", "save"));
            List<String> out = new ArrayList<>();
            StringUtil.copyPartialMatches(args[0], sub, out);
            return out;
        }
        if (args.length == 2 && List.of("delete", "setspawn", "setcenter").contains(args[0].toLowerCase())) {
            List<String> ids = new ArrayList<>();
            for (var a : plugin.arenas().all()) ids.add(a.getId());
            return ids;
        }
        return Collections.emptyList();
    }
}

package fr.trapparty.commands;

import fr.trapparty.TrapPartyPlugin;
import fr.trapparty.game.Game;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SpectateCommand implements CommandExecutor, TabCompleter {

    private final TrapPartyPlugin plugin;

    public SpectateCommand(TrapPartyPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) { plugin.messages().send(sender, "generic.player-only"); return true; }
        if (args.length == 0) {
            sender.sendMessage("§e/spectate <gameId|arena>");
            return true;
        }
        Game game = plugin.games().get(args[0]);
        if (game == null) {
            // chercher première game de l'arène
            for (Game g : plugin.games().all()) {
                if (g.getArena().getId().equalsIgnoreCase(args[0])) { game = g; break; }
            }
        }
        if (game == null || game.world() == null) {
            sender.sendMessage("§cAucune partie trouvée.");
            return true;
        }
        var locDef = game.getArena().getSpectatorSpawnDef();
        var loc = fr.trapparty.util.LocationUtil.parse(game.world(), locDef);
        if (loc == null) loc = game.world().getSpawnLocation();
        p.teleport(loc);
        p.setGameMode(GameMode.SPECTATOR);
        game.getSpectators().add(p.getUniqueId());
        plugin.scoreboards().attach(game, p);
        plugin.bossbars().attach(game, p);
        sender.sendMessage("§aSpectate de §e" + game.getId());
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> out = new ArrayList<>();
            for (Game g : plugin.games().all()) out.add(g.getId());
            for (var a : plugin.arenas().all()) out.add(a.getId());
            return out;
        }
        return Collections.emptyList();
    }
}

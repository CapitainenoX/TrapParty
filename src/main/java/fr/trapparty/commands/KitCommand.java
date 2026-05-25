package fr.trapparty.commands;

import fr.trapparty.TrapPartyPlugin;
import fr.trapparty.game.Game;
import fr.trapparty.kit.Kit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class KitCommand implements CommandExecutor, TabCompleter {

    private final TrapPartyPlugin plugin;

    public KitCommand(TrapPartyPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) { plugin.messages().send(sender, "generic.player-only"); return true; }
        if (args.length == 0) {
            plugin.kits().gui().open(p);
            return true;
        }
        Kit k = plugin.kits().get(args[0]);
        if (k == null) { plugin.messages().send(p, "kit.locked"); return true; }
        if (!k.canUse(p)) { plugin.messages().send(p, "kit.locked"); return true; }
        Game g = plugin.games().forPlayer(p);
        if (g == null) { plugin.messages().send(p, "generic.not-in-game"); return true; }
        g.selectKit(p, k);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> ids = new ArrayList<>();
            for (Kit k : plugin.kits().all()) ids.add(k.getId());
            return ids;
        }
        return Collections.emptyList();
    }
}

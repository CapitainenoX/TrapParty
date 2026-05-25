package fr.trapparty.commands;

import fr.trapparty.TrapPartyPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CraftsCommand implements CommandExecutor {

    private final TrapPartyPlugin plugin;

    public CraftsCommand(TrapPartyPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            plugin.messages().send(sender, "generic.player-only");
            return true;
        }
        plugin.crafts().open(p);
        return true;
    }
}

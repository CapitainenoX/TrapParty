package fr.trapparty.listeners;

import fr.trapparty.TrapPartyPlugin;
import fr.trapparty.game.Game;
import fr.trapparty.game.GameState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final TrapPartyPlugin plugin;

    public PlayerListener(TrapPartyPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        // tentative de reconnexion
        Game g = plugin.games().forPlayer(e.getPlayer().getUniqueId());
        if (g != null) g.reconnect(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Game g = plugin.games().forPlayer(e.getPlayer());
        if (g == null) return;
        boolean allowRejoin = plugin.configs().root().getBoolean("game.allow-rejoin", true);
        if (!allowRejoin || g.getState() == GameState.WAITING || g.getState() == GameState.STARTING) {
            plugin.games().leave(e.getPlayer());
        }
        // sinon, on garde le slot, la reconnexion sera gérée
    }
}

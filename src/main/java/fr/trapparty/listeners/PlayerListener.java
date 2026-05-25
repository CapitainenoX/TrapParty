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
        // envoi du resource pack
        sendResourcePack(e.getPlayer());
    }

    private void sendResourcePack(org.bukkit.entity.Player p) {
        var cfg = plugin.configs().root();
        if (!cfg.getBoolean("resource-pack.enabled", false)) return;
        String url = cfg.getString("resource-pack.url", "");
        if (url == null || url.isEmpty()) return;
        String sha1 = cfg.getString("resource-pack.sha1", "");
        boolean required = cfg.getBoolean("resource-pack.required", false);
        String prompt = fr.trapparty.config.MessagesManager.color(
                cfg.getString("resource-pack.prompt", "TrapParty resource pack"));
        try {
            // Modern Paper API : setResourcePack(url, hash, required, prompt)
            byte[] hash = decodeHex(sha1);
            p.setResourcePack(url, hash, prompt, required);
        } catch (Throwable t) {
            // fallback signature legacy
            try { p.setResourcePack(url); } catch (Throwable ignored) {}
        }
    }

    private static byte[] decodeHex(String hex) {
        if (hex == null || hex.isEmpty()) return new byte[0];
        hex = hex.replace(" ", "").toLowerCase();
        if (hex.length() % 2 != 0) return new byte[0];
        byte[] out = new byte[hex.length() / 2];
        for (int i = 0; i < out.length; i++) {
            out[i] = (byte) Integer.parseInt(hex.substring(i*2, i*2+2), 16);
        }
        return out;
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

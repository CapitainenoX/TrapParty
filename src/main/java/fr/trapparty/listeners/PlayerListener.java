package fr.trapparty.listeners;

import fr.trapparty.TrapPartyPlugin;
import fr.trapparty.game.Game;
import fr.trapparty.game.GamePlayer;
import fr.trapparty.game.GameState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class PlayerListener implements Listener {

    private final TrapPartyPlugin plugin;
    private final Set<String> commandWhitelist;

    public PlayerListener(TrapPartyPlugin plugin) {
        this.plugin = plugin;
        // Whitelist des commandes utilisables en partie (anti-cheat de base).
        List<String> raw = plugin.configs().root().getStringList("game.allowed-commands");
        this.commandWhitelist = new HashSet<>();
        if (raw.isEmpty()) {
            // Défauts raisonnables si l'admin n'a pas configuré
            commandWhitelist.add("tparty"); commandWhitelist.add("tp");
            commandWhitelist.add("trapparty"); commandWhitelist.add("kit");
            commandWhitelist.add("shop"); commandWhitelist.add("crafts");
            commandWhitelist.add("special"); commandWhitelist.add("items");
            commandWhitelist.add("spectate"); commandWhitelist.add("spec");
            commandWhitelist.add("msg"); commandWhitelist.add("r");
            commandWhitelist.add("tell"); commandWhitelist.add("w");
        } else {
            for (String s : raw) commandWhitelist.add(s.toLowerCase(Locale.ROOT));
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        // Reconnexion silencieuse dans une partie en cours
        Game g = plugin.games().forPlayer(e.getPlayer().getUniqueId());
        if (g != null) g.reconnect(e.getPlayer());
        sendResourcePack(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        Game g = plugin.games().forPlayer(e.getPlayer());
        if (g != null) {
            boolean allowRejoin = plugin.configs().root().getBoolean("game.allow-rejoin", true);
            if (!allowRejoin || g.getState() == GameState.WAITING || g.getState() == GameState.STARTING) {
                plugin.games().leave(e.getPlayer());
            }
        }
        // Cleanup TOUTES les caches pour éviter les fuites mémoire (audit sec #3)
        plugin.shop().handleClose(uuid);
        plugin.scoreboards().detach(e.getPlayer());
        plugin.bossbars().detach(e.getPlayer());
        plugin.traps().forgetTrigger(uuid);
        if (plugin.kits().gui() != null) plugin.kits().gui().handleClose(uuid);
        if (plugin.crafts() != null) plugin.crafts().handleClose(uuid);
    }

    /** Anti-cheat basique : bloque les commandes non whitelistées en partie. */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        if (p.hasPermission("trapparty.admin")) return;
        Game g = plugin.games().forPlayer(p);
        if (g == null) return;
        String msg = e.getMessage();
        if (msg.length() < 2) return;
        int sp = msg.indexOf(' ');
        String cmd = (sp < 0 ? msg.substring(1) : msg.substring(1, sp)).toLowerCase(Locale.ROOT);
        // strip eventual "plugin:cmd" prefix
        int colon = cmd.indexOf(':');
        if (colon >= 0) cmd = cmd.substring(colon + 1);
        if (!commandWhitelist.contains(cmd)) {
            e.setCancelled(true);
            p.sendMessage("§cCette commande est désactivée en partie.");
        }
    }

    /** Chat scopé : les joueurs en partie ne parlent qu'aux autres dans la même partie. */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
        if (!plugin.configs().root().getBoolean("game.scoped-chat", true)) return;
        Player p = e.getPlayer();
        Game g = plugin.games().forPlayer(p);
        if (g == null) return;
        Set<Player> recipients = new HashSet<>();
        for (GamePlayer gp : g.getPlayers()) {
            Player target = plugin.getServer().getPlayer(gp.getUuid());
            if (target != null) recipients.add(target);
        }
        for (UUID s : g.getSpectators()) {
            Player target = plugin.getServer().getPlayer(s);
            if (target != null) recipients.add(target);
        }
        e.getRecipients().retainAll(recipients);
    }

    // -------- Resource pack --------

    private void sendResourcePack(Player p) {
        var cfg = plugin.configs().root();
        if (!cfg.getBoolean("resource-pack.enabled", false)) return;
        String url = cfg.getString("resource-pack.url", "");
        if (url == null || url.isEmpty()) return;
        String sha1Hex = cfg.getString("resource-pack.sha1", "");
        boolean required = cfg.getBoolean("resource-pack.required", false);
        String prompt = fr.trapparty.config.MessagesManager.color(
                cfg.getString("resource-pack.prompt", "TrapParty resource pack"));

        byte[] hash;
        try { hash = decodeStrictHex(sha1Hex); }
        catch (IllegalArgumentException ex) {
            // Politique : on refuse d'envoyer un pack sans hash valide.
            plugin.getLogger().severe("Invalid resource-pack.sha1 (" + ex.getMessage()
                    + ") — pack NOT sent to " + p.getName()
                    + ". Run `bash scripts/build_pack.sh` to get a correct SHA-1.");
            return;
        }

        // Plusieurs signatures existent selon Paper. On essaye dans l'ordre.
        // Modern Paper : (UUID, String, byte[], String, boolean)
        try {
            UUID id = UUID.nameUUIDFromBytes(("trapparty:" + url).getBytes());
            p.setResourcePack(id, url, hash, prompt, required);
            return;
        } catch (Throwable ignored) {}
        // Spigot legacy : (String, byte[], String, boolean)
        try {
            p.setResourcePack(url, hash, prompt, required);
            return;
        } catch (Throwable ignored) {}
        // Vieille signature : (String, byte[], String)
        try {
            p.setResourcePack(url, hash, prompt);
            return;
        } catch (Throwable ignored) {}
        // Dernier recours sans hash
        try { p.setResourcePack(url); } catch (Throwable ignored) {}
    }

    /** SHA-1 = 40 hex chars STRICT. Lève IllegalArgumentException sinon. */
    private static byte[] decodeStrictHex(String hex) {
        if (hex == null) throw new IllegalArgumentException("sha1 is null");
        String clean = hex.replace(" ", "").trim();
        if (clean.length() != 40)
            throw new IllegalArgumentException("expected 40 hex chars, got " + clean.length());
        byte[] out = new byte[20];
        for (int i = 0; i < 20; i++) {
            int hi = Character.digit(clean.charAt(i*2), 16);
            int lo = Character.digit(clean.charAt(i*2 + 1), 16);
            if (hi < 0 || lo < 0)
                throw new IllegalArgumentException("non-hex char at position " + (i*2));
            out[i] = (byte) ((hi << 4) | lo);
        }
        return out;
    }
}

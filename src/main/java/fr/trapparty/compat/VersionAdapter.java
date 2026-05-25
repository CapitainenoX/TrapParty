package fr.trapparty.compat;

import fr.trapparty.TrapPartyPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.logging.Logger;

/**
 * Couche d'abstraction multi-version. Détecte la version serveur et expose
 * des helpers qui se reposent sur l'API Bukkit en s'adaptant aux APIs
 * apparues/disparues entre 1.13 et 1.21+.
 *
 * Pas de NMS direct : on utilise reflection uniquement quand l'API publique
 * ne suffit pas.
 */
public final class VersionAdapter {

    private final TrapPartyPlugin plugin;
    private final int major;     // ex: 1
    private final int minor;     // ex: 20
    private final int patch;     // ex: 4
    private final boolean paper;
    private final boolean adventure;

    private VersionAdapter(TrapPartyPlugin plugin, int major, int minor, int patch,
                           boolean paper, boolean adventure) {
        this.plugin = plugin;
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.paper = paper;
        this.adventure = adventure;
    }

    public static VersionAdapter detect(TrapPartyPlugin plugin) {
        String version = Bukkit.getBukkitVersion().split("-")[0]; // ex: 1.20.4-R0.1-SNAPSHOT
        String[] parts = version.split("\\.");
        int major = parseSafe(parts, 0, 1);
        int minor = parseSafe(parts, 1, 13);
        int patch = parseSafe(parts, 2, 0);

        boolean paper = classExists("io.papermc.paper.threadedregions.RegionizedServer")
                || classExists("com.destroystokyo.paper.PaperConfig")
                || classExists("io.papermc.paper.configuration.GlobalConfiguration");
        boolean adventure = classExists("net.kyori.adventure.text.Component");

        Logger log = plugin.getLogger();
        log.info("Detected MC " + major + "." + minor + "." + patch
                + " (paper=" + paper + ", adventure=" + adventure + ")");

        return new VersionAdapter(plugin, major, minor, patch, paper, adventure);
    }

    private static int parseSafe(String[] arr, int idx, int fallback) {
        try { return Integer.parseInt(arr[idx]); } catch (Exception e) { return fallback; }
    }

    private static boolean classExists(String name) {
        try { Class.forName(name); return true; } catch (Throwable t) { return false; }
    }

    public int major() { return major; }
    public int minor() { return minor; }
    public int patch() { return patch; }
    public boolean isPaper() { return paper; }
    public boolean hasAdventure() { return adventure; }

    public boolean isAtLeast(int targetMinor) { return major > 1 || minor >= targetMinor; }
    public boolean isAtLeast(int targetMinor, int targetPatch) {
        if (major > 1) return true;
        if (minor != targetMinor) return minor > targetMinor;
        return patch >= targetPatch;
    }

    public String describe() {
        return (paper ? "Paper" : "Spigot/Bukkit") + " 1." + minor + "." + patch
                + (adventure ? " +Adventure" : "");
    }

    /**
     * Récupère une Material en s'adaptant aux renommages (1.13 → 1.21+).
     * Si plusieurs candidats fournis, on retourne le premier non-null.
     */
    public Material material(String... candidates) {
        for (String c : candidates) {
            try {
                Material m = Material.matchMaterial(c);
                if (m != null) return m;
            } catch (Throwable ignored) {}
        }
        return Material.STONE;
    }

    /**
     * Envoie un titre à un joueur. Utilise l'API Adventure si disponible,
     * sinon l'API Bukkit Player#sendTitle (1.11+).
     */
    public void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        try {
            player.sendTitle(title == null ? "" : title, subtitle == null ? "" : subtitle,
                    fadeIn, stay, fadeOut);
        } catch (Throwable t) {
            player.sendMessage(title);
            if (subtitle != null && !subtitle.isEmpty()) player.sendMessage(subtitle);
        }
    }

    public void sendActionBar(Player player, String text) {
        // Voie universelle Spigot 1.11+ : BaseComponent via Spigot chat API
        try {
            net.md_5.bungee.api.chat.BaseComponent[] components =
                    net.md_5.bungee.api.chat.TextComponent.fromLegacyText(text == null ? "" : text);
            player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, components);
        } catch (Throwable t) {
            player.sendMessage(text);
        }
    }

    /**
     * Effet de particule cross-version. Sur 1.13+ on a Particle directement.
     * On expose un helper neutre nommé.
     */
    public void particle(Location loc, String particleName, int count, double dx, double dy, double dz, double speed) {
        try {
            org.bukkit.Particle p = org.bukkit.Particle.valueOf(particleName);
            loc.getWorld().spawnParticle(p, loc, count, dx, dy, dz, speed);
        } catch (Throwable ignored) {
            // fallback : try a common particle
            try { loc.getWorld().spawnParticle(org.bukkit.Particle.CLOUD, loc, count); } catch (Throwable ignored2) {}
        }
    }

    /**
     * Joue un son cross-version (utilise le nom string pour éviter les enum manquants).
     */
    public void sound(Player player, String soundName, float volume, float pitch) {
        try {
            player.playSound(player.getLocation(), org.bukkit.Sound.valueOf(soundName), volume, pitch);
        } catch (Throwable ignored) {
            // certaines versions exposent les noms namespaced ; on tente la version string
            try { player.playSound(player.getLocation(), soundName, volume, pitch); } catch (Throwable ignored2) {}
        }
    }

    /**
     * Définit une couleur d'armure en cuir, cross-version.
     */
    public ItemStack dyeLeather(ItemStack item, int rgb) {
        if (item == null || item.getType().name().startsWith("LEATHER_") == false) return item;
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof org.bukkit.inventory.meta.LeatherArmorMeta lam) {
            lam.setColor(Color.fromRGB(rgb));
            item.setItemMeta(lam);
        }
        return item;
    }
}

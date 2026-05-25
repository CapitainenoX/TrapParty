package fr.trapparty.world;

import fr.trapparty.TrapPartyPlugin;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

/**
 * Gestion des mondes temporaires : clone d'un template, chargement,
 * déchargement et suppression. Intégration MultiVerse-Core v4/v5 si présent.
 *
 * Sécurité :
 * - Tous les noms de monde sont validés contre une regex stricte avant
 *   toute opération filesystem (anti path-traversal).
 * - Toute opération filesystem vérifie via getCanonicalPath() qu'elle
 *   reste DANS le worldContainer.
 * - Au boot, on purge les mondes orphelins (prefix tp_) laissés par un
 *   crash précédent.
 */
public class WorldManager {

    /** Nom de monde valide : alphanum + tiret/underscore, 1-48 chars. */
    private static final Pattern SAFE_NAME = Pattern.compile("[a-zA-Z0-9_\\-]{1,48}");
    /** Préfixe utilisé pour les mondes temporaires créés par le plugin. */
    public static final String TEMP_WORLD_PREFIX = "tp_";

    private final TrapPartyPlugin plugin;
    private final MultiverseHook multiverse;
    private final Set<String> managedWorlds = ConcurrentHashMapSetWrapper.newSet();
    private final File container;
    private final Path containerCanonical;

    public WorldManager(TrapPartyPlugin plugin) {
        this.plugin = plugin;
        this.container = Bukkit.getWorldContainer();
        Path cp;
        try { cp = container.getCanonicalFile().toPath(); }
        catch (IOException e) { cp = container.toPath().toAbsolutePath().normalize(); }
        this.containerCanonical = cp;
        this.multiverse = MultiverseHook.tryHook(plugin);
        if (multiverse != null) {
            plugin.getLogger().info("Hooked Multiverse-Core: " + multiverse.describe());
        }
        purgeOrphans();
    }

    /** Purge au démarrage les dossiers `tp_*` orphelins (crash précédent). */
    public void purgeOrphans() {
        File[] all = container.listFiles();
        if (all == null) return;
        int purged = 0;
        for (File f : all) {
            if (!f.isDirectory()) continue;
            String name = f.getName();
            if (!name.startsWith(TEMP_WORLD_PREFIX)) continue;
            if (!SAFE_NAME.matcher(name).matches()) continue;
            if (Bukkit.getWorld(name) != null) continue; // déjà chargé
            try {
                Path resolved = f.getCanonicalFile().toPath();
                if (!resolved.startsWith(containerCanonical)) continue; // safety
                deleteRecursive(resolved);
                purged++;
            } catch (IOException ex) {
                plugin.getLogger().warning("Failed to purge orphan world " + name + ": " + ex.getMessage());
            }
        }
        if (purged > 0) plugin.getLogger().info("Purged " + purged + " orphan TrapParty world(s).");
    }

    /**
     * Clone un template vers un nouveau nom de monde et le charge.
     * Async pour la copie, main thread pour le load.
     */
    public CompletableFuture<World> cloneAndLoad(String templateName, String targetName) {
        CompletableFuture<World> future = new CompletableFuture<>();

        if (!SAFE_NAME.matcher(templateName).matches()) {
            future.completeExceptionally(new IllegalArgumentException("Unsafe template name: " + templateName));
            return future;
        }
        if (!SAFE_NAME.matcher(targetName).matches()) {
            future.completeExceptionally(new IllegalArgumentException("Unsafe target name: " + targetName));
            return future;
        }
        if (Bukkit.getWorld(targetName) != null) {
            future.completeExceptionally(new IllegalStateException("World already loaded: " + targetName));
            return future;
        }

        boolean async = plugin.configs().root().getBoolean("performance.async-world-ops", true);
        Runnable copy = () -> {
            try {
                File src = new File(container, templateName);
                File dst = new File(container, targetName);
                Path srcCanon = src.getCanonicalFile().toPath();
                if (!srcCanon.startsWith(containerCanonical) || !src.isDirectory()) {
                    future.completeExceptionally(new IllegalStateException("Template world missing or outside container: " + templateName));
                    return;
                }
                Path dstCanon = dst.getCanonicalFile().toPath();
                if (!dstCanon.startsWith(containerCanonical)) {
                    future.completeExceptionally(new SecurityException("Target outside worldContainer: " + targetName));
                    return;
                }
                if (dst.exists()) deleteRecursive(dstCanon);
                copyDir(srcCanon, dstCanon);
                deleteIfExists(new File(dst, "uid.dat"));
                deleteIfExists(new File(dst, "session.lock"));

                Bukkit.getScheduler().runTask(plugin, () -> {
                    try {
                        World w = loadWorld(targetName);
                        if (w == null) future.completeExceptionally(new IllegalStateException("Load returned null"));
                        else {
                            managedWorlds.add(targetName);
                            future.complete(w);
                        }
                    } catch (Throwable t) {
                        future.completeExceptionally(t);
                    }
                });
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        };

        if (async) Bukkit.getScheduler().runTaskAsynchronously(plugin, copy);
        else copy.run();

        return future;
    }

    private World loadWorld(String name) {
        if (multiverse != null) {
            World w = multiverse.importWorld(name);
            if (w != null) return w;
        }
        return new WorldCreator(name).createWorld();
    }

    public void unloadAndDelete(String worldName) {
        if (!SAFE_NAME.matcher(worldName).matches()) {
            plugin.getLogger().warning("Refused to delete unsafe world name: " + worldName);
            return;
        }
        // Idempotent : si déjà retiré, no-op
        if (!managedWorlds.remove(worldName) && Bukkit.getWorld(worldName) == null) return;

        World w = Bukkit.getWorld(worldName);
        if (w != null) {
            World fallback = Bukkit.getWorlds().get(0);
            for (Player p : new ArrayList<>(w.getPlayers())) {
                p.teleport(fallback.getSpawnLocation());
            }
            if (multiverse != null) multiverse.unloadWorld(worldName);
            else Bukkit.unloadWorld(w, false);
        }

        boolean deleteFiles = plugin.configs().root().getBoolean("arena.cleanup.delete-files", true);
        if (!deleteFiles) return;

        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            File dir = new File(container, worldName);
            if (!dir.exists()) return;
            try {
                Path resolved = dir.getCanonicalFile().toPath();
                if (!resolved.startsWith(containerCanonical)) return;
                deleteRecursive(resolved);
            } catch (IOException ex) {
                plugin.getLogger().warning("Failed to delete temp world " + worldName + ": " + ex.getMessage());
            }
        }, 40L);
    }

    public void cleanupAll() {
        for (String w : new ArrayList<>(managedWorlds)) {
            try { unloadAndDelete(w); } catch (Throwable ignored) {}
        }
    }

    public boolean isManaged(String name) { return managedWorlds.contains(name); }

    // --------- File helpers ----------

    private void copyDir(Path src, Path dst) throws IOException {
        Files.walkFileTree(src, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Files.createDirectories(dst.resolve(src.relativize(dir)));
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String name = file.getFileName().toString();
                if (name.equals("uid.dat") || name.equals("session.lock")) return FileVisitResult.CONTINUE;
                Files.copy(file, dst.resolve(src.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void deleteRecursive(Path root) throws IOException {
        if (!Files.exists(root)) return;
        // Final safety check
        if (!root.toAbsolutePath().normalize().startsWith(containerCanonical)) {
            throw new SecurityException("Refused deletion outside worldContainer: " + root);
        }
        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override public FileVisitResult visitFile(Path f, BasicFileAttributes a) throws IOException { Files.delete(f); return FileVisitResult.CONTINUE; }
            @Override public FileVisitResult postVisitDirectory(Path d, IOException e) throws IOException { Files.delete(d); return FileVisitResult.CONTINUE; }
        });
    }

    private void deleteIfExists(File f) { if (f.exists()) f.delete(); }

    private static final class ConcurrentHashMapSetWrapper {
        static <T> Set<T> newSet() {
            return java.util.Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<>());
        }
    }
}

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

/**
 * Gestion des mondes temporaires : clone d'un template, chargement,
 * déchargement et suppression. Intégration MultiVerse-Core si présent.
 */
public class WorldManager {

    private final TrapPartyPlugin plugin;
    private final MultiverseHook multiverse;
    private final Set<String> managedWorlds = ConcurrentHashMapSetWrapper.newSet();
    private final File container;

    public WorldManager(TrapPartyPlugin plugin) {
        this.plugin = plugin;
        this.container = Bukkit.getWorldContainer();
        this.multiverse = MultiverseHook.tryHook(plugin);
        if (multiverse != null) {
            plugin.getLogger().info("Hooked Multiverse-Core: " + multiverse.describe());
        }
    }

    /**
     * Clone un template vers un nouveau nom de monde et le charge.
     * Si MultiVerse présent, on l'utilise. Sinon, copie de fichiers + WorldCreator.
     */
    public CompletableFuture<World> cloneAndLoad(String templateName, String targetName) {
        CompletableFuture<World> future = new CompletableFuture<>();

        boolean async = plugin.configs().root().getBoolean("performance.async-world-ops", true);
        Runnable copy = () -> {
            try {
                File src = new File(container, templateName);
                if (!src.exists() || !src.isDirectory()) {
                    future.completeExceptionally(new IllegalStateException("Template world missing: " + templateName));
                    return;
                }
                File dst = new File(container, targetName);
                if (dst.exists()) deleteRecursive(dst.toPath());
                copyDir(src.toPath(), dst.toPath());
                // remove uid/session files
                deleteIfExists(new File(dst, "uid.dat"));
                deleteIfExists(new File(dst, "session.lock"));

                // load on main thread
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

    /** Décharge un monde et supprime ses fichiers. */
    public void unloadAndDelete(String worldName) {
        World w = Bukkit.getWorld(worldName);
        if (w != null) {
            // évacuer joueurs restants
            World fallback = Bukkit.getWorlds().get(0);
            for (Player p : new ArrayList<>(w.getPlayers())) {
                p.teleport(fallback.getSpawnLocation());
            }
            if (multiverse != null) multiverse.unloadWorld(worldName);
            else Bukkit.unloadWorld(w, false);
        }
        managedWorlds.remove(worldName);

        boolean deleteFiles = plugin.configs().root().getBoolean("arena.cleanup.delete-files", true);
        if (!deleteFiles) return;

        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            File dir = new File(container, worldName);
            if (dir.exists()) {
                try {
                    deleteRecursive(dir.toPath());
                } catch (IOException ex) {
                    plugin.getLogger().warning("Failed to delete temp world " + worldName + ": " + ex.getMessage());
                }
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
        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override public FileVisitResult visitFile(Path f, BasicFileAttributes a) throws IOException { Files.delete(f); return FileVisitResult.CONTINUE; }
            @Override public FileVisitResult postVisitDirectory(Path d, IOException e) throws IOException { Files.delete(d); return FileVisitResult.CONTINUE; }
        });
    }

    private void deleteIfExists(File f) { if (f.exists()) f.delete(); }

    // Tiny wrapper to avoid name collision with Bukkit ConcurrentHashMap import noise
    private static final class ConcurrentHashMapSetWrapper {
        static <T> Set<T> newSet() {
            return java.util.Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<>());
        }
    }
}

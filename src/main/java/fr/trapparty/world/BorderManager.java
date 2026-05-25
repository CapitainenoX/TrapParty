package fr.trapparty.world;

import fr.trapparty.TrapPartyPlugin;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;

/**
 * Wrapper pour la WorldBorder Bukkit. Cette API existe depuis 1.8 et a peu
 * changé, donc on l'utilise directement.
 */
public class BorderManager {

    private final TrapPartyPlugin plugin;

    public BorderManager(TrapPartyPlugin plugin) {
        this.plugin = plugin;
    }

    public void setBorder(World world, Location center, double size) {
        WorldBorder b = world.getWorldBorder();
        b.setCenter(center);
        b.setSize(size);
        b.setDamageBuffer(plugin.configs().root().getInt("arena.border.warning-distance", 5));
        b.setDamageAmount(plugin.configs().root().getDouble("arena.border.damage-per-block", 0.4));
        b.setWarningDistance(8);
    }

    public void shrink(World world, double targetSize, long seconds) {
        WorldBorder b = world.getWorldBorder();
        b.setSize(targetSize, Math.max(1, seconds));
    }
}

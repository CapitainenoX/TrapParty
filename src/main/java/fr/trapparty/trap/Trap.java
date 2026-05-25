package fr.trapparty.trap;

import org.bukkit.Location;

import java.util.UUID;

/**
 * Trap "léger" : représente un piège placé par un joueur. La logique
 * de déclenchement est résolue dans le TrapListener via les events
 * Bukkit (PlayerInteractEvent, EntityExplodeEvent, PlayerMoveEvent).
 *
 * On garde la trace de l'owner pour attribuer les kills.
 */
public class Trap {

    public enum Type { TNT, COBWEB, TRIPWIRE, LAVA, MAGMA, CACTUS, FALL_PIT, FAKE_BLOCK, REDSTONE }

    private final UUID owner;
    private final Type type;
    private final Location location;
    private final long placedAt;

    public Trap(UUID owner, Type type, Location location) {
        this.owner = owner;
        this.type = type;
        this.location = location;
        this.placedAt = System.currentTimeMillis();
    }

    public UUID getOwner() { return owner; }
    public Type getType() { return type; }
    public Location getLocation() { return location; }
    public long getPlacedAt() { return placedAt; }
}

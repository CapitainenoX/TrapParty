package org.bukkit.event.entity;
import org.bukkit.event.Cancellable;
public class EntityDamageEvent extends EntityEvent implements Cancellable {
    public enum DamageCause {
        FALL, FIRE, FIRE_TICK, LAVA, HOT_FLOOR, CONTACT, SUFFOCATION,
        ENTITY_EXPLOSION, BLOCK_EXPLOSION, PROJECTILE, ENTITY_ATTACK,
        VOID, DROWNING, STARVATION, MAGIC, POISON, WITHER, CUSTOM
    }
    public DamageCause getCause(){return null;}
    @Override public boolean isCancelled(){return false;}
    @Override public void setCancelled(boolean c){}
}

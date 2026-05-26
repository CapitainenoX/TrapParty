package org.bukkit;
import org.bukkit.entity.*;
import org.bukkit.block.Block;
import org.bukkit.util.RayTraceResult;
import java.util.*;
public interface World {
    String getName();
    UUID getUID();
    Location getSpawnLocation();
    int getHighestBlockYAt(Location loc);
    int getHighestBlockYAt(int x, int z);
    Block getBlockAt(int x,int y,int z);
    Block getBlockAt(Location loc);
    void setPVP(boolean enable);
    boolean getPVP();
    void setTime(long t);
    long getTime();
    void setStorm(boolean storm);
    void setThundering(boolean thundering);
    void setWeatherDuration(int ticks);
    void setSpawnFlags(boolean monsters, boolean animals);
    void setGameRuleValue(String rule, String value);
    <T> void setGameRule(GameRule<T> rule, T value);
    LightningStrike strikeLightning(Location loc);
    LightningStrike strikeLightningEffect(Location loc);
    <T extends Entity> T spawn(Location loc, Class<T> clazz);
    <T extends Entity> T spawn(Location loc, Class<T> clazz, java.util.function.Consumer<T> consumer);
    Collection<Entity> getNearbyEntities(Location loc, double x, double y, double z);
    List<Player> getPlayers();
    WorldBorder getWorldBorder();
    void spawnParticle(Particle p, Location loc, int count);
    void spawnParticle(Particle p, Location loc, int count, double x, double y, double z, double speed);
    enum Environment { NORMAL, NETHER, THE_END }
}

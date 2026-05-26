#!/usr/bin/env python3
"""Génère des stubs Bukkit/Paper/PAPI/Bungee minimalistes pour qu'un javac
local puisse vérifier la syntaxe et les usages d'API. Aucune sémantique runtime.
"""
import os, pathlib

OUT = pathlib.Path("stubs/src")
OUT.mkdir(parents=True, exist_ok=True)

def w(path, content):
    p = OUT / path
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(content.strip() + "\n")

# ========== org.bukkit (core) ==========

w("org/bukkit/Bukkit.java", """
package org.bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.ScoreboardManager;
import java.util.*;
public final class Bukkit {
    private Bukkit(){}
    public static Server getServer(){return null;}
    public static PluginManager getPluginManager(){return null;}
    public static ServicesManager getServicesManager(){return null;}
    public static BukkitScheduler getScheduler(){return null;}
    public static ScoreboardManager getScoreboardManager(){return null;}
    public static Player getPlayer(UUID uuid){return null;}
    public static Player getPlayer(String name){return null;}
    public static Player getPlayerExact(String name){return null;}
    public static OfflinePlayer getOfflinePlayer(UUID uuid){return null;}
    public static OfflinePlayer getOfflinePlayer(String name){return null;}
    public static Collection<? extends Player> getOnlinePlayers(){return Collections.emptyList();}
    public static World getWorld(String name){return null;}
    public static World getWorld(UUID uid){return null;}
    public static List<World> getWorlds(){return Collections.emptyList();}
    public static File getWorldContainer(){return null;}
    public static boolean unloadWorld(World w, boolean save){return false;}
    public static boolean unloadWorld(String name, boolean save){return false;}
    public static Inventory createInventory(InventoryHolder owner, int size, String title){return null;}
    public static String getBukkitVersion(){return "26.1.2-R0.1-SNAPSHOT";}
    public static BossBar createBossBar(String title, BarColor color, BarStyle style){return null;}
    public static void broadcastMessage(String msg){}
    public static Entity getEntity(UUID uuid){return null;}
}
""")

w("org/bukkit/Server.java", """
package org.bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.scheduler.BukkitScheduler;
import java.util.*;
public interface Server {
    PluginManager getPluginManager();
    ServicesManager getServicesManager();
    BukkitScheduler getScheduler();
    Player getPlayer(UUID uuid);
    Player getPlayerExact(String name);
    Collection<? extends Player> getOnlinePlayers();
    List<World> getWorlds();
    void broadcastMessage(String msg);
}
""")

# Import-only helper used by Bukkit class
w("org/bukkit/InventoryHolder.java", """
package org.bukkit;
public interface InventoryHolder {}
""")

# Use the right pkg for InventoryHolder & boss helpers
w("org/bukkit/inventory/InventoryHolder.java", """
package org.bukkit.inventory;
public interface InventoryHolder { default Inventory getInventory(){return null;} }
""")

# We import org.bukkit.InventoryHolder in Bukkit.java above; let's adjust.
# Recreate Bukkit.java with correct imports.
w("org/bukkit/Bukkit.java", """
package org.bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import java.io.File;
import java.util.*;
public final class Bukkit {
    private Bukkit(){}
    public static Server getServer(){return null;}
    public static PluginManager getPluginManager(){return null;}
    public static ServicesManager getServicesManager(){return null;}
    public static BukkitScheduler getScheduler(){return null;}
    public static ScoreboardManager getScoreboardManager(){return null;}
    public static Player getPlayer(UUID uuid){return null;}
    public static Player getPlayer(String name){return null;}
    public static Player getPlayerExact(String name){return null;}
    public static OfflinePlayer getOfflinePlayer(UUID uuid){return null;}
    public static OfflinePlayer getOfflinePlayer(String name){return null;}
    public static Collection<? extends Player> getOnlinePlayers(){return Collections.emptyList();}
    public static World getWorld(String name){return null;}
    public static World getWorld(UUID uid){return null;}
    public static List<World> getWorlds(){return Collections.emptyList();}
    public static File getWorldContainer(){return null;}
    public static boolean unloadWorld(World w, boolean save){return false;}
    public static boolean unloadWorld(String name, boolean save){return false;}
    public static Inventory createInventory(InventoryHolder owner, int size, String title){return null;}
    public static String getBukkitVersion(){return "26.1.2-R0.1-SNAPSHOT";}
    public static BossBar createBossBar(String title, BarColor color, BarStyle style){return null;}
    public static void broadcastMessage(String msg){}
    public static Entity getEntity(UUID uuid){return null;}
}
""")

w("org/bukkit/OfflinePlayer.java", """
package org.bukkit;
import org.bukkit.entity.Player;
import java.util.UUID;
public interface OfflinePlayer {
    UUID getUniqueId();
    String getName();
    boolean isOnline();
    Player getPlayer();
}
""")

w("org/bukkit/ChatColor.java", """
package org.bukkit;
public enum ChatColor {
    BLACK, DARK_BLUE, DARK_GREEN, DARK_AQUA, DARK_RED, DARK_PURPLE, GOLD, GRAY,
    DARK_GRAY, BLUE, GREEN, AQUA, RED, LIGHT_PURPLE, YELLOW, WHITE,
    MAGIC, BOLD, STRIKETHROUGH, UNDERLINE, ITALIC, RESET;
    public static final char COLOR_CHAR = (char)0xA7;
    public static String translateAlternateColorCodes(char altCol, String text){return text;}
    public char getChar(){return ' ';}
    @Override public String toString(){return "§"+name().charAt(0);}
}
""")

w("org/bukkit/Color.java", """
package org.bukkit;
public final class Color {
    public static Color fromRGB(int rgb){return new Color();}
    public static Color fromRGB(int r,int g,int b){return new Color();}
    public int asRGB(){return 0;}
}
""")

w("org/bukkit/GameMode.java", """
package org.bukkit;
public enum GameMode { SURVIVAL, CREATIVE, ADVENTURE, SPECTATOR }
""")

w("org/bukkit/Location.java", """
package org.bukkit;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
public class Location implements Cloneable {
    public Location(World w, double x, double y, double z){}
    public Location(World w, double x, double y, double z, float yaw, float pitch){}
    public World getWorld(){return null;}
    public double getX(){return 0;}
    public double getY(){return 0;}
    public double getZ(){return 0;}
    public float getYaw(){return 0;}
    public float getPitch(){return 0;}
    public int getBlockX(){return 0;}
    public int getBlockY(){return 0;}
    public int getBlockZ(){return 0;}
    public Block getBlock(){return null;}
    public Vector getDirection(){return new Vector();}
    public void setY(double y){}
    public Vector toVector(){return new Vector();}
    public Location add(double x,double y,double z){return this;}
    public Location add(Vector v){return this;}
    public Location subtract(Location other){return this;}
    @Override public Location clone(){return this;}
}
""")

w("org/bukkit/NamespacedKey.java", """
package org.bukkit;
public class NamespacedKey {
    public NamespacedKey(String ns, String key){}
    public NamespacedKey(org.bukkit.plugin.Plugin plugin, String key){}
    public static NamespacedKey minecraft(String key){return new NamespacedKey("minecraft", key);}
    public String getNamespace(){return "";}
    public String getKey(){return "";}
}
""")

w("org/bukkit/Registry.java", """
package org.bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
public interface Registry<T> {
    T get(NamespacedKey key);
    Registry<Enchantment> ENCHANTMENT = null;
    Registry<PotionEffectType> EFFECT = null;
    Registry<Attribute> ATTRIBUTE = null;
    Registry<PotionType> POTION = null;
}
""")

w("org/bukkit/Particle.java", """
package org.bukkit;
public enum Particle { CLOUD, EXPLOSION, EXPLOSION_NORMAL, CRIT, FLAME, LAVA, SMOKE }
""")

w("org/bukkit/Sound.java", """
package org.bukkit;
public enum Sound { ENTITY_TNT_PRIMED, BLOCK_NOTE_BLOCK_PLING, BLOCK_STONE_BREAK }
""")

w("org/bukkit/WorldType.java", """
package org.bukkit;
public enum WorldType { NORMAL, FLAT, LARGE_BIOMES, AMPLIFIED }
""")

w("org/bukkit/WorldCreator.java", """
package org.bukkit;
public class WorldCreator {
    public WorldCreator(String name){}
    public World createWorld(){return null;}
}
""")

w("org/bukkit/WorldBorder.java", """
package org.bukkit;
public interface WorldBorder {
    void setCenter(Location loc);
    void setSize(double size);
    void setSize(double size, long seconds);
    void setDamageBuffer(double n);
    void setDamageAmount(double n);
    void setWarningDistance(int d);
}
""")

w("org/bukkit/GameRule.java", """
package org.bukkit;
public final class GameRule<T> {
    public static GameRule<?> getByName(String name){return null;}
    public String getName(){return "";}
}
""")

w("org/bukkit/World.java", """
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
    <T extends Entity> T spawn(Location loc, Class<T> clazz, org.bukkit.util.Consumer<T> consumer);
    <T extends Entity> T spawn(Location loc, Class<T> clazz, java.util.function.Consumer<T> consumer);
    Collection<Entity> getNearbyEntities(Location loc, double x, double y, double z);
    List<Player> getPlayers();
    WorldBorder getWorldBorder();
    void spawnParticle(Particle p, Location loc, int count);
    void spawnParticle(Particle p, Location loc, int count, double x, double y, double z, double speed);
    enum Environment { NORMAL, NETHER, THE_END }
}
""")

# Some Bukkit code uses org.bukkit.util.Consumer; modern API uses java.util.function.Consumer
w("org/bukkit/util/Consumer.java", """
package org.bukkit.util;
@FunctionalInterface
public interface Consumer<T> { void accept(T t); }
""")

# ========== Material ==========
w("org/bukkit/Material.java", """
package org.bukkit;
public enum Material {
    AIR, STONE, COBBLESTONE, BEDROCK, WATER, LAVA, CHEST, TNT, COBWEB, STRING,
    TRIPWIRE_HOOK, MAGMA_BLOCK, CACTUS, SOUL_SAND, IRON_PICKAXE, NETHERITE_PICKAXE,
    DIAMOND_PICKAXE, IRON_SWORD, STONE_SWORD, IRON_AXE, BOW, ARROW, SHIELD, COMPASS,
    BLAZE_ROD, GRAY_DYE, FISHING_ROD, SPYGLASS, AMETHYST_SHARD, PRISMARINE_SHARD,
    LAVA_BUCKET, MILK_BUCKET, WATER_BUCKET, FIRE_CHARGE, WIND_CHARGE, FLINT_AND_STEEL,
    POTION, ENCHANTED_GOLDEN_APPLE, GOLDEN_APPLE, COOKED_BEEF, OAK_PLANKS, OBSIDIAN,
    TUFF, POLISHED_TUFF, GLASS, SLIME_BLOCK, HONEY_BLOCK, RESIN_BLOCK, TERRACOTTA,
    POINTED_DRIPSTONE, CREAKING_HEART, REDSTONE_BLOCK, REDSTONE, REDSTONE_TORCH,
    REDSTONE_LAMP, REPEATER, PISTON, STICKY_PISTON, OBSERVER, COPPER_BULB,
    CRAFTER, DISPENSER, CRAFTING_TABLE, ENDER_PEARL, FEATHER, SCAFFOLDING,
    BREEZE_ROD, HEAVY_CORE, BUNDLE, CROSSBOW, OMINOUS_BOTTLE, GLASS_BOTTLE,
    GOLDEN_DANDELION, GOLDEN_CARROT, GOLD_NUGGET, NETHER_STAR, BARRIER,
    LIME_DYE, RED_DYE, WHITE_STAINED_GLASS_PANE, GLASS_PANE, STAINED_GLASS_PANE,
    LEATHER_HELMET, LEATHER_CHESTPLATE, LEATHER_LEGGINGS, LEATHER_BOOTS,
    CHAINMAIL_HELMET, CHAINMAIL_CHESTPLATE, CHAINMAIL_LEGGINGS, CHAINMAIL_BOOTS,
    IRON_HELMET, IRON_CHESTPLATE, IRON_LEGGINGS, IRON_BOOTS,
    COPPER_HELMET, NETHERITE_AXE, NETHERITE_SWORD, DIAMOND_SWORD, DIAMOND_AXE,
    SNOWBALL, MACE, TRIAL_KEY, PAPER, STICK;
    public String name(){return super.name();}
    public boolean isSolid(){return true;}
    public boolean isAir(){return this == AIR;}
    public static Material matchMaterial(String name){
        if (name == null) return null;
        try { return Material.valueOf(name.toUpperCase()); } catch (Exception e) { return null; }
    }
}
""")

# attribute
w("org/bukkit/attribute/Attribute.java", """
package org.bukkit.attribute;
import org.bukkit.NamespacedKey;
import org.bukkit.Keyed;
public enum Attribute implements Keyed {
    MAX_HEALTH, GENERIC_MAX_HEALTH, GENERIC_MOVEMENT_SPEED;
    public NamespacedKey getKey(){return null;}
}
""")
w("org/bukkit/attribute/AttributeInstance.java", """
package org.bukkit.attribute;
public interface AttributeInstance { double getValue(); void setBaseValue(double v); }
""")
w("org/bukkit/Keyed.java", """
package org.bukkit;
public interface Keyed { NamespacedKey getKey(); }
""")

# block
w("org/bukkit/block/Block.java", """
package org.bukkit.block;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
public interface Block {
    Material getType();
    void setType(Material m);
    Location getLocation();
    Block getRelative(int dx, int dy, int dz);
    Block getRelative(BlockFace face);
    BlockState getState();
    World getWorld();
    boolean breakNaturally(ItemStack tool);
}
""")
w("org/bukkit/block/BlockState.java", """
package org.bukkit.block;
public interface BlockState {}
""")
w("org/bukkit/block/Chest.java", """
package org.bukkit.block;
import org.bukkit.inventory.Inventory;
public interface Chest extends BlockState { Inventory getInventory(); }
""")
w("org/bukkit/block/BlockFace.java", """
package org.bukkit.block;
public enum BlockFace { UP, DOWN, NORTH, SOUTH, EAST, WEST, SELF }
""")

# boss
w("org/bukkit/boss/BarColor.java", """
package org.bukkit.boss;
public enum BarColor { PURPLE, GREEN, YELLOW, RED, WHITE, BLUE, PINK }
""")
w("org/bukkit/boss/BarStyle.java", """
package org.bukkit.boss;
public enum BarStyle { SOLID, SEGMENTED_6, SEGMENTED_10, SEGMENTED_12, SEGMENTED_20 }
""")
w("org/bukkit/boss/BossBar.java", """
package org.bukkit.boss;
import org.bukkit.entity.Player;
public interface BossBar {
    void setTitle(String t);
    void setColor(BarColor c);
    void setProgress(double p);
    void addPlayer(Player p);
    void removePlayer(Player p);
    void removeAll();
}
""")

# command
w("org/bukkit/command/Command.java", """
package org.bukkit.command;
public class Command { public String getName(){return "";} }
""")
w("org/bukkit/command/CommandSender.java", """
package org.bukkit.command;
public interface CommandSender { void sendMessage(String msg); boolean hasPermission(String perm); }
""")
w("org/bukkit/command/CommandExecutor.java", """
package org.bukkit.command;
public interface CommandExecutor {
    boolean onCommand(CommandSender sender, Command command, String label, String[] args);
}
""")
w("org/bukkit/command/TabCompleter.java", """
package org.bukkit.command;
import java.util.List;
public interface TabCompleter {
    List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args);
}
""")
w("org/bukkit/command/PluginCommand.java", """
package org.bukkit.command;
public class PluginCommand extends Command {
    public void setExecutor(CommandExecutor e){}
    public void setTabCompleter(TabCompleter t){}
}
""")

# configuration
w("org/bukkit/configuration/ConfigurationSection.java", """
package org.bukkit.configuration;
import java.util.*;
public interface ConfigurationSection {
    Set<String> getKeys(boolean deep);
    String getString(String path);
    String getString(String path, String def);
    int getInt(String path);
    int getInt(String path, int def);
    double getDouble(String path);
    double getDouble(String path, double def);
    boolean getBoolean(String path);
    boolean getBoolean(String path, boolean def);
    List<String> getStringList(String path);
    List<Map<?,?>> getMapList(String path);
    ConfigurationSection getConfigurationSection(String path);
    ConfigurationSection createSection(String path);
    void set(String path, Object value);
    boolean contains(String path);
}
""")
w("org/bukkit/configuration/file/FileConfiguration.java", """
package org.bukkit.configuration.file;
import org.bukkit.configuration.ConfigurationSection;
import java.io.File;
import java.io.IOException;
import java.util.*;
public abstract class FileConfiguration implements ConfigurationSection {
    public void save(File f) throws IOException {}
    public void save(java.nio.file.Path p) throws IOException {}
    @Override public Set<String> getKeys(boolean deep){return Collections.emptySet();}
    @Override public String getString(String p){return null;}
    @Override public String getString(String p, String d){return d;}
    @Override public int getInt(String p){return 0;}
    @Override public int getInt(String p, int d){return d;}
    @Override public double getDouble(String p){return 0;}
    @Override public double getDouble(String p, double d){return d;}
    @Override public boolean getBoolean(String p){return false;}
    @Override public boolean getBoolean(String p, boolean d){return d;}
    @Override public List<String> getStringList(String p){return Collections.emptyList();}
    @Override public List<Map<?,?>> getMapList(String p){return Collections.emptyList();}
    @Override public ConfigurationSection getConfigurationSection(String p){return null;}
    @Override public ConfigurationSection createSection(String p){return null;}
    @Override public void set(String p, Object v){}
    @Override public boolean contains(String p){return false;}
}
""")
w("org/bukkit/configuration/file/YamlConfiguration.java", """
package org.bukkit.configuration.file;
import java.io.File;
public class YamlConfiguration extends FileConfiguration {
    public static YamlConfiguration loadConfiguration(File f){return new YamlConfiguration();}
}
""")

# enchantments
w("org/bukkit/enchantments/Enchantment.java", """
package org.bukkit.enchantments;
import org.bukkit.NamespacedKey;
import org.bukkit.Keyed;
public class Enchantment implements Keyed {
    public static final Enchantment LURE = new Enchantment();
    public static final Enchantment SHARPNESS = new Enchantment();
    public static final Enchantment DAMAGE_ALL = new Enchantment();
    public static Enchantment getByName(String name){return null;}
    public NamespacedKey getKey(){return null;}
}
""")

# entity
w("org/bukkit/entity/Entity.java", """
package org.bukkit.entity;
import org.bukkit.Location;
import org.bukkit.World;
import java.util.*;
public interface Entity {
    UUID getUniqueId();
    Location getLocation();
    World getWorld();
    void remove();
    Collection<Entity> getNearbyEntities(double x, double y, double z);
    void setVelocity(org.bukkit.util.Vector v);
    org.bukkit.util.Vector getVelocity();
    void teleport(Location loc);
    void setFireTicks(int t);
}
""")
w("org/bukkit/entity/LivingEntity.java", """
package org.bukkit.entity;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.Collection;
public interface LivingEntity extends Entity, Damageable {
    AttributeInstance getAttribute(Attribute attr);
    void addPotionEffect(PotionEffect effect);
    Collection<PotionEffect> getActivePotionEffects();
    void removePotionEffect(PotionEffectType type);
}
""")
w("org/bukkit/entity/Damageable.java", """
package org.bukkit.entity;
public interface Damageable extends Entity {
    double getHealth();
    void setHealth(double v);
    double getMaxHealth();
    void setMaxHealth(double v);
    Player getKiller();
}
""")
w("org/bukkit/entity/Player.java", """
package org.bukkit.entity;
import org.bukkit.*;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.RayTraceResult;
public interface Player extends LivingEntity, OfflinePlayer {
    PlayerInventory getInventory();
    void setGameMode(GameMode mode);
    GameMode getGameMode();
    void setLevel(int l);
    int getLevel();
    void setExp(float e);
    float getExp();
    void setFoodLevel(int f);
    int getFoodLevel();
    void setSaturation(float s);
    void setFlying(boolean b);
    void setAllowFlight(boolean b);
    void setFallDistance(float f);
    void sendMessage(String msg);
    void sendTitle(String title, String subtitle, int in, int stay, int out);
    void sendActionBar(String text);
    boolean hasPermission(String perm);
    boolean isOnline();
    Object spigot();
    void closeInventory();
    org.bukkit.inventory.InventoryView getOpenInventory();
    org.bukkit.inventory.InventoryView openInventory(org.bukkit.inventory.Inventory inv);
    void setScoreboard(Scoreboard sb);
    void playSound(Location loc, Sound sound, float vol, float pitch);
    void playSound(Location loc, String sound, float vol, float pitch);
    RayTraceResult rayTraceBlocks(double maxDistance);
    org.bukkit.block.Block getTargetBlockExact(int maxDistance);
    Location getEyeLocation();
    void teleport(Location loc);
    void setResourcePack(String url);
    void setResourcePack(String url, byte[] hash);
    void setResourcePack(String url, byte[] hash, String prompt);
    void setResourcePack(String url, byte[] hash, String prompt, boolean force);
    void setResourcePack(java.util.UUID id, String url, byte[] hash, String prompt, boolean force);
}
""")
w("org/bukkit/entity/Fireball.java", """
package org.bukkit.entity;
public interface Fireball extends Entity { void setYield(float y); void setIsIncendiary(boolean b); }
""")
w("org/bukkit/entity/TNTPrimed.java", """
package org.bukkit.entity;
public interface TNTPrimed extends Entity {
    void setFuseTicks(int t);
    void setYield(float y);
    void setSource(Entity src);
}
""")
w("org/bukkit/entity/ArmorStand.java", """
package org.bukkit.entity;
public interface ArmorStand extends LivingEntity {
    void setVisible(boolean v);
    void setGravity(boolean g);
    void setMarker(boolean m);
    void setSmall(boolean s);
    void setCustomNameVisible(boolean v);
    void setCustomName(String name);
}
""")
w("org/bukkit/entity/LightningStrike.java", """
package org.bukkit.entity;
public interface LightningStrike extends Entity {}
""")
w("org/bukkit/entity/EntityType.java", """
package org.bukkit.entity;
public enum EntityType { ARMOR_STAND, TNT, FIREBALL, LIGHTNING_BOLT }
""")

# event
w("org/bukkit/event/EventHandler.java", """
package org.bukkit.event;
import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD)
public @interface EventHandler {
    EventPriority priority() default EventPriority.NORMAL;
    boolean ignoreCancelled() default false;
}
""")
w("org/bukkit/event/EventPriority.java", """
package org.bukkit.event;
public enum EventPriority { LOWEST, LOW, NORMAL, HIGH, HIGHEST, MONITOR }
""")
w("org/bukkit/event/Listener.java", """
package org.bukkit.event;
public interface Listener {}
""")
w("org/bukkit/event/Event.java", """
package org.bukkit.event;
public abstract class Event {}
""")
w("org/bukkit/event/Cancellable.java", """
package org.bukkit.event;
public interface Cancellable { boolean isCancelled(); void setCancelled(boolean c); }
""")

# block events
w("org/bukkit/event/block/BlockEvent.java", """
package org.bukkit.event.block;
import org.bukkit.event.Event;
import org.bukkit.block.Block;
public abstract class BlockEvent extends Event { public Block getBlock(){return null;} }
""")
w("org/bukkit/event/block/BlockBreakEvent.java", """
package org.bukkit.event.block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
public class BlockBreakEvent extends BlockEvent implements Cancellable {
    public Player getPlayer(){return null;}
    @Override public boolean isCancelled(){return false;}
    @Override public void setCancelled(boolean c){}
}
""")
w("org/bukkit/event/block/BlockPlaceEvent.java", """
package org.bukkit.event.block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
public class BlockPlaceEvent extends BlockEvent implements Cancellable {
    public Player getPlayer(){return null;}
    @Override public boolean isCancelled(){return false;}
    @Override public void setCancelled(boolean c){}
}
""")
w("org/bukkit/event/block/Action.java", """
package org.bukkit.event.block;
public enum Action {
    LEFT_CLICK_BLOCK, RIGHT_CLICK_BLOCK, LEFT_CLICK_AIR, RIGHT_CLICK_AIR, PHYSICAL
}
""")

# entity events
w("org/bukkit/event/entity/EntityEvent.java", """
package org.bukkit.event.entity;
import org.bukkit.event.Event;
import org.bukkit.entity.Entity;
public abstract class EntityEvent extends Event { public Entity getEntity(){return null;} }
""")
w("org/bukkit/event/entity/EntityDamageEvent.java", """
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
""")
w("org/bukkit/event/entity/EntityDamageByEntityEvent.java", """
package org.bukkit.event.entity;
import org.bukkit.entity.Entity;
public class EntityDamageByEntityEvent extends EntityDamageEvent {
    public Entity getDamager(){return null;}
}
""")
w("org/bukkit/event/entity/EntityExplodeEvent.java", """
package org.bukkit.event.entity;
import org.bukkit.event.Cancellable;
public class EntityExplodeEvent extends EntityEvent implements Cancellable {
    @Override public boolean isCancelled(){return false;}
    @Override public void setCancelled(boolean c){}
}
""")
w("org/bukkit/event/entity/FoodLevelChangeEvent.java", """
package org.bukkit.event.entity;
import org.bukkit.event.Cancellable;
public class FoodLevelChangeEvent extends EntityEvent implements Cancellable {
    @Override public boolean isCancelled(){return false;}
    @Override public void setCancelled(boolean c){}
}
""")
w("org/bukkit/event/entity/PlayerDeathEvent.java", """
package org.bukkit.event.entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.List;
public class PlayerDeathEvent extends EntityEvent {
    @Override public Player getEntity(){return null;}
    public void setKeepInventory(boolean b){}
    public void setKeepLevel(boolean b){}
    public List<ItemStack> getDrops(){return java.util.Collections.emptyList();}
    public void setDeathMessage(String msg){}
}
""")

# inventory events
w("org/bukkit/event/inventory/InventoryEvent.java", """
package org.bukkit.event.inventory;
import org.bukkit.event.Event;
import org.bukkit.inventory.InventoryView;
public abstract class InventoryEvent extends Event { public InventoryView getView(){return null;} }
""")
w("org/bukkit/event/inventory/InventoryClickEvent.java", """
package org.bukkit.event.inventory;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.ItemStack;
public class InventoryClickEvent extends InventoryEvent implements Cancellable {
    public HumanEntity getWhoClicked(){return null;}
    public ItemStack getCurrentItem(){return null;}
    public int getRawSlot(){return 0;}
    @Override public boolean isCancelled(){return false;}
    @Override public void setCancelled(boolean c){}
}
""")
w("org/bukkit/event/inventory/InventoryCloseEvent.java", """
package org.bukkit.event.inventory;
import org.bukkit.entity.HumanEntity;
public class InventoryCloseEvent extends InventoryEvent {
    public HumanEntity getPlayer(){return null;}
}
""")
w("org/bukkit/entity/HumanEntity.java", """
package org.bukkit.entity;
public interface HumanEntity extends LivingEntity {
    void closeInventory();
}
""")

# player events
w("org/bukkit/event/player/PlayerEvent.java", """
package org.bukkit.event.player;
import org.bukkit.event.Event;
import org.bukkit.entity.Player;
public abstract class PlayerEvent extends Event { public Player getPlayer(){return null;} }
""")
w("org/bukkit/event/player/PlayerJoinEvent.java", """
package org.bukkit.event.player;
public class PlayerJoinEvent extends PlayerEvent {}
""")
w("org/bukkit/event/player/PlayerQuitEvent.java", """
package org.bukkit.event.player;
public class PlayerQuitEvent extends PlayerEvent {}
""")
w("org/bukkit/event/player/PlayerInteractEvent.java", """
package org.bukkit.event.player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.Action;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.EquipmentSlot;
public class PlayerInteractEvent extends PlayerEvent implements Cancellable {
    public Action getAction(){return null;}
    public Block getClickedBlock(){return null;}
    public ItemStack getItem(){return null;}
    public EquipmentSlot getHand(){return null;}
    @Override public boolean isCancelled(){return false;}
    @Override public void setCancelled(boolean c){}
}
""")
w("org/bukkit/event/player/PlayerCommandPreprocessEvent.java", """
package org.bukkit.event.player;
import org.bukkit.event.Cancellable;
public class PlayerCommandPreprocessEvent extends PlayerEvent implements Cancellable {
    public String getMessage(){return "";}
    @Override public boolean isCancelled(){return false;}
    @Override public void setCancelled(boolean c){}
}
""")
w("org/bukkit/event/player/AsyncPlayerChatEvent.java", """
package org.bukkit.event.player;
import org.bukkit.event.Cancellable;
import org.bukkit.entity.Player;
import java.util.Set;
public class AsyncPlayerChatEvent extends PlayerEvent implements Cancellable {
    public Set<Player> getRecipients(){return java.util.Collections.emptySet();}
    public String getMessage(){return "";}
    @Override public boolean isCancelled(){return false;}
    @Override public void setCancelled(boolean c){}
}
""")
w("org/bukkit/event/player/PlayerMoveEvent.java", """
package org.bukkit.event.player;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
public class PlayerMoveEvent extends PlayerEvent implements Cancellable {
    public Location getFrom(){return null;}
    public Location getTo(){return null;}
    @Override public boolean isCancelled(){return false;}
    @Override public void setCancelled(boolean c){}
}
""")
w("org/bukkit/event/player/PlayerRespawnEvent.java", """
package org.bukkit.event.player;
import org.bukkit.Location;
public class PlayerRespawnEvent extends PlayerEvent {
    public void setRespawnLocation(Location loc){}
}
""")

# inventory
w("org/bukkit/inventory/Inventory.java", """
package org.bukkit.inventory;
public interface Inventory {
    int getSize();
    ItemStack getItem(int slot);
    void setItem(int slot, ItemStack item);
    java.util.HashMap<Integer, ItemStack> addItem(ItemStack... items);
    void clear();
    void setContents(ItemStack[] items);
    ItemStack[] getContents();
    ItemStack[] getArmorContents();
    void setArmorContents(ItemStack[] items);
    InventoryHolder getHolder();
}
""")
w("org/bukkit/inventory/PlayerInventory.java", """
package org.bukkit.inventory;
public interface PlayerInventory extends Inventory {
    void setHelmet(ItemStack i);
    void setChestplate(ItemStack i);
    void setLeggings(ItemStack i);
    void setBoots(ItemStack i);
    void setItemInMainHand(ItemStack i);
    void setItemInOffHand(ItemStack i);
    ItemStack getItemInMainHand();
    ItemStack getItemInOffHand();
}
""")
w("org/bukkit/inventory/ItemStack.java", """
package org.bukkit.inventory;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
public class ItemStack implements Cloneable {
    public ItemStack(Material mat){}
    public ItemStack(Material mat, int amount){}
    public Material getType(){return null;}
    public int getAmount(){return 0;}
    public void setAmount(int a){}
    public ItemMeta getItemMeta(){return null;}
    public void setItemMeta(ItemMeta meta){}
    @Override public ItemStack clone(){return this;}
}
""")
w("org/bukkit/inventory/ItemFlag.java", """
package org.bukkit.inventory;
public enum ItemFlag {
    HIDE_ENCHANTS, HIDE_ATTRIBUTES, HIDE_UNBREAKABLE, HIDE_DESTROYS,
    HIDE_PLACED_ON, HIDE_POTION_EFFECTS, HIDE_DYE, HIDE_ARMOR_TRIM,
    HIDE_STORED_ENCHANTS;
    public static ItemFlag[] values(){return new ItemFlag[]{};}
}
""")
w("org/bukkit/inventory/EquipmentSlot.java", """
package org.bukkit.inventory;
public enum EquipmentSlot { HAND, OFF_HAND, HEAD, CHEST, LEGS, FEET, BODY }
""")
w("org/bukkit/inventory/InventoryView.java", """
package org.bukkit.inventory;
public abstract class InventoryView {
    public abstract Inventory getTopInventory();
    public abstract Inventory getBottomInventory();
}
""")
w("org/bukkit/inventory/meta/ItemMeta.java", """
package org.bukkit.inventory.meta;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import java.util.List;
public interface ItemMeta extends PersistentDataHolder, Cloneable {
    void setDisplayName(String name);
    String getDisplayName();
    boolean hasDisplayName();
    void setLore(List<String> lore);
    List<String> getLore();
    boolean hasLore();
    void addEnchant(Enchantment e, int lvl, boolean ignoreLevelRestriction);
    void addItemFlags(ItemFlag... flags);
    void setUnbreakable(boolean b);
    void setCustomModelData(Integer data);
    int getCustomModelData();
    boolean hasCustomModelData();
    @Override PersistentDataContainer getPersistentDataContainer();
}
""")
w("org/bukkit/inventory/meta/PotionMeta.java", """
package org.bukkit.inventory.meta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
public interface PotionMeta extends ItemMeta {
    void setBasePotionData(PotionData data);
    void setBasePotionType(PotionType type);
}
""")
w("org/bukkit/inventory/meta/LeatherArmorMeta.java", """
package org.bukkit.inventory.meta;
import org.bukkit.Color;
public interface LeatherArmorMeta extends ItemMeta {
    void setColor(Color c);
}
""")

# persistence
w("org/bukkit/persistence/PersistentDataContainer.java", """
package org.bukkit.persistence;
import org.bukkit.NamespacedKey;
public interface PersistentDataContainer {
    <T,Z> void set(NamespacedKey key, PersistentDataType<T,Z> type, Z value);
    <T,Z> Z get(NamespacedKey key, PersistentDataType<T,Z> type);
    <T,Z> boolean has(NamespacedKey key, PersistentDataType<T,Z> type);
}
""")
w("org/bukkit/persistence/PersistentDataType.java", """
package org.bukkit.persistence;
public interface PersistentDataType<P,C> {
    PersistentDataType<String, String> STRING = null;
    PersistentDataType<Integer, Integer> INTEGER = null;
    PersistentDataType<Byte, Byte> BYTE = null;
    PersistentDataType<Double, Double> DOUBLE = null;
}
""")
w("org/bukkit/persistence/PersistentDataHolder.java", """
package org.bukkit.persistence;
public interface PersistentDataHolder { PersistentDataContainer getPersistentDataContainer(); }
""")

# plugin
w("org/bukkit/plugin/Plugin.java", """
package org.bukkit.plugin;
import java.io.File;
import java.io.InputStream;
import java.util.logging.Logger;
import org.bukkit.Server;
public interface Plugin {
    String getName();
    File getDataFolder();
    Logger getLogger();
    boolean isEnabled();
    PluginDescriptionFile getDescription();
    Server getServer();
    org.bukkit.command.PluginCommand getCommand(String name);
    void saveDefaultConfig();
    void reloadConfig();
    org.bukkit.configuration.file.FileConfiguration getConfig();
    void saveConfig();
    void saveResource(String name, boolean replace);
    InputStream getResource(String name);
}
""")
w("org/bukkit/plugin/PluginDescriptionFile.java", """
package org.bukkit.plugin;
public class PluginDescriptionFile {
    public String getVersion(){return "";}
    public String getName(){return "";}
}
""")
w("org/bukkit/plugin/PluginManager.java", """
package org.bukkit.plugin;
import org.bukkit.event.Listener;
public interface PluginManager {
    Plugin getPlugin(String name);
    void registerEvents(Listener listener, Plugin plugin);
}
""")
w("org/bukkit/plugin/ServicesManager.java", """
package org.bukkit.plugin;
public interface ServicesManager {
    <T> RegisteredServiceProvider<T> getRegistration(Class<T> service);
}
""")
w("org/bukkit/plugin/RegisteredServiceProvider.java", """
package org.bukkit.plugin;
public class RegisteredServiceProvider<T> { public T getProvider(){return null;} }
""")
w("org/bukkit/plugin/java/JavaPlugin.java", """
package org.bukkit.plugin.java;
import org.bukkit.plugin.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.command.PluginCommand;
import java.io.File;
import java.io.InputStream;
import java.util.logging.Logger;
import org.bukkit.Server;
public abstract class JavaPlugin implements Plugin {
    public void onEnable(){}
    public void onDisable(){}
    @Override public String getName(){return "";}
    @Override public File getDataFolder(){return new File(".");}
    @Override public Logger getLogger(){return Logger.getLogger("stub");}
    @Override public boolean isEnabled(){return true;}
    @Override public PluginDescriptionFile getDescription(){return new PluginDescriptionFile();}
    @Override public Server getServer(){return null;}
    @Override public PluginCommand getCommand(String name){return null;}
    @Override public void saveDefaultConfig(){}
    @Override public void reloadConfig(){}
    @Override public FileConfiguration getConfig(){return null;}
    @Override public void saveConfig(){}
    @Override public void saveResource(String name, boolean replace){}
    @Override public InputStream getResource(String name){return null;}
}
""")

# potion
w("org/bukkit/potion/PotionEffect.java", """
package org.bukkit.potion;
public class PotionEffect {
    public PotionEffect(PotionEffectType type, int dur, int amp){}
    public PotionEffect(PotionEffectType type, int dur, int amp, boolean ambient, boolean particles){}
    public PotionEffectType getType(){return null;}
}
""")
w("org/bukkit/potion/PotionEffectType.java", """
package org.bukkit.potion;
import org.bukkit.NamespacedKey;
import org.bukkit.Keyed;
public class PotionEffectType implements Keyed {
    public static PotionEffectType getByName(String name){return null;}
    public String getName(){return "";}
    public NamespacedKey getKey(){return null;}
}
""")
w("org/bukkit/potion/PotionType.java", """
package org.bukkit.potion;
import org.bukkit.NamespacedKey;
import org.bukkit.Keyed;
public enum PotionType implements Keyed {
    HEALING, SWIFTNESS, STRENGTH, WATER, AWKWARD, MUNDANE, REGEN, REGENERATION;
    public NamespacedKey getKey(){return null;}
}
""")
w("org/bukkit/potion/PotionData.java", """
package org.bukkit.potion;
public class PotionData { public PotionData(PotionType type){} }
""")

# scheduler
w("org/bukkit/scheduler/BukkitRunnable.java", """
package org.bukkit.scheduler;
import org.bukkit.plugin.Plugin;
public abstract class BukkitRunnable implements Runnable {
    public BukkitTask runTask(Plugin plugin){return null;}
    public BukkitTask runTaskAsynchronously(Plugin plugin){return null;}
    public BukkitTask runTaskTimer(Plugin plugin, long delay, long period){return null;}
    public BukkitTask runTaskLater(Plugin plugin, long delay){return null;}
    public BukkitTask runTaskLaterAsynchronously(Plugin plugin, long delay){return null;}
    public void cancel(){}
}
""")
w("org/bukkit/scheduler/BukkitTask.java", """
package org.bukkit.scheduler;
public interface BukkitTask { void cancel(); int getTaskId(); }
""")
w("org/bukkit/scheduler/BukkitScheduler.java", """
package org.bukkit.scheduler;
import org.bukkit.plugin.Plugin;
public interface BukkitScheduler {
    BukkitTask runTask(Plugin plugin, Runnable r);
    BukkitTask runTaskAsynchronously(Plugin plugin, Runnable r);
    BukkitTask runTaskLater(Plugin plugin, Runnable r, long delay);
    BukkitTask runTaskLaterAsynchronously(Plugin plugin, Runnable r, long delay);
    BukkitTask runTaskTimer(Plugin plugin, Runnable r, long delay, long period);
    BukkitTask runTaskTimerAsynchronously(Plugin plugin, Runnable r, long delay, long period);
}
""")

# scoreboard
w("org/bukkit/scoreboard/Scoreboard.java", """
package org.bukkit.scoreboard;
import java.util.Set;
public interface Scoreboard {
    Objective registerNewObjective(String name, String criteria, String displayName);
    Objective getObjective(String name);
    Set<String> getEntries();
    void resetScores(String entry);
}
""")
w("org/bukkit/scoreboard/Objective.java", """
package org.bukkit.scoreboard;
public interface Objective {
    void setDisplaySlot(DisplaySlot slot);
    void setDisplayName(String name);
    Score getScore(String entry);
}
""")
w("org/bukkit/scoreboard/Score.java", """
package org.bukkit.scoreboard;
public interface Score { void setScore(int s); }
""")
w("org/bukkit/scoreboard/DisplaySlot.java", """
package org.bukkit.scoreboard;
public enum DisplaySlot { SIDEBAR, BELOW_NAME, PLAYER_LIST }
""")
w("org/bukkit/scoreboard/ScoreboardManager.java", """
package org.bukkit.scoreboard;
public interface ScoreboardManager {
    Scoreboard getNewScoreboard();
    Scoreboard getMainScoreboard();
}
""")

# util
w("org/bukkit/util/Vector.java", """
package org.bukkit.util;
public class Vector {
    public Vector(){}
    public Vector(double x, double y, double z){}
    public Vector setY(double y){return this;}
    public Vector multiply(double f){return this;}
    public Vector normalize(){return this;}
    public Vector add(Vector v){return this;}
    public Vector subtract(Vector v){return this;}
    public double length(){return 0;}
    public double getX(){return 0;}
    public double getY(){return 0;}
    public double getZ(){return 0;}
    public org.bukkit.Location toLocation(org.bukkit.World world){return null;}
}
""")
w("org/bukkit/util/RayTraceResult.java", """
package org.bukkit.util;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
public class RayTraceResult {
    public Block getHitBlock(){return null;}
    public BlockFace getHitBlockFace(){return null;}
    public Vector getHitPosition(){return new Vector();}
}
""")
w("org/bukkit/util/StringUtil.java", """
package org.bukkit.util;
import java.util.Collection;
public final class StringUtil {
    public static <T extends Collection<? super String>> T copyPartialMatches(String token, Iterable<String> originals, T collection){return collection;}
}
""")

# ========== net.md_5.bungee (chat API shim) ==========
w("net/md_5/bungee/api/ChatColor.java", """
package net.md_5.bungee.api;
public final class ChatColor {
    public static ChatColor of(String hex){return new ChatColor();}
    @Override public String toString(){return "";}
}
""")
w("net/md_5/bungee/api/ChatMessageType.java", """
package net.md_5.bungee.api;
public enum ChatMessageType { CHAT, SYSTEM, ACTION_BAR }
""")
w("net/md_5/bungee/api/chat/BaseComponent.java", """
package net.md_5.bungee.api.chat;
public abstract class BaseComponent {}
""")
w("net/md_5/bungee/api/chat/TextComponent.java", """
package net.md_5.bungee.api.chat;
public class TextComponent extends BaseComponent {
    public TextComponent(){}
    public TextComponent(String text){}
    public static BaseComponent[] fromLegacyText(String text){return new BaseComponent[0];}
}
""")

# Player#spigot() returns an object with sendMessage(ChatMessageType, BaseComponent...)
# We model spigot() as Object in stubs, but my code uses player.spigot().sendMessage(ACTION_BAR, comp).
# Need a proper spigot type:
w("org/bukkit/entity/Player_Spigot_Stub.java", """
package org.bukkit.entity;
// Placeholder: Player#spigot() returns an Object in our stubs.
// VersionAdapter.sendActionBar wraps in try/catch — Throwable catches MethodNotFound at runtime.
// To make compile succeed without changing my code, we need spigot() to return a type
// with sendMessage(ChatMessageType, BaseComponent[]). Simplest: a custom type Spigot.
final class Player_Spigot_Stub {} // not used
""")

# Adjust Player.spigot() — make it return a Spigot type
w("org/bukkit/entity/Player.java", """
package org.bukkit.entity;
import org.bukkit.*;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.RayTraceResult;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
public interface Player extends LivingEntity, OfflinePlayer {
    PlayerInventory getInventory();
    void setGameMode(GameMode mode);
    GameMode getGameMode();
    void setLevel(int l);
    int getLevel();
    void setExp(float e);
    float getExp();
    void setFoodLevel(int f);
    int getFoodLevel();
    void setSaturation(float s);
    void setFlying(boolean b);
    void setAllowFlight(boolean b);
    void setFallDistance(float f);
    void sendMessage(String msg);
    void sendTitle(String title, String subtitle, int in, int stay, int out);
    void sendActionBar(String text);
    boolean hasPermission(String perm);
    boolean isOnline();
    Spigot spigot();
    interface Spigot {
        void sendMessage(ChatMessageType type, BaseComponent[] components);
    }
    void closeInventory();
    org.bukkit.inventory.InventoryView getOpenInventory();
    org.bukkit.inventory.InventoryView openInventory(org.bukkit.inventory.Inventory inv);
    void setScoreboard(Scoreboard sb);
    void playSound(Location loc, Sound sound, float vol, float pitch);
    void playSound(Location loc, String sound, float vol, float pitch);
    RayTraceResult rayTraceBlocks(double maxDistance);
    org.bukkit.block.Block getTargetBlockExact(int maxDistance);
    Location getEyeLocation();
    void teleport(Location loc);
    void setResourcePack(String url);
    void setResourcePack(String url, byte[] hash);
    void setResourcePack(String url, byte[] hash, String prompt);
    void setResourcePack(String url, byte[] hash, String prompt, boolean force);
    void setResourcePack(java.util.UUID id, String url, byte[] hash, String prompt, boolean force);
}
""")

# ========== PAPI ==========
w("me/clip/placeholderapi/expansion/PlaceholderExpansion.java", """
package me.clip.placeholderapi.expansion;
import org.bukkit.OfflinePlayer;
public abstract class PlaceholderExpansion {
    public abstract String getIdentifier();
    public abstract String getAuthor();
    public abstract String getVersion();
    public boolean persist(){return false;}
    public String onRequest(OfflinePlayer player, String params){return null;}
    public boolean register(){return true;}
}
""")

print(f"Generated {len(list(OUT.rglob('*.java')))} stub files")

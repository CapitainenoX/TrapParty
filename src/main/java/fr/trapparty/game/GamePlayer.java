package fr.trapparty.game;

import fr.trapparty.kit.Kit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * État d'un joueur dans une partie.
 * On capture sa position/inventaire/gamemode pour les restaurer à la sortie.
 */
public class GamePlayer {

    private final UUID uuid;
    private final String name;
    private boolean alive = true;
    private boolean spectator = false;
    private Kit kit;
    private int coins;
    private int kills;
    private int trapKills;

    // snapshot avant rejoindre
    private Location previousLocation;
    private ItemStack[] previousInventory;
    private ItemStack[] previousArmor;
    private GameMode previousGameMode;
    private int previousLevel;
    private float previousExp;
    private double previousHealth;
    private int previousFood;

    public GamePlayer(Player player) {
        this.uuid = player.getUniqueId();
        this.name = player.getName();
    }

    public void snapshot(Player p) {
        this.previousLocation = p.getLocation().clone();
        this.previousInventory = p.getInventory().getContents().clone();
        this.previousArmor = p.getInventory().getArmorContents().clone();
        this.previousGameMode = p.getGameMode();
        this.previousLevel = p.getLevel();
        this.previousExp = p.getExp();
        this.previousHealth = p.getHealth();
        this.previousFood = p.getFoodLevel();
    }

    public void restore(Player p) {
        if (p == null) return;
        if (previousInventory != null) p.getInventory().setContents(previousInventory);
        if (previousArmor != null) p.getInventory().setArmorContents(previousArmor);
        if (previousGameMode != null) p.setGameMode(previousGameMode);
        p.setLevel(previousLevel);
        p.setExp(previousExp);
        try { p.setHealth(Math.max(1, previousHealth)); } catch (Throwable ignored) {}
        p.setFoodLevel(Math.max(1, previousFood));
        if (previousLocation != null) p.teleport(previousLocation);
        p.setFlying(false);
        p.setAllowFlight(false);
        p.setFireTicks(0);
    }

    public UUID getUuid() { return uuid; }
    public String getName() { return name; }
    public boolean isAlive() { return alive; }
    public void setAlive(boolean alive) { this.alive = alive; }
    public boolean isSpectator() { return spectator; }
    public void setSpectator(boolean s) { this.spectator = s; }
    public Kit getKit() { return kit; }
    public void setKit(Kit kit) { this.kit = kit; }
    public int getCoins() { return coins; }
    public void setCoins(int v) { this.coins = Math.max(0, v); }
    public void addCoins(int v) { this.coins = Math.max(0, this.coins + v); }
    public boolean spend(int amount) {
        if (coins < amount) return false;
        coins -= amount;
        return true;
    }
    public int getKills() { return kills; }
    public void addKill() { kills++; }
    public int getTrapKills() { return trapKills; }
    public void addTrapKill() { trapKills++; }
}

package org.bukkit.entity;
import org.bukkit.*;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.RayTraceResult;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
public interface Player extends HumanEntity, OfflinePlayer {
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

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

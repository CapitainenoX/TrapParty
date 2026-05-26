package org.bukkit;
public class NamespacedKey {
    public NamespacedKey(String ns, String key){}
    public NamespacedKey(org.bukkit.plugin.Plugin plugin, String key){}
    public static NamespacedKey minecraft(String key){return new NamespacedKey("minecraft", key);}
    public String getNamespace(){return "";}
    public String getKey(){return "";}
}

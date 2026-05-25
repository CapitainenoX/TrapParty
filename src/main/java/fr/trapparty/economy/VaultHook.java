package fr.trapparty.economy;

import fr.trapparty.TrapPartyPlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.lang.reflect.Method;

/**
 * Hook Vault entièrement réflectif : si Vault est présent et qu'un provider
 * Economy est enregistré, on l'utilise. Sinon, indisponible.
 *
 * On n'importe pas net.milkbowl.vault.* directement pour rester capable de
 * compiler/charger même sans Vault sur le classpath du serveur.
 */
public class VaultHook {

    private final TrapPartyPlugin plugin;
    private Object economy;            // net.milkbowl.vault.economy.Economy
    private Method getBalance;
    private Method withdraw;
    private Method deposit;
    private Method has;
    private Method format;
    private String currencySymbol = "$";

    public VaultHook(TrapPartyPlugin plugin) {
        this.plugin = plugin;
        tryLoad();
    }

    private void tryLoad() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) return;
        try {
            Class<?> econClass = Class.forName("net.milkbowl.vault.economy.Economy");
            RegisteredServiceProvider<?> rsp = Bukkit.getServicesManager().getRegistration(econClass);
            if (rsp == null) return;
            this.economy = rsp.getProvider();
            this.getBalance = economy.getClass().getMethod("getBalance", OfflinePlayer.class);
            this.withdraw   = economy.getClass().getMethod("withdrawPlayer", OfflinePlayer.class, double.class);
            this.deposit    = economy.getClass().getMethod("depositPlayer", OfflinePlayer.class, double.class);
            this.has        = economy.getClass().getMethod("has", OfflinePlayer.class, double.class);
            try {
                this.format = economy.getClass().getMethod("format", double.class);
            } catch (NoSuchMethodException ignored) {}
            try {
                Method symbol = economy.getClass().getMethod("currencyNameSingular");
                Object res = symbol.invoke(economy);
                if (res instanceof String s && !s.isEmpty()) currencySymbol = s;
            } catch (Throwable ignored) {}
            plugin.getLogger().info("Vault economy hooked : "
                    + economy.getClass().getSimpleName() + " (" + currencySymbol + ")");
        } catch (Throwable t) {
            plugin.getLogger().fine("Vault hook failed : " + t.getMessage());
        }
    }

    public boolean isAvailable() { return economy != null; }

    public double getBalance(Player p) {
        if (!isAvailable()) return 0;
        try { return (double) getBalance.invoke(economy, (OfflinePlayer) p); }
        catch (Throwable t) { return 0; }
    }

    public boolean has(Player p, double amount) {
        if (!isAvailable()) return false;
        try { return (boolean) has.invoke(economy, (OfflinePlayer) p, amount); }
        catch (Throwable t) { return false; }
    }

    public boolean withdraw(Player p, double amount) {
        if (!isAvailable()) return false;
        try {
            Object resp = withdraw.invoke(economy, (OfflinePlayer) p, amount);
            // EconomyResponse#transactionSuccess()
            Method ts = resp.getClass().getMethod("transactionSuccess");
            return (boolean) ts.invoke(resp);
        } catch (Throwable t) { return false; }
    }

    public boolean deposit(Player p, double amount) {
        if (!isAvailable()) return false;
        try {
            Object resp = deposit.invoke(economy, (OfflinePlayer) p, amount);
            Method ts = resp.getClass().getMethod("transactionSuccess");
            return (boolean) ts.invoke(resp);
        } catch (Throwable t) { return false; }
    }

    public String format(double amount) {
        if (format != null) {
            try { return (String) format.invoke(economy, amount); }
            catch (Throwable ignored) {}
        }
        return amount + " " + currencySymbol;
    }

    public String currencySymbol() { return currencySymbol; }
}

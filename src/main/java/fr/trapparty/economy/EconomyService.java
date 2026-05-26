package fr.trapparty.economy;

import fr.trapparty.TrapPartyPlugin;
import fr.trapparty.game.GamePlayer;
import org.bukkit.entity.Player;

/**
 * Façade économie : route vers Vault si configuré + disponible,
 * sinon vers les pièces de partie (GamePlayer#coins).
 *
 * Le mode est défini dans `config.yml > economy.mode`.
 */
public class EconomyService {

    public enum Mode { VAULT, COINS }

    private final TrapPartyPlugin plugin;
    private final VaultHook vault;
    private final Mode configuredMode;

    public EconomyService(TrapPartyPlugin plugin) {
        this.plugin = plugin;
        this.vault = new VaultHook(plugin);
        String configured = plugin.configs().root().getString("economy.mode", "vault").toLowerCase();
        this.configuredMode = configured.equals("vault") ? Mode.VAULT : Mode.COINS;
        // Warn explicitement si l'opérateur veut vault mais aucun provider n'est dispo
        if (configuredMode == Mode.VAULT && !vault.isAvailable()) {
            plugin.getLogger().warning(
                "economy.mode=vault mais aucun provider Vault Economy n'est enregistré.\n"
                + "  → Installe EssentialsX/CMI/TNE/autre, OU passe en economy.mode: coins.\n"
                + "  Fallback automatique sur les pièces de partie.");
        }
    }

    /**
     * Crédite un joueur de la différence entre son solde Vault et le minimum configuré
     * (uniquement si en mode VAULT). Évite que les nouveaux joueurs débutent à 0
     * face à des vétérans qui ont accumulé.
     */
    public void ensureMinBalance(Player p) {
        if (effectiveMode() != Mode.VAULT) return;
        double min = plugin.configs().root().getDouble("economy.vault-min-balance", 100);
        double bal = vault.getBalance(p);
        if (bal < min) {
            vault.deposit(p, min - bal);
        }
    }

    public Mode effectiveMode() {
        if (configuredMode == Mode.VAULT && vault.isAvailable()) return Mode.VAULT;
        return Mode.COINS;
    }

    public VaultHook vault() { return vault; }

    public double balance(Player p, GamePlayer gp) {
        if (effectiveMode() == Mode.VAULT) return vault.getBalance(p);
        return gp == null ? 0 : gp.getCoins();
    }

    public boolean canAfford(Player p, GamePlayer gp, double amount) {
        if (amount <= 0) return true;
        return balance(p, gp) >= amount;
    }

    public boolean withdraw(Player p, GamePlayer gp, double amount) {
        if (amount <= 0) return true;
        if (effectiveMode() == Mode.VAULT) return vault.withdraw(p, amount);
        if (gp == null) return false;
        return gp.spend((int) Math.ceil(amount));
    }

    public boolean deposit(Player p, GamePlayer gp, double amount) {
        if (amount <= 0) return true;
        if (effectiveMode() == Mode.VAULT) return vault.deposit(p, amount);
        if (gp == null) return false;
        gp.addCoins((int) Math.ceil(amount));
        return true;
    }

    public String format(double amount) {
        if (effectiveMode() == Mode.VAULT) return vault.format(amount);
        return ((int) Math.ceil(amount)) + " pièces";
    }

    public String currencySymbol() {
        if (effectiveMode() == Mode.VAULT) return vault.currencySymbol();
        return "pièces";
    }
}

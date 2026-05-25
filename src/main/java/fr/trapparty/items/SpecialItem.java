package fr.trapparty.items;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Item spécial : objet à pouvoirs custom, identifié par PersistentDataContainer.
 * Le SpecialItemManager dispatche les events Bukkit vers l'implémentation.
 */
public interface SpecialItem {

    /** Identifiant unique (snake_case). */
    String id();

    /** Nom affiché (couleurs &). */
    String displayName();

    /** Description multi-lignes. */
    List<String> lore();

    /** Matériau de base (déterminé par fallback "MAT|FALL1|FALL2"). */
    String baseMaterial();

    /** Identifiant numérique CustomModelData (1001-1999 réservés). */
    int customModelData();

    /** Nombre d'utilisations max (-1 = illimité). */
    default int maxUses() { return -1; }

    /** Coût en pièces dans le shop (-1 = pas en vente). */
    default int cost() { return -1; }

    /** Catégorie de shop ("utility", "traps", "mobility"...). */
    default String shopCategory() { return "utility"; }

    /** Appelé sur clic droit (air ou bloc). Retourne true si l'action a consommé une charge. */
    boolean onUse(Player player, PlayerInteractEvent event);
}

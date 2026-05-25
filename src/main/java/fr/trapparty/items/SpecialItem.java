package fr.trapparty.items;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

/**
 * Item spécial : objet à pouvoir custom, identifié par PersistentDataContainer.
 * Pas de texture custom — visuellement c'est juste le base material avec
 * un glow d'enchantement, un nom unique et de la lore. La logique
 * d'activation est dispatchée par SpecialItemManager via PlayerInteractEvent.
 */
public interface SpecialItem {

    /** Identifiant unique (snake_case). */
    String id();

    /** Nom affiché (couleurs &). */
    String displayName();

    /** Description multi-lignes. */
    List<String> lore();

    /** Matériau de base, syntaxe fallback "MAT|FALL1|FALL2". */
    String baseMaterial();

    /** Nombre d'utilisations max (-1 = illimité). */
    default int maxUses() { return -1; }

    /** Coût en pièces dans le shop (-1 = pas en vente). */
    default int cost() { return -1; }

    /** Catégorie de shop ("utility", "traps", "mobility"...). */
    default String shopCategory() { return "utility"; }

    /** Appelé sur clic droit (air ou bloc). Retourne true si l'action a consommé une charge. */
    boolean onUse(Player player, PlayerInteractEvent event);
}

# TrapParty Resource Pack — guide des textures

Toutes les textures du resource pack sont fournies sous forme de **PNG vides
16×16 transparents**. Tu peux les remplacer par tes propres designs en
gardant exactement les mêmes noms de fichier et dimensions (16×16 RGBA).

> Astuce : utilise [Pixilart](https://www.pixilart.com), [Lospec](https://lospec.com)
> ou Aseprite. Garde le style "vanilla-friendly" si tu veux une cohérence
> visuelle. Sauvegarde en PNG, 16×16, transparence activée.

## Items spéciaux

Chaque item spécial est associé à un base material vanilla overridé par
`custom_model_data`. Le fichier d'override est dans
`assets/minecraft/items/<base>.json`.

| Fichier (`assets/trapparty/textures/item/`) | Base vanilla | CMD | Item en jeu | À quoi ça ressemble |
|---|---|---:|---|---|
| `super_drill.png` | `netherite_pickaxe` | 1001 | Super Foreuse | Pioche futuriste, gemme cyan sur la tête, manche métallique. |
| `remote_activator.png` | `compass` | 1002 | Activateur à distance | Petit boîtier rond avec un gros bouton rouge + antenne. |
| `trap_caller.png` | `blaze_rod` | 1003 | Trap Caller | Bâton magique doré, étoile orange en haut, particules. |
| `wind_stomper.png` | `wind_charge` | 1004 | Wind Stomper | Boule de vortex bleu/cyan tournoyant. |
| `decoy_block.png` | `gray_dye` | 1005 | Faux Bloc | Bloc gris avec un point d'interrogation rouge "?". |
| `grappling_hook.png` | `fishing_rod` | 1006 | Grappling Hook | Crochet métallique avec corde. |
| `spy_lens.png` | `spyglass` | 1007 | Lunette d'Espion | Longue vue violette avec lentille cyan. |
| `magnet_bomb.png` | `amethyst_shard` | 1008 | Magnet Bomb | Bombe ronde violette/améthyste avec mèche allumée. |

## Shop UI

Le shop utilise PAPER avec différents `custom_model_data` pour les boutons
de catégorie (le PNG s'affiche sur l'item PAPER en main et en GUI).

| Fichier | Base vanilla | CMD | Rôle | Suggestion visuelle |
|---|---|---:|---|---|
| `shop_button_blocks.png` | `paper` | 2001 | Bouton "Blocs" | Icône cobblestone + flèche. |
| `shop_button_traps.png` | `paper` | 2002 | Bouton "Pièges" | TNT stylisée. |
| `shop_button_utility.png` | `paper` | 2003 | Bouton "Utilitaires" | Bouclier ou outil. |
| `shop_button_consumables.png` | `paper` | 2004 | Bouton "Consommables" | Steak ou potion. |
| `shop_button_redstone.png` | `paper` | 2005 | Bouton "Redstone" | Poussière rouge. |
| `shop_button_mobility.png` | `paper` | 2006 | Bouton "Mobilité" | Plume ou ender pearl. |
| `shop_button_special.png` | `paper` | 2007 | Bouton "Items Spéciaux" | Étoile magique. |
| `shop_currency.png` | `gold_nugget` | 2010 | Affichage des pièces | Pièce d'or stylisée. |
| `shop_buy_button.png` | `lime_dye` | 2020 | Bouton "Acheter" | Coche verte ✓. |
| `shop_locked_button.png` | `red_dye` | 2021 | "Fonds insuffisants" | Cadenas ou ✗. |
| `shop_back_button.png` | `arrow` | 2022 | Retour menu principal | Flèche gauche. |
| `shop_close_button.png` | `barrier` | 2023 | Fermer le shop | Croix rouge. |
| `shop_border.png` | `white_stained_glass_pane` | 2030 | Bordure décorative | Filet doré ou ornement. |
| `shop_logo.png` | `nether_star` | 2031 | Logo TrapParty | Logo plein écran (slot central). |

## Pack icon

`pack.png` (à la racine du resource pack) est l'icône affichée dans
*Resource Pack Selection* du client. Format : **64×64** ou **128×128** PNG.

## Workflow de design

1. Édite les PNG dans `src/main/resources/resourcepack/assets/trapparty/textures/item/`.
2. Garde exactement les noms de fichier (sinon les overrides ne trouvent
   plus la texture).
3. Rebuild le pack : `bash scripts/build_pack.sh` (zip + SHA-1 affiché).
4. Upload `trapparty-pack.zip` sur ton CDN, mets l'URL et le SHA-1 dans
   `plugins/TrapParty/config.yml > resource-pack`.
5. Les joueurs reçoivent automatiquement le pack à leur connexion.

## Ajouter un nouvel item spécial

1. Crée la classe Java dans `fr.trapparty.items.impl.*` qui implémente
   `SpecialItem`. Choisis un `customModelData()` unique au-delà de 2031.
2. Enregistre-la dans `SpecialItemManager#registerDefaults()`.
3. Crée le PNG dans ce dossier (`textures/item/<id>.png`).
4. Crée le modèle JSON correspondant dans
   `models/item/<id>.json` (copie un existant).
5. Ajoute l'override dans `assets/minecraft/items/<base_material>.json`
   (copie un existant et modifie `threshold` et `model`).
6. Rebuild le pack et relance le serveur.

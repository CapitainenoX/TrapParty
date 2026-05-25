# TrapParty - Contexte du projet

## Objectif
Plugin Minecraft mini-jeu **Trap Survival** compétitif, premium, scalable.
- Multi-parties simultanées
- **Cible : Minecraft Java 26.1.2 (Tiny Takeover, avril 2026)**
- Architecture pensée pour rester portable 1.20 → 26.1.2 (via fallback YAML + lookup runtime)
- Intégration MultiVerse-Core v4 **et** v5 (auto-détectée par package)
- Compatible Paper / Spigot / Purpur

## Branche de dev
`claude/plugin-custom-optimize-fvfdS`

## Stack
- **Java 21** (requis par Paper 26.1.x)
- **Maven**
- **Paper API 26.1.2-R0.1-SNAPSHOT** depuis `repo.papermc.io`
- **Multiverse-Core 5.6.1** depuis `repo.onarandombox.com`
  - Nouveau groupId v5 : `org.mvplugins.multiverse.core`
  - Hook reflection tolérant v4/v5

## Modules
| Module | Rôle |
|--------|------|
| `game` | Cycle de partie (états, manager, joueur) |
| `arena` | Arènes, templates |
| `world` | WorldManager (clone async), MultiverseHook v4/v5, BorderManager |
| `kit` | Kits PvP (Tank, Archer, Assassin, TrapMaster, Engineer, Berserker, **Macer**, **Breezer**) |
| `shop` | Boutique, catégories, cooldowns |
| `trap` | Pièges (TNT, Pit, Lava, Web, etc.) |
| `events` | Événements random (storm, meteor, mutation, loot chest, speed, gravity, night) |
| `ui` | Scoreboard, BossBar, GUI, hologrammes natifs |
| `stats` | Stats persistantes YAML |
| `commands` | Cmds admin + joueur |
| `config` | Hot-reload config et messages |
| `compat` | VersionAdapter (parse 1.X.Y et 26.X.Y) |
| `listeners` | Bukkit events |
| `util` | ItemBuilder (fallback materials/enchants), LocationUtil, RandomUtil, EffectUtil |

## Cross-version : comment ça marche
- `ItemBuilder.resolveMaterial("MACE|IRON_SWORD|STICK")` essaie les candidats dans l'ordre
- `ItemBuilder.lookupEnchant(...)` essaie Registry moderne puis getByName legacy ; alias intégrés (SHARPNESS↔DAMAGE_ALL, POWER↔ARROW_DAMAGE, …) + nouveaux enchants 1.21+ (DENSITY, BREACH, WIND_BURST)
- `EffectUtil.byName(...)` même principe pour les PotionEffectType (JUMP_BOOST↔JUMP, etc.)
- `Game#maxHealthOf(Player)` utilise Attribute moderne (MAX_HEALTH) avec fallback GENERIC_MAX_HEALTH puis getMaxHealth() déprécié
- `GameManager#applyRule()` GameRule moderne + fallback string

## Nouveaux items / enchants 1.21+ intégrés
- **Items** : `MACE`, `WIND_CHARGE`, `BREEZE_ROD`, `HEAVY_CORE`, `TRIAL_KEY`, `OMINOUS_BOTTLE`, `CRAFTER`, `TUFF`, `POLISHED_TUFF`, `COPPER_BULB`, `RESIN_BLOCK`, `CREAKING_HEART`, `BUNDLE`, `POINTED_DRIPSTONE`
- **Items 26.1 (Tiny Takeover)** : `GOLDEN_DANDELION`
- **Enchants mace** : `DENSITY`, `BREACH`, `WIND_BURST`
- **Nouveaux kits** : `macer` (mace + density/breach/wind_burst), `breezer` (breeze rod + wind charges illimitées)

Tous les items utilisent la syntaxe `MATERIAL|FALLBACK1|FALLBACK2` pour rester
fonctionnels même sur des serveurs plus anciens.

## États de partie
WAITING → STARTING → PREPARATION → COMBAT → SUDDEN_DEATH → ENDING → RESETTING

## Choix techniques
- Pas de NMS direct
- Hologrammes via ArmorStand markers (zéro overhead)
- Stockage stats : YAML, flush async débouncé 5s
- Clone de monde : copie fichiers en async, load main thread
- MultiVerse : soft-depend, reflection multi-version (package detection)
- Toutes les commandes & GUIs sont câblées

## Items spéciaux (4 essentiels, pas de texture custom)
Système `SpecialItem` (PersistentDataContainer + glow d'enchantement).
Visuellement ce sont les items vanilla — pas de modèle custom.

- **super_drill** (NETHERITE_PICKAXE) : casse un cube 3x3x3
- **remote_activator** (COMPASS) : TNT amorcée jusqu'à 30 blocs
- **trap_caller** (BLAZE_ROD) : piège aléatoire au bloc visé
- **grappling_hook** (FISHING_ROD) : projection 25 blocs

## Resource pack — menu uniquement
`src/main/resources/resourcepack/` (pack_format 84, MC 26.1) :
- 14 PNG 16×16 **vides** uniquement pour le menu shop
  (7 boutons catégorie + currency + buy/locked + back/close + border + logo)
- `TEXTURES.md` : prompts **Nano Banana** prêts à copier pour générer
  chaque texture, avec mapping fichier → base material → CMD → rôle
- Overrides vanilla via `assets/minecraft/items/*.json` (range_dispatch)
  - PAS d'overrides pour les special items — ils restent visuellement
    vanilla avec un glow d'enchantement
  - 8 overrides pour les éléments UI (paper, gold_nugget, lime_dye,
    red_dye, arrow, barrier, white_stained_glass_pane, nether_star)
- Modèles custom dans `assets/trapparty/models/item/*.json` (14 menu)
- Langs FR/EN (4 entrées de noms d'items spéciaux)
- Build via `scripts/build_pack.sh` → `trapparty-pack.zip` + SHA-1
- Envoi automatique aux joueurs via `config.yml > resource-pack`

## Économie Vault
- `fr.trapparty.economy.VaultHook` (reflection pure, pas d'import direct
  Vault → compile/charge même sans Vault).
- `fr.trapparty.economy.EconomyService` façade : route vers Vault si actif,
  sinon vers les pièces de partie (compat).
- Mode dans `config.yml > economy.mode` (`vault` ou `coins`).
- Tous les achats du shop + récompenses (kill, win, participation) passent
  par EconomyService.

## Shop custom
- GUI custom 6 lignes : bordure décorée (white_stained_glass_pane CMD 2030),
  logo central (nether_star CMD 2031), bouton close (barrier CMD 2023),
  bouton retour (arrow CMD 2022), affichage solde (gold_nugget CMD 2010).
- 7 boutons de catégorie sur PAPER (CMD 2001-2007) : blocs / pièges /
  utility / consumables / redstone / mobility / items spéciaux.
- Affichage par item : indicateur "✓ Clique pour acheter" vs
  "✗ Fonds insuffisants" calculé en live d'après EconomyService.

## Statut
✅ Initial + upgrade 26.1.2 + items spéciaux + resource pack + Vault economy + shop custom

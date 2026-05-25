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

## Items spéciaux + resource pack
Système `SpecialItem` (PersistentDataContainer + CustomModelData) avec 8 items :
super_drill (3x3x3), remote_activator (TNT à 30m), trap_caller (piège
aléatoire), wind_stomper (push), decoy_block (fausse pierre piégée),
grappling_hook (25m), spy_lens (glowing 10s), magnet_bomb (aspire+TNT).

Resource pack dans `src/main/resources/resourcepack/` (pack_format 84, MC 26.1) :
- 8 PNG 16x16 générées par `scripts/gen_textures.py`
- Overrides vanilla via `assets/minecraft/items/*.json` (range_dispatch sur custom_model_data 1001-1008)
- Modèles custom dans `assets/trapparty/models/item/*.json`
- Langs FR/EN
- Build via `scripts/build_pack.sh` → trapparty-pack.zip + SHA-1
- Envoi automatique aux joueurs via `config.yml > resource-pack`

## Statut
✅ Initial + upgrade 26.1.2 + items spéciaux + resource pack

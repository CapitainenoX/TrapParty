# TrapParty

Plugin Minecraft **Trap Survival** : mini-jeu compétitif où chaque joueur prépare
ses pièges puis tente de survivre au PvP dans une mini-arène à bordure rétrécissante.

> Construis. Piège. Survis. Domine.

## Highlights

- Plusieurs parties **simultanées**, chacune sur son monde clone.
- Cycle de partie complet : `WAITING → STARTING → PREPARATION → COMBAT → SUDDEN_DEATH → ENDING → RESETTING`.
- **8 kits** équilibrés (Tank, Archer, Assassin, TrapMaster, Engineer, Berserker, **Macer**, **Breezer**), entièrement configurables.
- Shop en jeu avec items 1.21+ (mace, wind charge, ominous bottle, golden dandelion, copper bulb, tuff, bundles, breeze rod, heavy core…), anti-spam, items gratuits.
- Pièges : TNT, toiles, lave, magma, tripwire, cactus, pointed dripstone… kills attribués à l'owner.
- Événements aléatoires : tempête, météorites, mutations, coffres mystères (loot 1.21+ inclus), speed boost, faible gravité, nuit.
- UI moderne : scoreboard, boss bar, titres, sons, hologrammes natifs ArmorStand.
- **Cible : Minecraft 26.1.2 (Tiny Takeover)** — fonctionne aussi sur les versions plus anciennes grâce au système de fallback YAML.
- Intégration **soft** avec MultiVerse-Core v4 **et** v5 (auto-détection du package).
- Stats persistantes (YAML), MMR simple, hooks pour SQLite/MySQL futurs.
- 0 dépendance lourde — hologrammes via ArmorStand natifs.

## Build

```bash
mvn -B package
```

Le plugin compilé sera dans `target/TrapParty-1.0.0.jar`.

### Dépendances de build

- **Java 21** (requis par Paper 26.1.x)
- **Paper API 26.1.2-R0.1-SNAPSHOT** (depuis `repo.papermc.io`)
- **Multiverse-Core 5.6.1** *(scope provided, optionnel à l'exécution)* — groupId v5 `org.mvplugins.multiverse.core`, repo `repo.onarandombox.com`

> Ces dépôts ne sont pas sur Maven Central : un accès réseau aux dépôts
> `repo.papermc.io` et `repo.onarandombox.com` est requis au build.

## Installation

1. Place `TrapParty-1.0.0.jar` dans `plugins/`.
2. (Optionnel) Installe **Multiverse-Core** pour la gestion des mondes.
3. Démarre le serveur ; édite `plugins/TrapParty/config.yml`, `arenas.yml`, `kits.yml`, `shop.yml`, `messages.yml`.
4. Crée une arène avec `/tpadmin create <id> <templateWorld>` puis `/tpadmin setcenter <id>` et `/tpadmin setspawn <id>` (à répéter pour chaque spawn).
5. `/tparty join` pour rejoindre.

## Commandes

| Commande | Description |
|----------|-------------|
| `/tparty join [arène]` | Rejoindre une partie (auto si arène omise) |
| `/tparty leave` | Quitter la partie |
| `/tparty stats` | Voir ses stats |
| `/tparty list` | Lister les parties en cours |
| `/tparty arenas` | Lister les arènes |
| `/kit [id]` | Ouvrir la GUI ou sélectionner un kit |
| `/shop` | Ouvrir la boutique |
| `/spectate <gameId\|arène>` | Spectate une partie |
| `/tpadmin reload` | Recharger la config |
| `/tpadmin create <arena> <templateWorld>` | Créer une arène |
| `/tpadmin delete <arena>` | Supprimer une arène |
| `/tpadmin setspawn <arena>` | Ajouter un spawn à la position courante |
| `/tpadmin setcenter <arena>` | Définir le centre de l'arène |
| `/tpadmin forcestart <gameId>` | Forcer le démarrage d'une partie |
| `/tpadmin forcestop <gameId>` | Forcer la fin d'une partie |
| `/tpadmin debug` | Toggle mode debug |
| `/tpadmin save` | Flush des stats sur disque |

## Permissions

- `trapparty.play` *(default)* — rejoindre
- `trapparty.spectate` *(default)* — spectate
- `trapparty.shop` *(default)* — shop
- `trapparty.kit.<id>` *(default)* — kit donné
- `trapparty.admin` *(op)* — admin
- `trapparty.*` *(op)* — tout

## Architecture

```
fr.trapparty/
├── TrapPartyPlugin            (bootstrap, hooks, services)
├── compat/VersionAdapter      (détection version + helpers cross-version)
├── config/{ConfigManager,MessagesManager}
├── arena/{Arena,ArenaManager}
├── world/{WorldManager,MultiverseHook,BorderManager}
├── game/{Game,GameManager,GameState,GamePlayer}
├── kit/{Kit,KitManager}
├── shop/{Shop,ShopManager,ShopItem,ShopCategory}
├── trap/{Trap,TrapManager}
├── events/                    (modificateurs random)
│   ├── EventManager
│   └── impl/{StormEvent,MeteorEvent,MutationEvent,LootChestEvent,
│             SpeedBoostEvent,LowGravityEvent,NightFallEvent}
├── ui/{ScoreboardManager,BossBarManager,HologramManager,KitSelectionGui}
├── stats/{PlayerStats,StatsManager}
├── commands/{TrapPartyCommand,AdminCommand,KitCommand,ShopCommand,SpectateCommand}
├── listeners/{PlayerListener,GameListener,TrapListener}
└── util/{ItemBuilder,LocationUtil,RandomUtil}
```

## Compatibilité multi-version

Cible principale : **MC 26.1.2** (api-version `26.1` dans `plugin.yml`).
Le code reste tolérant aux versions antérieures (1.20 → 1.21.x) grâce à :

- `VersionAdapter` qui parse le nouveau (`26.1.2`) et l'ancien (`1.20.x`)
  schéma de numérotation, et expose des helpers cross-version (titres,
  action bars Spigot chat API, sons, particules, couleurs cuir).
- `ItemBuilder.resolveMaterial("MACE|IRON_SWORD|STICK")` — syntaxe pipe
  pour fournir des fallbacks. Le premier matériau disponible sur le serveur
  est utilisé ; idéal pour shipper la même `kits.yml` sur tous les serveurs.
- `ItemBuilder.lookupEnchant(...)` — Registry moderne (`NamespacedKey`)
  puis `getByName` legacy, plus une table d'alias (`SHARPNESS` ⇆ `DAMAGE_ALL`,
  `POWER` ⇆ `ARROW_DAMAGE`…) qui couvre les renommages 1.20.5+.
- `EffectUtil.byName(...)` — même principe pour les `PotionEffectType`
  (`JUMP_BOOST` ⇆ `JUMP`, `STRENGTH` ⇆ `INCREASE_DAMAGE`…).
- `Game#maxHealthOf` utilise `Attribute.MAX_HEALTH` (26.1+) puis
  `GENERIC_MAX_HEALTH` (1.20.x) puis `Damageable#getMaxHealth` legacy.
- `GameManager#applyRule` utilise `GameRule.getByName` moderne avec
  fallback sur l'API string dépréciée.
- `MultiverseHook` détecte v4 (`com.onarandombox`) **ou** v5
  (`org.mvplugins`) par package et adapte ses appels par reflection.
- Aucune dépendance NMS directe ; pas de dépendance dure sur Adventure.

### Items et enchantements MC 1.21+ intégrés

| Catégorie | Exemples |
|-----------|----------|
| Items | `MACE`, `WIND_CHARGE`, `BREEZE_ROD`, `HEAVY_CORE`, `TRIAL_KEY`, `OMINOUS_BOTTLE`, `CRAFTER`, `COPPER_BULB`, `TUFF`, `POLISHED_TUFF`, `RESIN_BLOCK`, `CREAKING_HEART`, `BUNDLE`, `POINTED_DRIPSTONE` |
| Items 26.1 | `GOLDEN_DANDELION` |
| Enchants mace | `DENSITY`, `BREACH`, `WIND_BURST` |
| Nouveaux kits | `macer` (mace + density/breach/wind_burst), `breezer` (breeze rod, wind charges illimitées) |

## Performances

- Clonage de mondes asynchrone.
- Suppression des mondes temporaires en async + délai configurable.
- Stats : cache mémoire + flush debounced (5 s) en async.
- Tâches Bukkit limitées (BukkitRunnable annulables, pas de leak).
- Hologrammes via ArmorStands marker (zéro tick, pas d'overhead réseau).
- BossBar partagée par partie, pas une par joueur.
- Limite configurable du nombre de parties simultanées (`performance.max-arenas`).

## Configuration

Tout est dans `plugins/TrapParty/` :

- `config.yml` — game settings, timers, économie, performance.
- `messages.yml` — toutes les chaînes (codes couleurs `&`, hex `&#RRGGBB`).
- `arenas.yml` — arènes (templates de mondes).
- `kits.yml` — kits PvP.
- `shop.yml` — boutique avec catégories.

`/tpadmin reload` recharge tout à chaud.

## Roadmap (idées)

- [ ] Mode équipes (toggle déjà dans `config.yml`).
- [ ] Mode ranked complet (MMR Elo K-factor déjà câblé).
- [ ] Quêtes journalières / succès.
- [ ] Replay highlights.
- [ ] Hook PlaceholderAPI.

## Licence

MIT.

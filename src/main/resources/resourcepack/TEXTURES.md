# TrapParty Resource Pack — 3 GUIs custom

Le resource pack ne contient **que 3 PNG** : une image par interface
du plugin. Les boutons (acheter / sélectionner / fermer / etc.) sont
**dessinés directement dans l'image** ; le plugin place les items
Bukkit *par-dessus* aux positions correspondantes.

| GUI | Fichier PNG | Commande in-game | Inventaire |
|---|---|---|---|
| Shop pièges | `assets/trapparty/textures/gui/shop_traps.png` | `/shop` | 54 slots (9×6) |
| Sélection de kit | `assets/trapparty/textures/gui/kits.png` | `/kit` | 54 slots (9×6) |
| Items spéciaux | `assets/trapparty/textures/gui/crafts.png` | `/crafts` | 27 slots (9×3) |

## Comment ça marche techniquement

Le resource pack définit une font custom
(`assets/minecraft/font/default.json`) qui associe un caractère unicode
de la PUA (`` / `` / ``) à chaque image. Quand le
plugin ouvre l'inventaire, il met ce caractère unicode comme titre :

```java
inv = Bukkit.createInventory(null, 54, ""); // shop
```

Côté client, le renderer Minecraft remplace ce char par l'image avec
l'ascent défini dans `default.json` → l'image apparaît comme arrière-plan
de la GUI.

## Positions des slots à respecter dans tes designs

Les boutons cliquables sont des **slots Bukkit standards** (case
18×18 px dans la GUI). Conçois ton image en sachant où ils vont
apparaître.

### `shop_traps.png` — 21 emplacements item piège

3 lignes de 7 items. Slots Bukkit utilisés :

```
. . . . . . . . .     0  1  2  3  4  5  6  7  8
. ▦ ▦ ▦ ▦ ▦ ▦ ▦ .     9  10 11 12 13 14 15 16 17    ← items
. ▦ ▦ ▦ ▦ ▦ ▦ ▦ .     18 19 20 21 22 23 24 25 26    ← items
. ▦ ▦ ▦ ▦ ▦ ▦ ▦ .     27 28 29 30 31 32 33 34 35    ← items
. . . . . . . . .     36 37 38 39 40 41 42 43 44
. . . . X . . . .     45 46 47 48 49 50 51 52 53    ← X = close (slot 49)
```

Dessine donc 3 rangées d'emplacements pour items piège dans la zone
correspondante, et un bouton "Fermer" sous le slot 49.

### `kits.png` — 8 emplacements kit (2×4)

```
. . . . . . . . .     0  1  2  3  4  5  6  7  8
. . . . . . . . .     9  10 11 12 13 14 15 16 17
. ▦ . ▦ . ▦ . ▦ .     18 19 20 21 22 23 24 25 26    ← kits row 1 (19,21,23,25)
. . . . . . . . .     27 28 29 30 31 32 33 34 35
. ▦ . ▦ . ▦ . ▦ .     36 37 38 39 40 41 42 43 44    ← kits row 2 (37,39,41,43)
. . . . . . . . .     45 46 47 48 49 50 51 52 53
```

Dessine 8 cadres "kit" à ces 8 positions.

### `crafts.png` — 4 items spéciaux

```
. . . . . . . . .     0  1  2  3  4  5  6  7  8
. ▦ . ▦ . ▦ . ▦ .     9  10 11 12 13 14 15 16 17    ← 4 items (10,12,14,16)
. . . . . . . . .     18 19 20 21 22 23 24 25 26
```

Dessine 4 cadres avec sous chacun un panneau "info" indiquant
*Shop / Kit / Admin*.

## Prompts Nano Banana

> **Recette à ajouter à tous les prompts** : *"PNG with transparent
> background, 256x256, designed as a Minecraft inventory GUI
> overlay/background, vanilla Minecraft pixel art style, no
> anti-aliasing, sharp pixel edges, the design must keep slot
> positions clear and uncluttered."*

> Les textures sont **256×256 PNG transparent**. Tu peux dessiner à
> plus haute résolution puis downscale (nearest-neighbor) si Nano
> Banana ne sort pas en pixel art natif.

---

### 1. `shop_traps.png` — Shop pièges (``)

> **Prompt nano-banana :**
> "Minecraft custom GUI overlay 256x256 pixel art, titled 'TRAP SHOP'
> at the top in bold red and white pixel font with a flame motif,
> dark stone-brick background with a worn red banner header,
> three horizontal rows of seven empty item frame slots (each frame
> a 18x18 dark beveled square with a faint red glow), under each
> frame a small 'BUY' button drawn in gold pixel font, at the bottom
> center a red close button labeled 'X' with rounded edges, decorative
> chains and TNT motifs in the corners, transparent background outside
> the GUI area, Minecraft vanilla art direction, 256x256, no
> anti-aliasing, sharp pixel edges."

### 2. `kits.png` — Sélection de kit (``)

> **Prompt nano-banana :**
> "Minecraft custom GUI overlay 256x256 pixel art, titled 'CHOOSE
> YOUR KIT' at the top in bold blue and white pixel font with a
> sword motif, dark obsidian stone background with a worn blue banner
> header, two horizontal rows of four kit slots (each kit slot is a
> 18x18 framed square with a parchment scroll underneath showing
> 'SELECT'), the slots arranged in two evenly spaced rows separated
> by a thin gold divider, decorative armor pieces in the corners
> (helmet, chestplate, sword, bow), transparent background outside
> the GUI area, Minecraft vanilla art direction, 256x256, no
> anti-aliasing, sharp pixel edges."

### 3. `crafts.png` — Items spéciaux (``)

> **Prompt nano-banana :**
> "Minecraft custom GUI overlay 256x256 pixel art, titled 'SPECIAL
> ITEMS' at the top in bold purple and white pixel font with a
> sparkle motif, dark enchanted background with a worn purple banner
> header, a single horizontal row of four item frames (each frame
> 18x18 dark beveled square with a magenta glow), under each frame a
> small info plate showing icons for SHOP / KIT / ADMIN sources,
> decorative magical runes around the borders, transparent
> background outside the GUI area, Minecraft vanilla art direction,
> 256x256, no anti-aliasing, sharp pixel edges."

---

## Tuning de la position de l'image

Si après installation l'image apparaît trop haute / trop basse,
édite la valeur `ascent` correspondante dans
`assets/minecraft/font/default.json` :

```json
{
  "type": "bitmap",
  "file": "trapparty:gui/shop_traps.png",
  "ascent": -8,    // augmente pour descendre, diminue pour remonter
  "height": 256,
  "chars": [""]
}
```

Des caractères de décalage horizontal (`` à ``) sont
également fournis dans la font pour micro-ajuster, mais en pratique
l'image se centre automatiquement.

## Workflow

1. Pour chacun des 3 PNG, copie le prompt nano-banana dans Gemini /
   nano-banana et génère.
2. Si l'image n'est pas en 256×256, downscale nearest-neighbor.
3. Sauvegarde dans `assets/trapparty/textures/gui/` avec le nom exact.
4. Rebuild : `bash scripts/build_pack.sh` → produit `trapparty-pack.zip`.
5. Upload sur ton CDN, mets URL + SHA-1 dans
   `plugins/TrapParty/config.yml > resource-pack`.

Les joueurs reçoivent le pack à la connexion ; toutes les GUI du
plugin afficheront le fond custom.

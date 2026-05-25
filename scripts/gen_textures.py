#!/usr/bin/env python3
"""Génère des textures 16x16 PNG pour les items spéciaux TrapParty.
Chaque texture est un dessin pixel art simple, sans dépendance externe.
"""
import struct, zlib, os, pathlib

TEX_DIR = pathlib.Path("src/main/resources/resourcepack/assets/trapparty/textures/item")
TEX_DIR.mkdir(parents=True, exist_ok=True)

W = H = 16

def write_png(path, pixels):
    """pixels: list[list[(r,g,b,a)]] de H lignes x W colonnes."""
    raw = b""
    for row in pixels:
        raw += b"\x00"  # filter byte
        for r, g, b, a in row:
            raw += bytes((r, g, b, a))
    sig = b"\x89PNG\r\n\x1a\n"
    def chunk(kind, data):
        return struct.pack(">I", len(data)) + kind + data + struct.pack(">I", zlib.crc32(kind + data) & 0xffffffff)
    ihdr = struct.pack(">IIBBBBB", W, H, 8, 6, 0, 0, 0)  # 8-bit, RGBA
    idat = zlib.compress(raw, 9)
    out = sig + chunk(b"IHDR", ihdr) + chunk(b"IDAT", idat) + chunk(b"IEND", b"")
    path.write_bytes(out)

def blank(rgba=(0,0,0,0)):
    return [[rgba for _ in range(W)] for _ in range(H)]

def fill_rect(px, x0, y0, x1, y1, color):
    for y in range(max(0,y0), min(H,y1+1)):
        for x in range(max(0,x0), min(W,x1+1)):
            px[y][x] = color

def line(px, x0, y0, x1, y1, color):
    # bresenham simple
    dx = abs(x1-x0); sx = 1 if x0<x1 else -1
    dy = -abs(y1-y0); sy = 1 if y0<y1 else -1
    err = dx + dy
    x, y = x0, y0
    while True:
        if 0<=x<W and 0<=y<H: px[y][x] = color
        if x==x1 and y==y1: break
        e2 = 2*err
        if e2 >= dy: err += dy; x += sx
        if e2 <= dx: err += dx; y += sy

def circle(px, cx, cy, r, color):
    for y in range(H):
        for x in range(W):
            if (x-cx)**2 + (y-cy)**2 <= r*r:
                px[y][x] = color

# Couleurs
CYAN = (78, 220, 230, 255)
DARK_CYAN = (40, 110, 130, 255)
DARKER = (20, 60, 80, 255)
WHITE = (240, 240, 240, 255)
GRAY = (130, 130, 130, 255)
DARK_GRAY = (60, 60, 60, 255)
GOLD = (240, 200, 60, 255)
RED = (220, 60, 60, 255)
DARK_RED = (140, 30, 30, 255)
ORANGE = (240, 140, 40, 255)
PURPLE = (170, 60, 220, 255)
DARK_PURPLE = (90, 30, 130, 255)
GREEN = (80, 200, 80, 255)
BLUE = (60, 130, 240, 255)
BROWN = (130, 80, 40, 255)
BLACK = (15, 15, 15, 255)
YELLOW = (255, 230, 90, 255)
PINK = (240, 130, 200, 255)
AMETHYST = (180, 130, 240, 255)

# ----- super_drill -----  pioche avec gemme cyan
px = blank()
# manche brun
for i in range(8):
    fill_rect(px, 11-i, 11+i, 12-i, 12+i, BROWN)
# tête de pioche (triangle métallique)
fill_rect(px, 2, 2, 13, 4, GRAY)
fill_rect(px, 1, 3, 14, 3, GRAY)
fill_rect(px, 3, 5, 12, 5, DARK_GRAY)
# gemme centrale
fill_rect(px, 7, 2, 8, 3, CYAN)
fill_rect(px, 6, 4, 9, 4, CYAN)
fill_rect(px, 7, 5, 8, 5, DARK_CYAN)
write_png(TEX_DIR / "super_drill.png", px)

# ----- remote_activator -----  boussole/détonateur rouge
px = blank()
circle(px, 7, 7, 6, DARK_GRAY)
circle(px, 7, 7, 5, GRAY)
circle(px, 7, 7, 4, WHITE)
# antenne
fill_rect(px, 12, 1, 12, 4, DARK_GRAY)
fill_rect(px, 13, 0, 13, 1, RED)
# bouton rouge central
fill_rect(px, 6, 6, 8, 8, RED)
fill_rect(px, 6, 6, 6, 6, DARK_RED)
fill_rect(px, 8, 8, 8, 8, DARK_RED)
# LED
fill_rect(px, 2, 13, 3, 14, RED)
write_png(TEX_DIR / "remote_activator.png", px)

# ----- trap_caller -----  baton magique doré avec étoile
px = blank()
# manche doré
for i in range(7):
    fill_rect(px, 10-i, 9+i, 11-i, 10+i, GOLD)
# étoile orange en haut
fill_rect(px, 9, 3, 11, 5, ORANGE)
fill_rect(px, 8, 4, 12, 4, ORANGE)
fill_rect(px, 10, 2, 10, 6, ORANGE)
fill_rect(px, 10, 4, 10, 4, YELLOW)
# particule
fill_rect(px, 13, 5, 13, 5, YELLOW)
fill_rect(px, 7, 7, 7, 7, YELLOW)
write_png(TEX_DIR / "trap_caller.png", px)

# ----- wind_stomper -----  vortex bleu
px = blank()
circle(px, 7, 7, 6, DARK_CYAN)
circle(px, 7, 7, 5, CYAN)
circle(px, 7, 7, 3, WHITE)
# spirale
fill_rect(px, 7, 4, 7, 5, DARK_CYAN)
fill_rect(px, 10, 7, 11, 7, DARK_CYAN)
fill_rect(px, 7, 10, 7, 11, DARK_CYAN)
fill_rect(px, 4, 7, 5, 7, DARK_CYAN)
write_png(TEX_DIR / "wind_stomper.png", px)

# ----- decoy_block -----  bloc gris avec point d'interrogation
px = blank()
fill_rect(px, 2, 2, 13, 13, GRAY)
fill_rect(px, 2, 2, 13, 2, WHITE)  # top highlight
fill_rect(px, 2, 13, 13, 13, DARK_GRAY)
fill_rect(px, 2, 2, 2, 13, WHITE)
fill_rect(px, 13, 2, 13, 13, DARK_GRAY)
# ?
fill_rect(px, 6, 5, 9, 5, RED)
fill_rect(px, 9, 6, 9, 7, RED)
fill_rect(px, 7, 8, 8, 8, RED)
fill_rect(px, 7, 10, 8, 11, RED)
write_png(TEX_DIR / "decoy_block.png", px)

# ----- grappling_hook -----  crochet/grappin
px = blank()
# manche
fill_rect(px, 9, 10, 11, 11, BROWN)
fill_rect(px, 12, 7, 13, 9, BROWN)
# crochet métallique
fill_rect(px, 5, 4, 9, 4, GRAY)
fill_rect(px, 5, 3, 5, 6, GRAY)
fill_rect(px, 5, 6, 8, 6, GRAY)
fill_rect(px, 8, 6, 8, 8, GRAY)
fill_rect(px, 2, 4, 4, 4, GRAY)
fill_rect(px, 2, 3, 2, 5, GRAY)
# corde
line(px, 9, 5, 12, 8, BROWN)
write_png(TEX_DIR / "grappling_hook.png", px)

# ----- spy_lens -----  longue vue violette
px = blank()
fill_rect(px, 3, 6, 11, 9, DARK_PURPLE)
fill_rect(px, 4, 7, 10, 8, PURPLE)
fill_rect(px, 11, 5, 13, 10, DARK_PURPLE)
fill_rect(px, 12, 6, 12, 9, PURPLE)
# lentille
fill_rect(px, 13, 6, 13, 9, CYAN)
fill_rect(px, 13, 7, 13, 8, WHITE)
# yeux/visu
fill_rect(px, 2, 6, 2, 9, BLACK)
write_png(TEX_DIR / "spy_lens.png", px)

# ----- magnet_bomb -----  bombe ronde violette avec mèche
px = blank()
circle(px, 8, 9, 5, DARK_PURPLE)
circle(px, 8, 9, 4, AMETHYST)
fill_rect(px, 6, 8, 7, 8, WHITE)  # reflet
# mèche
fill_rect(px, 8, 3, 8, 5, BROWN)
fill_rect(px, 9, 2, 9, 3, ORANGE)
fill_rect(px, 10, 1, 10, 2, RED)
write_png(TEX_DIR / "magnet_bomb.png", px)

# ----- pack icon (pack.png) -----
px = blank((30, 30, 50, 255))
fill_rect(px, 2, 2, 13, 13, (40, 40, 70, 255))
# T stylisé rouge
fill_rect(px, 4, 4, 11, 5, RED)
fill_rect(px, 7, 5, 8, 12, RED)
# étincelles
fill_rect(px, 12, 3, 13, 3, YELLOW)
fill_rect(px, 3, 11, 3, 12, YELLOW)
write_png(pathlib.Path("src/main/resources/resourcepack/pack.png"), px)

print("Textures générées :", sorted(p.name for p in TEX_DIR.glob("*.png")))

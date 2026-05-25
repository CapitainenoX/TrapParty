package fr.trapparty.items.impl;

import fr.trapparty.TrapPartyPlugin;
import fr.trapparty.items.SpecialItem;
import fr.trapparty.trap.Trap;
import fr.trapparty.util.RandomUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;
import java.util.Map;

/**
 * Trap Caller — clic droit : pose un piège aléatoire au bloc visé
 * (toile, magma, cactus, ou TNT amorcée). 3 utilisations.
 */
public class TrapCaller implements SpecialItem {

    private static final List<String> POOL =
            List.of("COBWEB", "MAGMA_BLOCK", "CACTUS", "POINTED_DRIPSTONE", "LAVA", "TNT");

    @Override public String id() { return "trap_caller"; }
    @Override public String displayName() { return "&6&l☠ Trap Caller"; }
    @Override public List<String> lore() {
        return List.of(
                "&7Invoque un &cpiège aléatoire",
                "&7au bloc que tu vises.",
                "&7Toile / Magma / Cactus / Dripstone / Lave / TNT.");
    }
    @Override public String baseMaterial() { return "BLAZE_ROD"; }
    @Override public int maxUses() { return 3; }
    @Override public int cost() { return 50; }
    @Override public String shopCategory() { return "traps"; }

    @Override
    public boolean onUse(Player p, PlayerInteractEvent e) {
        TrapPartyPlugin plugin = TrapPartyPlugin.get();
        Block target = e.getClickedBlock();
        if (target == null) target = p.getTargetBlockExact(10);
        if (target == null) return false;
        Block above = target.getRelative(0, 1, 0);
        if (above.getType() != Material.AIR && !above.getType().isAir()) return false;

        String pick = RandomUtil.pick(POOL);
        Material mat = Material.matchMaterial(pick);
        if (mat == null) mat = Material.COBWEB;

        switch (pick) {
            case "TNT" -> {
                org.bukkit.entity.TNTPrimed tnt = above.getWorld().spawn(
                        above.getLocation().add(0.5, 0, 0.5),
                        org.bukkit.entity.TNTPrimed.class);
                tnt.setFuseTicks(30);
                try { tnt.setSource(p); } catch (Throwable ignored) {}
            }
            case "LAVA" -> above.setType(Material.LAVA);
            default -> {
                above.setType(mat);
                plugin.traps().register(
                        plugin.games().forPlayer(p) != null ? plugin.games().forPlayer(p).getId() : "global",
                        new Trap(p.getUniqueId(), trapTypeFor(mat), above.getLocation()));
            }
        }
        plugin.messages().send(p, "trap.placed", Map.of("trap", pick.toLowerCase()));
        plugin.version().sound(p, "BLOCK_BEACON_ACTIVATE", 1f, 1.8f);
        return true;
    }

    private Trap.Type trapTypeFor(Material m) {
        return switch (m) {
            case COBWEB -> Trap.Type.COBWEB;
            case MAGMA_BLOCK -> Trap.Type.MAGMA;
            case CACTUS -> Trap.Type.CACTUS;
            default -> Trap.Type.FAKE_BLOCK;
        };
    }
}

package fr.trapparty.util;

import org.bukkit.Location;
import org.bukkit.World;

public final class LocationUtil {
    private LocationUtil() {}

    /** Parse "x,y,z" ou "x,y,z,yaw,pitch". Retourne null si world est null (évite p.teleport NPE). */
    public static Location parse(World world, String def) {
        if (world == null || def == null || def.isEmpty()) return null;
        String[] parts = def.split(",");
        if (parts.length < 3) return null;
        try {
            double x = Double.parseDouble(parts[0].trim());
            double y = Double.parseDouble(parts[1].trim());
            double z = Double.parseDouble(parts[2].trim());
            float yaw = parts.length >= 4 ? Float.parseFloat(parts[3].trim()) : 0f;
            float pitch = parts.length >= 5 ? Float.parseFloat(parts[4].trim()) : 0f;
            return new Location(world, x, y, z, yaw, pitch);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static String serialize(Location loc) {
        return loc.getX() + "," + loc.getY() + "," + loc.getZ()
                + "," + loc.getYaw() + "," + loc.getPitch();
    }
}

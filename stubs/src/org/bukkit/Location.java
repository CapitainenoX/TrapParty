package org.bukkit;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
public class Location implements Cloneable {
    public Location(World w, double x, double y, double z){}
    public Location(World w, double x, double y, double z, float yaw, float pitch){}
    public World getWorld(){return null;}
    public double getX(){return 0;}
    public double getY(){return 0;}
    public double getZ(){return 0;}
    public float getYaw(){return 0;}
    public float getPitch(){return 0;}
    public int getBlockX(){return 0;}
    public int getBlockY(){return 0;}
    public int getBlockZ(){return 0;}
    public Block getBlock(){return null;}
    public Vector getDirection(){return new Vector();}
    public void setY(double y){}
    public Vector toVector(){return new Vector();}
    public Location add(double x,double y,double z){return this;}
    public Location add(Vector v){return this;}
    public Location subtract(Location other){return this;}
    @Override public Location clone(){return this;}
}

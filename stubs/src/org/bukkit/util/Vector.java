package org.bukkit.util;
public class Vector {
    public Vector(){}
    public Vector(double x, double y, double z){}
    public Vector setY(double y){return this;}
    public Vector multiply(double f){return this;}
    public Vector normalize(){return this;}
    public Vector add(Vector v){return this;}
    public Vector subtract(Vector v){return this;}
    public double length(){return 0;}
    public double getX(){return 0;}
    public double getY(){return 0;}
    public double getZ(){return 0;}
    public org.bukkit.Location toLocation(org.bukkit.World world){return null;}
}

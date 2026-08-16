package org.bukkit.util;

public class Vector {
    private double x;
    private double y;
    private double z;
    
    public Vector(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public Vector() {
        this(0, 0, 0);
    }
    
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    public double getZ() { return z; }
    public void setZ(double z) { this.z = z; }
    
    public Vector add(Vector other) {
        return new Vector(x + other.x, y + other.y, z + other.z);
    }
    
    public Vector subtract(Vector other) {
        return new Vector(x - other.x, y - other.y, z - other.z);
    }
    
    public Vector multiply(double factor) {
        return new Vector(x * factor, y * factor, z * factor);
    }
    
    public double length() {
        return Math.sqrt(x * x + y * y + z * z);
    }
    
    public double lengthSquared() {
        return x * x + y * y + z * z;
    }
    
    public Vector normalize() {
        double len = length();
        if (len == 0) {
            return new Vector(0, 0, 0);
        }
        return new Vector(x / len, y / len, z / len);
    }
    
    public double distance(Vector other) {
        return subtract(other).length();
    }
    
    public double distanceSquared(Vector other) {
        return subtract(other).lengthSquared();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Vector other = (Vector) obj;
        return Double.compare(other.x, x) == 0 &&
               Double.compare(other.y, y) == 0 &&
               Double.compare(other.z, z) == 0;
    }
    
    @Override
    public int hashCode() {
        int result = Double.hashCode(x);
        result = 31 * result + Double.hashCode(y);
        result = 31 * result + Double.hashCode(z);
        return result;
    }
    
    @Override
    public String toString() {
        return "Vector{x=" + x + ", y=" + y + ", z=" + z + "}";
    }
}
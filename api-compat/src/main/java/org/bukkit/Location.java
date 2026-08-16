package org.bukkit;

public class Location {
    private World world;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    
    public Location(World world, double x, double y, double z) {
        this(world, x, y, z, 0, 0);
    }
    
    public Location(World world, double x, double y, double z, float yaw, float pitch) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }
    
    public World getWorld() { return world; }
    public void setWorld(World world) { this.world = world; }
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    public double getZ() { return z; }
    public void setZ(double z) { this.z = z; }
    public float getYaw() { return yaw; }
    public void setYaw(float yaw) { this.yaw = yaw; }
    public float getPitch() { return pitch; }
    public void setPitch(float pitch) { this.pitch = pitch; }
    
    public int getBlockX() { return (int) Math.floor(x); }
    public int getBlockY() { return (int) Math.floor(y); }
    public int getBlockZ() { return (int) Math.floor(z); }
    
    public double distance(Location other) {
        if (world != other.world) return Double.MAX_VALUE;
        double dx = x - other.x;
        double dy = y - other.y;
        double dz = z - other.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
    
    public Location add(double x, double y, double z) {
        return new Location(world, this.x + x, this.y + y, this.z + z, yaw, pitch);
    }
    
    public Location add(Location other) {
        return new Location(world, x + other.x, y + other.y, z + other.z, yaw, pitch);
    }
    
    public Location subtract(double x, double y, double z) {
        return new Location(world, this.x - x, this.y - y, this.z - z, yaw, pitch);
    }
    
    public Location subtract(Location other) {
        return new Location(world, x - other.x, y - other.y, z - other.z, yaw, pitch);
    }
    
    public Location multiply(double factor) {
        return new Location(world, x * factor, y * factor, z * factor, yaw, pitch);
    }
    
    @Override
    public String toString() {
        return "Location{world=" + (world != null ? world.getName() : "null") +
               ", x=" + x + ", y=" + y + ", z=" + z + "}";
    }
}
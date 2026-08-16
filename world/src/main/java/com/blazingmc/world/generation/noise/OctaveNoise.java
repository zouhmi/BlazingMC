package com.blazingmc.world.generation.noise;

public class OctaveNoise {
    private final SimplexNoise[] octaves;
    private final double[] amplitudes;
    private final double[] frequencies;
    private final double persistence;
    private final double lacunarity;
    
    public OctaveNoise(long seed, int octaves, double persistence, double lacunarity) {
        this.octaves = new SimplexNoise[octaves];
        this.amplitudes = new double[octaves];
        this.frequencies = new double[octaves];
        this.persistence = persistence;
        this.lacunarity = lacunarity;
        
        for (int i = 0; i < octaves; i++) {
            this.octaves[i] = new SimplexNoise(seed + i * 1000L);
            this.amplitudes[i] = Math.pow(persistence, i);
            this.frequencies[i] = Math.pow(lacunarity, i);
        }
    }
    
    public double noise2D(double x, double y) {
        double total = 0;
        double maxAmplitude = 0;
        
        for (int i = 0; i < octaves.length; i++) {
            total += octaves[i].noise2D(x * frequencies[i], y * frequencies[i]) * amplitudes[i];
            maxAmplitude += amplitudes[i];
        }
        
        return total / maxAmplitude;
    }
    
    public double noise3D(double x, double y, double z) {
        double total = 0;
        double maxAmplitude = 0;
        
        for (int i = 0; i < octaves.length; i++) {
            total += octaves[i].noise3D(x * frequencies[i], y * frequencies[i], z * frequencies[i]) * amplitudes[i];
            maxAmplitude += amplitudes[i];
        }
        
        return total / maxAmplitude;
    }
    
    public int getOctaves() {
        return octaves.length;
    }
    
    public double getPersistence() {
        return persistence;
    }
    
    public double getLacunarity() {
        return lacunarity;
    }
}
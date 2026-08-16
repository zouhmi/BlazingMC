package org.bukkit.potion;

public class PotionEffect {
    private final PotionEffectType type;
    private final int duration;
    private final int amplifier;
    private final boolean ambient;
    private final boolean particles;
    private final boolean icon;
    
    public PotionEffect(PotionEffectType type, int duration, int amplifier) {
        this(type, duration, amplifier, true, true, true);
    }
    
    public PotionEffect(PotionEffectType type, int duration, int amplifier, boolean ambient) {
        this(type, duration, amplifier, ambient, true, true);
    }
    
    public PotionEffect(PotionEffectType type, int duration, int amplifier, boolean ambient, boolean particles) {
        this(type, duration, amplifier, ambient, particles, true);
    }
    
    public PotionEffect(PotionEffectType type, int duration, int amplifier, boolean ambient, boolean particles, boolean icon) {
        this.type = type;
        this.duration = duration;
        this.amplifier = amplifier;
        this.ambient = ambient;
        this.particles = particles;
        this.icon = icon;
    }
    
    public PotionEffectType getType() { return type; }
    public int getDuration() { return duration; }
    public int getAmplifier() { return amplifier; }
    public boolean isAmbient() { return ambient; }
    public boolean hasParticles() { return particles; }
    public boolean hasIcon() { return icon; }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PotionEffect other = (PotionEffect) obj;
        return type.equals(other.type) && duration == other.duration && amplifier == other.amplifier;
    }
    
    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + duration;
        result = 31 * result + amplifier;
        return result;
    }
}
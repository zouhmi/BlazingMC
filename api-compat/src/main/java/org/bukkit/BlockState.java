package org.bukkit;

public class BlockState {
    private final Block block;
    private Material type;
    private byte data;
    
    public BlockState(Block block) {
        this.block = block;
        this.type = block.getType();
        this.data = block.getData();
    }
    
    public Block getBlock() { return block; }
    public Material getType() { return type; }
    public void setType(Material type) { this.type = type; }
    public byte getData() { return data; }
    public void setData(byte data) { this.data = data; }
    
    public void update() {
        block.setType(type);
    }
    
    public void update(boolean force) {
        update();
    }
    
    public void update(boolean force, boolean applyPhysics) {
        update();
    }
}
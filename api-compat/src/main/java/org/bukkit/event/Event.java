package org.bukkit.event;

public abstract class Event {
    private final String eventName;
    private final boolean async;
    
    public Event() {
        this.eventName = this.getClass().getSimpleName();
        this.async = false;
    }
    
    public Event(boolean isAsynchronous) {
        this.eventName = this.getClass().getSimpleName();
        this.async = isAsynchronous;
    }
    
    public String getEventName() {
        return eventName;
    }
    
    public boolean isAsynchronous() {
        return async;
    }
}
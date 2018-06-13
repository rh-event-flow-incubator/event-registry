package io.streamzi.strombrau.registry.items;

/**
 * Represents a type of event stored in the registry
 * @author hhiden
 */
public class EventType {
    public String name;
    public String topicName = "";

    public EventType() {
    }

    public EventType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name + "->" + topicName;
    }
    
    
}
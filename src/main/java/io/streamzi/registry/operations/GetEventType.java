package io.streamzi.registry.operations;

import io.streamzi.registry.RegistryException;
import io.streamzi.registry.RegistryOperation;
import io.streamzi.registry.items.EventType;

/**
 * Get an event type with a specified name
 * @author hhiden
 */
public class GetEventType extends RegistryOperation {
    private String name;
    private EventType eventType;

    public GetEventType(String name) {
        this.name = name;
    }

    @Override
    public void performOperation() throws RegistryException {
        try {
            String key = "events/" + name;
            String data = connection.getStringForKey(key);
            eventType = mapper.readValue(data, EventType.class);
        } catch (Exception ex){
            throw new RegistryException("Error performing operation" , ex);
        }
    }

    public EventType getEventType() {
        return eventType;
    }
}

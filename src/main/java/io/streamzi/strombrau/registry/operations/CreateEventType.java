package io.streamzi.strombrau.registry.operations;

import io.streamzi.strombrau.registry.RegistryException;
import io.streamzi.strombrau.registry.RegistryOperation;
import io.streamzi.strombrau.registry.items.EventType;

/**
 * Create a new type of event
 * @author hhiden
 */
public class CreateEventType extends RegistryOperation {
    private EventType evt;
    
    public CreateEventType(EventType evt) {
        this.evt = evt;
    }

    @Override
    public void performOperation() throws RegistryException {
        try {
            String data = mapper.writeValueAsString(evt);
            String key = "events/" + evt.name;
            connection.addChildForKey("events", evt.name);
            connection.setStringForKey(key, data);
        } catch (Exception ex){
            throw new RegistryException("Error performing operation" , ex);
        }
    }
}

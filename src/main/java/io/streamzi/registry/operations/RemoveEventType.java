package io.streamzi.registry.operations;

import io.streamzi.registry.RegistryException;
import io.streamzi.registry.RegistryOperation;
import io.streamzi.registry.items.EventType;

/**
 * Removes an event type from the registry
 * @author hhiden
 */
public class RemoveEventType extends RegistryOperation {
    String eventTypeName;

    public RemoveEventType(String name) {
        this.eventTypeName = name;
    }
    
    public RemoveEventType(EventType eventType){
        this.eventTypeName = eventType.name;
    }

    @Override
    public void performOperation() throws RegistryException {
        try {
            connection.removeKey("events/" + eventTypeName);
        } catch (Exception ex){
            throw new RegistryException("Error performing operation" , ex);
        }        
    }    
}
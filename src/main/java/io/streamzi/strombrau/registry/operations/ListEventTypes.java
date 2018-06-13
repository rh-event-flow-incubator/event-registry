package io.streamzi.strombrau.registry.operations;

import io.streamzi.strombrau.registry.RegistryException;
import io.streamzi.strombrau.registry.RegistryOperation;
import io.streamzi.strombrau.registry.items.EventType;
import java.util.ArrayList;
import java.util.List;

/**
 * Lists all of the event types
 * @author hhiden
 */
public class ListEventTypes extends RegistryOperation {
    private final List<EventType> results = new ArrayList<>();
    
    @Override
    public void performOperation() throws RegistryException {
        try {
            List<String> children = connection.getChildStringsForKey("events");
            for(String child : children){
                results.add(mapper.readValue("evets/" + child, EventType.class));
            }
        } catch (Exception ex){
            throw new RegistryException("Error performing operation" , ex);
        }
    }

    public List<EventType> getResults() {
        return results;
    }
}
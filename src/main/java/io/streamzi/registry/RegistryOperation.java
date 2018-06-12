package io.streamzi.registry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.Yaml;
import java.util.concurrent.FutureTask;

/**
 * Represents an operation that can be performed on the registry
 * @author hhiden
 */
public abstract class RegistryOperation {
    protected ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    
    protected RegistryConnection connection;

    public RegistryOperation() {
        super();
    }

    public void setConnection(RegistryConnection connection) {
        this.connection = connection;
    }

    public RegistryConnection getConnection() {
        return connection;
    }
    
    public abstract void performOperation() throws RegistryException;
}
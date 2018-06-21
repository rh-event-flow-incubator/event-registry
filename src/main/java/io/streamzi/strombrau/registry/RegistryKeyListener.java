package io.streamzi.strombrau.registry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;

/**
 * This class defines a listener that receives events from a single key in the registry
 *
 * @author hhiden
 */
public abstract class RegistryKeyListener<T> {
    private static final Logger logger = Logger.getLogger(RegistryKeyListener.class.getName());
    
    private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    private PathChildrenCache cache;
    private RegistryConnection connection;
    private String key;

    public RegistryKeyListener() {
    }

    protected void setup(String key, RegistryConnection connection) throws RegistryException {
        this.connection = connection;
        this.key = key;
        try {
            cache = new PathChildrenCache(connection.getClient(), connection.buildKey(key), true);
            cache.start();
            cache.rebuild();
            cache.getListenable().addListener((client, event) -> {
                switch(event.getType()){
                    case CHILD_ADDED:
                        try {
                            byte[] data = event.getData().getData();
                            T keyValue = recreatObject(data);
                            objectAdded(keyValue);
                        } catch (Exception e){
                            logger.log(Level.SEVERE, "Error processing CHILD_ADDED event: " + e.getMessage(), e);
                        }

                        break;

                    case CHILD_REMOVED:
                        try {
                            byte[] data = event.getData().getData();
                            T keyValue = recreatObject(data);
                            objectRemoved(keyValue);
                        } catch (Exception e){
                            logger.log(Level.SEVERE, "Error processing CHILD_REMOVED event: " + e.getMessage(), e);
                        }
                        break;

                    case CHILD_UPDATED:
                        try {
                            byte[] data = event.getData().getData();
                            T keyValue = recreatObject(data);
                            objectChanged(keyValue);
                        } catch (Exception e){
                            logger.log(Level.SEVERE, "Error processing CHILD_UPDATED event: " + e.getMessage(), e);
                        }
                        break;

                    default:
                        logger.info(event.getType().toString());
                }
            });
        
        } catch (Exception e){
            logger.severe(e.getMessage());
        }    
    }

    public String getKey() {
        return key;
    }

    public void close(){
        if(connection!=null){
            if(cache!=null){
                try {
                    cache.close();
                } catch(Exception e){
                    logger.log(Level.SEVERE, "Error closing cache: " + e.getMessage(), e);
                }
            }
            connection.removeKeyListener(this);
            connection = null;
            
        }
    }
    
    public List<T> getValues() throws RegistryException {
        try {
            final List<T> results = new ArrayList<>();

            for(ChildData child : cache.getCurrentData()){
                results.add(recreatObject(child.getData()));
            }
            return results;
        } catch (Exception e){
            throw new RegistryException("Cannot get cached values; " + e.getMessage(), e);
        }
    }
    
    private T recreatObject(byte[] data) throws RegistryException {
        try {
            final Type superclassType = getClass().getGenericSuperclass();
            final Type t = ((ParameterizedType) superclassType).getActualTypeArguments()[0];
            final Class<?> clazz = Class.forName(t.getTypeName());
            return (T) mapper.readValue(data, clazz);
        } catch (Exception e) {
            throw new RegistryException("Error creating object: " + e.getMessage(), e);
        }
    }

    public abstract void objectAdded(T value);

    public abstract void objectRemoved(T value);

    public abstract void objectChanged(T value);
}

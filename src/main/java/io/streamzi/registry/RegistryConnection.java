package io.streamzi.registry;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.log4j.Level;

/**
 * This class maintains a connection to a Zookeper server
 *
 * @author hhiden
 */ 
public class RegistryConnection implements Closeable {
    private static final Logger logger = Logger.getLogger(RegistryConnection.class.getName());
    
    private String baseKey = "/strombrau";
    private ExecutorService executor;
    private CopyOnWriteArrayList<RegistryKeyListener> listeners = new CopyOnWriteArrayList<>();

    public RegistryConnection() {
        createExecutor();
    }

    public RegistryConnection(String zookeeperUrl) {
        this.zookeeperUrl = zookeeperUrl;
        createExecutor();
    }

    private void createExecutor(){
        try {
            executor = InitialContext.doLookup("java:comp/DefaultManagedExecutorService");
            logger.log(java.util.logging.Level.INFO, "Using managed executor service");
        } catch (NamingException e) {
            executor = Executors.newSingleThreadExecutor();  
            logger.log(java.util.logging.Level.INFO, "Using single threaded executor service");
        }
    }
        
    @Override
    public void close() throws IOException {
        // No more operations
        executor.shutdownNow();
        
        // Shutdown the listners
        for(RegistryKeyListener l : listeners){
            l.close();
        }

        // CLose the client
        client.close();
    }

    /**
     * Perform an operation synchronously
     */
    public void executeSync(RegistryOperation operation) throws RegistryException {
        assertConnected();
        operation.setConnection(this);
        operation.performOperation();
    }
    
    /**
     * Perform an operation on the registry
     * @param operation to be performed
     * @return a Future object that can be used late r
     * @throws RegistryException if the registry connection has been closed
     */
    public CompletableFuture execute(RegistryOperation operation) {
        if(!executor.isShutdown()){
            CompletableFuture<RegistryOperation> future = new CompletableFuture<>();
            operation.setConnection(this);
            executor.submit(()->{
                try {
                    operation.performOperation();
                    future.complete(operation);
                } catch (Exception e){
                    future.completeExceptionally(e);
                }
            });
            return future;
        } else {
            CompletableFuture<RegistryOperation> future = new CompletableFuture<>();
            executor.submit(()->{
                future.completeExceptionally(new RegistryException("Registry not connected"));
            });
            return future;
        }
    }
    
    /**
     * Add a listener
     */
    public RegistryKeyListener addKeyListener(String key, RegistryKeyListener listener) throws RegistryException {
        assertConnected();
        try {
            // Add the key if needed
            if(!keyExists(buildKey(key))){
                client.create().creatingParentContainersIfNeeded().forPath(buildKey(key));
            }
            
            synchronized(listeners){
                listener.setup(key, this);
                listeners.add(listener);
            }
            return listener;
        } catch (Exception e){
            throw new RegistryException("Error adding RegistryKeyListener: " + e.getMessage(), e);
        }
    }
    
    /** 
     * Remove a listener
     */
    protected void removeKeyListener(RegistryKeyListener listener){
        synchronized(listeners){
            listeners.remove(listener);
        }
    }
    
    
    /**
     * URL pointing to the zookeeper instance that maintains all of the state data
     */
    private String zookeeperUrl;

    private CuratorFramework client;
    private volatile boolean connected = false;

    public String getBaseKey() {
        return baseKey;
    }

    public void setBaseKey(String baseKey) {
        this.baseKey = baseKey;
    }

    public void connect() {
        try {
            client = CuratorFrameworkFactory.newClient(zookeeperUrl, new ExponentialBackoffRetry(1000, 3));
            client.start();
            client.blockUntilConnected();
            
            client.getConnectionStateListenable().addListener(new ConnectionStateListener() {
                @Override
                public void stateChanged(CuratorFramework client, ConnectionState newState) {
                    logger.info("Connection state change: " + newState.toString());
                }
            });
            
            connected = true;

        } catch (Exception e) {
            connected = false;
        }
    }

    public CuratorFramework getClient() {
        return client;
    }

    private void assertConnected() throws RegistryException {
        if (!connected) {
            throw new RegistryException("Registry not connected to Zookeper");
        }
    }

    public void addChildForKey(String key, String child) throws RegistryException {
        assertConnected();
        try {
            if(!keyExists(buildKey(key) + "/" + child)){
                client.create().creatingParentContainersIfNeeded().forPath(buildKey(key) + "/" + child);
            }
        } catch (Exception e){
            throw new RegistryException("Error adding child: " + child, e);
        }
    }
    
    public void setStringForKey(String key, String value) throws RegistryException {
        assertConnected();
        try {
            if(!keyExists(buildKey(key))){
                client.create().creatingParentContainersIfNeeded().forPath(buildKey(key));
            }
            client.setData().forPath(buildKey(key), value.getBytes());
        } catch (Exception e){
            throw new RegistryException("Error setting data for key: " + key, e);
        }
    }
    
    public List<String> getChildStringsForKey(String key) throws RegistryException {
        assertConnected();
        try {
            List<String> children = client.getChildren().forPath(buildKey(key));
            for(String c : children){
                logger.info(c);
            }
            return children;
        } catch (Exception e){
            throw new RegistryException("Error listing children for key: " + key, e);
        }
    }
    
    public String getStringForKey(String key) throws RegistryException {
        assertConnected();
        try {
            byte[] data = client.getData().forPath(buildKey(key));
            return new String(data);
        } catch (Exception e) {
            throw new RegistryException("Error getting data for: " + key, e);
        }
    }
    
    public void removeKey(String key) throws RegistryException {
        assertConnected();
        try {
            if(keyExists(buildKey(key))){
                client.delete().forPath(buildKey(key));
            }
        } catch (Exception e){
            throw new RegistryException("Error removing key: " + key, e);
        }
    }
    
    private boolean keyExists(String key) throws RegistryException {
        assertConnected();
        try {
            return(client.checkExists().forPath(key)!=null);
        } catch (Exception e){
            throw new RegistryException("Error checking if key exists: " + key, e);
        }
    }
    
    protected String buildKey(String key){
        return baseKey + "/" + key;
    }

    public boolean isConnected() {
        return connected;
    }
}

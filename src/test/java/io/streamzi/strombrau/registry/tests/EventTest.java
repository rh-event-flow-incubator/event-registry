package io.streamzi.strombrau.registry.tests;

import io.streamzi.strombrau.registry.RegistryConnection;
import io.streamzi.strombrau.registry.RegistryException;
import io.streamzi.strombrau.registry.RegistryKeyListener;
import io.streamzi.strombrau.registry.items.EventType;
import io.streamzi.strombrau.registry.operations.CreateEventType;
import io.streamzi.strombrau.registry.operations.ListEventTypes;
import io.streamzi.strombrau.registry.operations.RemoveEventType;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests basic event listening
 *
 * @author hhiden
 */
public class EventTest {

    private static final Logger logger = Logger.getLogger(EventTest.class.getName());
    private static RegistryConnection connection1;
    private static RegistryConnection connection2;
    private static boolean operationCompleted;
    public EventTest() {

    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        connection1 = new RegistryConnection("localhost:2181");
        connection1.setBaseKey("/strombrautests");
        connection1.connect();
        
        connection2 = new RegistryConnection("localhost:2181");
        connection2.setBaseKey("/strombrautests");
        connection2.connect();
        
        logger.info("Zookeeper connected");
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        connection1.close();
        connection2.close();
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void testListener() throws Exception {
        final RegistryKeyListener<EventType> listener = connection2.addKeyListener("events", new RegistryKeyListener<EventType>() {
            @Override
            public void objectAdded(EventType value) {
                logger.info("OBJECT_ADDED event:" + value.name);
                
                try {
                    for(EventType evt : getValues()){
                        logger.info(evt.toString());
                    }
                } catch (RegistryException re){
                    re.printStackTrace();
                    fail();
                }
            }

            @Override
            public void objectRemoved(EventType value) {
                logger.info("OBJECT_REMOVED event: " + value);
            }

            @Override
            public void objectChanged(EventType value) {
                logger.info("OBJECT_CHANGED event: " + value);
            }
        });
        

        operationCompleted = false;
        connection1.execute(new CreateEventType(new EventType("MeterReading"))).thenRun(new Runnable() {
            @Override
            public void run() {
                logger.info("MeterReading added");
                EventType updated = new EventType("MeterReading");
                updated.topicName = "TOPICS/METERREADING";
                connection1.execute(new CreateEventType(updated)).thenRun(new Runnable() {
                    @Override
                    public void run() {
                        logger.info("MeterReading modified");
                        
                        // List event types
                        try {
                            final ListEventTypes list = new ListEventTypes();
                            connection1.executeSync(list);
                            for(EventType evt : list.getResults()){
                                logger.info(evt.toString());
                            }
                        } catch (Exception e){
                            logger.log(Level.SEVERE, "Error listing event types:" + e.getMessage());
                            fail();
                        }
                        
                        
                        // Wait for a second before deleting
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e){}
                        connection1.execute(new RemoveEventType("MeterReading")).thenRun(new Runnable() {
                            @Override
                            public void run() {
                                logger.info("MeterReading removed");
                                operationCompleted = true;
                            }
                        });
                    }

                }
                );
            }
        });


        while(!operationCompleted){
            Thread.sleep(100);
        }
    }
}

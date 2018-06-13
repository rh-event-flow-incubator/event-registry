/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.streamzi.registry.strombrau.tests;

import io.streamzi.strombrau.registry.RegistryConnection;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test the registry function
 *
 * @author hhiden
 */
public class RegistryTest {

    private static final Logger logger = Logger.getLogger(RegistryTest.class.getName());
    private static RegistryConnection connection;

    public RegistryTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        connection = new RegistryConnection("localhost:2181");
        connection.setBaseKey("/strombrautests");
        connection.connect();
        logger.info("Zookeeper connected");
    }

    @AfterClass
    public static void tearDownClass() {
        try {
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    
    @Test
    public void registerKey() throws Exception {
        logger.info("Registering key");
        String data = "This is some test data";
        connection.setStringForKey("testdata", data);
        String returnData = connection.getStringForKey("testdata");
        assertEquals(data, returnData);

        // Try setting it again
        data = "This is some new data";
        connection.setStringForKey("testdata", data);
        returnData = connection.getStringForKey("testdata");
        assertEquals(data, returnData);

    }
    

}

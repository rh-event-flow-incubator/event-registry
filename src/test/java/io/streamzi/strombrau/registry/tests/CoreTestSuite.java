/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.streamzi.strombrau.registry.tests;

import java.util.logging.Logger;
import org.apache.curator.test.TestingServer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Run all of the core tests
 * @author hhiden
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    RegistryTest.class,
    EventTest.class
})
public class CoreTestSuite {
    private static final Logger logger = Logger.getLogger(CoreTestSuite.class.getName());
    private static TestingServer testServer;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        logger.info("Setup Class");
        testServer = new TestingServer(2181, true);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        logger.info("Teardown class");
        testServer.close(); // Deletes storage
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {   
    }
}
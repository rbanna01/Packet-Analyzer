/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tests;

import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Ruaridhi Bannatyne
 */
public class InputThreadsTest {
        private InputThread iT;
        private int SAMPLE = 50;
        private int INTERVAL = 500;
        private Random r;
        private Utils tU;
    public InputThreadsTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    tU = new Utils();
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void inputJobsTest () {
    //tests inputThread's handling of batches; 2 or 3 full ones plus and incomplete one; 125 in all
    LinkedBlockingQueue<Packet> test = tU.getData((SAMPLE*3)+(SAMPLE/2), null, null, null, 0, 0, null);
    iT = new InputThread(test, true);
    //if(iT.setFilter(true)) System.out.println("Filter on");
    LinkedBlockingQueue<LinkedList<Packet>> output = iT.getJobList();
    //System.out.println(test.size());
    iT.start();
    try{
    Thread.sleep(1000 * 10);
    iT.interrupt();
    Thread.sleep(10);
    assertEquals("Wrong output quantity! " + output.size(), 4, output.size());
    } catch(InterruptedException e) {}   
    }
   
    @Test public void inputNoJobsTest() {
    LinkedBlockingQueue<Packet> data = tU.getData(SAMPLE, null, null, null, 0, 0, null);
    iT = new InputThread(data, false);
    LinkedBlockingQueue<Packet> output = new LinkedBlockingQueue<Packet>();
    iT.setOutput(output);
    iT.start();
    try{
    Thread.sleep(1000);
    iT.interrupt();
    Thread.sleep(10);
    assertEquals("Wrong output quantity!" + output.size(), output.size(), SAMPLE);
    } catch(InterruptedException e) {}  
    }
       
}

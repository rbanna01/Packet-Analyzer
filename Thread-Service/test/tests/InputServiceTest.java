/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tests;

import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import thread.service.InputService;
import thread.service.*;
import thread.service.Utils;
/**
 *
 * @author Ruaridhi Bannatyne
 */
public class InputServiceTest {
    private InputService iS;
        private int SAMPLE = 50;
        private int INTERVAL = 500;
        private Random r;
        private ExecutorService eS = Executors.newFixedThreadPool(1);
    
    
    public InputServiceTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
      
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void inputJobsTest () {
    //tests inputThread's handling of batches; 2 or 3 full ones plus and incomplete one; 125 in all
    LinkedBlockingQueue<Packet> test = Utils.getData((SAMPLE*3)+(SAMPLE/2), null, null, null, 0, 0, null);
    iS = new InputService(test);
    //if(iT.setFilter(true)) System.out.println("Filter on");
    LinkedBlockingQueue<LinkedList<Packet>> output = iS.getJobList();
    iS.setExecutor(eS);
    //System.out.println(test.size());
    iS.start();
    try{
    Thread.sleep(1000 * 10);
    iS.cancel();
    Thread.sleep(10);
    assertEquals("Wrong output quantity! " + output.size(), 4, output.size());
    } catch(InterruptedException e) {}   
    }
   
    @Test 
    public void inputNoJobsTest() {
    LinkedBlockingQueue<Packet> data = Utils.getData(SAMPLE, null, null, null, 0, 0, null);
    iS = new InputService(data);
    LinkedBlockingQueue<Packet> output = new LinkedBlockingQueue<>();
    iS.setOutput(output);
    iS.start();
    try{
    Thread.sleep(1000);
    iS.cancel();
    Thread.sleep(10);
    assertEquals("Wrong output quantity!" + output.size(), output.size(), SAMPLE);
    } catch(InterruptedException e) {}  
    }
           




}

/* Has been tested
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
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Ruaridhi Bannatyne
 */
public class RuleThreadTests {
    private Random r;
    private final int SAMPLE = 50;
    private final int INTERVAL = 100;
    private LinkedBlockingQueue<LinkedList<Packet>> jobList;
    private Rules rules;
    private InputThread iT;
    private RuleThread rT;
    private LinkedBlockingQueue<Packet> output;
    public RuleThreadTests() {
    }
    
    @BeforeClass
    public static void seUtilspClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void seUtilsp() {
    r = new Random();    
    rules = new Rules();
    output = new LinkedBlockingQueue<>();
    }
    
    @After
    public void tearDown() {
    }

    @Test 
    public void processTest() {
       byte[] toUse = { 11, 11, 11, 11,11, 11};
       LinkedBlockingQueue<LinkedList<Packet>> q = new LinkedBlockingQueue<>();
       LinkedList<Packet> pL = new LinkedList<>();
       for(Packet p: Utils.getData(50, toUse, null, null, 0, 0, null)) pL.add(p);
       LinkedBlockingQueue<Packet> out = new LinkedBlockingQueue<>();
       rules = new Rules();
       rules.setSource(toUse);
       q.add(pL);
       RuleThread rT = new RuleThread(q, rules.getTests(), out);
       try{
       rT.start();
       Thread.sleep(200);
       rT.interrupt();
       Thread.sleep(200);
       assertTrue("Predicates" + out.size(),out.size() == 50);
       } catch(InterruptedException e) {} 
     }
      
    @Test
    public void filterTimeAfterTest() {
    //tests RuleThread's handling of time after
    long trigger = r.nextLong();
    Timestamp startingTime = new Timestamp(trigger);
    rules.setStart(new Timestamp(trigger));
    LinkedBlockingQueue<Packet> start = Utils.getData(SAMPLE, null, null, startingTime, 0, 0, null);
    LinkedBlockingQueue<Packet> before = Utils.getData(SAMPLE, null,null, new Timestamp(trigger-10), 0, 0, null);
    LinkedBlockingQueue<Packet> after = Utils.getData(SAMPLE, null, null, new Timestamp(trigger+10), 0, 0, null);
    LinkedBlockingQueue<Packet> output1 = new LinkedBlockingQueue<>();
    LinkedBlockingQueue<Packet> output2 = new LinkedBlockingQueue<>();
    LinkedBlockingQueue<Packet> input1 = Utils.mix(start, before);  //outputsize should be 50
    LinkedBlockingQueue<Packet> input2 = Utils.mix(start, after); //output size should be 100
    System.out.println(input2.size() +"input 2");
    System.out.println(input1.size() +"input 1");
    //System.out.println(input1.size());
        
    try {
        InputThread iT = new InputThread(input1, true);
        LinkedBlockingQueue<LinkedList<Packet>> forrT = iT.getJobList();
        InputThread iT2 = new InputThread(input2, true);
        LinkedBlockingQueue<LinkedList<Packet>> forrT2 = iT2.getJobList();
        RuleThread rT2 = new RuleThread(forrT2, rules.getTests(), output2);
        RuleThread rT = new RuleThread(forrT, rules.getTests(), output1);    
        rT.start();
        iT.start();
        iT2.start();
        rT2.start();
        Thread.sleep(INTERVAL);
        //System.out.println(forrT.size());
        Thread.sleep(INTERVAL*5);
        rT.interrupt();
        Thread.sleep(INTERVAL*5);
        //System.out.println("Output size is " +output1.size());
        Thread.sleep(INTERVAL);
        iT.interrupt();
        iT2.interrupt();
        Thread.sleep(100);
        rT.interrupt();
        rT2.interrupt();
        Thread.sleep(100);
        assertEquals("Valid time thread output is " + output2.size(), 2*SAMPLE, output2.size());
    } catch(InterruptedException e) {}
    }
   
        
     @Test
     public void FilterDestTest() {
     //RT's handling of filtering by destination   
        byte[] target = { 23, 32, 12, 45, 11, 22};
        String targetString = target.toString();
        byte[] control = { 11, 11, 11, 11, 11, 11 };
        LinkedBlockingQueue<Packet> valid = Utils.getData(SAMPLE,null, target, null,0, 0, null);
        LinkedBlockingQueue<Packet> invalid = Utils.getData(SAMPLE, null, control, null, 0, 0, null);
        LinkedBlockingQueue toUse = Utils.mix(valid, invalid);
        LinkedBlockingQueue<Packet> output = new LinkedBlockingQueue<>();
        rules.setDest(target);
        iT = new InputThread(toUse, true);
        LinkedBlockingQueue<LinkedList<Packet>> feed = iT.getJobList();
        rT = new RuleThread(feed, rules.getTests(),output);
        iT.start();
        rT.start();
        try{
            Thread.sleep(1000);
            iT.interrupt();
            rT.interrupt();
            assertEquals("Size of filtered list is " + output.size(), output.size(), SAMPLE);
            for(Packet p: output)
            {
                byte[] b = p.getDestination();
               for(int i = 0; i < target.length; i++)
                assertTrue("Filter not working " + b, b[i] == target[i]);
            }
        }
        catch(InterruptedException e) {}
     }
        
        
     @Test
    public void FilterSourceDestTest() {
    //RT's handling of filtering by both    
        byte[] targetS = { 23, 32, 12, 45, 11, 22};
        byte[] targetD = {22, 11, 45, 12, 32, 23};
        byte[] control = { 11, 11, 11, 11, 11, 11 };
        rules = new Rules(); 
        rules.setSource(targetS);
        rules.setDest(targetD);
        LinkedList<LinkedBlockingQueue<Packet>> testData = new LinkedList<>();
        LinkedBlockingQueue<Packet> valid =  Utils.getData(SAMPLE,targetS, targetD, null, 0, 0, null);
        LinkedBlockingQueue<Packet> invalid = Utils.getData(SAMPLE, control, control, null, 0, 0, null);
        LinkedBlockingQueue<Packet> invalidS = Utils.getData(SAMPLE, control, targetD, null, 0, 0, null);
        LinkedBlockingQueue<Packet> invalidD = Utils.getData(SAMPLE, targetS, control, null, 0, 0, null);
        //combinations: valid & invalid D, valid & invalidS
        testData.add(Utils.mix(valid, invalid));
        testData.add(Utils.mix(valid, invalidS));
        testData.add(Utils.mix(valid,invalidD));
        testData.add(Utils.mix(invalidS, invalidD));
        for(LinkedBlockingQueue<Packet> set: testData) 
        {
        LinkedBlockingQueue<Packet> output = new LinkedBlockingQueue<>();
        iT = new InputThread(set, true);
        LinkedBlockingQueue<LinkedList<Packet>> feed = iT.getJobList();
        rT = new RuleThread(feed, rules.getTests(),output);
        iT.start();
        rT.start();
        try{
            Thread.sleep(1000);
            iT.interrupt();
            rT.interrupt();
            //assertEquals("Size of filtered list is " + output.size(), output.size(), SAMPLE);
            for(Packet p: output)
            {
                byte[] source = p.getSource();
                byte[] dest = p.getDestination();
                for(int i = 0; i < 6; i++) {
               assertEquals("Filter not working ", source[i], targetS[i]);
               assertEquals("Dest filter not working", dest[i], targetD[i]);
                }
            }
        }
        catch(InterruptedException e) {}
        }
    }   
       
    @Test
    public void FilterSourceTest() {
    // RT's handling of filtering by source    
        rules = new Rules();
        byte[] target = { 23, 32, 12, 45, 11, 22};
        byte[] control = { 11, 11, 11, 11, 11, 11 };
        LinkedBlockingQueue<Packet> valid = Utils.getData(SAMPLE,target, null, null, 0, 0, null);
        LinkedBlockingQueue<Packet> invalid = Utils.getData(SAMPLE, control, null, null, 0, 0, null);
        LinkedBlockingQueue<Packet> toUse = Utils.mix(valid, invalid);
        LinkedBlockingQueue<Packet> output = new LinkedBlockingQueue<>();
        rules.setSource(target);
        iT = new InputThread(toUse, true);
        LinkedBlockingQueue<LinkedList<Packet>> feed = iT.getJobList();
        rT = new RuleThread(feed, rules.getTests(),output);
        iT.start();
        rT.start();
        try{
            Thread.sleep(10000);
            iT.interrupt();
            rT.interrupt();
            Thread.sleep(500);
            assertEquals("Size of filtered list is " + output.size(), output.size(), SAMPLE);
            for(Packet p: output) {  
                byte[] s1 = p.getSource();
                for(int i = 0; i <6; i++) {
                    assertEquals("Filter not working ", s1[i], target[i]);
                } }
        }
        catch(InterruptedException e) {}
    }
    @Test
    public void filterTimeStartTest() {
    // should work with RuleThread directly
    long target = r.nextLong();
    long negative = target - 1;
    LinkedBlockingQueue<Packet> positive = Utils.getData(SAMPLE, null, null, new Timestamp(target), 0, 0, null);
    LinkedBlockingQueue<Packet> control = Utils.getData(SAMPLE, null, null, new Timestamp(negative), 0, 0, null);    
    LinkedBlockingQueue<Packet> out = new LinkedBlockingQueue<>();
    rules.setStart(new Timestamp(target));
    iT = new InputThread(Utils.mix(positive, control), true);
    jobList = iT.getJobList();
    rT = new RuleThread(jobList, rules.getTests(), out);
    iT.start();
    rT.start();
    try {
    Thread.sleep(INTERVAL);
      iT.interrupt();
      Thread.sleep(INTERVAL);
      rT.interrupt();
      Thread.sleep(INTERVAL);
      assertEquals("Wrong output quantity!" + out.size(), out.size(), SAMPLE);
      assertTrue("Time start filter not working", 
              Utils.check(null, null, new Timestamp(target), null, 0, 0, 0, out));
    } catch (InterruptedException e) {}
   }
   
    @Test
    public void filterTimeStopTest() {
    // should work with RuleThread directly
    rules = new Rules();
    long target = r.nextLong();    
    long negative = r.nextLong();
    while(true) {
    if(target >= negative) negative = r.nextLong();
    else break;
    }
    LinkedBlockingQueue<Packet> positive = 
            Utils.getData(SAMPLE, null, null, new Timestamp(target), 0, 0, null);
    LinkedBlockingQueue<Packet> control =
            Utils.getData(SAMPLE, null, null, new Timestamp(negative), 0, 0, null);    
    LinkedBlockingQueue<Packet> toTest = Utils.mix(positive, control);
    LinkedBlockingQueue<Packet> out = new LinkedBlockingQueue<>();
    rules.setStop(new Timestamp(target));
    //use an inputThread to break into batches
    iT = new InputThread(toTest, true);
    jobList = iT.getJobList();
    rT = new RuleThread(jobList, rules.getTests(), out);
    iT.start();
    rT.start();
    try {
    Thread.sleep(INTERVAL);
      iT.interrupt();
      rT.interrupt();
      assertEquals("Wrong output quantity!" + out.size(), out.size(), SAMPLE);
      assertTrue("Stop filter not working",
              Utils.check(null,null, null, new Timestamp(target), 0, 0, 0, out));
    } catch (InterruptedException e) {}
   }
    
    @Test
    public void filterSourceTimeBeforeTest() {
        byte[] source = { 11, 22, 33, 44, 55, 66};
        long time = r.nextLong();
        Timestamp t = new Timestamp(time);
        rules = new Rules();
        rules.setSource(source);
        rules.setStop(t);
        
        iT = new InputThread(Utils.mix
        (Utils.mix(Utils.getData(SAMPLE, source, null, new Timestamp(time-1), 0, 0, null), Utils.getData(SAMPLE, source, null, t,0, 0, null)),
                Utils.mix(Utils.getData(SAMPLE, source, null, null, 0, 0, null), Utils.getData(SAMPLE, null,null, null, 0, 0, null))), true);
        rT =new RuleThread(iT.getJobList(), rules.getTests(),output);
        iT.start();
        rT.start();
        try{
        Thread.sleep(INTERVAL); }
        catch(InterruptedException e) {}
        assertTrue("Time-source filters failed ", 
                Utils.check(source, null, null, t, 0, 0, 0, output));
    //filtering by source and time    
    }
    @Test
    public void filterSourceTimeAfterTest() {
        byte[] source = { 11, 22, 33, 44, 55, 66};
        long time = r.nextLong();
        Timestamp t = new Timestamp(time);
        rules = new Rules();
        rules.setSource(source);
        rules.setStart(t);
        
        iT = new InputThread(Utils.mix(
                Utils.mix(Utils.getData(SAMPLE, source, null, t, 0, 0, null), Utils.getData(SAMPLE, null, null, t, 0, 0, null)),
                Utils.mix(Utils.getData(SAMPLE, source, null, new Timestamp(time -10), 0, 0, null), Utils.getData(SAMPLE, null,null, new Timestamp(time -20), 0, 0, null))), true);
        rT =new RuleThread(iT.getJobList(), rules.getTests(),output);
        iT.start();
        rT.start();
        try{
        Thread.sleep(INTERVAL); }
        catch(InterruptedException e) {}
        assertTrue("TimeAfter-source filters failed ",
                Utils.check(source, null, t, null, 0, 0, 0, output));
    }
    
    @Test 
    public void filterDestTimeBeforeTest() {
    // filtering by destination and time    
        byte[] dest = { 22, 33, 44, 55, 66, 77 };
        long time = r.nextLong();
        Timestamp t = new Timestamp(time);
        rules = new Rules();
        rules.setDest(dest);
        rules.setStop(t);
        
        iT = new InputThread(Utils.mix(
                Utils.mix(Utils.getData(SAMPLE, null, dest, t, 0, 0, null), Utils.getData(SAMPLE, null, null, t, 0, 0, null)),
                Utils.mix(Utils.getData(SAMPLE, null, dest, new Timestamp(time -10), 0, 0, null), Utils.getData(SAMPLE, null,dest, null, 0, 0, null))), true);
        rT =new RuleThread(iT.getJobList(), rules.getTests(),output);
        iT.start();
        rT.start();
        try{
        Thread.sleep(INTERVAL); }
        catch(InterruptedException e) {}
        assertTrue("TimeAfter-source filters failed ", Utils.check(null, dest, null, t, 0, 0, 0, output));
    }
    
    @Test
    public void filterDestTimeAfterTest() {
        byte[] dest = { 22, 33, 44, 55, 66, 77 };
        long time = r.nextLong();
        Timestamp t = new Timestamp(time);
        rules = new Rules();
        rules.setDest(dest);
        rules.setStart(t);
        
        iT = new InputThread(Utils.mix(
                Utils.mix(
                        Utils.getData(SAMPLE, null, dest, t, 0, 0, null), Utils.getData(SAMPLE, null, null, t, 0, 0, null)),
                Utils.mix(
                        Utils.getData(SAMPLE, null, dest, new Timestamp(time -10), 0, 0, null), Utils.getData(SAMPLE, null,dest, new Timestamp(time + 10), 0, 0, null))), true);
        rT =new RuleThread(iT.getJobList(), rules.getTests(),output);
        iT.start();
        rT.start();
        try{
        Thread.sleep(INTERVAL); }
        catch(InterruptedException e) {}
        assertTrue("TimeAfter-source filters failed ", 
                Utils.check(null, dest, t, null, 0, 0, 0, output));
       assertTrue("time after output wrong ", output.size() == 2*SAMPLE);
    }
   @Test
    public void FilterSourceDestTimeBeforeTest() {
    //filtering by source, destination and time   
   byte[] dest = { 22, 33, 44, 55, 66, 77 };
   byte[] source = {11, 22, 33, 44, 55, 66};
        long time = r.nextLong();
        Timestamp t = new Timestamp(time);
        rules = new Rules();
        rules.setDest(dest);
        rules.setSource(source);
        rules.setStop(t);
        LinkedBlockingQueue<Packet> input = Utils.mix(
                Utils.mix(Utils.getData(SAMPLE, source, dest, new Timestamp(time-10), 0, 0, null), Utils.getData(SAMPLE, source, null, t,0, 0, null)),
                Utils.mix(Utils.getData(SAMPLE, null, dest, t, 0, 0, null), Utils.getData(SAMPLE, source,dest, new Timestamp(time + 10), 0, 0, null)));
        iT = new InputThread(input, true);
        System.out.println("sourcedesttimebefore test  input size is " + input.size());
        rT =new RuleThread(iT.getJobList(), rules.getTests(),output);
        iT.start();
        rT.start();
        try{
        Thread.sleep(INTERVAL); }
        catch(InterruptedException e) {}
        assertTrue("TimeAfter-source filters failed ", 
                Utils.check(source, dest, null, t, 0, 0, 0, output));
       
    }
    @Test
    public void filterSourceDestTimeAfterTest() {
        byte[] dest = { 22, 33, 44, 55, 66, 77 };
        byte[] source = {11, 22, 33, 44, 55, 66};
        long time = r.nextLong();
        Timestamp t = new Timestamp(time);
        rules = new Rules();
        rules.setDest(dest);
        rules.setSource(source);
        rules.setStart(t);
        LinkedBlockingQueue<Packet> r = Utils.getData(SAMPLE, source, dest, t, 0, 0, null);
        LinkedBlockingQueue<Packet> s =  Utils.getData(SAMPLE, source, null, t, 0, 0, null);
        LinkedBlockingQueue<Packet> q = Utils.mix(
                Utils.mix(Utils.getData(SAMPLE, source, dest, t, 0, 0, null), Utils.getData(SAMPLE, source, null, t, 0, 0, null)),
                Utils.mix(Utils.getData(SAMPLE, null, dest, t, 0, 0, null), Utils.getData(SAMPLE, source,dest, new Timestamp(time - 10), 0, 0, null)));
        System.out.println("q is " + q.size());
        try{
            iT = new InputThread(q, true);
            rT =new RuleThread(iT.getJobList(), rules.getTests(),output);
            iT.start();
            rT.start();
            Thread.sleep(3000);
            Thread.sleep(INTERVAL*10); }
        catch(InterruptedException e) {}
        assertTrue("Packets lost!" + output.size(), output.size() == SAMPLE);
        assertTrue("TimeAfter-source filters failed ", Utils.check(source, dest, null, t, 0, 0, 0, output));
    }
    
    @Test
        public void mixTest() {
            LinkedBlockingQueue<Packet> q = Utils.mix(Utils.getData(SAMPLE, null, null, null, 0, 0, null),
                    Utils.getData(SAMPLE, null, null, null, 0, 0, null));
            assertEquals("Mix output is " +q.size(), q.size(), 2*SAMPLE);
        }
 
   @Test
   public void portTest() {
       int validPort = 56;
       int invalidPort = 65;
       LinkedBlockingQueue<Packet> q = Utils.mix(Utils.getData(SAMPLE, null, null, null, 0, validPort, null),
                    Utils.getData(SAMPLE, null, null, null, 0, invalidPort, null));
       rules.setPort(validPort);
       iT = new InputThread(q, true);
       rT = new RuleThread(iT.getJobList(), rules.getTests(),output);
       rT.start();
       try{ 
       Thread.sleep(3000);
       Thread.sleep(INTERVAL*10);  }
       catch(InterruptedException e) {}
       assertTrue("Port filter failed ", Utils.check(null, null, null, null, 0, 0, validPort, output));
   }
        
   @Test 
   public void minLengthTest()  {
       int minLength = 45;
       int invalidLength = 30;
        LinkedBlockingQueue<Packet> q = Utils.mix(Utils.getData(SAMPLE, null, null, null, 0, minLength+10, null),
                    Utils.getData(SAMPLE, null, null, null, invalidLength, 0, null));
       rules.setLengthMin(minLength);
       iT = new InputThread(q, true);
       rT = new RuleThread(iT.getJobList(), rules.getTests(),output);
       rT.start();
       try{ 
       Thread.sleep(3000);
       Thread.sleep(INTERVAL*10);  }
       catch(InterruptedException e) {}
       assertTrue("Port filter failed ", Utils.check(null, null, null, null, minLength, 0, 0, output));
   }
   @Test 
   public void maxLengthTest()  {
       int maxLength = 45;
       int invalidLength = 70;
        LinkedBlockingQueue<Packet> q = Utils.mix(Utils.getData(SAMPLE, null, null, null, 0, maxLength-10, null),
                    Utils.getData(SAMPLE, null, null, null, invalidLength, 0, null));
       rules.setLengthMax(maxLength);
       iT = new InputThread(q, true);
       rT = new RuleThread(iT.getJobList(), rules.getTests(),output);
       rT.start();
       try{ 
       Thread.sleep(3000);
       Thread.sleep(INTERVAL*10);  }
       catch(InterruptedException e) {}
       assertTrue("Port filter failed ", Utils.check(null, null, null, null, 0, maxLength, 0, output));
   }


}


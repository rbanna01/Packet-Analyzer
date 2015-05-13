/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tests;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.function.Predicate;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import thread.service.Packet;

/**
 *
 * @author rbanna01
 */
public class RulesTest {
    Date d;
    short[] dummyS = { 10, 20, 30, 50, 60, 70 };
    short[] dummyD = { 10, 20, 30, 50, 60, 70 };
    int dummyPort = 6;
    int[] dummyPayload = { 123, 43, -43, 22};
    int dummyLength = 512;
    Timestamp t;
    ArrayList<Predicate> tests;
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    d = new Date();
    Rules.setLengthMax(0);
    Rules.setLengthMin(0);
    Rules.setSource(null);
    Rules.setDest(null);
    Rules.setPort(0);
    Rules.setMinPort(0);
    Rules.setMaxPort(0);
    Rules.setStart(null);
    Rules.setStop(null);
    
    }
    
    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void initTest() {
    Rules.setLengthMax(0);
    Rules.setLengthMin(0);
    Rules.setSource(null);
    Rules.setPort(0);
    
    Rules.setSource(dummyS);
    tests = Rules.getTests();
    assertEquals("Too many tests initialized" + tests.size(), tests.size(), 1);     
    }
    
    @Test
    public void initTest2() {
   Rules.setLengthMax(0);
    Rules.setLengthMin(0);
    Rules.setSource(null);
    Rules.setPort(0);
    
    Rules.setSource(dummyS);
    Rules.setDest(dummyD);
    tests =  Rules.getTests();
    assertEquals("Too few tests initialized" + tests.size(), 2, tests.size());     
    }
    
    @Test
    public void testSource() {
        Rules.setLengthMax(0);
        Rules.setLengthMin(0);
        Rules.setSource(null);
        Rules.setDest(null);
        Rules.setPort(0);

        short[] source = { 11, 11, 11, 11, 11, 11};
        short[] dummy = { 22, 22, 22, 22, 22, 22};
        Timestamp toUse = new Timestamp(new Date().getTime());
        Packet valid = new Packet(source, dummy, toUse, 0, 0, dummyPayload);
        Packet invalid = new Packet(dummy, dummy, toUse, 0, 0, dummyPayload);
        Rules rules = new Rules();
        Rules.setSource(source);
        tests = Rules.getTests();
        
        assertTrue("Positive test not passed", tests.get(0).test(valid));
        assertFalse("Negative test passed", tests.get(0).test(invalid));
    } //ends testSource
    
    @Test
    public void testDestination() {
        Rules.setLengthMax(0);
        Rules.setLengthMin(0);
        Rules.setSource(null);
        Rules.setDest(null);
        Rules.setPort(0);

        short[] destination = { 11, 11, 11, 11, 11, 11};
        short[] dummy = { 22, 22, 22, 22, 22, 22};
        Timestamp toUse = new Timestamp(new Date().getTime());
        Packet valid = new Packet(dummy, destination,  toUse, 0, 0, dummyPayload);
        Packet invalid = new Packet(dummy, dummy, toUse, 0, 0, dummyPayload);
        Rules rules = new Rules();
        Rules.setDest(destination);
        tests = Rules.getTests();
        assertTrue("Positive test not passed", tests.get(0).test(valid));
        assertFalse("Negative test passed", tests.get(0).test(invalid));
    } //ends testSource
    
    @Test
    public void testStart()
    {
        Rules.setLengthMax(0);
        Rules.setLengthMin(0);
        Rules.setSource(null);
        Rules.setDest(null);
        Rules.setPort(0);

        
    t = new Timestamp(d.getTime());
    Timestamp past = new Timestamp(d.getTime() - 500);
    Timestamp future = new Timestamp(d.getTime() + 500);
    
    Packet earlier = new Packet(dummyS, dummyD, past, 0, 0, dummyPayload);
    Packet later = new Packet(dummyS, dummyD, future, 0, 0, dummyPayload);
    Rules.setStart(t);
    tests = Rules.getTests();
   assertFalse("Early test returns true", tests.get(0).test(earlier));
   assertTrue("Test after beginning returns false", tests.get(0).test(later));
    }
    
    @Test
    public void testStop()
    {
    Rules.setLengthMax(0);
        Rules.setLengthMin(0);
        Rules.setSource(null);
        Rules.setDest(null);
        Rules.setPort(0);

        
    t = new Timestamp(d.getTime());
    Timestamp past = new Timestamp(d.getTime() - 500);
    Timestamp future = new Timestamp(d.getTime() + 500);
    Packet earlier = new Packet(dummyS, dummyD, past, 0, 0, dummyPayload);
    Packet later = new Packet(dummyS, dummyD, future, 0, 0, dummyPayload);
    Rules.setStop(t);
    tests = Rules.getTests();
    assertFalse("Test after stop time returns true", tests.get(0).test(later));
    assertTrue("Test before stop time returns false", tests.get(0).test(earlier));
    } 

    @Test //as given
    public void testLengthMin() {
     Rules.setLengthMax(0);
    Rules.setLengthMin(0);
    Rules.setSource(null);
    Rules.setDest(null);
    Rules.setPort(0);
    Rules.setMinPort(0);
    Rules.setMaxPort(0);
    int targetLength = 515;
     int invalid = 512;
     Rules.setLengthMin(515);
     Packet valid = new Packet(dummyS, dummyD, new Timestamp(d.getTime()), targetLength, dummyPort, dummyPayload);
     Packet notValid = new Packet(dummyS, dummyD, new Timestamp(d.getTime()), invalid, dummyPort, dummyPayload);
     assertTrue("min length test fails valid value", Rules.getTests().get(0).test(valid));
     assertTrue("min length test passes invalid value", Rules.getTests().get(0).test(notValid) == false);
    }
    
    @Test
    public void testLengthMax() {
    Rules.setLengthMax(0);
    Rules.setLengthMin(0);
    Rules.setSource(null);
    Rules.setDest(null);
    Rules.setPort(0);
    Rules.setMinPort(0);
    Rules.setMaxPort(0);
    Rules.setStart(null);
    Rules.setStop(null);
    int targetLength = 512;
     int invalid = 515;
     Rules.setLengthMax(512);
     Packet valid = new Packet(dummyS, dummyD, new Timestamp(d.getTime()), targetLength, dummyPort, dummyPayload);
     Packet notValid = new Packet(dummyS, dummyD, new Timestamp(d.getTime()), invalid, dummyPort, dummyPayload);
     assertTrue("max length test fails valid value", Rules.getTests().get(0).test(valid));
     assertTrue("max length test passes invalid value", Rules.getTests().get(0).test(notValid) == false);
    }
    @Test 
    public void testPort() {
    Rules.setLengthMax(0);
    Rules.setLengthMin(0);
    Rules.setSource(null);
    Rules.setDest(null);
    Rules.setPort(0);
    int validPort = 535;
    //dummyPort  = 6;
    Rules.setPort(validPort);
    Packet valid = new Packet(dummyS, dummyD, new Timestamp(d.getTime()), dummyLength, validPort, dummyPayload);
    Packet notValid = new Packet(dummyS, dummyD, new Timestamp(d.getTime()), dummyLength, dummyPort, dummyPayload);
    System.out.println(Rules.getTests().size());
    assertTrue("port test fails valid value", Rules.getTests().get(0).test(valid));
    assertTrue("port test passes invalid value", Rules.getTests().get(0).test(notValid) == false);
    }
    
    
    
}

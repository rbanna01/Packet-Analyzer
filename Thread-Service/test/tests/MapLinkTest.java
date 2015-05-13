package tests;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import network.DefaultNetwork;
import network.KohonenNeuron;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import thread.service.MapLink;
import thread.service.Packet;
import thread.service.Utils;

/**
 *
 * @author Ruaridhi Bannatyne
 */
public class MapLinkTest {
    public final double HIWEIGHT = 0.8;
    public MapLinkTest() {
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
   
    //Test: network initialization
    @Test
    public void initTest() {
    /*MapLink mL = new MapLink();
    short[] a = {1,1,1,1,1,1};
    short[] b = {2,2,2,2,2,2};
    Packet p = Utils.getData(1, a, b, null, 0, 0, null).poll();
    Packet q = Utils.getData(1, b, a, null, 0, 0, null).poll();
    ArrayList<Packet> input = new ArrayList<>();
    input.add(p);
    input.add(q);
    //DefaultNetwork out = mL.init(input); 
    assertTrue(" " + out.getNumbersOfNeurons(), out.getNumbersOfNeurons() == 2);
    KohonenNeuron o1 = (KohonenNeuron) out.getNeuron(0);
    KohonenNeuron o2 = (KohonenNeuron) out.getNeuron(1);
    //check weights: 0, 256, 512, 768, 1024, 1280. Self in source
    double[] o1Weight = o1.getWeight();
    double[] o2Weight = o2.getWeight();
    for(int i = 0; i < 6; i++)
    {
        if( i < 4) {
        assertTrue("o1 weight wrong! " + i + " " + o1Weight[1+(256*i)], o1Weight[1+(256*i)]== 0.8);
        assertTrue("o2 weight wrong! " + o2Weight[1+(256*i)], o1Weight[1+(256*i)]== 0.8);}
        else {
            assertTrue("o1 weight wrong! " + i + " " + o1Weight[2+(256*i)], o1Weight[2+(256*i)]== 0.8);
        assertTrue("o2 weight wrong! " + o2Weight[2+(256*i)], o1Weight[2+(256*i)]== 0.8);
        }
    }
    //also need: Broadcast address: FF:FF:FF:FF
    for(int i = 0; i < 6; i++)
    {
        if( i < 4) 
            assertTrue("o1 broadcast address not weighted!" + i + " " + o1Weight[255+(256*i)], o1Weight[255+(256*i)]== 0.8);
        else 
            assertTrue("o1 weight wrong! " + i + " " + o1Weight[255+(256*i)], o1Weight[255+(256*i)]== 0.8);
        
    }
    //other weight locations: 1st 3 port categories, broadcast address
    assertTrue("weight at " + (3077) + "wrong!", o1Weight[3077] == 0.8);
    assertTrue("weight at " + (3078) + "wrong!", o1Weight[3078] == 0.8);
    assertTrue("weight at " + (3079) + "wrong!", o1Weight[3079] == 0.8);
    HashMap<String, Boolean> h = new HashMap<>();
    int[] x = {1,1,1,1,1,1};
    int[] x2 = {1,1,1,1,1,1};
    String one = "1.1.1.1.1.1";
    String two = "1.1.1.1.1.1";
    h.put(one, new Boolean(true));
    h.put(two, new Boolean(true));
    System.out.println(h.size());*/
    //DefaultNetwork dN = mL.doInit();
    }
    @Test
    public void primeTest() {
        MapLink mL = new MapLink();
        int[] ports = { 53, 80, 45};
        mL.setPriorityPorts(ports);
        HashMap<String, short[]> h = new HashMap<>();
        String s1 = "16:34:32:DA:56:01";
        String s2 = "1A:C4:5F:D3:A6:01";
        String s3 = "C4:23:32:DA:56:01";
        h.put(s1, Utils.stringShort(s1));
        h.put(s2, Utils.stringShort(s2));
        h.put(s3, Utils.stringShort(s3));
        //KohonenNeuron[] output =  mL.prime(h); //just use as-is; logic seems good in the code
        
    } //ends primetest
    
    
}

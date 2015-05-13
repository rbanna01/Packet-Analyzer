/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tests;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import javafx.collections.ObservableList;
import javax.swing.Timer;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import thread.service.Analyzer;
import thread.service.Packet;
import thread.service.Utils;
/**
 *
 * @author Ruaridhi Bannatyne
 */
public class AnalyzerTest {
    private Analyzer a;
    private final String ATDB = "D:\\Test\\sample.db";
    private final String ATT = "analyzerTest";
    private  final int SAMPLE = 50;
    private final int INTERVAL = 500;
    private ObservableList<Packet> out;
    public AnalyzerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    a = new Analyzer();
    out = a.getOutput();
    }
    
    @After
    public void tearDown() {
    }
        
    @Test
    public void dbReadTest() { //good test? can two connections to one db be maintained at once?
       LinkedBlockingQueue<Packet> sample = Utils.getData(SAMPLE, null, null, null, 0, 0, null);
       try{
       System.out.println(a.setDB("memory"));}
       catch(IOException iOE) {System.out.println();}
       a.write(true);
       a.setInput(sample);
       //a.read();
       a.sniff();
       try {
       Thread.sleep(500);}
       catch(InterruptedException e) {}
       a.stop();
       a.write(false);
       a.read(true);
       a.sniff();
       Timer t = new Timer(INTERVAL, new ActionListener() {
           @Override
           public void actionPerformed(ActionEvent e)
          {
          a.stop();    
          assertEquals("Not all packets processed!" + out.size(), out.size(), SAMPLE);
         }
      });
      t.setRepeats(false);
      t.setInitialDelay(INTERVAL);
      t.start();
    }

    @Test
    public void dBWriteTest() {
       LinkedList<Packet> sample = new LinkedList<Packet>();
       for(Packet p:Utils.getData(SAMPLE, null, null, null, 0, 0, null)) sample.add(p);
       try {
       a.setDB("memory");}
       catch(IOException iOE) { System.out.println(iOE.getMessage());} 
       //a.setDirectInput(true);
       a.setInput(Utils.getData(SAMPLE, null, null, null, 0, 0, null));
       a.write(true);
       a.sniff();
       Timer t = new Timer(INTERVAL, new ActionListener() {
           @Override
           public void actionPerformed(ActionEvent e)
          {
          a.stop();    
          Collection<Packet> out = a.getOutput();
          assertTrue("not all packets written!", out.size() == SAMPLE);
                    
          }
      });
      t.setRepeats(false);
      t.setInitialDelay(INTERVAL);
      t.start();
    } //ends WriteTest
    
//remaining tests: testing filtered and unfilteredoutput, both from DB and direct input(ie)

  @Test
  public void filterTest () {
      // filter by: source, time, port, destination, length
      //check all of this
      short[] valid = { 11, 22, 33, 44, 55, 66};
      short[] invalid = { 66, 55, 44, 33, 22, 11};
      int vPort = 56;
      int iPort = 43;
      int minLength = 50;
      int maxLength = 100;
      int tooShort = 45;
      int tooLong= 110;
      int validL = 75;
      Timestamp before = new Timestamp(100);
      Timestamp after = new Timestamp(50);
      Timestamp validTime = new Timestamp(75);
      Timestamp tooEarly = new Timestamp(40);
      Timestamp tooLate = new Timestamp(110);
      LinkedBlockingQueue<Packet> okay  = Utils.getData(SAMPLE, valid, valid, validTime, validL, vPort, null);
      LinkedList<LinkedBlockingQueue<Packet>> bad = new LinkedList<>();
      bad.add(Utils.getData(SAMPLE, invalid, valid, validTime, validL, vPort, null));
      bad.add(Utils.getData(SAMPLE, valid, invalid, validTime, validL, vPort, null));
      bad.add(Utils.getData(SAMPLE, valid, valid, tooEarly, validL, vPort, null));
      bad.add(Utils.getData(SAMPLE, valid, valid, tooLate, validL, vPort, null));
      bad.add(Utils.getData(SAMPLE, valid, valid, validTime, validL, iPort, null));
      bad.add(Utils.getData(SAMPLE, valid, valid, validTime, tooShort, vPort, null));
      bad.add(Utils.getData(SAMPLE, valid, valid, validTime, tooLong, vPort, null));  //*/
      System.out.println(1);
      int count = 0;
      for(int i = 0; i < 1; i++)
      {
         a = new Analyzer();
         out = a.getOutput();
         a.setSource("11.22.33.44.55.66");
         a.setDest("11.22.33.44.55.66");
         a.setPorts(String.valueOf(vPort));
         a.setMinLength(String.valueOf(minLength));
         a.setMaxLength(String.valueOf(maxLength));
         String afterTime = after.toString(); //better check these
         String beforeTime = before.toString();
         a.setStart(afterTime);
         a.setEnd(beforeTime);
         a.setInput(Utils.mix(okay, bad.get(i)));
         a.sniff();
          try{
          Thread.sleep(5000); }
          catch(InterruptedException e) {} 
          a.stop();
          System.out.println(2);
          assertTrue("filter not working" + out.size(), out.size() == count*SAMPLE);
          //assertTrue("filter off", Utils.check( valid, valid, after, before, validL, vPort, 0, out)); */
      } 
  } //ends filterTest
//
  /*
  public static void main(String[] args)
  {
   Timestamp before = new Timestamp(100);   
   String beforeTime = before.toString();
   String date = new String(beforeTime.substring(0, beforeTime.indexOf(" ")));
   date = date.trim();
   String time = new String (beforeTime.substring(beforeTime.indexOf(" "), beforeTime.indexOf(".")));
   time = time.trim();
   String[] comps = time.split(":");
     int hour = Integer.parseInt(comps[0]);
     int min = Integer.parseInt(comps[1]);
     int sec = Integer.parseInt(comps[2]);
     String[] dateComps = date.split("-");
     int year = Integer.parseInt(dateComps[0]);
     int day = Integer.parseInt(dateComps[2]);
     int month = Integer.parseInt(dateComps[1]);
     GregorianCalendar g = new GregorianCalendar(year, month, day, hour, min, sec);
  
  
  }
   */
}





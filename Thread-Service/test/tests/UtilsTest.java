/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tests;

import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import thread.service.Utils;

/**
 *
 * @author Ruaridhi Bannatyne
 */
public class UtilsTest {
    
    public UtilsTest() {
    }
    
    @Test
    public void processStringTest() {
    String in = "45:13:87:54:90:55:0:-127:127";
    int[] out = Utils.processString(in);
    //assertTrue("processString not updating properly" + out[Byte.MAX_VALUE + 45], out[Byte.MAX_VALUE + 45] == 1);
    String[] vals = in.split("\\:");
    for(String s: vals)
    {
        assertTrue("processString not updating properly" + Integer.parseInt(s),
                out[Byte.MAX_VALUE + Integer.parseInt(s)] == 1);
    }
    }
    
    
    @Test //byte[] in int[] out 
    public void processByteTest() {
    byte[] in = { 45,13,87,54,90,55,0,-127,127};
    int[] out = Utils.processByte(in);
    for(byte b: in)
    {
        assertTrue("processByte not working" + b, out[Byte.MAX_VALUE + b] == 1);
    }
    } //ends byteTest
    
    
    
    
    
    @Test
    public void intStringTest() {
    int[] in = { 567, 54, -56, -78};
    String comparator = "567:54:-56:-78";
    String out = Utils.intString(in);
    assertTrue("instString failed " + out, comparator.equals(out));
    }
    
    @Test
    public void stringIntTest(){
    String in = "456:5678:-300:32";
    int[] control = { 456, 5678, -300, 32};
    int[] out = Utils.stringInt(in);
    System.out.println(out.length);
    //for(int i = 0; i < control.length; i++) assertTrue("stringInt gives wrong output " + out[i], out[i] == control[i]);
    }
    
    @Test  //String in byte[] out
    public void getBytesTest() {
    String in = "34.-56.43.11";
    byte[] control = { 34, -56, 43, 11};
    byte[] out = Utils.getBytes(in);
    for(int i = 0; i < 4; i++ ) assertTrue("getBytes output wrong " + out[i], out[i] == control[i]);
    }
    
    @Test //byte[] in String out separated with "."
    public void getStringTest() {
    byte[] in = {45, -34, 12, 90, -54, 11};
    assertTrue("getString output wrong! " + Utils.getString(in), Utils.getString(in).equals("45.-34.12.90.-54.11"));
    }
    
}

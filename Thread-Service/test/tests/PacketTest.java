/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tests;

import java.sql.Timestamp;
import java.util.Random;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import thread.service.Packet;

/**
 *
 * @author Ruaridhi Bannatyne
 */
public class PacketTest {
    
    public PacketTest() {
    }

    @Test
    public void compareTest()
    {
        short[] add = { 11,11,11,11,11,11};
        Random r = new Random();
        int[] payload = new int[255];
        for(int i = 0 ; i< payload.length; i++) payload[i] = r.nextInt();
        Packet p = new Packet(add, add, new Timestamp(10), 5, 5, payload);
        Packet q = new Packet(add, add, new Timestamp(15), 5, 5, payload);
        Packet s = new Packet(add, add, new Timestamp(25), 5, 5, payload);
        assertTrue("compare gives bad value for lower other", q.compareTo(p) == 1);
        assertTrue("compare gives bad value for higher other", q.compareTo(s) == -1);
        assertTrue("compare gives bad value for lower other", q.compareTo(q) == 0);
    }
    
    @Test
    public void equalsTest()
    {
        short[] add = { 11,11,11,11,11,11};
        short[] control = { 11,11,11,11,11,12};
        int controlInt = 4;
        Random r = new Random();
        int[] payload = new int[255];
        for(int i = 0 ; i< payload.length; i++) payload[i] = r.nextInt();
        Packet p = new Packet(add, add, new Timestamp(10), 5, 5, payload);
        Packet q = new Packet(add, add, new Timestamp(10), 5, 5, payload);
        assertTrue("equality not recognized", p.equals(q));
        Packet source = new Packet(control, add, new Timestamp(10), 5, 5, payload);
        assertFalse("inequality not recognized", p.equals(source));
        Packet dest = new Packet(add, control, new Timestamp(10), 5, 5, payload);
        assertFalse("inequality not recognized", p.equals(dest));
        Packet time = new Packet(add, add, new Timestamp(33), 5, 5, payload);
        assertFalse("inequality not recognized", p.equals(time));
        Packet length = new Packet(add, add, new Timestamp(10), controlInt, 5, payload);
        assertFalse("inequality not recognized", p.equals(length));
        Packet port = new Packet(add, add, new Timestamp(10), 5, controlInt, payload);
        int[] controlPayload = new int[255];
        for(int i = 0 ; i< controlPayload.length; i++) controlPayload[i] = r.nextInt();
        Packet pl = new Packet(add, add, new Timestamp(10), 5, 5, controlPayload);
        assertFalse("inequality not recognized", p.equals(pl));
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package thread.service;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import javafx.beans.property.IntegerProperty;

/** 
 * Class Packet represents a packet captured over the network;
 * @author rbanna01
 * 
 */
public class Packet  implements Comparable{
    /*
    * Source Ethernet address
    */
    private short[] source;
    /*
    * Source Ethernet address in String form
    */
    private String sourceString;
    /*
    * Destination Ethernet address
    */
    private short[] destination;
    /*
    * Destination Ethernet address in String form
    */
    private String destString;
    /*
    * Time of capture
    */
     private java.sql.Timestamp time;
    /*
    * Destination port
    */
    private Integer port;
    /*
    * This Packet's length
    */
    private Integer length; 
    /*
    * This Packet's payload
    */
    private int[] payload; //not to be tested. Needs to use integers
    /*
    * Range of byte values
    */
    private final int BYTECEILING = 511;

    public int getByteCeiling() {return BYTECEILING; }
    
    /* Gets this packet's port
    * @return port this packet's port
    */
    public Integer getPort() {
        return port;
    }
    
    /* Gets this packet's port in String form. Used for the table.
    * @return this packet's port as a String.
    */
    public String getPortProperty() { return String.valueOf(port);}
    
    /* Gets this packet's length in String form. Used for the table.
    * @return Length this packet's length in String form
    */
    public String getLengthProperty() { return String.valueOf(length);}
    
    /* Sets this packet's port
    * @param port this packet's port
    */
    public void setPort(int port) {
      this.port = port;
    }

    /* Gets this packet's length
    * @return length this packet's length
    */
    public Integer getLength() {
        return length;
    }
    
    /* Sets this packet's length
    * @param port this packet's length
    */
    public void setLength(int length) {
        this.length = length;
    }

    /* Gets this packet's payload
    * @return payload this packet's payload
    */
    public int[] getPayload() {
        return payload;
    }
    
    /* Sets this packet's payload
    * @param payload this packet's payload
    */
    public void setPayload(int[] pl) {
        this.payload = pl;
    }
    
    
    public Packet(short[] sIn, short[] dIn, Timestamp tIn, int l, int p, int[] pL)
    {
        source = sIn;
        destination = dIn;
        time = tIn;
        sourceString = Utils.shortHexString(source);
        destString = Utils.shortHexString(destination);
        length = l;
        port = p;   
        payload = pL;
    } 
    
    public Packet(String sIn, String dIn, Timestamp tIn, int l, int p, int[]pL)
    {
        sourceString  = sIn;
        destString  = dIn;
        time = tIn;
        source = Utils.stringShort(sIn); //last change, but shouldn't be a problem
        destination = Utils.stringShort(dIn);
        length = l;
        port = p;
        int half = 128; 
       payload = pL;
    }
    
    /* Gets this packet's destination in string form. For the use of the table and database
    * @return sourceString this packet's source in String form
    */
    public String getSourceString() 
    { 
        return this.sourceString;
    }
    
    /* Gets this packet's destination in string form. For the use of the table and database.
    * @return destString this packet's destination in String form.
    */
    public String getDestinationString() 
    { 
        return this.destString;
    }
    
    /* Gets this packet's Source in short[] form.
    * @return source this packet's source in short[] form.
    */  
    public short[] getSource() 
    { 
        return this.source;
    }
    
    /* Gets this packet's destination in short[] form. 
    * @return destination this packet's source in short[] form
    */
    public short[] getDestination() 
    { 
        return this.destination;
    }
    
    /* Gets this packet's time in string form. For use of table.
    * @return this packet's time in String form
    */
    public String getTimeString()
    {
        SimpleDateFormat s = new SimpleDateFormat("hh:mm:ss yy/mm/dd");
        return s.format(new Date(time.getTime()));
    }
    
    /* Gets this packet's time.
    * @return time. This packet's time.
    */
    public Timestamp getTime() 
    { 
        return this.time;
    }
    
    /* Sets this packet's time.
    * @param toAdd the time to assign this Packet
    */
    public void setTime(Timestamp toAdd)
    {
        this.time = toAdd;
    } 
    
    /* Sets this packet's Source in short[] form. 
    * @param toAdd the source to assign this Packet.
    */
    public void setSource(short[] toAdd)
    {
        this.source= toAdd;
    }
    
    /* Sets this packet's destination.
    * @param toAdd the destination to assign this packet.
    */
    public void setDestination(short[] toAdd)
    {
        this.destination = toAdd;
    }
    
    /* Tests this packet and another for equality. Testing...
    *@param p a packet with which this is to be compared.
    *@return whether this packet and p are equal.
    */    
    public boolean equals(Packet p) {
        if(this.sourceString.equals(p.getSourceString()) && this.destString.equals(p.getDestinationString()) 
                && time.getTime() == p.getTime().getTime() && this.port.equals(p.getPort()) && length.equals(p.getLength()))
        {
            int[] otherPl = p.getPayload();
            if(payload.length != otherPl.length) return false; //prevents NullPointerException
            for(int i = 0; i < payload.length; i++) if(payload[i] != otherPl[i]) return false;
            return true;
        } //ends if
        else return false;
    }
    /* Compares this packet to another wrt time values
    *@param x another packet to compare to this one.
    * @return sourceString this packet's source in String form.
    */
    @Override
    public int compareTo(Object x)
    {
     Packet other = (Packet) x;
     long t = this.time.getTime();
     long o  = other.getTime().getTime();
     if(t> o) return 1;
     else if(o > t) return -1;
     else return 0;
    }   

/*
    public static void main(String[] args)
    {
        Packet p = Utils.getData(1,null, null, null, 0, 0, null).poll();
        System.out.println(p.getPortProperty());
        System.out.println(p.getPort());
    } //ends main
*/

}

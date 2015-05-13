/* Needs to be static, if this is compatible with concurrency
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.

 */

package thread.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author Ruaridhi Bannatyne
 */
public class Utils {
    /*
    * Generates random numbers where needed
    */
    private static Random r;
    
    /*
    * Range of possible byte values
    */
    public final static int byteRange = 512;
    
    
    /* Gets packets generated according to specified input.
    *@param size  the number of packets to generate.
    *@param source the source to be used in generating packets, if any.
    *@param dest the destination to be used in generating packets if any.
    *@param time the time value to be used in generating packets, if any.
    *@param port the port value to be used in generating packets, if any.
    *@param length the length value to be used in generating packets, if any.
    *@param pl[] the payload value to be used in generating packets, if any.
    *@return a LinkedBlockingQueue with the requisite number of packets conforming to the given specifications.
    */
    public static LinkedBlockingQueue<Packet> getData(int size, short[] source, short[] dest, Timestamp time, 
            int port, int length, int[] pl) 
    {
        LinkedBlockingQueue<Packet> output = new LinkedBlockingQueue<>(size);
 
        r = new Random();
        int lengthVar;
        int p;
        int[] payload; 
        short[] outS = new short[6];
        Timestamp t;
        short[] outD = new short[6];//potential lenght range: 512 bits, max of 1518 
        //ports: 0 -1023 well-known, 1024- 49151 registered
        for(int i =0; i < size; i++)
        {
            if(source == null) {
                for(int j =0 ; j< outS.length; j++) {
                    outS[j] = (short) r.nextInt(256);
                }
            }
            else
            { outS = source;}
            
            if(dest == null){
                for(int k = 0; k < outD.length; k++){
                    outD[k] = (short) r.nextInt(256);
                }
            }
            else
            {
                outD = dest;
            }
            if(time == null) { t = new Timestamp(r.nextLong());}
            else{ t = new Timestamp(time.getTime()); }
            if(length == 0) {lengthVar = r.nextInt(1016)+512;}
            else {lengthVar = length;}
            if(port == 0){ p = r.nextInt(49151);}
            else{ p = port;}
            if(pl == null) { 
                payload = new int[byteRange]; 
                for(int j = 0; j < byteRange; j++) {payload[j] = r.nextInt(512); }
                }
            else{ payload = pl;}
            try {
            output.put(new Packet( outS, outD, t, lengthVar, p, payload ));   
            }
            catch(InterruptedException e) {System.out.println(e.getMessage());}
    //        System.out.println(i);
        }
       
        return output;
    }

    /* Mixes packets from the 2 input queues randomly.
    *@param l  a queue of packets.
    *@param m  a queue of packets.
    *@return a LinkedBlockingQueue made of a random mixture of l and m.
    */
    public static LinkedBlockingQueue<Packet> mix(LinkedBlockingQueue<Packet> l, LinkedBlockingQueue<Packet> m) 
   { 
       Iterator<Packet> lI = l.iterator();
       Iterator<Packet> mI = m.iterator();
       LinkedBlockingQueue<Packet> output = new LinkedBlockingQueue<>();
       r = new Random();
       int i;
       while(true) {
           i = r.nextInt(1);
           if(i== 0){
               if(lI.hasNext()){
               output.add(lI.next());
               }
               else
               {
                   while(mI.hasNext()) output.add(mI.next());
                   break;
               } //ends catch
           }
           else {
               if(mI.hasNext()) output.add(mI.next());
               else {
                   while(lI.hasNext()) output.add(lI.next());
                   break;
               }
           }
       } //ends while
     return output;  
   } //ends mix
           
  
   /* Converts a byte array to a String Ethernet address
    *@param in a byte array
    *@return a String with equivalent value to the input array..
    */
    public static String getString(byte[] in)
    { //to remove
        String out ="";
        int count = 0;
        while(count< in.length-1)
        {
           out += Byte.toString(in[count]) + ":"; 
           count++;
        }
        out += Byte.toString(in[count]);
        return out;
    }
    
    /* Checks a collection of packets against specified input.
    *@param source the source to be used in checking packets, if any.
    *@param dest the destination to be used in checking packets if any.
    *@param start the minimum time value to be used in checking packets, if any.
    *@param stop the maximum time value to be used in checking packets, if any.
    *@param port the port value to be used in checking packets, if any.
    *@param minLength the minumum length value to be used in checking packets, if any.
    *@param maxLength the maximum length value to be used in checking packets, if any.
    *@return boolean whether all packets in the collection conformed to input criteria.
    */
    protected static boolean check(short[] source, short[] dest, Timestamp start, 
            Timestamp stop, int minLength, int maxLength, int port, Collection<Packet> input)
    { 
    short[] pS;
    short[] pD;
    for(Packet p: input)
        {
            if(start != null) if(p.getTime().getTime() < start.getTime()) return false;
            if(stop != null) if(p.getTime().getTime() > stop.getTime()) return false;
            if(source != null) 
            {
                pS = p.getSource();
                for(int i = 0; i < 6; i++) if(source[i]!= pS[i]) return false;
            }
            if(dest != null)
            {
                pD = p.getDestination();
                for(int i = 0; i < 6; i++) if(dest[i] != pD[i]) return false;
            }
            if(port != 0) if(port != p.getPort()) return false;
            if(minLength != 0) if(p.getLength() < minLength) return false;
            if(maxLength !=0) if(p.getLength() > maxLength) return false;
        }
    return true;
    }
    
    /* Converts a String value into a byte array
    *@param in an Ethernet address in String form
    *@return a byte[] of equivalent value to the input
    */
    public static byte[] getBytes(String in)
    {
        String[] vals = in.split("\\:");
       // for(String s: vals) System.out.println(s);
        int limit = vals.length;
        //System.out.println(limit);
        byte[] out = new byte[limit];
        for(int i = 0; i < limit; i++)
        {
            out[i] = Byte.parseByte(vals[i]);
        } 
        
        return out;
    }
    
    /* Converts a short[] value into a String in hex.
    *@param in an Ethernet address in short[] form
    *@return a String hex value of equivalent value to the input
    */
    public static String shortHexString(short[] in)
    {
        String out = "";
        for(int i = 0; i < in.length-1; i++) out += Integer.toHexString(in[i]) + ":";
        out+= Integer.toHexString(in[in.length-1]);
        return out;
    } //ends shortHexString
    

    /* Converts an int[] value into String
    *@param in an Ethernet address in int[] form
    *@return a String of equivalent value to the input
    */
    public static String intString(int[] in)
    {
        String out = "";
        for(int i = 0; i < in.length-1; i++) out += in[i] + ":";
        out+= in[in.length-1];
        return out;
    }
    
    /* Converts a String value into a short array
    *@param in an Ethernet address in String form
    *@return a short[] of equivalent value to the input
    */
    public static short[] stringShort(String in)
    {
     in = in.trim();
     String[] vals = in.split(":");
     short[] out = new short[vals.length];
     for(int i = 0; i < out.length; i++) 
     {
         if(vals[i].equals("*")) out[i] = Short.MIN_VALUE;
         else out[i] = Short.parseShort(vals[i], 16);
     }
     return out;
    }
    
    /* Converts a String value into an int[] array
    *@param in an Ethernet address in String form
    *@return an int[] of equivalent value to the input
    */
    public static int[] stringInt(String in)
    {
     String[] vals = in.split(":");
     int[] out = new int[vals.length];
     for(int i = 0; i < out.length; i++) 
     {
         if(vals[i].equals("*")) out[i] = Short.MIN_VALUE;
         else out[i] = Integer.parseInt(vals[i], 16);
     }
     return out;
    }
    
    
    /* Converts a byte[] value into an int[] array
    *@param in an Ethernet address in byte[] form
    *@return an int[] of equivalent value to the input
    */
    public static int[] byteInt(byte[] in)
    {
        int[] out = new int[512];
        for(byte b: in) out[256 + b]++;
        return out;
    }
    
    /* Converts a byte[] value into an int[] array
    *@param in an Ethernet address in byte[] form
    *@return an int[] of equivalent value to the input
    
    public static int[] byteInt(byte[] in)
    {
        int[] out = new int[in.length];
        for(byte b: in) out[(int) b]++;
        return out;
    }
    */
    /* Converts a byte[] value into a short array
    *@param in an Ethernet address in byte[] form
    *@return a short[] of equivalent value to the input
    */
    public static short[] byteShort(byte[] in)
    {
        short[] out = new short[in.length];
        for(int i = 0; i < out.length; i++)
        {
         Byte b = in[i];
         out[i] = b.shortValue();
        }
        return out;
    }
    
    
     /* Converts a byte[] value into an int array
    *@param in an Ethernet address in byte[] form
    *@return an int[] of equivalent value to the input
    */  
     public static int[] processByte(byte[] in)
        { //so does this by represeting no of occurrances of each value
            int[] out = new int[byteRange+1];
            int half = byteRange/2;
            for(byte b: in) { out[half+ (int) b]++; }
            return out;
        }//ends byte version

    /* Converts a String payload value into an int[] array representative of a packet's payload.
    *@param in an int[] in String form
    *@return an int[] of equivalent value to the input
    */
    public static int[] processString(String in)
     { //really need to do this properly; go thru' string, get all vals, convert
         int[] out = new int[byteRange+1];//assumes all vals are within byte range
    //     System.out.print(out[45]); Given bytesize, we have range 127*2 = 254 + 0 = 255 vals
         char[] workingCopy = in.toCharArray();
         int half = Byte.MAX_VALUE; //accounts for 0
         int i = 0;
         int nextInt;
         int start = 0; //since must start with a val 
         int lastPeriod = 0;
         for(int j = workingCopy.length-1; j > 0; j--) 
         {
             if(workingCopy[j] == ':') 
             {
              lastPeriod = j;
              break;
             }
         }
        // System.out.println(lastPeriod);
         int end;
         String val;
         char nextChar;
         while(i < workingCopy.length)
         { //need to: start w/ number, go on till a period is hit, get chars between last and this, 
             // convert to integer and add to output
             nextChar = workingCopy[i];
             if(nextChar == ':'){
                 end = i-1;
                 val = new String(workingCopy, start, end-start+1);
                 System.out.println("val is " + val); //test use
                 try{
                 nextInt = Integer.parseInt(val);
                 out[nextInt+ half]++;
                 i++;//passes end of last number
                 start = i;
                 }
                 catch(NumberFormatException e) //indicates bad input data
                 {
                     System.out.println("null");
                     return null;
                 }
              }
             else i++;
         } //ends while */
         nextInt =(Integer.parseInt(new String( workingCopy, lastPeriod+1, (workingCopy.length - lastPeriod-1))));
         out[nextInt + half]++;
         return out;
    } //ends string version

     
    /* Checks a collection of packets against specified input.
    *@param source the source to be used in checking packets.
    *@param dest the destinations to be used in generating packets.
    *@param time the time value to be used in generating packets.
    *@param port the port values to be used in generating packets.
    *@param length the length value to be used in checking packets.
    *@param pl the payload value to be used in generating packets, if any.
    *@return a LinkedBlockingQueue of packets with values within the range specified.
    */
    public static LinkedBlockingQueue<Packet> getData(int size, short[] source, ArrayList<short[]> dest, Timestamp time, 
            int length, int[] port, int[] pl) 
    { //used to return random input within range specified in input parameters
        LinkedBlockingQueue<Packet> output = new LinkedBlockingQueue<>(size);
        r = new Random();
        int hosts = dest.size();
        int portNo =port.length;
        int lengthVar;
        int portVar;
        int p;
        int[] payload; 
        short[] outS;
        Timestamp t;
        short[] outD;//potential lenght range: 512 bits, max of 1518 
        //ports: 0 -1023 well-known, 1024- 49151 registered
        outS = source;
            for(int i = 0 ; i < size; i++){
                outD = dest.get(r.nextInt(hosts));
                if(time == null) { t = new Timestamp(r.nextLong());}
                else{ t = new Timestamp(time.getTime()); } //timestamp doesnt' matter, really
                if(length == 0) {lengthVar = r.nextInt(1016)+512;} //again, length not really in consideration
                else {lengthVar = length;}
                portVar = port[r.nextInt(portNo)];
                if(pl == null) { 
                    payload = new int[byteRange]; 
                    for(int j = 0; j < byteRange; j++) {payload[j] = r.nextInt(512); }
                    }
                else{ payload = pl;}
                try {
                output.put(new Packet( outS, outD, t, lengthVar, portVar, payload ));   
                }
                catch(InterruptedException e) {System.out.println(e.getMessage());}
        //        System.out.println(i);
        }
       
        return output;
    }

     
public static void main(String[] args)
     {
        //Utils.getData(55, null, null, null, 0, 0, null); 
         String s = "ff";
         System.out.println(Short.valueOf(s, 16));
        //System.out.println(test.length);
     } //checka again tomorrow; should be okay
//need to do util test methods, too */
}

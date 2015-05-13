/* To make static
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package thread.service;
import tests.*;
import thread.service.*;
import java.util.ArrayList;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.function.Predicate;

/**
 *
 * @author Ruaridhi Bannatyne
 * ruaridhi.bannatyne@gmail.com
 */
class Rules {
    
    /*
    * Source address to allow.
    */
    private static short[] source;
    
    /*
    *Destination address to allow.
    */
    private static short[] dest;
    
    /*
    * Upper bound of time interval.
    */
    private static Timestamp stop;
    
    /*
    *Lower bound of time interval.
    */
    private static Timestamp start;
    
    /*
    * Minimum alllowed length.
    */
    private static int lengthMin;
    
    /*
    * Maximum allowed length.
    */
    private static int lengthMax;
    
    /*
    * Allowed ports.
    */
    private static ArrayList<Integer> ports; //could be > 1
    
    /*
    * Minimum of allowed ports.
    */
    private static int minPort;
    
    /*
    *Maximum of allowed ports.
    */
    private static int maxPort;
    
    
    /* Gets minimum length.
    *@return minimum allowed length.
    */
    public static int getLengthMin() {
        return lengthMin;
    }

    /* Sets minimum length.
    *@param lengthMin minimum allowed length.
    */
    public static void setLengthMin(int lengthMinIn) {
        lengthMin = lengthMinIn;
    }

    /* Gets maximum length.
    *@return lengthMax maximum allowed length.
    */
    public static int getLengthMax() {
        return lengthMax;
    }

    /* Sets maximum length.
    *@param lengthMax maximum allowed length.
    */
    public static void setLengthMax(int lengthMaxIn) {
        lengthMax = lengthMaxIn;
    }
       
    //just one for the end?
     public Rules() 
    {
     
     }
    
     /* Sets source.
    *@param newSource source to be allowed.
    */
    public static void setSource(short[] newSource)
    { //Here:check format (ethernet with *wildcard char allowed)
        source= Arrays.copyOf(newSource, newSource.length);
    }
    
    /* Sets destination.
    *@param lengthMin minimum allowed length.
    */
    public static void setDest(short[] newDest)
    {
        dest = Arrays.copyOf(newDest, newDest.length);
    }
    
    public static void resetDest() { dest = null;}
    
    public static void resetSource() { source = null; }
    
    /* Sets minimum time.
    *@param newStart minimum allowed time.
    */
    public static void setStart(Timestamp newStart)
    {
        start = newStart;
    }
    
    /* Sets maximum time.
    *@param newStop maximum time value.
    */
    public static void setStop(Timestamp newStop)
    {
        stop = newStop;
    }
    
    /* Adds allowed port.
    *@param toAdd a new port to add.
    */
    public static void setPort(int toAdd) { 
        if(toAdd == 0) ports = null;
        else if(ports == null){
            ports = new ArrayList<>();
            ports.add(toAdd);
        }
        else if(!ports.contains(toAdd))ports.add(toAdd);  
    }
    
    /* Gets minimum time value.
    *@return start minimum allowed time.
    */
    public static Timestamp getStart() {return start; }
    
    /* Sets maximum time value.
    *@param stop maximum allowed time value.
    */
    public static Timestamp getStop() {return stop;}
    
    /* Gets source.
    *@return source allowed source.
    */
    public static short[] getSource() {return source;}
    
    /* Gets destination.
    *@return destination allowed destination.
    */
    public static short[] getDestination() { return dest; }
    
    /*
    * Sets minPort
    */
    public static void setMinPort(int in) { minPort = in; }
    /*
    * Gets minPort
    @return minPort
    */
    public static int getMinPort() { return minPort;}
    
    /*
    * Sets maxPort
    */
    public static void setMaxPort(int in) { maxPort = in; }
    
    /*
    * Sets maxPort
    @return maxPort
    */
    public static int getMaxPort() { return maxPort;}
    
    
    /* Gets all tests to be sued to determine whether a given packet can be deleted.
    *@return tests ArrayList<Predicate> containing Predicate tests for compatibility with this Rules' settings.
    */
    public static ArrayList<Predicate> getTests()
    { //order: those with ability to remove most potential candidates first
        //ie
        
        
        if(source == null && dest == null && stop == null && start== null && lengthMin == 0 &&
                lengthMax == 0 && (ports == null || ports.isEmpty()) && maxPort == 0 && minPort == 0) return null; //so not dealing with payload wrt UI
        ArrayList<Predicate> output = new ArrayList<>(5);
        if(source != null) 
        {
            output.add(new Predicate() {
            @Override    
            public boolean test(Object p)
                    {
                    Packet target;
                     try{
                         target = (Packet) p;
                    }
                     catch(ClassCastException e) {
                         System.out.println("Screwed");
                         throw new IllegalArgumentException("Can only test Objects of class Packet!");
                    }
                     short[] toCheck = target.getSource();
                     for(int i = 0; i < source.length; i++)
                     { //wildcard denoted by Integer.MIN_VALUE
                         if(source[i] == Integer.MIN_VALUE) continue;
                         else if(source[i] !=toCheck[i]) return false;
                     }
                     return true;
                    }
                    });
            System.out.println("source");
        }
        if(dest != null) 
            {
            output.add(new Predicate() {
                @Override
                public boolean test(Object p)
                    {
                     Packet target;
                     try{
                         target = (Packet) p;
                     }
                     catch(ClassCastException e) {
                         throw new IllegalArgumentException("Can only test Objects of class Packet!");
                     }
                     short[] toCheck = target.getDestination();
                     for(int i = 0; i < dest.length; i++)
                     {
                         if(dest[i] == Integer.MIN_VALUE) continue; //wildcard
                         if(dest[i] != toCheck[i]) return false;
                     }
                     return true;
                    }
                    });
            System.out.println("dest");
        }
        if(start != null) //should time always go first?
            {
            output.add(new Predicate() {
                @Override
                public boolean test(Object p)
                    {
                     Packet target;
                     try{
                         target = (Packet) p;
                        }
                     catch(ClassCastException e) {
                         throw new IllegalArgumentException("Can only test Objects of class Packet!");
                     }
                     Timestamp toCheck = target.getTime();
                     if(start.after(toCheck)) return false;
                     else return true;
                    }
                    }
                   );
            System.out.println("after");
        }
        if(stop != null) 
            {
            output.add(new Predicate() {
                @Override
                public boolean test(Object p)
                    {
                     Packet target;
                     try{
                         target = (Packet) p;
                     }
                    catch(ClassCastException e) {
                         throw new IllegalArgumentException("Can only test Objects of class Packet!");
                     }
                     Timestamp toCheck = target.getTime();
                     if(stop.before(toCheck)) return false;
                     return true;
                    }
                    });
            System.out.println("before");
            }
            if(lengthMin != 0) 
            {
            output.add(new Predicate() {
                @Override
                public boolean test(Object p)
                    {
                     Packet target;
                     try{
                         target = (Packet) p;
                     }
                    catch(ClassCastException e) {
                         throw new IllegalArgumentException("Can only test Objects of class Packet!");
                     }
                     int toCheck = target.getLength();
                     if(toCheck >= lengthMin) return true;
                     else return false;
                    }
                    });
            System.out.println("lmin");
            }
            if(lengthMax != 0) 
            {
            output.add(new Predicate() {
                @Override
                public boolean test(Object p)
                    {
                     Packet target;
                     try{
                         target = (Packet) p;
                     }
                    catch(ClassCastException e) {
                         throw new IllegalArgumentException("Can only test Objects of class Packet!");
                     }
                     int toCheck = target.getLength();
                     if(toCheck <= lengthMax) return true;
                     else return false;
                    }
                    });
            System.out.println("lmax");
            //candidates.add(new Candidate(temp, getWeight(lengthMax, 1500, false)));
            }
            if(ports != null) 
            {//need to handle both individual ports and > < operators. Used with min and max vars; them first
                //ignore individual ports? add/subtract?
            output.add(new Predicate() {
                @Override
                public boolean test(Object p)
                    {
                     Packet target;
                     try{ 
                         target = (Packet) p;
                     }
                     catch(ClassCastException e) {
                         throw new IllegalArgumentException("Can only test Objects of class Packet!");
                     }
                     int toCheck = target.getPort();
                     for(int i: ports) { if(toCheck == i) return true; }
                     return false;
                    }
                    });
            System.out.println("ports");
            }
            if(minPort != 0) 
            {//need to handle both individual ports and > < operators. Used with min and max vars; them first
                //ignore individual ports? add/subtract?
            output.add(new Predicate() {
                @Override
                public boolean test(Object p)
                    {
                     Packet target;
                     try{ 
                         target = (Packet) p;
                     }
                     catch(ClassCastException e) {
                         throw new IllegalArgumentException("Can only test Objects of class Packet!");
                    }
                     int toCheck = target.getPort();
                     return toCheck >= minPort;
                    }
                    });
            System.out.println("MinPorts");
            }
            if(maxPort != 0) 
            {//need to handle both individual ports and > < operators. Used with min and max vars; them first
                //ignore individual ports? add/subtract?
            output.add(new Predicate() {
                @Override
                public boolean test(Object p)
                    {
                     Packet target;
                     try{ 
                         target = (Packet) p;
                     }
                     catch(ClassCastException e) {
                         throw new IllegalArgumentException("Can only test Objects of class Packet!");
                     }
                     int toCheck = target.getPort();
                     return maxPort < toCheck;
                    }
                    });
            System.out.println("ports");
            }
            //candidate ranking goes here
        System.out.println("output size is " + output.size());
        return output;
    }
  
   public static void main(String[] args)
   {
       Rules r = new Rules();
   }



}
    


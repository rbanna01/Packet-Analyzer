/* So: ignore file validity: file created automatically, can't detect.
Means it needs to delete any file thus created. (PITA)
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package thread.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;



/**
 * Class DBInterface is responsible for obtaining access to a given database file, 
 * and reading from and writing to it as necessary.
 * @author Ruaridhi Bannatyne
 * ruaridhi.bannatyne@gmail.com
 */
public class DBInterface {
    /*
    * The file which contains the database used by the class.
    */
    private File db;
    /*
    * The JDBC driver's connection to the above database.
    */
    private Connection c; 
    /*
    * The JDBC statement used to interact with the database.
    */
    private Statement s; 
    /*
    * The packet data read from the database.
    */
    private LinkedBlockingQueue<Packet> output;
    
    //constants
    /*
    *Name of the database within the file
    */
    private final String TABLENAME = "packetTable";
    /*
    * Used to initialize a table, if none exists.
    */                                //"CREATE TABLE " 
    private final String INITSTRING = TABLENAME+ "( source VARCHAR[10], dest VARCHAR[10], time TIMESTAMP, length INT, port INT, payload String)";
    /*
    *Prefix needed to access a table through the JDBC
    */
    private final String fluff = "jdbc:sqlite:";
    /*
    * Used to access a virtual database in RAM.
    */
    private final String MEMDB = "jdbc:sqlite::memory:";
    
    
    //Constructor: just needs an InputThread
    public DBInterface()
    {
        try { Class.forName("org.sqlite.JDBC");} //can only be thrown if problem within code itself; outwith scope of errors to be passed back to caller
        catch(ClassNotFoundException x) { System.out.println(x.getMessage()); }  
    }
    
    /* Provides this DBINterface with a file in whicha  database is to be accessed.
    *@param prospective - the file to be read
    *@return boolean - whether the file can be read
    */
    public boolean setFile(String prospective) 
    {// Here: check file exists and can be accessed (handled by SQLException). Too much trouble;just auto-create
        try{ 
        if(prospective.equals("memory")) c = DriverManager.getConnection(MEMDB);
        else if(!Files.exists(Paths.get(prospective))) return false;
        else c = DriverManager.getConnection(fluff+prospective);
        s = c.createStatement();
        s.executeUpdate("CREATE TABLE IF NOT EXISTS " + INITSTRING + ";");
        /*ResultSet temp = s.executeQuery("SELECT * FROM " + TABLENAME);
        if(temp.next() ==false)   //must be empty*/
        return true;
        }
        catch(SQLException e) 
        { System.out.println(e.getMessage());
        return false;}
    } //ends setFile
    
    /* Writes provided collection of packets to the database
    *@param input - the Collection of packets to be written.
    *@return boolean - whether they were written successfully
    */
    public boolean write(Collection<Packet> input) {
        PreparedStatement pS;     //need to check DB can be read
        Packet p; 
        int count = 0; //needs to consume input, else backlog will be ridiculous
        try { //shoudn't consume
            pS = c.prepareStatement("INSERT INTO " + TABLENAME + " VALUES(?, ?, ?, ?, ?, ?);");
            Iterator<Packet> i = input.iterator();
            while(i.hasNext()) {
                p = i.next();
                s.executeUpdate("INSERT INTO " + TABLENAME + " VALUES ('" + p.getSourceString() + "', '" + 
                        p.getDestinationString() + "', '" + p.getTime() + "', '" + p.getLength() + "', '" + p.getPort() +"', '" 
                        + Utils.intString(p.getPayload())+"');");
                    }
                    //pS.close();    
                    System.gc();
    }catch(SQLException cs) 
    {
        System.out.println(cs.getMessage());
        return false;
    }
    System.out.println(count);
    return true;
    } //ends write
    

    /* Removes all data from the current database
    *
    */
    public void clear() {
        try{
        s.execute("DELETE * FROM " + TABLENAME);}
        catch(SQLException e) { System.out.println(e.getMessage());}
        }
    
    /* Reads all data from selected database.
    *@return LinkedBlockingQueue<Packet> containing all data from database.
    */
    public LinkedBlockingQueue<Packet> read()  { 
      ResultSet rS;
      System.out.println("reading");
       try {
       output = new LinkedBlockingQueue<>();   
       rS =  s.executeQuery("SELECT * FROM " + TABLENAME + ";"); //does mot return resultset? Try in test
       while(rS.next()) 
       {
          try {
          output.put(new Packet(Utils.stringShort(rS.getString(1)), Utils.stringShort(rS.getString(2)),
                  rS.getTimestamp(3), rS.getInt(4), rS.getInt(5), Utils.stringInt(rS.getString(6))));
          }
          catch(InterruptedException e) {
              System.out.println(e.getMessage());
              rS.close();
              System.gc();
              return null;
          }
      } //ends while   
       
      } catch(SQLException e) 
      {
          System.out.println(e.getMessage());
          System.gc();
          return null;
      } //SQL Exceptions program's responsibility, so no need to pass up
   try {
       rS.close();
   }
   catch(SQLException SQLExc) { System.out.println(SQLExc.getMessage());}
   System.gc();
   System.out.println("read");
   return output;
   } 

   public static void main(String[] args)
   {
       DBInterface d = new DBInterface();
       d.setFile("D:\\sample.db");
       LinkedBlockingQueue<Packet> input = d.read();
       LinkedBlockingQueue<LinkedList<Packet>> jobList = new LinkedBlockingQueue<>();
       System.out.println(input.size());
       int JOBSIZE = 50;
       int count = 0;
       boolean quit = false;
         LinkedList<Packet> job;
         input.poll();
         System.out.println(input.size());
        /*
         while(input.peek() != null)
         {
             job = new LinkedList<>();
             for(int i = 0; i < JOBSIZE; i++)
             {
              job.add(input.poll());
             }
             System.out.println(input.size());
         } */
         while(true)
                { //problem is here; stuff loops, no more than 50 items taken
                  job = new LinkedList<>();  
                  count = 0;
                  while(count < JOBSIZE) {
                  if(input.peek() == null) break;
                   job.add(input.poll()); //job.add()...
                   ++count;  
                   } //ends while
                  jobList.add(job);
               System.out.println(input.size());
               System.out.println("jobs " + jobList.size());
               if(input.size() == 0) break;
                } //ends outer while
         
         /* while(input.peek()!= null){
         try {
         input.take();}
         catch(InterruptedException e) { System.out.println(e.getStackTrace());}
      count++;
      System.out.println(count);} */
       
   } //ends main
   
   
}     
   



package thread.service;



import java.io.FileNotFoundException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

/* Analyzer is used to coordinate various operations on packets captured from the network and 
* the neural network. It allows the user to access each of these functions while validating input.
 * @author Ruaridhi Bannatyne
 */

public class Analyzer {
    /*
    * Output to be passed to Interface
    */
    private final ObservableList<Packet> outputList;
    /*
    * This Analyzer's ManagerService, which controls the different threads used in packet capture.
    */
    private final ManagerService mS;
    /*
    * The Task object produced by mS
    */
    private Task mT;
    /*
    * This Analyzer's database interface, which is used to read from and write to databases.
    */
    private final DBInterface dB;
    /*
    *This Analyzer's interface with the neural network. Used to access, instantiate and train neural networks.
    */
    private final MapLink mL;
    

   //State variables
    
    /*
    *Indicates whether the analyzer is running.
    */
    private boolean running; //will need helper method for timestamp
    /*
    *Indicates whether output is to be written to a database.
    */
    private boolean writeFile;
    /*
    *Indicates whether input is to be read from a database.
    */
    private boolean readFile;
    
    
    public Analyzer() 
    {
    outputList = FXCollections.observableList(new LinkedList<Packet>());
    dB = new DBInterface();
    mL = new MapLink();
    running = false;
    mS = new ManagerService(outputList, dB); //problem here? what if no other settings changed?
    }
    /**
     Checks the input filename and returns true  or false;
     * N.B.: input of "" indicates that the user has 
     * clicked on the "clear filters" button on the interface.
     * @param file - the name of the file containing the database from which the user wants
     * to load data, or to which they want to write it.
     * @return boolean -  indicating whether current settings are acceptable.
     * @throws FileNotFoundException - if the input causes this exception to be thrown.
    */
    public boolean setDB(String file) throws FileNotFoundException {
        if(file.length() == 0) 
        {
            System.out.println("input length 0");
            return true;
        }
        else return (dB.setFile(file));
    }
    
    /**
     Checks the input integer values and returns true if input valid, false otherwise;
     * N.B.: input of "" indicates that the user has 
     * clicked on the "clear filters" button on the interface.
     * @param in - values input by the user, which should be integers separated by commas and spaces.
     * @return boolean -  indicating whether current settings are valid.
     */
    public boolean setPorts(String in)
    { //bounds: 0 and  to modify
        in = in.trim();
        if(in.length() == 0)
        {
            Rules.setPort(0);
            return true;
        }
        int temp;
        if(in.indexOf(",") == -1) {
            try {
            temp = Integer.parseInt(in);}
            catch(NumberFormatException e) {return false;}
            if( temp > 0 && temp <= 65535) Rules.setPort(temp);
            return true;
        }
        else {
            ArrayList<Integer> ints = new ArrayList<>();
            String[] vals = in.split(",");
            for(String s: vals) {
            try {
            String t = s.trim();    
            temp = Integer.parseInt(t);}
            catch(NumberFormatException e) {return false;}
            if( temp > 0 && temp <= 65535) ints.add(temp);
           }
        for(Integer i: ints)  Rules.setPort(i);
        }      
      return true;  
    }
    
    
    /**
    Checks that any minimum length value input by the user is valid.
     * N.B.: input of "" indicates that the user has 
     * clicked on the "clear filters" button on the interface.
     * @param in - the user's input, which should simply be an integer in String form
     * @return boolean -  indicating whether current settings are valid.
     */
    public boolean setMinLength(String in)
    {
        if(in.length() == 0)
        {
            Rules.setLengthMin(0);
            return true;
        }
        in = in.trim();
        try {
            int x = Integer.parseInt(in);
        if(x > 0) {
            System.out.println("minLength " + x);
            Rules.setLengthMin(x);
            return true;
        }
        } catch(NumberFormatException e) {}
        return false; 
    }
    
    /**
    Checks the input maximum packet length,returns a boolean value indicating whether this 
    * value is valid.
    * N.B.: input of "" indicates that the user has 
    * clicked on the "clear filters" button on the interface.
    * @param in - the maximum length value input by the user, which should simply be an integer.
    * @return boolean -  indicating whether current settings are valid.
    */
    public boolean setMaxLength(String in)
    {
        System.out.println(in);
        if(in.length() == 0)
        {
            Rules.setLengthMax(0);
            return true;
        }
       //in = in.trim();
        try {
            int x = Integer.parseInt(in);
            //System.out.println(x);
        if(x > 0) {
            //System.out.println("maxlength " + x);
            Rules.setLengthMax(x);
            return true;
        }
        } catch(NumberFormatException e) {}
        return false; 
    }
    
    /**
    Used to provide the managerService with direct input from a database or other non-network source
    * N.B.: input of "" indicates that the user has 
    * clicked on the "clear filters" button on the interface.
    * @param input - LinkedBlcokingQueue containing packets to be input to the filter
    */
    public void setInput(LinkedBlockingQueue<Packet> input)
    {
        mS.setDirectInput(true);
        mS.provideInput(input);
    }
    /**
    Used to indicate whether the ManagerService is to use input from neither the database nor
    * the network. Mainly used for testing. 
    * @param whether  whether the ManagerService is to use the input previously mentioned.
    */
    protected void setDirectInput(boolean whether)
    {
        mS.setDirectInput(whether);
    }
    
    /**
    Returns whether the ManagerService is using direct input from neither the network nor the
    * database.
    * @return boolean -  indicating whether current settings are acceptable.
    */
    protected boolean getDirectInput() { return mS.getDirectInput();}
    
    /**
    Begins processing such input as has been set, using any filters provided by the user.
    * @return boolean- whether sniffing has begun. If not, user needs to be notified.
    */
    public boolean sniff()
    { //"Can only start a Service in the READY state. Was in state RUNNING"
        if(!running)  
        {
            mS.setReadFile(readFile);
            System.out.println("analyzer's readfile is " + readFile);
            ExecutorService e = Executors.newFixedThreadPool(1);
            mS.start();
            running = true;
            return true;
        }
        else return false;
    }
    /**
    * Halts ManagerService and all other threads, outputting all packets still being processed.
    */
    public void stop()
    {
        mS.cancel();
        mS.reset();
        running = false;
        if(writeFile)dB.write(outputList);
        //else, stuff already output to ObervableList
    }
        
    /**
     * Sets the writeFile flag to the parameter value, indicating whether the analyzer should
     * write captured traffic to the database.
    * @param in - whether captured packet data is to be written to the database.
    */
    public void write(boolean in) {writeFile = in;}
    
    /**
     * Sets the readFile flag to the parameter value, indicating whether the analyzer should
     * read packet data to analyze from the database.
    * @param in - whether captured packet data is to be read from the database.
    */
    public void read(boolean in) { readFile = in;}
    
    /**
     * Used to get the list to which output is written, used to display information to the user.
     * @return outputList - the list used to hold captured packet data.
    */
    public ObservableList<Packet> getOutput()
    {
        return this.outputList;
    }
    
    /**
     * Sets provided listener to the Analyzer's outputList. 
     * @param l - the listener to be assigned to outputList.
    */
    public void setListener(ListChangeListener l)
    {
        outputList.addListener(l);
    }
    
    /**
     * Sets source value in this Analyzer's Rules object to the given value.
     * N.B.: if input length = 0, user has clicked the "Clear Filters" button.
     * @param newSource - the new source value to be used
     * @return boolean - whether the input was valid.
    */
    public boolean setSource( String newSource)
    {
        //no source or user has clicked "Clear Filters"
        if(newSource.length() == 0)
        {
            Rules.resetSource();
            return true;
        }
      //need to check input source
      if(checkAdd(newSource)) 
      {
          Rules.setSource(Utils.stringShort(newSource));
          return true;
      }
      else return false;
    }
    
    /**
     * Sets destination value in this Analyzer's Rules object to the given value.
     * N.B.: if input length = 0, user has clicked the "Clear Filters" button.
     * @param newDest - the new destination value to be used
     * @return boolean - whether the input was valid
    */
    public boolean setDest(String newDest)
    {
        if(newDest.length() == 0) 
        {
            Rules.resetDest();
            return true;
        }
        if(checkAdd(newDest)) {
           Rules.setDest(Utils.stringShort(newDest));
           return true;
      }  
      else return false; 
    }
    /**
     * Used to check any String address value input by user. 
     * Input is assumed to be in form: "12:34:56:78:9A". Wildcard value "*" may be used.
     * N.B.: if input length = 0, user has clicked the "Clear Filters" button, or deleted setting manually.
     * @param input - the String input value to be checked.
     * @return boolean - whether the input was valid.
    */
    private boolean checkAdd(String input)
    {
        String[] parts = input.split("\\:");
        int temp;
        if(parts.length < 6) return false;
        for(String s: parts)
        {
            try {
            temp = Integer.parseInt(s, 16);}
            catch(NumberFormatException e)
            {//* indicates a wildcard value
             if(s.equals("*")) continue;
             else return false;
            }
        }
       return true;
    }
    
    /**
     * Used to set the Rules object's starting time to the input string.
     * It is assumed that this input is in the form: DD/MM/YYYY HH:MM:SS
     * N.B.: if input length = 0, user has clicked the "Clear Filters" button.
     * @param time - the String input time to be used.
     * @return boolean - whether the input was acceptable.
    */
    public boolean setStart(String time)
    {
        time = time.trim();
        if(time.length() == 0)
        {
            Rules.setStart(null);
            return true; //user has clicked on "CLear Filters" button.
        }
        Timestamp toUse;
        try {
            toUse = getTimestamp(time);
        } 
        catch(Exception e) 
        { //bad input; to be passed on up to the interface.
            return false;
        }
        if(toUse == null) return false;
        Rules.setStart(toUse);
        return true;
    }
    
    /**
     * Used to set the Rules object's end time to the input string.
     * It is assumed that this input is in the form: DD/MM/YYYY HH:MM:SS
     * N.B.: if input length = 0, user has clicked the "Clear Filters" button.
     * @param time - the String input time to be used.
     * @return boolean - whether the input is acceptable.
    */
    public boolean setEnd(String time)
    { 
        time = time.trim();
        if(time.length() == 0)
        {
            Rules.setStop(null);
            return true; //user has clicked on the "Clear Filters" button  
        }
        Timestamp toUse;
        try {
            toUse = getTimestamp(time);
        } 
        catch(Exception e) { 
            return false; //bad input provided; notify user
        }
        //if(toUse == null) return false;
        Rules.setStop(toUse);
        return true;
    }
    //What's this for?  Needed?
    public void addOutput(LinkedList<Packet> toAdd)
    {
      for(Packet p: toAdd) outputList.add(p);  
    }
       
    /** Used to convert input String to a Timestamp which can be used by the Rules member instance.
     * It is assumed that this input is in the form: DD/MM/YYYY HH:MM:SS
     * @param time - time value input in above form.
     * @return - timestamp value equals to the given String
     */
    private Timestamp getTimestamp(String time)
    {
     try{
         time = time.trim();
         String date = time.substring(0, time.indexOf(" "));
         String hourmin = time.substring(time.indexOf(" ")+1, time.indexOf("."));
         date = date.trim();
         String[] comps = hourmin.split(":");
         int hour = Integer.parseInt(comps[0]);
         int min = Integer.parseInt(comps[1]);
         int sec = Integer.parseInt(comps[2]);
         String[] dateComps = date.split("/");
         int year = Integer.parseInt(dateComps[0]);
         int month = Integer.parseInt(dateComps[1]);
         int day = Integer.parseInt(dateComps[2]);
         GregorianCalendar g = new GregorianCalendar(year, month, day, hour, min, sec);
         long toUse = g.getTimeInMillis();
         return new Timestamp(toUse);
    }
    catch(NumberFormatException e)
     {
         System.out.println(e.getMessage()); //to remove after testing is done
         return null;
     }
    } //ends getTimestamp
    
    /* Used to return data indicating the weights of all neurons in the neural network after processing 
    * the current input
    *@return -ArrayList<double> indicating said weight values
    */
    public ArrayList<double[]> analyzeTraffic() {
     return mL.processInput(outputList);
    }


    public boolean clearTable() 
    {
       return outputList.removeAll(outputList);
    }

    public boolean setMap(String prospective)
    {
     mL.loadNetwork(prospective);
     return true;
    }

    public boolean saveMap(String input)
    {
        mL.saveNetwork(input);
        return true;
    }

    public static void main(String[] args)
    {
     Analyzer a = new Analyzer();
     a.read(true);
     a.sniff();
    }


}

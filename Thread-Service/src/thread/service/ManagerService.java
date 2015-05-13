/* Install FIndBugs?
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package thread.service;


import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 *This class is responsible for coordinating and managing all other threads needed
 * for packet analysis, and writing output to the Analyzer's outputList.
 * 
 */
public class ManagerService extends Service {
/**
 * @author Ruaridhi Bannatyne
 * ruaridhi.bannatyne@gmail.com
 */
    /*
    * The Analyzer's outputList
    */
    private final ObservableList<Packet> output;
    /*
    *The DBInterface to be used
    */
    private final DBInterface dBInterface; //DBInterface should be here; responsible for both input and output
    /*//rules to be static
    * The rules to be used for packet capture
    */
    private Rules r;
    /*
    * Whether input is to be taken from a database.
    */
    private boolean readFile;
    /*
    *Whether input is to be written to a database.
    */
    private boolean writeFile;
    /*
    * Whether a database is being accessed.
    */
    private boolean db;
    
    private boolean directInput;
    
    private LinkedBlockingQueue<Packet> in;
    
public ManagerService(ObservableList<Packet> output, DBInterface intIn)
    {
    this.output = output;
    dBInterface = intIn;
    }
/*
*to delete
*/
 public void setDirectInput(boolean whether)
    {
        directInput = whether;
    }
   
 /*
 * Sets database file to be read
 @param file the file to be read
 @return boolean whether the input arg is valid.
 */
    
   //to be deleted when testing is done
   public void provideInput(LinkedBlockingQueue<Packet> toInput) { in = toInput;}

 /*
 * Sets whether database file is to be read
 @param in whether the file is to be read
 */
  protected void setReadFile(boolean in) { this.readFile = in;}
 
  /*
 * Sets whether database file is to be written to
 @param in whether the file is to be written to
 */
 protected void setWriteFile(boolean in) {this.writeFile = in;}

  /*
 * Gets whether database file is to be read
 @return whether the database is to be read
 */
 protected boolean getDirectInput() { return directInput;} 
  
 //testing method: to be removed
 protected void giveInput(LinkedBlockingQueue<Packet> in) { this.in = in;}
   
 /*
 * This service's Task product.
 */
 private ManagerTask mT;
 
 
    
    @Override
    public boolean cancel() 
    {
        mT.doCancel();
        return true;
    }
 
   @Override     
   public Task createTask()
   { 
       mT = new ManagerTask(output);
       this.mT = mT;
       if(readFile) {
           mT.setRead(true); 
        }
       else if(directInput){ //to remove
         
           mT.setDirectInput(true);
           mT.provideInput(in);
       }
       else {
           mT.setRead(false);
           mT.setDirectInput(false);
       }
       if(writeFile) {
           mT.setWrite(true);
        }
       else mT.setWrite(false);
         System.out.println("directInput " + directInput);
         System.out.println("readFile " + readFile);
       return mT;
    } //ends createTask
   
private class ManagerTask extends Task {
    //testing: to remove
     private LinkedBlockingQueue<Packet> input;
    
     /*
     * The length of time in ms to be waitd when there's nothign for this Task to do
     */
     private final int INTERVAL = 200;
     /*
     * The Analyzer's output
     */
     private final ObservableList<Packet> output;
     /*
     * Synchronized queue for use by both this and RuleServices
     */
     private LinkedBlockingQueue<Packet> toOutput;
     /*
     * This ManagerTask's InputService
     */
     private InputService iS;
     /*
     * The InputService's output list if filters are to be applied 
     */
    private LinkedBlockingQueue<LinkedList<Packet>> jobList;
   /*
    * The SnifferService, instantiated if traffic is read from the network
    */
    private SnifferThread sT;
    /*
     * The RuleService instanitated if filtering is to be done.
     */
    private RuleService rS ; //will be okay
    /*
     * The InputService's output list if filters are to be applied 
     */
    private Task[] rT;
    /*
     * Whether input is to be taken from a database
     */
    private boolean readFile;
    /*
     * Whether input is to be written to a database
     */
    private boolean writeFile;
    /*
     * Whether a database has been instantiated
     */
    
    LinkedList<Packet> in;
    // ot be made static
    
    public boolean directInput; //used for testing only
    /*
    *Used to allocate threads to all Tasks.
    */
    private ExecutorService eS;
    /*
     * This ManagerService's InputTask, if needed.
     */
    private Task iT;
    /*
     * This ManagerService's SnifferTask, if needed.
     */
    private Task outputTask;
    
    RuleService[]rSs;

    public ManagerTask(ObservableList<Packet> suppliedOutput)
    {
        this.output = suppliedOutput;
    }
    /*Sets whether output is to be written to database.
     * @param toWrite whether output is to be written to a database
    @return whether action succeeded
     */
    
    public boolean setWrite(boolean toWrite) 
    { 
     writeFile = toWrite;
     return true;
    }
    //to remove
    public void setDirectInput(boolean whether)
    {
        directInput = whether;
    }
    
    //Used for testing: to remove later
    public boolean setRead(boolean whether)
    {//needs to change for final version; only db access to be alllowed
            readFile = whether;
            return true;
    }
    //testing: to remove
    public void provideInput(LinkedBlockingQueue<Packet> toInput) { input = toInput;}
    
    @Override
    protected Boolean call() {
        try{ //differentiating between input from network and input from file?
            int processors = Runtime.getRuntime().availableProcessors()-2;
            System.out.println("running");
            toOutput = new LinkedBlockingQueue<>();
            iS = new InputService();
            LinkedBlockingQueue<Packet> iSInput;
            if(directInput) 
            {
                System.out.println("directInput");
                iSInput = input;
            } //accounts for testing; no readon to keep around otherwise
            else if(readFile == false) { //i.e. getting stuff from network
               System.out.println("network");
               sT = new SnifferThread(); 
               iSInput = sT.getOutput();
               sT.start();
               System.out.println("sS running");
            } else
            {
                System.out.println("database input");
                iSInput = dBInterface.read();
            } 
            try {
                System.out.println("tests " + Rules.getTests().size());
            }
            catch(Exception e) {
                System.out.println(e.getMessage());
            }
            //1 work thread per processor plus processing threads
            if(Rules.getTests() != null) { //i.e. there's a filter to be applied
                System.out.println("tests");
                iS.setFilter(true);
                jobList = iS.getJobList();    
                rSs = new RuleService[processors];
                for(int i = 0 ; i < processors; i++){
                    rS = new RuleService(jobList, toOutput);
                    rSs[i] = rS;
                 //   rS.start();
                    } //ends RuleService for
            }
            else {
                System.out.print("direct output");
                iS.setOutput(toOutput);
                iS.setFilter(false);
            }
            iS.setInput(iSInput);
            iS.start();
            while(true) {        
                if(isCancelled()) doCancel();
                while(toOutput.peek() != null) output.add(toOutput.poll()); //might need to change if this thread hogs too much time?
                Thread.sleep(INTERVAL);
            } //ends while   */
        } catch(Exception e) //InterruptedException e
        { 
         try{ 
        
        if(!readFile && !directInput) sT.interrupt(); //directInput reference to be removed
        Thread.sleep(200);
        iT.cancel(true);
        if(Rules.getTests() != null) { for(Task r: rT) r.cancel(true);}
        Thread.sleep(INTERVAL);
        while(toOutput.peek() != null) output.add(toOutput.poll());
        if(writeFile) dBInterface.write(output);
         } catch(InterruptedException eX) {
            if(isCancelled()) doCancel();
         } //anything to be done here? Notify user?
        }
        return true;
    }//ends run
   
    public void doCancel() {
        try{ 
        if(sT != null) sT.interrupt();
        iS.cancel();
        iS.reset();
        Thread.sleep(INTERVAL);
        if(rSs != null) {
            for(int i = 0; i < rSs.length; i++)
            {
                rSs[i].cancel();
                rSs[i].reset();
            }
        }
        while(toOutput.peek() != null) output.add(toOutput.poll());
        if(writeFile) dBInterface.write(output);
        }
        catch(InterruptedException e) {}
        super.cancel();
    }

}; 




} //ends Service
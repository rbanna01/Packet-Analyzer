/*
 * This class is sued to take packets from a SnifferTask or direct input, then
* either pass the directly to output or pass them on to RuleTasks for filtering in batches.
 */

package thread.service;

import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 *
 * @author Ruaridhi Bannatyne
 * ruaridhi.bannatyne@gmail.com
 */
public class InputService extends Service {
    /*
    * Used to pass batche dof packets to RuleTasks for processing.
    */ 
    private LinkedBlockingQueue<LinkedList<Packet>> jobList;  
    /*
    *Time to sleep in ms when input is empty.
    */
     private final int TOWAIT = 10; //
     /*
     * The number of packets in each batch
     */
     private final int JOBSIZE = 50;
     /*
     *Output from SnifferTask or DBInterface.
     */
     private LinkedBlockingQueue<Packet> input;
    /*
     * The Inputservices' output
     */
     private LinkedBlockingQueue<Packet> output;
     
     /*
     * Whether there is to be filtering of input
     */
     private boolean filter;
     /*
     * Sets whether joblist or the direct output is used.
     @param in the new value for filter
     */
     public void setFilter(boolean in) {this.filter = in;
     if(filter) jobList = new LinkedBlockingQueue<>();
     else jobList = null;
     } 
     
     public InputService(LinkedBlockingQueue<Packet> inputToUse)
     {
         this.input= inputToUse;
     }
     @Override
    public Task createTask()
    {
        System.out.println("IT created");
        InputTask iT = new InputTask(input, output);
        return iT;
    }
    public InputService() {}
    
    public void setInput(LinkedBlockingQueue<Packet> in)
    {
        this.input = in;
    }
    
    /*
    * Used to get this InputService's jobList field
    @return jobList LinkedBlockingQueue<LinkedList<Packet>> batches of packets for processing
    */
    public LinkedBlockingQueue<LinkedList<Packet>> getJobList() { return jobList; } 
    /*
    * Used to set list to which direct (i.e. non-filtered) output will be read
    */
    public void setOutput(LinkedBlockingQueue<Packet> in) { output = in; }
    
    
protected class InputTask extends Task {
    /*
    *The input, no matter the source
    */
    private LinkedBlockingQueue<Packet> input;
    /*
    *The batch of packets being assembled at a given point.
    */
    private LinkedList<Packet> job;
    /*
    * Single-packet output for when no filtering occurs
    */
    private final LinkedBlockingQueue<Packet> output;
    
    public InputTask(LinkedBlockingQueue<Packet> snifferOutput) {
        input = snifferOutput;
        System.out.println("Instantiated IT");
        output = null;
    }
    //Testing constructor
    public InputTask(LinkedBlockingQueue<Packet> snifferOutput, LinkedBlockingQueue<Packet> taskOutput)
    {
      input = snifferOutput;
      this.output = taskOutput;
    }
    
    @Override
    public Boolean call() {
        System.out.println("IT called");
        try{
        if(filter) {
          int count;  
          System.out.println(input.size() + " is input list size");
          while(true)
                { //problem is here; stuff loops, no more than 50 items taken
                    if(this.isCancelled()) break;
                  System.out.println("IS doing batch thing");
                  job = new LinkedList<>();  
                  count = 0;
                  while(count < JOBSIZE) {
                    if(input.peek() == null)
                        {
                           Thread.sleep(TOWAIT);
                        }
                    else {
                    job.add(input.poll()); //job.add()...
                    count++;  }
                   } //ends while
                  System.out.println("one batch done");
                  jobList.add(job);
                  System.out.println("add triggered");
                System.out.println("to do: " + input.size());
                //System.out.println("jobs: " + jobList.size());
               } //ends outer while
        } //ends filtering option
        else { 
            while(true) {
                if(this.isCancelled()) break;
                System.out.println("IS Not filtering");
                while(input.peek() != null) {
                output.add(input.poll());
                System.out.println("packet added");    
                } //ends while
                Thread.sleep(200);
            }
        }
        }
        catch(InterruptedException e) {
            if(jobList !=null && job.size() > 0) {
                jobList.add(job);
                System.out.println("ended thru' exception");
            }
            } //just ends
        System.out.println("ended");
     return true;    
    } //ends call
    }
}
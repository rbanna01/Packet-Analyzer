/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package thread.service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Predicate;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
/**
 *
 * @author Ruaridhi Bannatyne
 */
public class RuleService extends Service {
/**
 *Provides RuleTasks which filter packet input according to the values in Rules. 
 *
 */
    /*
    * An InputTask's output
    */
    private final LinkedBlockingQueue<LinkedList<Packet>> input;
    /*
    * The RuleTask's output
    */
    private final LinkedBlockingQueue<Packet> output;
    /*
    * Interval to wait if input queue empty.
    */
    private final int TOWAIT = 10;
    /*
    * Current job.
    */
    private LinkedList<Packet> current;   
    
    public RuleService(LinkedBlockingQueue<LinkedList<Packet>> input, LinkedBlockingQueue<Packet> q)
    {
        this.input = input;
        this.output =q;
    }
    @Override
    public Task createTask() { return new RuleTask(input, output);}
    
    private class RuleTask extends Task {
        /*
         * The output from an InputService.
         */   
        LinkedBlockingQueue<LinkedList<Packet>> input;
        /*
        * This Task's output.
        */
        LinkedBlockingQueue<Packet> output;

     public RuleTask(LinkedBlockingQueue<LinkedList<Packet>> input, LinkedBlockingQueue<Packet> output)   
     {
       this.input = input;
       this.output = output;
     } //ends constructor
     
     @Override
    public Boolean call(){  
        try{ 
          System.out.println("RuleService running");
          while(true)
          {
              if(this.isCancelled()) break;
              current = input.take();
              process(current);
          } 
        } 
        catch(InterruptedException e) {
            if(current != null && current.size() >0){ process(current); }
        }
    return false; 
    }
    
    /* Filters the input list of packets
    *@param input  a LinkedList of packets to be filtered.
    */
    private void process(LinkedList<Packet> input)
    { ArrayList<Predicate> toUse = Rules.getTests();
       boolean valid;
       while(input.peek() !=null) {
            Packet pa = input.poll();
            valid = true;
            for(Predicate p: toUse)
           {
               if(!p.test(pa))
               {
                   valid = false;
                   break;
               }
          }
          System.out.println(valid);
          if(valid) output.add(pa);
      }
    }
}//ends RuleTask

    
    
    
    
} //emds RuleService

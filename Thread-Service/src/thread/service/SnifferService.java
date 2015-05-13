/* Will all need to be TCP/IP?
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
//Problem seemingly not related to code here in isolation. Try converting to poj Thread class; may play nice with
//FX setup (UI thread)
package thread.service;

import java.util.concurrent.LinkedBlockingQueue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;
import org.jnetpcap.packet.JPacket;
import org.jnetpcap.packet.JPacketHandler; 
import org.jnetpcap.protocol.lan.Ethernet; 
import org.jnetpcap.protocol.tcpip.Tcp;
import org.jnetpcap.protocol.tcpip.Udp;

/** 
 *Gets packets from an Ethernet network.
 * @author Ruaridhi Bannatyne
 */
public class SnifferService extends Service {
    
    /*
    * this SnifferService's output
    */
     private final LinkedBlockingQueue<Packet> output;
    
    public SnifferService() {
        output = new LinkedBlockingQueue<>();
    }
    @Override
    public Task createTask() { return new SnifferTask(output);}
    
    
    protected LinkedBlockingQueue<Packet> getOutput() { return output;}
    
    
    private class SnifferTask extends Task {
        
    //JNETPCAP constants and variables
    private final int snaplen =  64 * 1024;  
    private final int timeout = 10000;
    private  final int mode = Pcap.MODE_PROMISCUOUS;
    private StringBuilder sB;
    private Pcap p;
    /*
    *This SnifferTask's output
    */
    private final LinkedBlockingQueue<Packet> output;
       
    public SnifferTask(LinkedBlockingQueue<Packet> outputToUse)
    {
      this.output = outputToUse;   
    }
    
    //reads all Ethernet packets from the network.
    @Override
   public Boolean call()
   {        
   try{//needs to change
      sB = new StringBuilder();
      System.out.println("1");
      PcapIf device = PcapIf.findAllDevs(sB).get(0);   //problem here
      System.out.println(sB.toString());
      System.out.println(2);
      p = Pcap.openLive(device.getName(), snaplen, timeout, mode, sB);
      System.out.println("PcapIf initialized");
      if(p == null) System.out.println("Error initializing network adapter!");
      else {
                 Ethernet e = new Ethernet();
                 Tcp t = new Tcp();
                 Udp u = new Udp();
                  while(true) {
                      if(this.isCancelled()) break;
                      System.out.println("3"); 
                  //Pcap.LOOP_INFINITE.Test: 50
                     p.loop(50, new JPacketHandler<StringBuilder>()
                      {
                        @Override   
                       public void nextPacket(JPacket jp, StringBuilder sB)
                      {
                          System.out.println("okay");
                         System.out.println("4"); 
                         if(jp.hasHeader(Ethernet.ID)) {
                             if( jp.hasHeader(Tcp.ID)) //jp.hasHeader(Ethernet.ID) &&
                             {  //anything should have ethernet, but need to account for UDP too
                                  jp.getHeader(e);
                                  jp.getHeader(t);
                                  //validation? What if fault to be diagnosed? Will need another option...
                                  output.add(new Packet(Utils.byteShort(e.sourceToByteArray(e.source())), 
                                          Utils.byteShort(e.destinationToByteArray(e.destination())), 
                                          new java.sql.Timestamp(jp.getCaptureHeader().timestampInMillis()), 
                                           e.getLength(), t.destination(), Utils.processByte(t.getPayload())));
                             }
                             else if( jp.hasHeader(Udp.ID)) //jp.hasHeader(Ethernet.ID) &&
                             {  //anything should have ethernet, but need to account for UDP too
                                  jp.getHeader(e);
                                  jp.getHeader(u);
                                  //validation? What if fault to be diagnosed? Will need another option...
                                  output.add(new Packet(Utils.byteShort(e.sourceToByteArray(e.source())), 
                                          Utils.byteShort(e.destinationToByteArray(e.destination())), 
                                          new java.sql.Timestamp(jp.getCaptureHeader().timestampInMillis()), 
                                           e.getLength(), u.destination(), Utils.processByte(u.getPayload())));
                             }
                         }
                      } //ends nextPacket
                  }
                  , sB);
               } //ends viable collection loop 
            }
   }
    catch(Exception e) { 
        if(isCancelled()) {
            System.out.println(e.getMessage()); 
            if(p!= null)p.close();
        }
    }
   return true;
   }

   /* Gets this SnifferTask's output value.
   *@return output this SnifferTask's output list
   */
   public LinkedBlockingQueue<Packet> getOutput() { return output;}
       
    } //ends class

   public static void main(String[] args)
   {
   /*    List<PcapIf> alldevs = new ArrayList<PcapIf>(); // Will be filled with NICs  
        StringBuilder errbuf = new StringBuilder(); // For any error msgs  
  
        /*************************************************************************** 
         * First get a list of devices on this system 
         **************************************************************************/  
     /*   int r = Pcap.findAllDevs(alldevs, errbuf);  
        if (r == Pcap.NOT_OK || alldevs.isEmpty()) {  
            System.err.printf("Can't read list of devices, error is %s", errbuf  
                .toString());  
            return;  
        }  
  
        System.out.println("Network devices found:");  
  
        int i = 0;  
        for (PcapIf device : alldevs) {  
            String description =  
                (device.getDescription() != null) ? device.getDescription()  
                    : "No description available";  
            System.out.printf("#%d: %s [%s]\n", i++, device.getName(), description);  
        }  
      System.out.println(1);
      PcapIf pI = PcapIf.findDefaultIf(errbuf);
      System.out.println(2);
      System.out.println(pI.getDescription()); */
       try{//needs to change
      StringBuilder sB = new StringBuilder();
      System.out.println("1");
      PcapIf device = PcapIf.findAllDevs(sB).get(0);   //problem here
      System.out.println(sB.toString());
      System.out.println(2);
      final int snaplen =  64 * 1024;  
      final int timeout = 10000;
      final int mode = Pcap.MODE_PROMISCUOUS;
    
      Pcap p = Pcap.openLive(device.getName(), snaplen, timeout, mode, sB);
      System.out.println("PcapIf initialized");
      if(p == null) System.out.println("Error initializing network adapter!");
      else {
                 Ethernet e = new Ethernet();
                 Tcp t = new Tcp();
                 Udp u = new Udp();
                  while(true) {
      
                      System.out.println("3"); 
                  //Pcap.LOOP_INFINITE.Test: 50
                     p.loop(50, new JPacketHandler<StringBuilder>()
                      {
                        @Override   
                       public void nextPacket(JPacket jp, StringBuilder sB)
                      {
                         System.out.println("okay");
                         System.out.println("4"); 
                         if(jp.hasHeader(Ethernet.ID)) {
                             if( jp.hasHeader(Tcp.ID)) //jp.hasHeader(Ethernet.ID) &&
                             {  //anything should have ethernet, but need to account for UDP too
                                  jp.getHeader(e);
                                  jp.getHeader(t);
                                  //validation? What if fault to be diagnosed? Will need another option...
                                  System.out.println(new java.sql.Timestamp(jp.getCaptureHeader().timestampInMillis()).toString()); 
                                  System.out.println(e.getLength());

                             }
                             else if( jp.hasHeader(Udp.ID)) //jp.hasHeader(Ethernet.ID) &&
                             {  //anything should have ethernet, but need to account for UDP too
                                  jp.getHeader(e);
                                  jp.getHeader(u);
                                  //validation? What if fault to be diagnosed? Will need another option...
                                  System.out.println(new java.sql.Timestamp(jp.getCaptureHeader().timestampInMillis()).toString()); 
                                  System.out.println(e.getLength());

                             }
                         }
                      } //ends nextPacket
                  }
                  , sB);
               } //ends viable collection loop 
            }
   }
    catch(Exception e) { 
        System.out.println(e.getMessage());
        //should close p, but can't here. Test code anyway
    }
   
      
   }

   
}
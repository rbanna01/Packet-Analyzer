/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package thread.service;

import java.util.List;
import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;

/**
 *
 * @author Ruaridhi Bannatyne
 */
public class PcapTest {
    private static final int snaplen =  64 * 1024;  
    private static final int timeout = 10000;
    private  static final int mode = Pcap.MODE_PROMISCUOUS;
    
    
    public static void main(String[] args)
    {
      StringBuilder sB = new StringBuilder();
      List<PcapIf> device = PcapIf.findAllDevs(sB);   //problem here
      for(PcapIf p: device)
      {
      System.out.println(p.toString());
      }
      //Pcap p = Pcap.openLive(device.getName(), snaplen, timeout, mode, sB);
    }
}

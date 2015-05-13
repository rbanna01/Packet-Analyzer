/* Map class takes n-dimensional double array as input; could normalize and use that. Rationale?
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.

*/

package thread.service;

import activationFunction.HardLimitActivationFunction;
import activationFunction.TransparentActivationFunction;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import kohonen.LearningData;
import kohonen.ParetoWTMLearningFunctionWithTired;
import learningFactorFunctional.ConstantFunctionalFactor;
import metrics.EuclidesMetric;
import network.DefaultNetwork;
import network.KohonenNeuron;
import network.KohonenNeuronWithTired;
import network.NetworkModel;
import topology.GaussNeighbourhoodFunction;
import topology.MatrixTopology;

/** DO not expose network; allow to be renewed, but not set.
 *
 * @author Ruaridhi Bannatyne
 */
public class MapLink {
   /*
    * Input vectors to be provided to the neural network.
    */
   private ArrayList<double[]> vectors;
   /*
    * Maximum weight values for neurons in the neurl network.
    */
   private final double[] maxWeights;
   /*
    * All host addresses in the network.
    */
   private String[] addresses;
   /*
    * Java Kohonen Neural Network Library instance
    */
    private DefaultNetwork dN;
   
    /*
    * Bias value to be used with network
    */
   private final ConstantFunctionalFactor cFF = new ConstantFunctionalFactor(0.9);
   /*
    *The size of the neural network
    */
    private int nodes;
   /*
    * JKNNL learningData instance; holds vectors to be applied
    */
    private LearningData lD;
   /*
    *used to total unique hosts within this traffic set
    */
   private final HashMap<String, short[]> hosts; 
   
   private MatrixTopology mT;
   //Constants
   private final int WEIGHTLENGTH = 3595;
   private final double HIWEIGHT = 0.8;
   private final double LOWEIGHT = 0.5;
   private final int DESTOFFSET = 1536;
   private final int SPORTOFFSET = 3072;//needed?
   private final int DPORTOFFSET = 3077;
   private final int LOFFSET = 3082;
   private final int PLOFFSET = 3083;
   private final int NEIGHBOURHOODRADIUS = 4; 
   
   /*
   * Used to store temporary networks. Needed?
   */
   private final String TEMPNAME = "tempNetwork.txt";
   /*
   * Used as a default name
   */
   private final String DEFAULTNAME = "defaultNetwork.txt";
   /*
   * High andlow weights values, for important and unimportant input dimensions respectively  
   */
   private final double HI = 0.8;
   private final double LO = 0.3;
   /*
   *Number fo port and length diu
   */
   private final int PORTS = 5;
   private final int LENGTHS = 2;
   
   Integer[] toBoost;
   /*
   * Ports considered to be a risk
   */
   private int[] priorityPorts; //should be set somewhere
   /*
   * Ethernet broadcast address.
   */
   private final short[] BROADCAST = { 255, 255, 255, 255, 255, 255}; 
   /*
   * current network file
   */
   private File CURRENTFILE;
   //If other stuff works out tomorrow, ignore this
   public MapLink() {
        //all network setup stuff goes in processInput; need infro wrt number of nodes, etc.
       hosts = new HashMap(100);
      if(Files.exists(Paths.get(DEFAULTNAME))) CURRENTFILE = new File(DEFAULTNAME);
      maxWeights = new double[WEIGHTLENGTH];
      for(int i = 0; i < 3; i++)
      {
          maxWeights[DPORTOFFSET+i] = HIWEIGHT;
      }
      //Again, BROADCAST address to be weighted more heavily
      for(int i = 0; i <= 5; i++)
      {
        maxWeights[255 + i*256] = HIWEIGHT;    
        maxWeights[DESTOFFSET + 255 + (i*256)] = HIWEIGHT;
      } //ends for
      for(int i = 0; i < maxWeights.length; i++) if(maxWeights[i] != HIWEIGHT) maxWeights[i] = LOWEIGHT;
      
      //dN = new DefaultNetwork(5, maxWeights, mT);
   }
    /*
   * Used to run captured traffic through the network to check for anomalous activity.
   *@param in collection of Packets to be processed
   *@return ArrayList<double> weights resulting from application of input.
   */
    public ArrayList<double[]> processInput(Collection<Packet> in) {
        init(in); 
        ParetoWTMLearningFunctionWithTired lF = new ParetoWTMLearningFunctionWithTired(dN, 1,  new EuclidesMetric(), lD, cFF, new GaussNeighbourhoodFunction(NEIGHBOURHOODRADIUS));
        lF.learn();
        KohonenNeuron temp;
        ArrayList<double[]> out = new ArrayList<>();
        double[] tempWeights = new double[dN.getNumbersOfNeurons()];
        for(int i = 0; i < tempWeights.length; i++)
        {           
          out.add(dN.getNeuron(i).getWeight());  
        }
        return out;
    }
    
    /*used to train a neural network. Public or private? exposed?
    * @param inputData input to be used to train the network.
    *@return boolean whther training was completed
    */
    public boolean trainMap(Collection<Packet> inputData) 
    { //no output needed
    try {
    init(inputData); //reps indicated as a min of network size by kohonen
    ParetoWTMLearningFunctionWithTired lF = new ParetoWTMLearningFunctionWithTired(
            dN, (hosts.size() *1000),  new EuclidesMetric(), lD, cFF, 
              new GaussNeighbourhoodFunction(NEIGHBOURHOODRADIUS));
     lF.learn();
     dN.networkToFile(TEMPNAME); //will this need to change?
     return true; }
        catch(Exception e) {
            return false;
        }
    }
    
    /*used to initialize a network. Nodes enough to cover number of neurons are created.
    *@param in traffic for which a network is to be constructed
    */ //Not done yet
    private void init(Collection<Packet> in)
    {
        int size = in.size();
        vectors = new ArrayList<>(size);
        Iterator<Packet> it = in.iterator();
        Packet p;
        while(it.hasNext())  vectors.add(getVector(it.next()));
        nodes = hosts.size();
        KohonenNeuron[] neurons = new KohonenNeuron[nodes];
        if(nodes%2 == 0)
           { mT = new MatrixTopology(nodes/2, nodes/2);}
        else { mT = new MatrixTopology(nodes/2, nodes/2+1);}
        lD = new LearningData(); // vectors, basically
        lD.setData(vectors); 
        double hardLimit = 10; //in practice, this will need to depend on number of hosts or something
        Set<String> set = hosts.keySet();
        Iterator<String> iter = set.iterator();
        HardLimitActivationFunction aF = new HardLimitActivationFunction(hardLimit);
        double[] weights;
        short[] e2; 
        String temp;
        for(int i = 0; i < nodes; i++)
        { 
            temp = "";
            weights = Arrays.copyOf(maxWeights, WEIGHTLENGTH);
            e2 = hosts.get(iter.next());
            for(int l = 0; l < 5; l++)  temp+= Integer.toHexString(e2[l]) + ":";
            temp+= Integer.toHexString(e2[5]);
            addresses[i] = temp;
            for(int j = 0; j < 6; j++)
            {
                if(j < 4)weights[j*(256) + e2[j]] = HIWEIGHT;
               else weights[j*(256) + e2[j]+1] = HIWEIGHT;
            }
            neurons[i] = new KohonenNeuron(weights, aF);
        }
        dN = new DefaultNetwork( neurons, mT);
        } //ends init
            
    /* Used to validate and load a new network (i.e. list of weight values)
    *@param name the name of the file in which the network is saved
    *@ boolean whether name represented a vaid file
    */
    protected boolean loadNetwork(String name)
    {
      try {
        dN = new DefaultNetwork(name, mT);
        return true;
      } 
      catch(Exception e){return false;}
    }
    //needed?
    public boolean saveNetwork(String name)
    {
        try{
            dN.networkToFile(name);
            return true;
        }catch(Exception e) { return false;}
        
    }
    //Needed? Not that I see... Delete when cleaning up
   public boolean createNetwork(String name, int size)
   { //must be tied to network constraints: leave till I've reviewed the theory
       
   return false;
   }
    
    /* used to convert a packet into a vector of values suited to input into a network
   * @param p a packet whose values are to be converted
   *@return double[] neural network input vector
   */
    private double[] getVector(Packet p)
    {//order of input: source(1536)  destination(1536) source port(1) dest port(1) length(1) payload(511)
     //valid length: 771   BUT: 6 vals in MAC address. so: source and dest are of length 6, so 1530 vals in
        //can't; 1530
        /* all should be shorts, frankly
        DESTOFFSET = 1536;
        SPORTOFFSET = 3072;
        DPORTOFFSET = 3077;
        LOFFSET = 3082;
        PLOFFSET = 3083;
        Modelling ports: 
        5 tcp/25 (mail)
        4 tcp/80 web
        3 UDP/53 DNS
        2 <= 1023 other reserved system ports
        1 any not the above
        */
        String destToAdd = p.getDestinationString();
        String sourceToAdd = p.getSourceString();
        if(hosts.get(destToAdd) == null) hosts.put(destToAdd, p.getDestination());
        if(hosts.get(sourceToAdd) == null) hosts.put(sourceToAdd, p.getSource());
        double[] output = new double[WEIGHTLENGTH];
        short[] source = p.getSource();
        for(int i = 0; i < source.length; i++)
        {
            ++output[(i*256)-1 +source[i]];
        }
        short[] dest = p.getDestination();
        for(int k = 0; k < dest.length; k++)
        {
            ++output[(k*256)-1+DESTOFFSET + dest[k]];
        }
        int portMod;
        if(p.getPort() <= 1023) portMod = 4;
        else if(p.getPort() == 25)  portMod = 1;
        else if(p.getPort() == 53) portMod = 3;
        else if (p.getPort() == 80 ) portMod = 2;
        else portMod = 5;
        ++output[SPORTOFFSET+portMod];
        ++output[DPORTOFFSET+portMod];
        if(p.getLength() >= 100) output[LOFFSET] = 1;
        else output[LOFFSET] = 0;
        int[] toCopy  = p.getPayload();
        
        for(int i =0; i < toCopy.length; i++) 
        {
            output[PLOFFSET+i] = toCopy[i];
        }
        return output;
    }
    
    /* get all weights which are part of this network
    *@return maxWeights this network's maximum weights
    */    
    public double[] getWeights() { return maxWeights;}

   /* Initializes a network and returns the DefaultNetwork
    *@param input a collection fo packets for whcih a suitable network is to be instantiated
    * @return a DefaultNetwork model which is suited to the input
    */     
   protected DefaultNetwork doInit(Collection<Packet> input) 
        {
            init(input);
            return this.dN;
        }

    /* Instantiates and trains a neural network capable of modelling network activity between the given hosts.
   *@param hosts a map contianing all hosts in teh network
   *@return File which contains the weights of the output network.
   */
    public ArrayList<double[]> prime(HashMap<String, short[]> hosts)
    {
        if(lD == null) lD = new LearningData(DEFAULTNAME);
        TransparentActivationFunction t = new TransparentActivationFunction();
        int size = hosts.size();
        ArrayList<short[]> adds = new ArrayList<>();
        int[] BROADCASTOffsets = new int[6];
        for(int i = 0; i < 6; i++) BROADCASTOffsets[i] = (DESTOFFSET + (255*i)); 
        Set<String> s = hosts.keySet();
        for(String st: s) adds.add(hosts.get(st));
        int listSize = adds.size();
        System.out.println("addresses: " + listSize);
        ArrayList<KohonenNeuron> neurons = new ArrayList<>(size*size);
        Iterator<short[]> addIt = adds.iterator();
        int destOffset = 6;
        int portOffsetS = 12;
        int portOffsetD =17;
        int lengthOffset = 22;
        short[] thatAdd;
        int temp;
        for(int i = 0; i < listSize; i++){ //outer
            toBoost = new Integer[24];
            short[] thisAdd = adds.get(i);
            for(int x = 0; x < thisAdd.length; x++){
                temp = thisAdd[x];
                toBoost[x] = (x*256) + temp-1;
                }
            for(int j = 0; j < listSize; j++){ //destination
                    if(j == i) continue; //can't send stuff to self
                    thatAdd = adds.get(j);
                    for(int y =0; y <6; y++){
                        temp = thatAdd[y];
                        toBoost[destOffset+y] =  DESTOFFSET + (y*256) + temp;
                    }
                    for(int k = 0; k <= PORTS; k++ ){ 
                        toBoost[portOffsetS] = SPORTOFFSET + k;
                        for(int l = 0; l < PORTS; l++) {
                            toBoost[portOffsetD] = DPORTOFFSET + l;
                            for(int m = 0; m < LENGTHS; m++){
                                  toBoost[lengthOffset] = LOFFSET + m;
                                  neurons.add(new KohonenNeuron(this.initWeights(toBoost), t));
                                }
                          }
                    }
            }
          //  System.out.println("before BROADCASTs added " + neurons.size());
            for(int y =0; y <6; y++){
                    toBoost[destOffset+y] =  BROADCASTOffsets[y];
                }
            for(int k = 0; k < PORTS; k++ ){ 
                toBoost[portOffsetS] = SPORTOFFSET + k;
                for(int l = 0; l < PORTS; l++) {
                    toBoost[portOffsetD] = DPORTOFFSET + l;
                    for(int m = 0; m < LENGTHS; m++){
                        toBoost[lengthOffset] = LOFFSET + m;
                        neurons.add(new KohonenNeuron(this.initWeights(toBoost), t));
                        }
                  }
            }
            //System.out.println("After BROADCASTs added " + neurons.size());
        }
        //System.out.println("size " + neurons.size());
        KohonenNeuron[] nA = new KohonenNeuron[neurons.size()];
        Iterator<KohonenNeuron> i = neurons.iterator();
        for(int o = 0; o < nA.length; o++)
        { 
          nA[o] = i.next();  
        } //ends for
        LinkedBlockingQueue<Packet> trainingData = new LinkedBlockingQueue<>();
        //for each host: dodgy port data to random destinations and BROADCASTing
        //100 of each
        //just use adds
        int count;
        for(int a= 0; a < size; a++)
        {
            count = 0;
            ArrayList<short[]> others = new ArrayList<>(size);
            for(int b = 0; b < adds.size(); b++)
            {
            if(b == a) continue;
            else others.add(adds.get(b));
            others.add(BROADCAST);
            Utils.getData(100, adds.get(a), others, null, 0, priorityPorts, null).drainTo(trainingData);
            }
        }
        ArrayList<double[]> vectors = new ArrayList<>(100*listSize);
        while(trainingData.peek() != null) vectors.add(getVector(trainingData.poll()));
        mT = new MatrixTopology(nA.length/2, nA.length/2);
        dN = new DefaultNetwork(nA, mT);
        lD.setData(vectors);
        ParetoWTMLearningFunctionWithTired lF = new ParetoWTMLearningFunctionWithTired(dN, neurons.size()*500,  new EuclidesMetric(), lD, cFF, new GaussNeighbourhoodFunction(NEIGHBOURHOODRADIUS));
        
        lF.learn();
        dN.networkToFile("dummy.txt");
        ArrayList<double[]> out = new ArrayList<>();
        for(KohonenNeuron neuron: nA) out.add(neuron.getWeight());
        return out;
        }
    
    /* Initializes the weights of each neuron
    *@param an integer array containing the indices of dimensions to be set to HI weight value
    *@return out a double[] with indices contained in in set to HI and all other set to LO.
    */
    public double[] initWeights(Integer[] in)
    {
       double[] out = new double[WEIGHTLENGTH];
       /*System.out.println(out.length);
       System.out.println(out[195]); */
       try{
       for(int i: in) {
           //System.out.println(i);
           out[i] = HI;
       }
       for(int j = 0; j < out.length; j++) {
           if(out[j] != HI) out[j] = LO;
       } 
       } catch(NullPointerException e) 
       {
           e.getMessage();
       }
       return out;
    }

    /* Defines the ports considered dangerous by this neural network. Assumed to be 3 in size.
    *@param in[] with the ports considered dangerous
    */
  public void setPriorityPorts( int[] in)
  {
      priorityPorts = in;
  }

  public static void main(String[] args)
        {
          //test
            MapLink m = new MapLink();
            HashMap<String, short[]> h = new HashMap<>();
            String s1 = "16:34:32:DA:56:01";
            String s2 = "1A:C4:5F:D3:A6:01";
            String s3 = "C4:23:32:DA:56:01";
            h.put(s1, Utils.stringShort(s1));
            h.put(s2, Utils.stringShort(s2));
            h.put(s3, Utils.stringShort(s3));
            int[] ports = { 53, 80, 45};
            m.setPriorityPorts(ports);
            ArrayList<double[]> out = m.prime(h);
            try{
            FileWriter f = new FileWriter("D:\\dummy.txt");
            for(double[] array: out)
            {
                for(double d: array)
                {
                f.write(d + "\\t");    
                }
                f.write("\\n");
                f.flush();
            }
            }
            catch(IOException e)
            {
                System.out.println(e.getMessage());
            }
            
           // double[] d = new double[m.WEIGHTLENGTH];
            //System.out.println(d[195]);
        }

}

/* to do
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tests;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.LinkedList;
import java.util.Random;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static threadtest.DBInterfaceTest.s;

/**
 *
 * @author Ruaridhi Bannatyne
 */
public class ManagerThreadTest {
    private final String dbName = "memory";
    private final String dbTable = "test";
    private Random r;
    private final int SAMPLE = 50;
    private final int INTERVAL = 2000;
    private LinkedBlockingQueue<Packet> data;
    private ObservableList<Packet> output;
    private LinkedBlockingQueue<LinkedList<Packet>> jobList;
    private Rules rules;
    private ManagerThread mT;
    private DBInterface d;
    @BeforeClass
    public static void setUpClass() {
    try {
        Class.forName("org.sqlite.JDBC");} //can only be thrown if problem within code itself; outwith scope of errors to be passed back to caller
        catch(ClassNotFoundException x) { System.out.println(x.getMessage()); }  
    try {//fluff+ prospective
        Connection c = DriverManager.getConnection("jdbc:sqlite:memory");
        s = c.createStatement();
        s.executeUpdate("CREATE TABLE test(source VARCHAR(29), dest VARCHAR(29), time TIMSTAMP, length INT, port INT, payload VARBINARY(1982));");
        }
    catch(SQLException e) {
        System.out.println("setup failure");
        System.out.println(e.getMessage());}
    }
    
    @AfterClass
    public static void tearDownClass() 
    {
    try {
    s.executeQuery("DROP TABLE test" ); }
        catch(SQLException e) {}
    }
    
    @Before
    public void setUp() {
    output = FXCollections.observableList(new LinkedList<Packet>());
    r = new Random();
    rules = new Rules();
    data = new LinkedBlockingQueue<>();
    mT = new ManagerThread(rules, output);
    mT.setInput(data);
    }
    @After
    public void tearDown() {
            try {
    s.executeQuery("DELETE * FROM test"); }
        catch(SQLException e) {}
    }
    
    
//tests: that data is read and output correctly from a database and otherwise
//reading  writing?    problem
    @Test
    public void dbNoFilterReadTest() {
    d = new DBInterface();
    d.setFile("memory");
    LinkedList<Packet> l = new LinkedList<>();
    for(Packet p:Utils.getData(SAMPLE, null, null, null, 0, 0, null)) l.add(p);
    d.write(l);
    if(mT.setDB("memory")) System.out.println("db opkay");
    mT.setRead(true);
    mT.start();
    try{
    Thread.sleep(2000);
    mT.interrupt();}
    catch(InterruptedException e) {System.out.println(e.getMessage());}
    assertTrue("not all packets recovered in dbreadtest" + output.size(), output.size() == SAMPLE);
    } //ends noFilterTest
 
    @Test
    public void dbNoFilterWriteTest() {
    d = new DBInterface();
    d.setFile("memory");
    mT.setInput(Utils.getData(SAMPLE, null, null, null, 0, 0, null));
    mT.setDB("memory");    
    if(mT.setWrite(true)) System.out.println("write works!");
    if(mT.setRead(true)) System.out.println("read works!");
    mT.start();
    try {
    Thread.sleep(500);
    mT.interrupt();
    Thread.sleep(500);
    }
    catch(InterruptedException e) {fail();}
    LinkedBlockingQueue<Packet> out = d.read();
    assertTrue("packet disparity in nofilterwritetest " + out.size(), out.size() == 2*SAMPLE);
    } //ends dbNoFilterWriteTest
    
    
    
    @Test 
    public void directInputNoFilterTest() {}

    
    @Test
    public void dbFilterTest() {
    //
    
    
    
    }
    
    @Test 
    public void directInputFilterTest() {}
    
    




}

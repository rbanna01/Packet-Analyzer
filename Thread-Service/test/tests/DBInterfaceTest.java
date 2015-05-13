/* Has been tested
 * 
 * To do: find out about SQLITE_ERRORs, see if they can be used
 * knock-on modifications: InputThread and ManagerThread
 */

package tests;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import thread.service.DBInterface;
import thread.service.Packet;
import thread.service.Utils;

/**
 *
 * @author Ruaridhi Bannatyne
 */
public class DBInterfaceTest {
    private DBInterface d;
    // so db schema = source(), dest(eth)    
    //test database file,a dn table names
    private String dbDir = "jdbc:sqlite:memDB";
    private String empty = "empty";
    private String read = "testRead";
    private String write = "testWrite";
    private int SAMPLE = 50;
    static Statement s;
    private Connection c;
    public DBInterfaceTest() {
    
    }
    
    @BeforeClass
    public static void setUpClass() {
    //here: initialize db schema
    
       
    }
    
    @AfterClass
    public static void tearDownClass() {}
        
    
    @Before
    public void setUp() {
    d = new DBInterface();
    try{
     Class.forName("org.sqlite.JDBC");} //can only be thrown if problem within code itself; outwith scope of errors to be passed back to caller
        catch(ClassNotFoundException x) { System.out.println(x.getMessage()); }  
        try {//fluff+ prospective
        c = DriverManager.getConnection("jdbc:sqlite:memory");
        s = c.createStatement();
        s.executeUpdate("DROP TABLE IF EXISTS packetTable;");
        s.executeUpdate("CREATE TABLE packetTable(source VARCHAR[10], dest VARCHAR[10], time TIMESTAMP, length INT, port INT, payload String);");
        }
        catch(Exception e) { System.out.println(e.getMessage());} 
    }
    
    @After
    public void tearDown() {
    }
 
    @Test //needs to be done in GUI manually. Problem here: SQL Error string thrown
    public void  fileDoesntExistTest() {
       //System.out.println(d.setFile("jkghjkf", "giuhgiugiu"));
      assertTrue("setFile passes false filename", (d.setFile("kjhgkjaehjgjkerb") == false));
    }
    
    @Test
    public void validReadWriteTest ()
    { //hangs. Why?
       System.out.println(d.setFile("memory"));
       System.out.println(d.read()); //clear db before data input
        LinkedBlockingQueue<Packet>  q = Utils.getData(50, null, null, null, 0, 0, null);
        LinkedBlockingQueue<Packet> copy = new LinkedBlockingQueue<>();
        Iterator<Packet> i = q.iterator();
        while(i.hasNext()) copy.add(i.next());
        d.write(q);
        LinkedBlockingQueue<Packet> output = d.read();
        
        assertTrue("Size of list as read is " + output.size(), output.size() == 50);
        //for(Packet pa: output) assertTrue("Packets lost in write and read ops", copy.contains(pa));
        //d.write
    }
    //*/
    @Test 
    public void invalidReadTest() {
        d.setFile("unused.db");
    try{        
        assertTrue("invalidRead op returns non-null", d.read()== null);
    }
    catch(NullPointerException e) { assertTrue( true);}
    } //ends invalidReadTest
    
    
}

/* to-do: get other file choosers and whatnot implemented, write manual text
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package thread.service;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


/**
 * @author Ruaridhi Bannatyne
 * ruaridhi.bannatyne@gmail.com
 */
public class MenuHandler implements EventHandler {
    /*
    * The menu for which this MenuHandler is responsible
    */
    private Menu m;
    /*
    *The stage to be used when creating popups and whatnot
    */
    private final Stage s;
    /*
    *FrontEnd which instantiated this.
    */
    private final FrontEnd fE;
    /*
    *The analyzer this MenuHandler will need to manipulate.
    */
    private final Analyzer a;
    
    /*
    * To be displayed when the "about" option is selected
    */
    private final String ABOUT_TEXT = "Developed 05/14- 09/14 as a Master's Degree project at Birkbeck College.";
    
    /*
    * To be displayed when help selected from menu
    */
    private final String INSTRUCTIONS_TEXT = "Enter desired filters, select database file to read/ write to, and click \"Start\" to begin scanning.\n"
            + "After data has been captured, select \\\"Map\\ Analyze Traffic \" to analyze captured traffic for evidence of malicious activity.";
    /*
    * Sets this MenuHanlder's menu
    *@param the menu to be used with this MenuHandler
    */
    public void addMenu(Menu toAdd)
    {
        this.m = toAdd;
    }
   
    
    public MenuHandler(Stage s, FrontEnd f, Analyzer in) 
    {
    this.s = s;
    this.fE = f;
    a = in;
    }
    @Override
    public void handle (Event e)
    { 
      MenuItem mI = (MenuItem) e.getSource();
      String eventName = process(mI.getId());
      System.out.println(eventName);
      Method toExecute;      

      Class current = this.getClass();
      try { //This code assumes all methods to be called conform to form: toDoSomething with no parameters
      toExecute = current.getMethod(eventName); 
       toExecute.invoke(this);}
      catch(NoSuchMethodException nsm) { System.out.println(2);}
      catch(InvocationTargetException third) 
      { //outwith scope of user interaction with program.
          System.out.println("invoc tar");
          third.getMessage();}
      catch(IllegalAccessException ex) {
          System.out.println("illegalaccess");
      }
      
      //}
      //catch(Exception exc) { System.out.println("Exception : " + exc.getMessage());}
    }
    /*
    * Used to show the player an optiosn screen (for what? Delete if no time tomorrow)
    */
    public void doOptions() 
    { //Here: changing text color etc? What is there, really? Leave for if there's time later
        Stage optionsStage= new Stage();  
        optionsStage.setTitle("Options");
        Text dummy = new Text("optionswindow dummy");
        GridPane gP = new GridPane();
        gP.add(dummy, 0, 0);
        Button close = new Button("Close");
        close.setOnAction(e -> optionsStage.close());
        Button apply = new Button("Apply changes");
        //apply.setOnAction(e - > doUpdate());
        Scene options = new Scene(gP, 200, 200);
        options.getStylesheets().add(FrontEnd.class.getResource("frontEndStyles.css").toExternalForm());
        optionsStage.setScene(options);
        optionsStage.show();
    }
    
    /*
    * Used to dispaly instruction manual
    */
    public void doInstructions() 
    {
        Stage instructionsStage= new Stage();  
        instructionsStage.setTitle("Instructions");
        Text dummy = new Text(INSTRUCTIONS_TEXT);
        GridPane fP = new GridPane(); //get from .txt file
        fP.add(dummy,0,0);
        Scene options = new Scene(fP, 200, 200);
        options.getStylesheets().add(FrontEnd.class.getResource("frontEndStyles.css").toExternalForm());
        instructionsStage.setScene(options);
        Button b = new Button("Close");
        fP.add(dummy, 0, 1);
        b.setOnAction( e -> instructionsStage.close());
        instructionsStage.show();
    }
    
    /*
    * Used to display version number and author, etc.
    */
    public void doAbout() {
        Stage aboutStage= new Stage();  
        aboutStage.setTitle("About");
        Text dummy = new Text("ABOUT_TEXT");
        FlowPane fP = new FlowPane();
        fP.getChildren().add(dummy);
        Scene options = new Scene(fP, 200, 200);
        options.getStylesheets().add(FrontEnd.class.getResource("frontEndStyles.css").toExternalForm());
        aboutStage.setScene(options);
        Button b = new Button("Close");
        b.setOnAction( e-> aboutStage.close());
        aboutStage.show();
    }
    
    /*
    * Closes application.
    */
    public void doQuit() { 
        s.close();}
   
    /*
    * Used to convert the id of a button which fired an event into a method name of the form doX()
    */
    private String process(String in) {
        String out = in.replace(" ", "");
        return("do" + out);
    }
        
   /*
    * Analyzes packet data captured by the user and displays results in grpahical form.
    
   public void doAnalyzeTraffic() {
            try {
            int radius = 15; //since space also = 15, can just use 3*radius between circle centers 
            int padding = 10;
            int space = 15;
            
            ArrayList<double[]> vals = a.analyzeTraffic();
            //get on with it from here: get weights from wining neuron @source offsets, use that
            int[] toUse = { 1971, 2047, 2303, 2559, 2815, 3071, 3077, 3078, 3079};
            int divisor = toUse.length;
        //take it from here; see notes
        //Here: just run through network, and present a number of nodes in a window with 
        //color shading to show strength of connection. What about identifying individual nodes?
        //so: take total val of all sensitive weights, and those which are above-average to be 
        //get average of all weights; less than average means more green, more than average means 
        //displaying stuff: all nodes in a row, with address printed on top
        //get values first; need to set colors upon instantiation of circles.
        Set<String> adds = vals.keySet();
        int total = 0; //made of average indices of suspicious activity
        int size = adds.size();
        double[] nodeVals = new double[size];
        int index = 0;
        for(String s: adds)
        {
            double[] toAdd = vals.get(s);
            int temp = 0;
            for(int i: toUse) temp += toAdd[i]; 
            nodeVals[index] = temp;
            total += temp/divisor;
            ++index;
        }
        total = total/size; //this is the average weight of all sensitised neurons.
        LinkedList<Text> names = new LinkedList<>();
        //from here: use nodeVals[i] to assign color to circles upon instantiation
        int nodes =vals.size(); //this many, in rectangle with height/width ratio of 3/2
        int rows = nodes/10 + nodes%10;
        //assigning values to colors; 
        Color strokeColor = Color.BLACK;
        int width = 500;
        int height = 500;
        Rectangle r = new Rectangle(500, 500);
        Iterator<String> iterator = adds.iterator();
        ArrayList<Circle> c = new ArrayList<>();
        for(int i = 0; i < rows; i++)
        {
            int y = 25+ (45*i);
            for(int j = 0; j < 10; j++)
            {
                int thisX = 25+(45*j);
                Circle next = new Circle(thisX, y, radius);
                double x  = nodeVals[i] -total;
                //order is red, green, blue
                Color temp = new Color(155.0+ x, 155.0, 0.0, 255.0); //4th is opacity
                next.setFill(temp);
                next.setStroke(strokeColor);
                Text t = new Text(iterator.next());
                t.setX(thisX-15);
                t.setY(y);
                names.add(t);
                //adjust size as needed
            }
        }//ends circle setup for
        Stage temp= new Stage();  
        temp.setTitle("Output");
        Pane p = new Pane();
        p.setStyle("-fx-background-color: white;");
        for(Circle circle: c) p.getChildren().add(circle);
        for(Text text: names )p.getChildren().add(text);
        Scene display = new Scene(p, 500, 500);
        display.getStylesheets().add(FrontEnd.class.getResource("frontEndStyles.css").toExternalForm());
        temp.setScene(display);
        Button b = new Button("Close");
        b.setOnAction(e-> temp.close());
        GridPane g = new GridPane();
        Label l = new Label("The stronger the red shading on a host in teh diagram, the more suspicious traffic"
                + "has been assocaited with that node. Likewise, the less such traffic, the greener the node.");
        temp.show();
        }
        catch(Exception e) {System.out.println(e.getMessage()); }
        }
 /*       
        public void docreateNewNetwork() {
        System.out.println("createNewNetwork");
        //need to get input here: name and size
        Label nameText = new Label("Enter name:");
        Label sizeText = new Label("Enter number of hosts to be represented");
        Stage temp = new Stage();
        Text nameInput = new Text();
        Button cancel = new Button("Cancel");
        cancel.setOnAction( e -> temp.close());
        Text sizeInput = new Text();
        Button ok = new Button("Create");
        ok.setOnAction(new EventHandler() {
           @Override
           public void handle(Event e)
           {
               while(true) {
            String name = nameInput.getText();
            try {
            int size = Integer.parseInt(sizeInput.getText());
            
            if(a.createNetwork(name, size)) break;
            else fE.doPopup("Unable to create a new network. Please check name and size.");
            }
            catch(IllegalArgumentException IAe) { fE.doPopup("Invalid size value!");}
             }
           } //ends handle
        });
        Popup p = new Popup();
        p.setX(250);
        p.setY(250);
        p.getContent().addAll(nameText, nameInput, sizeText, sizeInput, ok, cancel);
        p.show(temp);
        } */

        //Testing: to be deleted. Remember to delete.
        public void doLoadDummyData()
        {
        Stage temp = new Stage();    
        TextField sizeInput = new TextField("Enter number of packets");
        Button cancel = new Button("Cancel");
        cancel.setOnAction( e -> temp.close());
        Button ok = new Button("Generate");
        ok.setOnAction(new EventHandler() {
           @Override
           public void handle(Event e)
           {
               while(true) {
            try {
            int size = Integer.parseInt(sizeInput.getText());
            a.setInput(Utils.getData(size, null, null, null, 0, 0, null));//Crashes the application. why?
            a.setDirectInput(true);
            break;
            }
            catch(IllegalArgumentException IAe) { fE.doPopup("Invalid input", "Please enter an integer.");}
            }
            temp.close();
        }});
    GridPane gP = new GridPane();
        gP.add(sizeInput, 0, 0);
        gP.add(ok, 0, 3 );
        gP.add(cancel, 1, 3);
        Scene s = new Scene(gP);
        temp.setScene(s);
        temp.setX(300);
        temp.setY(300);
        temp.show();
        }
//  */


}

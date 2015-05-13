
package thread.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PopupControl;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;


/**
* Author: Ruaridhi Bannatyne
 * ruaridhi.bannatyne@gmail.com
 */
public class FrontEnd extends Application {
    //Components
    private ActionHandler aH;
    private MenuHandler MH;
    private Analyzer a;
    private  TextField sMAC;
    private TextField dMAC;
    private TextField startTime;
    private TextField startDate;
    private TextField stopTime;
    private TextField stopDate;
    private Label file;
    private TextField port;
    private TextField minLength;
    private TextField maxLength;
    private Button cB;
    private Button sB;
    private Button cTB;
    private Button dB;
    private CheckBox lF;
    private CheckBox sT;
    private PopupControl optionsWindow;
    private PopupControl mapWindow;
    private PopupControl helpWindow;
    private PopupControl aboutWindow;
    Stage stage;
    Scene scene;

    ObservableList<Packet> packetList;
    LinkedList<Packet> tableContents;
    
    //Default text for input TextFields; used to demonstrate expected input format
    private final String DEFAULT_TIME_TEXT = "HH:MM:SS";
    private final String DEFAULT_DATE_TEXT= "YYYY/MM/DD";
    private final String DEFAULT_ADDRESS_TEXT = "E0:05:C5:FB:AB";
    private final String DEFAULT_FILE_TEXT = "C:\\DUMMYDIR\\FILE.DB";
    private final String DEFAULT_PORT_TEXT = "20, 25, 88";
    private final String DEFAULT_LENGTH_TEXT = "45";
    /*
    * Default height of interface window.
    */ 
    private final int DEFAULT_HEIGHT = 800;
    /*
    * Default weight of interface window.
    */
    private final int DEFAULT_WIDTH = 800;
    /*
    * 5 columns in the table
    */
    private final int COLUMN_WIDTH = DEFAULT_WIDTH/5; 
    /*
    * State variable. Records whether user has initiated packet capture.
    */
    private boolean capturing; //needed? Use to block input when capturing
   /*
    * Used to hold error message for user in the event of invalid input.
    */
    private String check = "";
    /*
    * Shows whether a valid database file has been input
    */
    private boolean validFile;
    
    /*
    *Initializes all components and assigns default values to TextFields.
    */
    @Override
    public void start(Stage primaryStage) {
    stage = primaryStage;   
    a = new Analyzer();    
    BorderPane bp = new BorderPane();
    MH = new MenuHandler(stage, this, a);
    MenuBar menu = getMenu();
    StackPane root = new StackPane();
    root.getChildren().add(bp);
    scene = new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    stage.setScene(scene);
    scene.getStylesheets().add(FrontEnd.class.getResource("frontEndStyles.css").toExternalForm());
    aH = new ActionHandler(scene); 
    GridPane params = getInputPane();
    packetList = a.getOutput();
    bp.setTop(menu);
    bp.setCenter(params);              
    bp.setBottom(getOutputTable());
    stage.setTitle("Front end");
    stage.show();
    }
    /*
    * Initializes the table on which packet data is to be displayed
    * @return FlowPane containing said table 
    */
    private FlowPane getOutputTable() {
        TableView<Packet> pTable = new TableView<>();
        TableColumn<Packet, String> sourceColumn = getTableColumn("Source", "sourceString");
        TableColumn<Packet, String> destColumn = getTableColumn("Destination", "destinationString");
        TableColumn<Packet, String> timeColumn = getTableColumn("Time received", "timeString");
        TableColumn<Packet, String> portColumn = getTableColumn("Port", "portProperty");
        TableColumn<Packet, String> lengthColumn = getTableColumn("Length", "lengthProperty");
        packetList = a.getOutput();
        pTable.setItems(packetList); 
        pTable.getColumns().setAll(sourceColumn, destColumn,  portColumn, lengthColumn, timeColumn); 
        pTable.setMinWidth(DEFAULT_WIDTH);
        FlowPane fP = new FlowPane();
        fP.getChildren().add(pTable);
        fP.setPrefWidth(DEFAULT_WIDTH);
        return fP;
    } //ends getOutputPane
    
    /*
    * Used to initialize each column in the table
    *@param title the title to be displayed at the top of the column
    *@param prop the name of the property to be displayed in each column
    *@return a new TableColumn with the given title, which displays the given property 
    */
    private TableColumn<Packet, String> getTableColumn(String title, String prop)
    {
     TableColumn<Packet, String> output = new TableColumn<>(title);
     output.setCellValueFactory(new PropertyValueFactory<>(prop)); 
     output.setMinWidth(COLUMN_WIDTH);
     return output;
    }
    
    /*used to initialize all components in the user input pane.
    *@return GridPane containing all components needed for the user to define a filter.
    */
    private GridPane getInputPane() {
        Label headLabel = new Label("Enter search criteria, or intercept all packets.");
        Label startTimeLabel = new Label("Start time:");
        Label stopTimeLabel = new Label("Stop time:");
        Label startDateLabel = new Label("Start date:");
        Label stopDateLabel = new Label("Stop date:");
        Label sourceLabel = new Label("Source:");
        Label destinationLabel = new Label("Destination:");
        Label fileLabel = new Label("Database file");
        Label portLabel = new Label("Port(s)"); //multiple ports? How much work?
        Label minLengthLabel = new Label("Min. length");
        Label maxLengthLabel = new Label("Max. length");
        lF = new CheckBox("Load data from specified DB");
        sT = new CheckBox("Save data to specified DB");
        file = new Label();
        file.setText(DEFAULT_FILE_TEXT);
        cB = getButton("Clear filters", aH);
        sB = getButton("Start collecting packets", aH);
        cTB = getButton("Clear table", aH);
        dB = getButton("Browse", aH);
        sMAC = new TextField();
        sMAC.setText(DEFAULT_ADDRESS_TEXT);
        dMAC = new TextField();
        dMAC.setText(DEFAULT_ADDRESS_TEXT);
        startTime = new TextField();
        startTime.setText(DEFAULT_TIME_TEXT);
        startDate = new TextField();
        startDate.setText(DEFAULT_DATE_TEXT);
        stopTime = new TextField();
        stopTime.setText(DEFAULT_TIME_TEXT);
        stopDate = new TextField();
        stopDate.setText(DEFAULT_DATE_TEXT);
        port = new TextField();
        port.setText(DEFAULT_PORT_TEXT);
        minLength = new TextField();
        minLength.setText(DEFAULT_LENGTH_TEXT);
        maxLength = new TextField();
        maxLength.setText(DEFAULT_LENGTH_TEXT);
        GridPane gp = new GridPane();
        gp.setHgap(10);
        gp.setVgap(10);
        gp.setPadding(new Insets(0,10,0,10));
        gp.add(headLabel, 1, 0);
        gp.add(startTimeLabel, 0, 1);
        gp.add(startTime, 1, 1);
        gp.add(startDateLabel, 0, 2);
        gp.add(startDate, 1, 2);
        gp.add(stopTimeLabel, 0, 3);
        gp.add(stopTime, 1, 3);
        gp.add(stopDateLabel, 0, 4);
        gp.add(stopDate, 1, 4);
        gp.add(sMAC, 1, 5);
        gp.add(sourceLabel, 0, 5);
        gp.add(dMAC, 1, 6);
        gp.add(destinationLabel, 0, 6);
        gp.add(cB, 3, 7);
        gp.add(sB,0, 7);
        gp.add(fileLabel, 0, 8);
        gp.add(file, 1, 8);
        gp.add(dB, 3,8);
        gp.add(minLengthLabel, 3, 1);
        gp.add(minLength, 4, 1);
        gp.add(maxLength, 4, 2);
        gp.add(maxLengthLabel, 3, 2);
        gp.add(portLabel, 3, 3);
        gp.add(port, 4, 3);
        gp.add(lF, 3, 4);
        gp.add(sT, 3, 5);
        gp.add(cTB, 3, 6);
        minLength.setVisible(true);
        maxLength.setVisible(true);
        port.setVisible(true);
        file.setVisible(true);
        sMAC.setVisible(true);
        dMAC.setVisible(true);
        startTime.setVisible(true); 
        stopTime.setVisible(true); 
        stopDate.setVisible(true);
        stopTime.setVisible(true);
        return gp;
    } //ends getInputPane
      
    /*
    * Initializes the menu to be placed at the top of the interface window.
    *@return MenuBar containing all required menus. 
    */
    private MenuBar getMenu() {
        MenuBar top = new MenuBar();
        Menu fileMenu = new Menu("File");
        MH.addMenu(fileMenu);
        Menu mapMenu = new Menu("Map");
        MH.addMenu(mapMenu);
        Menu helpMenu = new Menu("About");
        MH.addMenu(helpMenu); //remove load adummy data when done; more trouble than it's worth
        fileMenu.getItems().addAll(getItem("Load Dummy Data"), getItem("Quit"));
        mapMenu.getItems().addAll(getItem("Analyze Traffic"));
        helpMenu.getItems().addAll(getItem("Instructions"), getItem("About"));
        top.getMenus().addAll(fileMenu, mapMenu, helpMenu);
        return top;
    } //ends getMenu
      
    /*
    * Used to intiialize a button and assign it the provided EventHandler
    *@param s the text to be displayed on the button and to be sued as its id
    *@param eH the eventHandler to be used with this button
    */
    private Button getButton(String s, EventHandler eH)
    {
       Button output = new Button(s);
       output.setId(s);
       output.setOnAction(eH);
       return output;
    }
    
    /*
    * Used to intiialize a menu and assign it a MenuHandler
    *@param s the text to be displayed on the menu and to be sued as its id
    *@return a new Menutem with the given text, id, and handler.
    */
    private MenuItem getItem(String s)
    {
        MenuItem output = new MenuItem(s);
        output.setId(s); 
        output.setOnAction(MH);
        return output;
    } //ends getItem
    
    /*
    This class is used to handle actions affecting both the buttons and ObservableList
    */
    private class ActionHandler implements EventHandler{
        Scene s;
        public ActionHandler(Scene toAdd) {
            s = toAdd;
        }
        
        @Override
        public void handle(Event event) {
            Object b = event.getSource();
           if(b == cB) { //cB = clearButton
               maxLength.setText("");
               minLength.setText("");
                port.setText("");
                sMAC.setText("");
                dMAC.setText("");
                startTime.setText(""); 
                startDate.setText("");
                stopTime.setText("");
                stopDate.setText("");
              }
           else if (b == sB) { //sB= startButton
               if(capturing) 
               {
                   capturing = false;
                   minLength.setEditable(true);
                   maxLength.setEditable(true);
                   port.setEditable(true);
                   sMAC.setEditable(true);
                   dMAC.setEditable(true);
                   startTime.setEditable(true); 
                   startDate.setEditable(true); 
                   stopTime.setEditable(true); 
                   stopDate.setEditable(true); 
                   a.stop();
                   sB.setText("Start capture");
                   cB.arm();
                   lF.arm();
                   sT.arm();
                   cTB.arm();
               } //Need to test every potential input value before beginning capture
               else if(a.setDest(dMAC.getText()) && a.setSource(sMAC.getText())
                        && a.setStart(startDate.getText() + " " + startTime.getText()) &&
               a.setEnd(stopDate.getText() + " " +stopTime.getText()) && a.setPorts(port.getText())
                        && a.setMinLength(minLength.getText()) && a.setMaxLength(maxLength.getText()) && !((lF.isSelected() || sT.isSelected()) && !validFile))
               {  //need to check for consistency in 
               capturing = true;
               minLength.setEditable(false);
               maxLength.setEditable(false);
               port.setEditable(false);
               sMAC.setEditable(false);
               dMAC.setEditable(false);
               startTime.setEditable(false); 
               startDate.setEditable(false); 
               stopTime.setEditable(false); 
               stopDate.setEditable(false); 
               sB.setText("Halt capture");
               cB.disarm();
               lF.disarm();
               sT.disarm();
               cTB.disarm();
               System.out.print("lF: " + lF.isSelected());
               if(lF.isSelected()) {
                    a.read(true);}
               else{ a.read(false);}
               if(sT.isSelected()) {a.write(true);}
               else { a.write(false);}
               try {
               a.sniff();}
               catch(Exception e) { System.out.println(e.getMessage());}
               }
                else { //there was an error in the user's input filter settings.
                String check = "Errors found in: "; //all errors will be concatenated to this String
                if(!a.setDest(dMAC.getText())) {
                    check += "\n destination address";
                }
                if(!a.setSource(sMAC.getText())) {
                    check += "\n source address";
                }
                if(!a.setStart(startDate.getText() + " " + startTime.getText())) {
                    check += "\n start time/date";
                }
                if(!a.setEnd(stopDate.getText() + " " +stopTime.getText())) {
                    check += "\n stop time/date";
                }
                if(!a.setPorts(port.getText())) {
                    check += "\n ports";
                }
                if(!a.setMinLength(minLength.getText())) {
                    check += "\n min. length";
                }
                if(!a.setMaxLength(maxLength.getText())) {
                    check += "\n max. length";
                }
                if((lF.isSelected() || sT.isSelected())&&  !validFile) check += "\n database filename \n";
                doPopup("Illegal settings", check); //displays a window indicating the invalid filter settings
                }
            }
           else if(b == cTB) { a.clearTable();} //ends cTB
           else if(b == dB)
           {
               FileChooser chooser = new FileChooser();
               chooser.setTitle("Select a valid SQLite file");
               chooser.getExtensionFilters().add(new ExtensionFilter("SQLite database files", "*.db"));
               File toSave = chooser.showOpenDialog(stage);
               try{
                   if((toSave != null) && a.setDB(toSave.toString())) {
                   System.out.println("file set");
                   file.setText(toSave.toString());
                   validFile = true;
               }
               else {
                       doPopup("Invalid name", "Invalid file name. Please enter another.");
                       validFile = false;
                   }
               System.out.println(validFile);
               }
               catch(FileNotFoundException e){ doPopup("Invalid name", "Invalid file name. Please enter another.");}
           }
        }
    
  } //ends ActionHandler

     /**
     * @param args the command line arguments
     */  
    public static void main(String[] args) {
        try {
        launch(args);
        }
        catch(Exception e){ System.out.println(e.getMessage()); }
     
        }
           
    /*
    * Provides a popup window with a close button and the input message.
    *@param message the text to be displayed in this popup
    */
    public void doPopup(String title, String message) {
      Stage temp= new Stage();  
        temp.setTitle(title); //fix formatting
        Text dummy = new Text(message);
        dummy.setWrappingWidth(190);
        Button b = new Button("OK");
        b.setOnAction( e-> temp.close());
        /*GridPane gP = new GridPane();
        System.out.println(message);
        gP.setPadding(new Insets(0,10,0, 10));
        /*gP.add(dummy, 0, 0);
        gP.add(b, 0, 2);*/
        FlowPane fP = new FlowPane();
        fP.setPadding(new Insets(5,5,5,5));
        fP.getChildren().addAll(dummy, b);
        Scene options = new Scene(fP, 200, 200);
        options.getStylesheets().add(FrontEnd.class.getResource("frontEndStyles.css").toExternalForm());
        temp.setScene(options);
        temp.show();
    }  
}

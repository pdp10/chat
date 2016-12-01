//Server.java

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.io.*;

/**
 * @author Piero Dalle Pezze
 * @version 1.0
 * Graphical interface of a Server.*/ 
public class Server extends JFrame {


    private ServerLogic server = null;
    private boolean openChat = false;
    private String who = "ALL";

    private JMenuBar mb = new JMenuBar();
    private JMenu
	file = new JMenu("File"),
	function = new JMenu("Function"),
	info = new JMenu("Info");
    private JMenuItem
	connect = new JMenuItem("Connect"),
	logout = new JMenuItem("Logout"),
	save = new JMenuItem("Save"),
	quit = new JMenuItem("Quit"),
	showDB = new JMenuItem("Show Database"),
	about = new JMenuItem("About");
    private JButton 
	send = new JButton("Send"),
	kill = new JButton("Kill");
    private static JTextArea         
	chatArea = new JTextArea(25, 30),
	listArea = new JTextArea(15, 5);
    private JTextField 
	inputLine = new JTextField(35),
	removeLine = new JTextField(5);
    private JPanel 
	p1 = new JPanel(),
	p2 = new JPanel(),
	p3 = new JPanel();
    private JLabel 
	chatLabel = new JLabel("Chat room:"),
	onLineLabel = new JLabel("Users connected:"),
	writeLabel = new JLabel("Input:"),
	removeLabel = new JLabel("Remove nick:"),
	sendToLabel = new JLabel("Send message to:");
    private static JComboBox selUsr = new JComboBox();
    private SetAddress adrs = new SetAddress(null);
    private Database dbWin = new Database(null);
    private About news = new About(null);

    /**Constructor of a graphical server interface.*/
    public Server() {

	super("Server Chat");
	chatArea.setEditable(false);
	listArea.setEditable(false);
	inputLine.setEnabled(false);
	send.setEnabled(false);
	removeLine.setEnabled(false);
	kill.setEnabled(false);
	logout.setEnabled(false);
	save.setEnabled(false);
	if(server != null && server.isConnected()) { connect.setEnabled(false); }


	//Associate events with components
	inputLine.addActionListener(new ActionListener() {  
		public void actionPerformed(ActionEvent e) {
		    if(who.equals("ALL")) { server.sendData(e.getActionCommand()); }
		    else { server.sendToOne(e.getActionCommand(), who); }
		}
	    });
	send.addActionListener(new ActionListener() { 
		public void actionPerformed(ActionEvent e) {
		    if(who.equals("ALL")) { server.sendData(inputLine.getText()); }
		    else { server.sendToOne(inputLine.getText(), who); }
		}
	    });
	removeLine.addActionListener(new ActionListener() {  
		public void actionPerformed(ActionEvent e) { server.logout(e.getActionCommand()); }
	    });
	kill.addActionListener(new ActionListener() { 
		public void actionPerformed(ActionEvent e) { server.logout(removeLine.getText()); }
	    });
	connect.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    if(server == null || !server.isConnected()) { adrs.setVisible(true); }
		    if(server != null && server.isConnected()) {
			server.runServer();
			inputLine.setEnabled(true);
			send.setEnabled(true);
			removeLine.setEnabled(true);
			kill.setEnabled(true);
			logout.setEnabled(true);
			save.setEnabled(true);
			connect.setEnabled(false);
		    }
		}
	    });
	logout.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    if(server != null && server.isConnected()) { server.logout(); }
		    server = null;
		    openChat = false;
		    connect.setEnabled(true);
		    logout.setEnabled(false);
		    inputLine.setEnabled(false);
		    send.setEnabled(false);
		    removeLine.setEnabled(false);
		    kill.setEnabled(false);
		}
	    });
	save.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {      //(Eckel)
		    String chatSession = chatArea.getText();
		    PrintWriter out = null;
		    JFileChooser c = new JFileChooser();
		    int rVal = c.showSaveDialog(null);    //Saving window
		    if(rVal == JFileChooser.APPROVE_OPTION) {  
			File path = new File(c.getCurrentDirectory(), c.getSelectedFile().getName());
			Saving s = new Saving(null, saveFile(path, chatSession));
			s.setVisible(true);
		    }
		}
	    });
	quit.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    if(server !=null && server.isConnected()) { server.logout(); }
		    dispose();            //close the window
		}
	    });
	showDB.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) { 
		    dbWin.update();
		    dbWin.setVisible(true); 
		}
	    });
	about.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) { news.setVisible(true); }
	    });
	selUsr.addItemListener(new ItemListener() {
		public void itemStateChanged(ItemEvent e) { who = (String)e.getItem(); }
	    });
	addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) { System.exit(0); }
	    });

	setJMenuBar(mb);   
	mb.add(file);
	mb.add(function);
	mb.add(info);
	file.add(connect);
	file.add(logout);
	file.addSeparator();
	file.add(save);
	file.addSeparator();
	file.add(quit);
	function.add(showDB);
	info.add(about);


	Box chatBox = Box.createVerticalBox();       //panel 1  ..default FlowLayout
	chatBox.add(chatLabel);
	chatBox.add(new JScrollPane(chatArea));
	p1.add(chatBox);                        

	Box onLineBox = Box.createVerticalBox();          //panel 2
	Box removeBox = Box.createHorizontalBox();
	removeBox.add(removeLine);
	removeBox.add(kill);
	onLineBox.add(onLineLabel);
	onLineBox.add(new JScrollPane(listArea));
	onLineBox.add(Box.createVerticalStrut(15)); //separator
	onLineBox.add(removeLabel);
	onLineBox.add(removeBox);
	onLineBox.add(Box.createVerticalStrut(15)); //separator
	onLineBox.add(sendToLabel);
	onLineBox.add(selUsr);
	selUsr.addItem("ALL");
	p2.add(onLineBox);


	p3.add(writeLabel);                             //panel 3
	p3.add(inputLine);
	p3.add(send);


	Container c = getContentPane();                  //contains 3 panels
	c.add(p1, BorderLayout.WEST);	
	c.add(p2, BorderLayout.EAST);	
	c.add(p3, BorderLayout.SOUTH);


	kill.setToolTipText("Kill a nickname");//insert a description
	send.setToolTipText("Send a message");
	selUsr.setToolTipText("Select a nickname");


	chatLabel.setForeground(Color.white); //set the color
	onLineLabel.setForeground(Color.white);
	removeLabel.setForeground(Color.white);
	sendToLabel.setForeground(Color.white);
	writeLabel.setForeground(Color.white);
	p1.setBackground(new Color(30, 15, 102));
	p2.setBackground(new Color(30, 15, 102));
	p3.setBackground(new Color(30, 15, 102));
	c.setBackground(new Color(30, 15, 102));

	pack();
	setSize(600, 550);
	setVisible(true);
	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
  
    /**Append the string s in the output chatting area of the server.*/  
    public static void writeChat(String s) { chatArea.append("\n" + s); }
    /**Print the Vector of the list of the nicknames connected, in the output list area of the server.*/
    public static void writeList(Vector v) { 
	String s = "";
	Nick n;
	Object o;
	selUsr.removeAllItems();
	selUsr.addItem("ALL");
	for(int i = 0; i < v.size(); i++) {
	    o =  v.elementAt(i);
	    if(o instanceof Nick) {
		n = (Nick)o;
		selUsr.addItem(n.getName());
		s += "\n" + n.getName();
	    }
	}
	listArea.setText(s);

    }

    //window for saving
    private class Saving extends JDialog {
	private JLabel okL = new JLabel("File saved!");
	private JLabel errL = new JLabel("Error: file not saved!");
	private JButton close = new JButton("Close", new ImageIcon("images/Icon.gif") );
	//constructor
	public Saving(JFrame parent, boolean ok) {
	    super(parent, "Saving", true);

	    close.addActionListener(new ActionListener() { 
		    public void actionPerformed(ActionEvent e) { setVisible(false); }
		});
	    addWindowListener(new WindowAdapter() {
		    public void windowClosing(WindowEvent e) { setVisible(false); }
	    });

	    close.setVerticalTextPosition(JButton.BOTTOM);
	    close.setHorizontalTextPosition(JButton.CENTER);	    

	    getContentPane().setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
	    if(ok) { getContentPane().add(okL); }
	    else { getContentPane().add(errL); }
	    getContentPane().add(close);

	    close.setToolTipText("Close this window");

	    okL.setForeground(Color.white);
	    errL.setForeground(Color.white);
	    getContentPane().setBackground(new Color(175, 74, 236));

	    setSize(180, 120);
	    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
    };

    //Window for the IP parameter
    private class SetAddress extends JDialog {  
	private JLabel ipL = new JLabel("IP number:");
	private JTextField ip = new JTextField("127.0.0.1", 10);
	private JButton ok = new JButton("Ok", new ImageIcon("images/ModSet.gif") );

	public SetAddress(JFrame parent) {
	    super(parent, "Insert an IP number..", true);

	    ip.addActionListener(new ActionListener() {  
		    public void actionPerformed(ActionEvent e) {
			server = new ServerLogic(ip.getText());
			if(server != null) { connect.setEnabled(true); }
			setVisible(false);
		    }
		});
	    ok.addActionListener(new ActionListener() { 
		    public void actionPerformed(ActionEvent e) {
			server = new ServerLogic(ip.getText());
			if(server != null) { connect.setEnabled(true); }
			setVisible(false);
		    }
		});
	    addWindowListener(new WindowAdapter() {
		    public void windowClosing(WindowEvent e) { setVisible(false); }
		});
	    
	    ok.setVerticalTextPosition(JButton.BOTTOM);
	    ok.setHorizontalTextPosition(JButton.CENTER);


	    getContentPane().setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
	    getContentPane().add(ipL);
	    getContentPane().add(ip);
	    getContentPane().add(ok);

	    ok.setToolTipText("Open a connection..");

	    ipL.setForeground(new Color(203, 215, 235));
	    getContentPane().setBackground(new Color(77, 87, 138));

	    setSize(300, 120);
	    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
    };

    //Window for the inner database
    private class Database extends JDialog {
	private JLabel text = new JLabel("Database of all nicknames:");
	private JTextArea a = new JTextArea(20, 28);
	private JButton close = new JButton("Close", new ImageIcon("images/Torii.gif") );
	//constructor
	public Database(JFrame parent) {
	    super(parent, "Database..", true);
	    a.setEditable(false);

	    close.addActionListener(new ActionListener() { 
		    public void actionPerformed(ActionEvent e) { setVisible(false); }
		});
	    addWindowListener(new WindowAdapter() {
		    public void windowClosing(WindowEvent e) { setVisible(false); }
	    });

	    close.setVerticalTextPosition(JButton.BOTTOM);
	    close.setHorizontalTextPosition(JButton.CENTER);

	    getContentPane().setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
	    getContentPane().add(text);
	    getContentPane().add(new JScrollPane(a));
	    getContentPane().add(close);

	    close.setToolTipText("Close this window");
	    a.setToolTipText("Show the database of all user connected from the server start up. " +
			     "The informations are nickname, number of connection, age and sex of a nick.");

	    text.setForeground(Color.black);
	    getContentPane().setBackground(new Color(175, 74, 236));

	    setSize(423, 430);
	    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
	public void update() {
	    if(server != null) {
		a.setText("NICK" + "\t" + "CONN" + "\t" + "AGE" + "\t" + "SEX" + "\n");
		a.append(server.printDB()); 
	    }
	}
    };


    //Window for the info
    private class About extends JDialog {
	private JLabel text1 = new JLabel("Project of Programmazione 3");
	private JLabel text2 = new JLabel("University of Padova, Italy");
	private JLabel text3 = new JLabel("Author Piero Dalle Pezze.");
	private JButton close = new JButton("Close", new ImageIcon("images/Kakejiku.gif"));
	//constructor
	public About(JFrame parent) {
	    super(parent, "About", true);

	    close.addActionListener(new ActionListener() { 
		    public void actionPerformed(ActionEvent e) { setVisible(false); }
		});
	    addWindowListener(new WindowAdapter() {
		    public void windowClosing(WindowEvent e) { setVisible(false); }
	    });

	    close.setVerticalTextPosition(JButton.BOTTOM);
	    close.setHorizontalTextPosition(JButton.CENTER);

	    Box vBox = Box.createVerticalBox();
	    vBox.add(text1);
	    vBox.add(text2);	    
	    vBox.add(text3);

	    getContentPane().setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
	    getContentPane().add(vBox);
	    getContentPane().add(close);

	    close.setToolTipText("Close this window");

	    text1.setForeground(Color.black);
	    text2.setForeground(Color.black);
	    text3.setForeground(Color.black);
	    getContentPane().setBackground(new Color(175, 74, 236));

	    setSize(300, 110);
	    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
    };

    /**Save the text in the path.*/ 
    public boolean saveFile(File path, String text) {
	boolean b = true;
	String s;
	PrintWriter out = null;
	try { 
	    out = new PrintWriter( new BufferedWriter( new FileWriter(path) ));
	    out.println(new Date());
	    out.println(text);
	} catch(IOException e) { b = false; }
	finally { if(out != null) out.close(); }	    
	return b;
    }

    /**Create a server.*/
    public static void main(String[] args) {
	Server s = new Server();
    }

} //end class Server

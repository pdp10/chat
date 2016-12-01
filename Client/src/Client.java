//Client.java

//appletviewer Client.java
// <applet code="Client.class" width=650 height=550>
// </applet>

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Vector;

/**
 * @author Piero Dalle Pezze
 * @version 1.0
 * Graphical interface of a Client.*/ 
public class Client extends JApplet {

    private static JTextArea         
	chatArea = new JTextArea(25, 30),
	listArea = new JTextArea(15, 5);
    private JButton 
	send = new JButton("Send", new ImageIcon("images/Shodou.GIF") ),
	start = new JButton("Start", new ImageIcon("images/AfpClient.gif") ),
	exit = new JButton("Exit", new ImageIcon("images/SystemNetwork.gif") );
    private JTextField inputLine = new JTextField(35);
    private AskName ask = new AskName(null);
    private SetAddress adrs = new SetAddress(null);
    private JPanel 
	p1 = new JPanel(),
	p2 = new JPanel(),
	p3 = new JPanel();
    private JLabel 
	chatLabel = new JLabel("Chat room:"),
	onLineLabel = new JLabel("Users connected:"),
	writeLabel = new JLabel("Insert your message:");
    private ClientLogic client = null;


    /**Init method of the applet java.*/
    public void init() {
	chatArea.setEditable(false);
	listArea.setEditable(false);
	inputLine.setEnabled(false);
	send.setEnabled(false);
	if(client == null) { start.setEnabled(false); }
	adrs.setVisible(true);

	//Set the text position of an icon
	send.setVerticalTextPosition(JButton.BOTTOM);
	send.setHorizontalTextPosition(JButton.CENTER);
	start.setVerticalTextPosition(JButton.BOTTOM);
	start.setHorizontalTextPosition(JButton.CENTER);
	exit.setVerticalTextPosition(JButton.BOTTOM);
	exit.setHorizontalTextPosition(JButton.CENTER);

	//Associate events with components
	inputLine.addActionListener(new ActionListener() {  
		public void actionPerformed(ActionEvent e) {
		    if(client != null)  { client.sendData(e.getActionCommand()); }
		}
	    });
	start.addActionListener(new ActionListener() { 
		public void actionPerformed(ActionEvent e) { if(client != null) { ask.setVisible(true); } }
	    });
	send.addActionListener(new ActionListener() { 
		public void actionPerformed(ActionEvent e) { client.sendData(inputLine.getText()); }
	    });
	exit.addActionListener(new ActionListener() { 
		public void actionPerformed(ActionEvent e) {  
		    inputLine.setEnabled(false);
		    send.setEnabled(false);
		    start.setEnabled(false);
		    if(client != null && client.clientStatus()) { client.logout(); }  //first click
		    else { System.exit(0); } //second click
		}
	    });

	Box chatBox = Box.createVerticalBox();       //panel 1  ..default FlowLayout
	chatBox.add(chatLabel);
	chatBox.add(new JScrollPane(chatArea));
	p1.add(chatBox);                        

	Box onLineBox = Box.createVerticalBox();          //panel 2
	onLineBox.add(onLineLabel);
	onLineBox.add(new JScrollPane(listArea));
	p2.add(onLineBox);

	Box sendBox = Box.createVerticalBox();          //panel 3
	sendBox.add(writeLabel);
	sendBox.add(inputLine);
	p3.add(sendBox);
	p3.add(send);
	p3.add(start);
	p3.add(exit);

	Container c = getContentPane();                  //contains 3 panels
	c.add(p1, BorderLayout.WEST);	
	c.add(p2, BorderLayout.EAST);	
	c.add(p3, BorderLayout.SOUTH);

	send.setToolTipText("Send a message to all users");  //insert a description
	start.setToolTipText("Choose a nickname and start to chat");
	exit.setToolTipText("When you want to exit..");

	chatLabel.setForeground(Color.black); //set the color
	onLineLabel.setForeground(Color.black);
	writeLabel.setForeground(Color.black);
	p1.setBackground(new Color(7, 157, 184));
	p2.setBackground(new Color(7, 157, 184));
	p3.setBackground(new Color(7, 157, 184));
	c.setBackground(new Color(7, 157, 184));

    }
    /**Append the string s in the output chatting area of the client.*/
    public static void writeChat(String s) { chatArea.append("\n" + s); }

    /**Print the Vector of the list of the nicknames connected, in the output list area of the client.*/
    public static void writeList(Vector v) {
	String s = "";
	Nick n;
	Object o;
	for(int i = 0; i < v.size(); i++) {
	    o =  v.elementAt(i);
	    if(o instanceof Nick) {
		n = (Nick)o;
		s += "\n" + n.getName();
	    }
	}
	listArea.setText(s);

    }

    //Window for the IP parameter
    private class SetAddress extends JDialog {  
	private JLabel ipL = new JLabel("IP number:");
	private JTextField ip = new JTextField("127.0.0.1", 10);
	private JButton ok = new JButton("Ok", new ImageIcon("images/ModSet.gif") );

	public SetAddress(JFrame parent) {
	    super(parent, "Insert an IP number..", true);

	    ip.addActionListener(new ActionListener() {  
		    public void actionPerformed(ActionEvent e) {
			client = new ClientLogic(ip.getText());
			if(client != null) { start.setEnabled(true); }
			setVisible(false);
		    }
		});
	    ok.addActionListener(new ActionListener() { 
		    public void actionPerformed(ActionEvent e) {
			client = new ClientLogic(ip.getText());
			if(client != null) { start.setEnabled(true); }
			setVisible(false);
		    }
		});
	    addWindowListener(new WindowAdapter() {
		    public void windowClosing(WindowEvent e) { System.exit(0); }
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

    //Finestra di dialogo per l'inserimento del nickname
    private class AskName extends JDialog {  
	private JLabel l1 = new JLabel("Nickname: ");
	private JLabel l2 = new JLabel("Age: ");
	private JLabel l3 = new JLabel("Sex: ");
	private JTextField inputName = new JTextField(12);
	private JComboBox inputAge = new JComboBox();
	private ButtonGroup bg = new ButtonGroup();
	private JRadioButton 
	    m = new JRadioButton("m"),
	    f = new JRadioButton("f");
	private JButton ok = new JButton("Ok", new ImageIcon("images/Gunsen.gif") );
	private String age = "1960";
	private boolean sex = true;

	public AskName(JFrame parent) {

	    super(parent, "Nickname..", true);

	    inputAge.addItemListener(new ItemListener() {
		    public void itemStateChanged(ItemEvent e) {	age = (String)e.getItem(); }
		});
	    m.addItemListener(new ItemListener() {
		    public void itemStateChanged(ItemEvent e) { sex = true; }
		});
	    f.addItemListener(new ItemListener() {
		    public void itemStateChanged(ItemEvent e) { sex = false; }
		});
	    inputName.addActionListener(new ActionListener() { 
		    public void actionPerformed(ActionEvent e) {
			if(client != null) {
			    Nick n = new Nick(e.getActionCommand(), Integer.parseInt(age), sex);
			    if( client != null && client.setUser(n) ) {
				inputLine.setEnabled(true);
				send.setEnabled(true);
				start.setEnabled(false);
				exit.setEnabled(true);
				client.runClient();
			    }
			}
			setVisible(false);
		    }
		});
	    ok.addActionListener(new ActionListener() { 
		    public void actionPerformed(ActionEvent e) {
			if(client != null) {
			    Nick n = new Nick(inputName.getText(), Integer.parseInt(age), sex);
			    if( client != null && client.setUser(n) ) {
				inputLine.setEnabled(true);
				send.setEnabled(true);
				start.setEnabled(false);
				exit.setEnabled(true);
				client.runClient();
			    }
			}
			setVisible(false);
		    }
		});
	    addWindowListener(new WindowAdapter() {
		    public void windowClosing(WindowEvent e) { setVisible(false); }
		});
	    
	    ok.setVerticalTextPosition(JButton.BOTTOM);
	    ok.setHorizontalTextPosition(JButton.CENTER);

	    for(int i = 1940; i < 2005; i++) { inputAge.addItem(Integer.toString(i)); }

	    bg.add(m);
	    bg.add(f);

	    getContentPane().setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
	    getContentPane().add(l1);
	    getContentPane().add(inputName);
	    getContentPane().add(l2);
	    getContentPane().add(inputAge);
	    getContentPane().add(l3);
	    getContentPane().add(m);
	    getContentPane().add(f);
	    getContentPane().add(ok);

	    ok.setToolTipText("Close this window");

	    l1.setForeground(new Color(203, 215, 235));
	    l2.setForeground(new Color(203, 215, 235));
	    l3.setForeground(new Color(203, 215, 235));
	    getContentPane().setBackground(new Color(77, 87, 138));

	    setSize(250, 200);
	    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
    };

    /**Create a client.*/
    public static void main(String[] args) { 
	JApplet a = new Client();
	JFrame f = new JFrame("Chat Room");

	//close
	f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	f.getContentPane().add(a);
	f.setSize(650, 550);
	a.init();
	a.start();
	f.setVisible(true);

    }

} //end class Client

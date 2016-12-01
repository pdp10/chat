//ServerLogic.java

import java.io.*;
import java.net.*;
import java.util.Vector;


/**Logic of a server.*/
public class ServerLogic implements ClientServer { 

    private ServerSocket server = null;
    private int port = 8000;
    private InetAddress ip;
    private SyncString sharedMsg = new SyncString();
    //already Serializable and synchronized                //In JDK 1.5 
    private Vector sharedVec = new Vector(20, 20);    // Vector<Nick> sharedVec = new Vector<Nick>(20, 20);
    private Vector threadSocket = new Vector(20, 20); // ....     

    private boolean listening = false;
    private Thread listen = null;

    private boolean bc = true;  //sending message in broadcast
    private int online = 0;     //# threads --> user connected
    private int sentMsg = 0;    //count the users who have received the message in broadcast
    private int accepted = 0;   //number of accepted connection


    /**Open a connection to the localhost on the port 8000.*/
    public ServerLogic() { this("127.0.0.1"); }
    /**Create a logic implementation of a server at to the address _ip, on the port 8000.
     * If it cannot, shutdown.*/
    public ServerLogic(String _ip) { 
	try {
	    ip = InetAddress.getByName(_ip);
	    server = new ServerSocket(port, 10, ip);    //port, max length of the queue
	    listening = true;
	}
	catch(UnknownHostException e) { Server.writeChat("Impossible to set a connection. Unknown Host."); }
	catch(IOException e) { Server.writeChat("Server is not able to listen to this port."); 	}
	finally { 
	    if(!listening) {
		Server.writeChat("Session over.");
		if(server != null) 
		    try { server.close(); } 
		    catch(IOException e) { e.getMessage(); e.printStackTrace(); }
	    }
	}
    }

    /**{@inheritDoc}*/
    public boolean isConnected() { return listening; }

    /**Logout all connected client and close the server socket.*/
    public void logout() { 
	if(listening) { 
	    listening = false;   
	    sendData("logout");          
	    while(online > 0);       
	    if(server != null) try { 
		server.close();
		Server.writeChat("Server close... OK");
	    } 
	    catch(IOException e) { e.getMessage(); e.printStackTrace(); }
	}
    }

    /**Logout the user with nickname nick.*/
    public void logout(String nick) { sendToOne("logout", nick); }


    /**Find the nick n in the file with the list of the nick, nickname.dat 
     * @return -1 if there is no Nick with that name, otherwise return the index of the username inside the file.*/
    public synchronized int searchDB(Nick name) { 
	BufferedReader in = null;
	int line = -1, i = 0;
	boolean found = false;
	String s;
	try {
	    in = new BufferedReader(new FileReader("nickname.dat") );
	    while((s = in.readLine()) != null && !found) {
		if(s.equals(name.getName())) {
		    line = i;
		    found = true;
		} 
		i++;
	    }
	}
	catch(FileNotFoundException e) { System.out.println("File nickname.dat not found."); }
	catch(IOException e) { e.getMessage();  e.printStackTrace(); }
	finally {  
	    try { if(in != null) { in.close(); } }
	    catch(IOException e) { System.out.println("Errore nella chiusura di nickname.dat"); e.printStackTrace(); }
	}	    
	return line;
    }

    /**Insert a Nick in a database in the file nickDB.dat.*/ 
    public synchronized void insertDB(Nick n) {
	int point = searchDB(n);
	String s, s2 = new String();
	RandomAccessFile rf = null;
	PrintWriter out = null;
	BufferedReader in = null;
	try {
	    if(point == -1) {        //new user
		rf = new RandomAccessFile("nickDB.dat", "rw");
		rf.seek(rf.length());
		rf.writeInt(1);              // num connections
		rf.writeInt(n.getAge());     //age
		rf.writeBoolean(n.getSex()); //sex
		try {
		    in = new BufferedReader(new FileReader("nickname.dat") );
		    while((s = in.readLine()) != null) { s2 += s + "\n"; }
		} catch(FileNotFoundException e) {  } 
		out = new PrintWriter(new BufferedWriter(new FileWriter("nickname.dat") ));
		out.println(s2 + n.getName());
	    } else {
		rf = new RandomAccessFile("nickDB.dat", "r");
		rf.seek(point*(4+4+1));
		int conn = rf.readInt();
		rf.close();
		rf = new RandomAccessFile("nickDB.dat", "rw");
		rf.seek(point*(4+4+1));
		rf.writeInt(++conn);                       // incr n. connection
	    }
	} catch(IOException e) { e.getMessage(); e.printStackTrace(); }
	finally {
	    try { if(rf != null) { rf.close(); } }
	    catch(IOException e2) { System.out.println("Error when closing file nickDB.dat"); e2.printStackTrace(); }
	    try { if(in != null) { in.close(); } }
	    catch(IOException e2) { System.out.println("Error when closing file nickname.dat"); e2.printStackTrace(); }
	    if(out != null) { out.close(); }
	}
    }


    /**Insert all database in a string.*/
    public synchronized String printDB() {
	String s,s2 = new String();
	RandomAccessFile rf = null;
	BufferedReader in = null;
	int count = 0;
	try {
	    in = new BufferedReader(new FileReader("nickname.dat") );
	    rf = new RandomAccessFile("nickDB.dat", "r");
	    while( (s = in.readLine()) != null ) { 
		rf.seek(count*(4+4+1));
		s2 += s + "\t" + rf.readInt() + "\t" + rf.readInt() + "\t";
		if(rf.readBoolean()) { s2 += "m" + "\n"; }
		else { s2 += "f" + "\n"; }
		count++;
	    }
	} catch(FileNotFoundException e) { System.out.println("File nickname.dat still not exists because \n" +
							      "nobody has ever been in the chat room."); }
	catch(IOException e) { e.getMessage(); e.printStackTrace(); }
	finally {
	    try { if(rf != null) { rf.close(); } }
	    catch(IOException e2) { System.out.println("Error when closing file nickDB.dat"); e2.printStackTrace(); }
	    try { if(in != null) { in.close(); } }
	    catch(IOException e2) { System.out.println("Error when closing file nickname.dat"); e2.printStackTrace();}
	}
	return s2;
    }

    /**Send a private message by server to a client nick.*/
    public void sendToOne(String mex, String nick) { 
	privateMessage("SERVER to " + nick + "> " + mex, nick); 
    }

    /**Send a private message to a client nick. This function can be used for a possible private chatting.*/
    public void privateMessage(String mex, String nick) {
	new Prive(mex, nick);
	Server.writeChat(mex); 
    }

    /*Inner class for a private message.*/ 
    private class Prive extends Thread {
	private String mex, nick;
	public Prive(String m, String n) { 
	    mex = m; 
	    nick = n;
	    start();
	}
	public void run() {
	    boolean go = false;
	    int i = 0;
	    try {
		synchronized(threadSocket) {
		    while(i < threadSocket.size() && !go) {
			Object obj = threadSocket.elementAt(i);
			if(obj instanceof ServerThread) {
			    ServerThread s = (ServerThread)obj;
			    if(nick.equals(s.getNick().getName()) ) { 
				s.sendData(mex);
				go = true;
			    }
			}
			sleep(50);
			i++;
		    }
		}
	    } 
	    catch(InterruptedException e) { Server.writeChat("Thread early interrupted."); }
	}
    };

    /**{@inheritDoc}.
     * Send a message in broadcast to all users.*/
    public void sendData(String mex) { new Sender("SERVER> " + mex); }

    /*Inner class used for send a message ffrom server to all user. The broadcast process is the same of normal 
      sending of a client.*/
    private class Sender extends Thread {
	private String mex;
	private boolean send = false, show = false;
	/*Active the thread which sends the message.*/
	public Sender(String _mex) { mex = _mex; start(); } 
	/*Send the server message in broadcast to all users.*/
	public void run() {
	    int i = 0;
	    while(!send) {
		if(!show) {
		    synchronized(sharedMsg) {        //The message is shared
			if(bc) {
			    sharedMsg.put(mex);
			    sentMsg = 0;       
			    bc = false;  
			    show = true;
			}
		    }
		}
		try { sleep(250); }
		catch(InterruptedException e) { Server.writeChat("Thread early interrupted."); }
		if(show && !bc && sentMsg >= online) {
		    synchronized(sharedMsg) {        //Notify broadcasting is complete
			bc = true;
			sharedMsg.notifyAll();
			send = true;
		    }
		}
	    }
	    Server.writeChat(mex);
	}
    };  //end class Sender

    /**Active the server to listen. */
    public void runServer() {
	if(listening && listen == null) {
	    listen = new Thread() {
		    public void run() { 
			try {
			    Server.writeChat("Waiting for a connection..");
			    while (listening) {

				Socket connection = server.accept();
				Server.writeChat("\nNEW CONNECTION RECEIVED FROM: " + 
						 connection.getInetAddress().getHostName() );
				new ServerThread(connection);
				synchronized(server) {
				    accepted++;
				    while(accepted > 9) { server.wait(); }
				}
				sleep(1000);
			    }
			}
			catch(InterruptedException e) { Server.writeChat("Thread early interrupted."); }
			catch(SocketException e) { Server.writeChat("Connection fell down."); }
			catch(IOException e) { e.getMessage(); e.printStackTrace(); }
			finally {
			    listening = false; 
			    Server.writeChat("Session over.");
			    if(server != null) 
				try { server.close(); } 
				catch(IOException e) { e.getMessage(); e.printStackTrace(); }			
			}
		    }
		};
	    listen.start();
	}
    }

    /*Inner class for broadcast.*/
    private class ServerThread extends Thread implements ChatStatus { 
	
	private boolean sent = true;
	private boolean myMsg = false; 
	private boolean inChat = false;  //state of a client
	private String message = "";
	private Nick user = null;
	
	private Socket st = null;
	private ObjectInputStream inText = null;
	private ObjectOutputStream outText = null;
	private int allUser = threadSocket.size();
	
	/*Create a ServerThread object.*/
	public ServerThread(Socket soc) {
	    st = soc;
	    start();
	}

	/*Send a message s to the client which the socket is connected.*/
	public synchronized void sendData(String s) {
	    if(s.equals("SERVER> logout")) {
		inChat = false;
	    }
	    try {
		outText.writeObject(s);
		outText.flush();
	    }
	    catch(SocketException e) { Server.writeChat("Connection fell down. Client not connected."); }
	    catch(IOException e)  { Server.writeChat("Error in server output."); }
	}	
	
	/*Open the I/O stream.*/
	public void openIO() throws IOException {
	    outText = new ObjectOutputStream(st.getOutputStream());
	    outText.flush();
	    inText  = new ObjectInputStream(st.getInputStream()); 
	    Server.writeChat("Opened I/O streams..");
	}
	
	/*Return an object of type Nick.*/
	public Nick getNick() { return user; }
	
	/*Return true if client can chat.*/
	public boolean clientStatus() { return inChat; }
	
	/*Read a message from the client.*/
	public synchronized void readClient() throws ClassNotFoundException, IOException {
	    Object o = inText.readObject();
	    if(o instanceof String) {
		message = (String)o;
		Server.writeChat(message);
	    }
	}

	/*Return true if Nick client is inserted in the nickname list. 
	  False otherwise. Nick must be unique and the name different by "SERVER".*/ 
	public synchronized boolean insertToChat() throws IOException, ClassNotFoundException {
	    Object obj = inText.readObject();
	    if(obj instanceof Nick) {
		user = new Nick(obj);
		if(!sharedVec.contains(user) && !user.getName().equals("SERVER") && !user.getName().equals("")) {
		    sharedVec.addElement(user); 
		    inChat = true;
		    online++;
		    threadSocket.addElement(this);
		    Server.writeList(sharedVec);
		    sendData("SERVER> " + user + " enter the Chat room..");
		    Server.writeChat("User " + user + " enter the Chat room..");
		} else {
		    if(!user.getName().equals("SERVER") && !user.getName().equals("")) {
			Server.writeChat("A new user wants the nickname " + user + " already used.");
			outText.writeObject("Warning: you cannot use this nickname. It is already used.");
		    } else {
			Server.writeChat("A new user wants an illegal nickname.");
			outText.writeObject("Warning: you cannot use this nickname.");
		    } 
		    outText.flush();
		}
	    }
	    return inChat;
	}
	
	/*Send to the client the nickname list.*/
	public synchronized void sendVec() throws IOException {
	    outText.writeObject(new Vector(sharedVec));
	    outText.flush();
	}
	
	/*Run the socket associated to a client.*/
	public void run() {
	    try {
		openIO();
		
		while(!insertToChat());  //Insert the nick in the nick list
		insertDB(user);
		do {
		    try {
			
			if(!bc && !myMsg) {               //no broadcast and no my msg
			    synchronized(sharedMsg) {       //broadcast
				while(!bc) {
				    sendData(sharedMsg.get());
				    sentMsg++;            //count threads which have sent the message
				    sharedMsg.wait(); 
				}
			    }
			}
			if(allUser != online) {   //Send the nickname list only if number of users changes
			    allUser = online;
			    sendVec();
			}

			if(bc && !myMsg && sent) { 
			    st.setSoTimeout(50);
			    readClient();
			    sent = false;
			}
			synchronized(sharedMsg) {        //SHARES THE MESSAGE
			    if(bc) {
				sharedMsg.put(message);
				sentMsg = 1;       
				bc = false;         //other threads cannont enter in this block
				myMsg = true;
			    }
			}
			sleep(500);
			if(!bc && sentMsg >= online) {
			    synchronized(sharedMsg) {        //NOTIFY BROADCAST COMPLETE
				bc = true;
				sharedMsg.notifyAll();
				myMsg = false;
				sent = true;
			    }
			}
		    }
		    catch(SocketTimeoutException e) { }
		    catch(InterruptedException e) { Server.writeChat("Thread interrupted early."); }
		} while (!message.equals(user.getName() + "> logout"));
	    }
	    catch(ClassNotFoundException e) { Server.writeChat("Unknown object type received."); }
	    catch(SocketException e) { Server.writeChat("Connection fell down. Client not connected."); }
	    catch(EOFException e) { Server.writeChat("Client early closed the connection."); }
	    catch(StreamCorruptedException e) { Server.writeChat("Received corrupted data."); }
	    catch(IOException e) { e.getMessage(); e.printStackTrace(); }
	    finally {
		sharedVec.remove(user);
		threadSocket.remove(this);
		Server.writeList(sharedVec);
		if(outText != null && user != null)  try { 
		    if(inChat && message.equals(user + "> logout") ) {
			sendData("SERVER> logout");
		    }
		    outText.close();
		}
		catch(SocketException e) { Server.writeChat("Connection fell down."); }
		catch(IOException e) { e.getMessage(); e.printStackTrace(); } 
		if(inText  != null)  try { inText.close();  } 
		catch(IOException e) { e.getMessage(); e.printStackTrace(); }
		if(st != null)       try { st.close(); st = null; }     
		catch(IOException e) { e.getMessage(); e.printStackTrace(); }    
		if(user != null) { Server.writeChat(user + " exit the Chat room."); }
		if(--online == 0) { Server.writeChat("Nobody is inside the Chat room."); }
		synchronized(server) {
		    accepted--;
		    if(accepted <= 9) { server.notify(); }
		}
	    }
	}
    }; //end class ServerThread

}  //end class ServerLogic

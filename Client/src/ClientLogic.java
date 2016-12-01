//ClientLogic.java

import java.io.*;
import java.net.*;
import java.util.Vector;


/**Logic of a client.*/
public class ClientLogic implements ClientServer, ChatStatus {

    private Socket s = null;
    private ObjectInputStream inText = null;
    private ObjectOutputStream outText = null;
    private int port = 8000;
    private InetAddress ip;
    private String message = "";
    private Nick user = null;
    private Vector vec = null;
    private Thread exec = null;     //thread for runClient()
    private boolean inChat = false;   //state of a client
    
    /**Open a connection with a ServerSocket to the localhost on the port 8000. */
    public ClientLogic() { this("127.0.0.1"); } 

    /**Create a logic implementation of a client and connect it with a ServerSocket to the address _ip, 
       on the port 8000. The constructor connects the client with a server which is listening and open the I/O stream. 
       If it cannot connect, it shutdown.*/
    public ClientLogic(String _ip) {
	boolean connect = false;
	try {
	    ip = InetAddress.getByName(_ip);
	    Client.writeChat("Connecting..");
	    s = new Socket(ip, port);
	    Client.writeChat("Connected to: " + s.getInetAddress().getHostName());

	    inText  = new ObjectInputStream(s.getInputStream()); 
	    outText = new ObjectOutputStream(s.getOutputStream());
	    outText.flush();
	    connect = true;
	    Client.writeChat("Opened I/O streams..");
	    Client.writeChat("Press Start and choose a nickname..");
	}
	catch(ConnectException e) { Client.writeChat("Impossible to connect. Server not connected."); }
	catch(SocketException e) { Client.writeChat("Connection fell down."); }
	catch(UnknownHostException e) { Client.writeChat("Impossible to set a connection. Unknown Host."); }
	catch(EOFException e) { Client.writeChat("Server early closed the connection."); }
	catch(StreamCorruptedException e) { Client.writeChat("Received corrupted data."); }
	catch(IOException e) { e.getMessage(); e.printStackTrace(); }
	finally {
	    if(!connect) {
		if(outText != null) try { outText.close(); } 
		catch(IOException e) { e.getMessage(); e.printStackTrace(); }
		if(inText  != null)  try { inText.close(); }  
		catch(IOException e) { e.getMessage(); e.printStackTrace(); }
		if(s != null)  try { s.close(); s = null; }
		catch(IOException e) { e.getMessage(); e.printStackTrace(); }    
		Client.writeChat("Session over.");
	    }
	}
    }

    /**{@inheritDoc}.
     * Send the string s to the server.
     */
    public void sendData(String s) {
	try {
	    message = s;
	    outText.writeObject(user + "> " + message);
	    outText.flush();
	    Client.writeChat(user + "> " + message);
	}
	catch(SocketException e) { Client.writeChat("Connection fell down."); }
	catch(IOException e)  { Client.writeChat("Error in client output."); }
    }

    /**If the client socket is connected, send a request of logout.*/
    public void logout() { 
	if(isConnected()) { 
	    inChat = false;
	    sendData("logout");
	}
    }

    /**{@inheritDoc}*/
    public Nick getNick() { return user; }

    /**Send to the server the nickname. If the server accept this nickname, then client can chatting.
     * @return  true if user is accepted by the server, false otherwise.
     */
    public boolean setUser(Nick n) {
	if(isConnected()) { 
	    try {
		user = n;
		outText.writeObject(user);
		outText.flush();
		
		Object obj = inText.readObject();
		if(obj instanceof String) {			
		    message = (String)obj; 
		    Client.writeChat(message);
		    if(message.equals("SERVER> " + user + " enter the Chat room..")) 		
			inChat = true;
		}
	    } 
	    catch(IOException e)  { Client.writeChat("Cannot enter the Chat room."); }
	    catch(ClassNotFoundException e) { Client.writeChat("Unknown object type received."); }
	}
	return inChat;
    }

    /**{@inheritDoc}*/
    public boolean isConnected() { 
	if(s != null)
	    return s.isConnected(); 
	return false;
    }

    /**{@inheritDoc}*/
    public boolean clientStatus() { return inChat; }

    /*Read a string message ora a vector which is the nickname list from the server.*/
    private synchronized void readServer() throws ClassNotFoundException, IOException {
	Object o = inText.readObject();
	if(o instanceof String) {			
	    message = (String)o; 
	    Client.writeChat(message);
	} else if(o instanceof Vector) {
	    vec = (Vector)o;
	    Client.writeList(vec);
	}
    }

    /**Start client to receive or send messages.*/
    public void runClient() {
	if(exec == null && inChat) {
	    exec = new Thread() {
		    public void run() {
			try{
			    do {
				try {
				    readServer();
				    sleep(50);
				}
				catch(InterruptedException e) { Client.writeChat("Thread early interrupted."); }
			    } while(!message.equals("SERVER> logout") && 
				    !message.equals("SERVER to " + user + "> logout") );
			}
			catch(ClassNotFoundException e) { Client.writeChat("Unknown object type received"); }
			catch(SocketException e) { Client.writeChat("Connection fell down."); }
			catch(EOFException e) { Client.writeChat("Server early closed the connection."); }
			catch(IOException e) { e.getMessage(); e.printStackTrace(); }
			finally {  
			    if(outText != null)  try { 
				if(inChat && (message.equals("SERVER> logout") ||   
				   message.equals("SERVER to " + user + "> logout")) )
				    sendData("logout");
				outText.close(); 
			    }
			    catch(SocketException e) { Client.writeChat("Connection fell down."); }
			    catch(IOException e) { e.getMessage(); e.printStackTrace(); }
			    if(inText  != null)  try { inText.close(); }  
			    catch(IOException e) { e.getMessage(); e.printStackTrace(); }
			    if(s != null)  try { s.close(); s = null; inChat = false; }
			    catch(IOException e) { e.getMessage(); e.printStackTrace(); }    
			    Client.writeChat("Session over.");
			}
		    }
		};
	    exec.start();
	}
    }


}//end class ClientLogic

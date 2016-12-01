//ClientServer.java

/**Interface of server/client common functions.*/
public interface ClientServer {

    /**Test if the socket is connected to an other socket.
     * @return  true if the socket is connected, false otherwise.
     */
    boolean isConnected();
    /**Disable the client/server.*/
    void logout();
    /**Send a message to a connected socket.*/
    void sendData(String s);

} //end interface ClientServer

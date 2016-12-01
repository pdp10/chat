//ChatContext.java


/**Interface of the state of a chatting session.*/
public interface ChatStatus {

    /**Indicate the state of connection of a client.
     * @return  true if the client is connected. false, otherwise. 
     */
    boolean clientStatus();
    /**Return the object Nick used by the client. Nick is an abstraction of a nickname.*/
    Nick getNick();

} //end interface ChatStatus

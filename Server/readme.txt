

Chat



Description:
	This program implements a multithreading chat. A chat is a place to write messages (chatting) in a 
	common desk. All users can read all messages. If a message is not visible to all user, but only to 
	one-self, we call this discussion mode 'private chat' (this option is not available for this application). 


What is a chat:
     A usual chat, like this, is a client-server based system, where some client applications set a connection 
     to the same address and port of the server application. In the picture, there is a client-server model:

     On details, a normal client-server communication is maked up from these phases: 

     1. The server starts and is listening for a client. 
     2. A client asks to the listening server a request of connection. 
     3. The server sends to the client its response. 
     4. If all is ok, the client is connected to the server. 
     5. The client makes some activities. 
     6. When the client must go, a logout request is send to the server. 
     7. The server closes the connection from its part. 
     8. The client closes the connection from its part. 

     It is relevant to underline that a connection can fall down in a lot of circumstances. Infact for example 
     a client can close the connection aborting its application before closing the connection with the server, 
     a server can turn out a client for some reasons, the net is not reliable etc.

     In the particular chat model, a client sends a message to the server and this one provides to send in 
     broadcasting the received message to the other clients. Because more of a client can send a different 
     message to the server in the same time and this fact is not deterministic, the server must support the 
     concurrency using a thread model. 

										Piero Dalle Pezze

 

Testing ...
Server: ./server.sh <client_limit> <ip> <port>

Client: ./client.sh <client_name> <ip> <port>

File : Server.java
	creating new thread for each client join by extending Thread Class
	Hashmap which pairs chatid with username
	chatrooms are described by chatarray intialized with 0
	threads array is shared among threads and acceses outputstream and inputstream.
	inputstream receives input from client outputstream and processes commands asked by the client.
	ex: 1. list chatrooms
	    2. reply file tcp
	    3. reply file udp
    	    4. list users etc...

 File : Client.java
	It uses 2 threads one for reading input and 1 for getting input from server
	1 thread reads input from user and send it to server for processing from outputstream
	another thread receives the processed output from inputstream and prints to System.out
	


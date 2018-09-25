import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

public class Server
{
  private static ServerSocket ss = null;
  private static Socket cs = null;
  private static  int maxClientsCount ;
  private static clientThread[] threads ;
  private static HashMap<Integer,String> hashmap = new HashMap<Integer,String>();
  private static int[] chatArray ;
  private static int chatid=0;
  private static DatagramSocket dsoc = null;
  public static int port;
  public static String host;

  public static void main(String args[]) {
    if (args.length < 3) {
      System.out.println("Usage: java filename <max clients> <host> <portNumber>\n");
    } else {
      maxClientsCount = Integer.valueOf(args[0]).intValue();
      threads = new clientThread[maxClientsCount];
      chatArray = new int[1+maxClientsCount];
      host = args[1];
      port = Integer.valueOf(args[2]).intValue();
    }
    try{
      ss = new ServerSocket(port);
      dsoc = new DatagramSocket(port);
    }
    catch (IOException e) {
      System.out.println(e);
    }
    while(true){
      try{
        cs = ss.accept();
        int i=0;
        for(i=0;i<maxClientsCount;i++){
            if(threads[i]==null){
              (threads[i]=new clientThread(cs,threads,i,hashmap,chatid,chatArray,dsoc)).start();
              break;
            }
        }
        if(i==maxClientsCount){
          PrintStream os = new PrintStream(cs.getOutputStream());
          os.println("Server too busy. Try later.");
          os.close();
          cs.close();
        }
      }catch (IOException e) {
        System.out.println(e);
      }
    }
  }
}


class clientThread extends Thread {

  private String clientName = null;
  private BufferedReader is = null;
  private PrintStream os = null;
  private Socket clientSocket = null;
  private DatagramSocket dsoc = null;
  private final clientThread[] threads;
  private int maxClientsCount;
  private int tid;
  private int chatid;
  HashMap<Integer,String> hashmap = new HashMap<Integer,String>();
  private static int[] chatArray ;
  public clientThread(Socket clientSocket, clientThread[] threads,int tid,HashMap<Integer,String> hashmap,int chatid,int[] chatArray,DatagramSocket dsoc) {
    this.clientSocket = clientSocket;
    this.threads = threads;
    maxClientsCount = threads.length;
    this.tid=tid;
    this.hashmap = hashmap;
    this.chatid=chatid;
    this.chatArray=chatArray;
    this.dsoc=dsoc;
  }

  public void run() {
    int maxClientsCount = this.maxClientsCount;
    clientThread[] threads = this.threads;
    int[] chatArray = this.chatArray;
    HashMap<Integer,String> hashmap = this.hashmap;
    DatagramSocket dsoc = this.dsoc;
    try {
      is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
      os = new PrintStream(clientSocket.getOutputStream());
      String name;


      synchronized (this) {

        /*while(true){
          int re=0;
          name = is.readLine().trim();
          for (int i = 0; i < maxClientsCount; i++) {
            if (threads[i] != null && threads[i] != this && threads[i].clientName.equals(name)) {
              os.println("Error: User with this name already exists");
              os.println("Re-enter your name.");
              re=1;
              break;
            }
          }
          if(re==0){
            os.println("Welcome " + name
                + ".\nTo leave enter quit in a new line.");
                break;
          }
        }
        */
        name = is.readLine().trim();
        for (int i = 0; i < maxClientsCount; i++) {
          if (threads[i] != null && threads[i] == this) {
            clientName = name;
            break;
          }
        }
        os.println("Welcome " + name
            + ".\nTo leave enter quit in a new line.");

      }

      while (true) {
        os.println(">>");
        String line = is.readLine();
        if (line.startsWith("quit")) {
          break;
        }
        String[] words = line.trim().split("\\s+");
          synchronized (this) {
            if(this.chatid!=0 && words[0].equals("reply")){
            for (int i = 0; i < maxClientsCount; i++) {
              if (threads[i] != null && threads[i].clientName != null && threads[i].chatid==this.chatid && threads[i]!=this ) {
                Pattern p = Pattern.compile("\"([^\"]*)\"");
                Matcher m = p.matcher(line);
                while (m.find()) {
                threads[i].os.println( name + ": " + m.group(1));
                }
              }
            }
            }
            else if(this.chatid==0 && words[0].equals("reply")){
              os.println("Error: You can't communicate outside chatroom");
              continue;
            }
          }
          if(words[0].equals("list") && words.length==2 && words[1].equals("chatrooms") ){
            synchronized (this) {
              if(this.chatid==0){
            if(hashmap.isEmpty()){
              os.println("No chatrooms available");
            }
            for(Object objname:hashmap.keySet()) {
                os.println(hashmap.get(objname));
            }
          }
          else
          os.println("Error: Sorry you are in a chatroom");
          }
          }
          else if(words[0].equals("create") && words.length==3 && words[1].equals("chatroom") ){
            synchronized (this) {
              if(this.chatid==0 && hashmap.containsValue(words[2])==false){
            int i;
            for(i=0;i<chatArray.length;i++){
              if(chatArray[i]==0){
                chatArray[i]=1;
                break;
              }
            }
            this.chatid = i+1;
            hashmap.put(this.chatid,words[2]);
          }
          else if(this.chatid==0 && hashmap.containsValue(words[2]))
          os.println("Error: chatroom already exists with this name");
          else
          os.println("Error: Sorry you are in a chatroom");
        }
        }
        else if(words[0].equals("join") && words.length==2){
          synchronized (this) {
            if(this.chatid==0 && hashmap.containsValue(words[1])){
          for(Object objname:hashmap.keySet()) {
              if(words[1].equals(hashmap.get(objname))){
                    this.chatid = (int)objname;
              }
          }
            for (int i = 0; i < maxClientsCount; i++) {
              if (threads[i] != null && threads[i].clientName != null && threads[i].chatid == this.chatid && this.chatid!=0 && threads[i]!=this) {
                    threads[i].os.println("A new user "+name+" entered chat room");
              }
            }
          }
          else if(this.chatid==0 && hashmap.containsValue(words[1])==false)
          os.println("Error: chatroom doesnt exist with this name");
          else
          os.println("Error: Sorry you are in a chatroom");
        }
        }
        else if(words[0].equals("add") && words.length==2){
          synchronized (this) {
            if(this.chatid!=0){
            int c=0;
            for (int i = 0; i < maxClientsCount; i++) {
              if (threads[i] != null && threads[i].clientName.equals(words[1])  && this.chatid!=0 && this.chatid!=threads[i].chatid && threads[i].chatid==0 ) {
                      threads[i].chatid = this.chatid;
                      c++;
                      threads[i].os.println("you are added to chatgroup.");

                      break;
              }
            }
            if(c==0)
            os.println("Error: No user exists with this name");
          }
          else
          os.println("Error: Sorry you are not in a chatroom");
          }
        }
        else if(words[0].equals("leave") && words.length==1){
          synchronized (this) {
            if(this.chatid!=0){
            int c=0;
            for (int i = 0; i < maxClientsCount; i++) {
              if (threads[i] != null && threads[i].clientName != null && threads[i].chatid == this.chatid && this.chatid!=0 && threads[i]!=this) {
                  threads[i].os.println("The user "+name+" is leaving chatroom");
                  c++;
              }
            }
            if(c==0 && this.chatid!=0){
              chatArray[this.chatid-1]=0;
              String retval = (String)hashmap.remove(this.chatid);
            }
          this.chatid=0;
         }
         else
         os.println("Error: You are not in a chatroom to leave");
       }
        }
        else if(words[0].equals("list") && words[1].equals("users") && words.length==2){
          synchronized (this) {
            if(this.chatid!=0){

            for (int i = 0; i < maxClientsCount; i++) {
              if (threads[i] != null && threads[i].clientName != null && threads[i].chatid == this.chatid && this.chatid!=0) {
                    os.println("<" + threads[i].clientName + "> ");
              }
            }
          }
          else
          os.println("Error: You are not in a chatroom");

        }
        }
        else if(words[0].equals("transfer") && words[2].equals("tcp") && words.length==3){
          String data=null;
          synchronized (this) {
                for (int i = 0; i < maxClientsCount; i++) {
                  if (threads[i] != null && threads[i].clientName != null && threads[i].chatid == this.chatid && this.chatid!=0 && this!=threads[i]) {
                    threads[i].os.println("Receiving "+words[1]+" tcp " + this.clientName);
                  }
                }

	          while ((data = is.readLine()) != null) {
                  if(data.equals("complete"))
                  break;
                  for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] != null && threads[i].clientName != null && threads[i].chatid == this.chatid && this.chatid!=0 && this!=threads[i]) {
                      threads[i].os.println(data);
                    }
                  }
                }

                  for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] != null && threads[i].clientName != null && threads[i].chatid == this.chatid && this.chatid!=0 && this!=threads[i]) {
                      threads[i].os.println("Received");
                    }
                  }
            }

           os.println("File sent");
        }
        else if(words[0].equals("transfer") && words[2].equals("udp") && words.length==3){
          synchronized (this) {
                for (int i = 0; i < maxClientsCount; i++) {
                  if (threads[i] != null && threads[i].clientName != null && threads[i].chatid == this.chatid && this.chatid!=0 && this!=threads[i]) {
                    threads[i].os.println("Receiving "+words[1]+" tcp "+this.clientName);
                  }
                }

	          while (true) {
              byte[] recData = new byte[65536];
              DatagramPacket recPacket = new DatagramPacket(recData, recData.length);
              dsoc.receive(recPacket);
              String linedata = new String(recPacket.getData());
              if(linedata.trim().equals("complete")){
                  break;
                }
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] != null && threads[i].clientName != null && threads[i].chatid == this.chatid && this.chatid!=0 && this!=threads[i]) {
                      threads[i].os.println(linedata);
                    }
                  }
              }

                  for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] != null && threads[i].clientName != null && threads[i].chatid == this.chatid && this.chatid!=0 && this!=threads[i]) {
                      threads[i].os.println("Received");
                    }
                  }
            }

           os.println("File sent");
        }
        else if(!line.equals("") && !words[0].equals("reply")){
        os.println("Error: Invalid Command");
      }
      }

      os.println("Bye " + name + "\n");

      synchronized (this) {
        for (int i = 0; i < maxClientsCount; i++) {
          if (threads[i] == this) {
            threads[i] = null;
          }
        }
      }
      is.close();
      os.close();
      clientSocket.close();
    } catch (IOException e) {
      System.err.println(e);
    }
  }
}

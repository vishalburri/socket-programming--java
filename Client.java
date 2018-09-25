import java.io.*;
import java.net.*;
import java.util.*;
public class Client implements Runnable {

  private static Socket clientSocket = null;
  private static DatagramSocket cdsoc = null;
  private static PrintStream os = null;
  private static BufferedReader is = null;
  private static BufferedReader inputLine = null;
  private static boolean closed = false;
  public static int portNumber;
  public static String host;
  public static void main(String[] args) {


    int port;
    if (args.length < 3) {
      System.out.println("Usage: java filename <name>  <host> <portNumber>\n");
    } else {
      host = args[1];
      portNumber = Integer.valueOf(args[2]).intValue();
    }

    try {
      clientSocket = new Socket(host, portNumber);
      cdsoc = new DatagramSocket();
      inputLine = new BufferedReader(new InputStreamReader(System.in));
      os = new PrintStream(clientSocket.getOutputStream());
      is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

    } catch (UnknownHostException e) {
      System.err.println("Don't know about host " + host);
    } catch (IOException e) {
      System.err.println(e);
      System.err.println("Couldn't get I/O for the connection to the host "
          + host);
    }


    if (clientSocket != null && os != null && is != null && cdsoc!=null ) {
      try {
        String response;
        new Thread(new Client()).start();
        os.println(args[0]);
        while (!closed) {
          response = inputLine.readLine().trim();
          String[] words = response.trim().split("\\s+");
          if(words[0].equals("reply")  && words.length==3 && words[2].equals("tcp")){
            try{
            FileInputStream fis = new FileInputStream(words[1]);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
  	        String data = null;
            System.out.println("Sending "+words[1]);
            os.println("transfer "+words[1]+" tcp");
            while ((data = br.readLine()) != null) {
              os.println(data);
            }
            os.println("complete");
            br.close();
            }
            catch(IOException e){
              System.err.println("Error: File not found");
            }
          }
          else if(words[0].equals("reply")  && words.length==3 && words[2].equals("udp") ){
            try{
            FileInputStream fis = new FileInputStream(words[1]);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
  	        String data = null;
            System.out.println("Sending "+words[1]);
            os.println("transfer "+words[1]+" udp");
            while ((data = br.readLine()) != null) {
              byte[] sendData = data.getBytes();
              DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,InetAddress.getByName(host), portNumber);
              cdsoc.send(sendPacket);
            }
            data="complete";
            byte[] sendData = data.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,InetAddress.getByName(host), portNumber);
            cdsoc.send(sendPacket);
            br.close();
            }
            catch(IOException e){
              System.err.println("Error: File not found");
            }
          }
          else{
          os.println(response);
        }
        }
        os.close();
        is.close();
        cdsoc.close();
        clientSocket.close();
      } catch (IOException e) {
        System.err.println(e);
      }
    }
  }


  public void run() {
    String responseLine;
    try {
      while ((responseLine = is.readLine()) != null) {
        if(responseLine.indexOf("tcp")!=-1){
          String[] words = responseLine.trim().split("\\s+");
          System.out.println("Receiving "+words[1]+" from "+words[3]);
          File fout = new File(System.getProperty("user.dir") +"/"+ words[1]);
	        FileOutputStream fos = new FileOutputStream(fout);
          BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
          String response;
          while((response = is.readLine()) != null){
            if(response.equals("Received")){
            System.out.println(response+" "+words[1]+" from "+words[3]);
            break;
            }
            bw.write(response+"\r\n");
          }
          bw.close();
        }


        else if (responseLine.indexOf(">>") != -1)
        System.out.print(responseLine);
        else if (responseLine.indexOf("Bye") != -1)
        break;
        else if(responseLine.indexOf("Error")!=-1)
        System.err.println(responseLine);
        else{
        System.out.println(responseLine);
        }
        }

      closed = true;
    } catch (IOException e) {
      System.err.println("IOException:  " + e);
    }
  }
}

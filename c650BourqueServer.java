/*
 * For COSC650 - Networking final project.
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.List;
import java.net.URL;
import java.net.UnknownHostException;

/**
 *
 * @author Matthew Bourque, Dave Borncamp, Dennis Hayden
 *
 * This is the main Java class for the server for the main project
 */
public class c650BourqueServer {


    public static void main (String[] args) {

        ServerDriver driver = new ServerDriver();
        try {
            driver.drive();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}



/**
 *
 * This class serves as the main driver
 */
class ServerDriver{

    // Define class attributes
    ServerSocket server; // server socket
    Socket connection; // connection to client
    InputStream fromSocket; // to retreive data from socket
    OutputStream toSocket; // to send data to socket
    int datagramport = 13671;
    int timeout;


    /**
     * The main driver
     * @throws UnknownHostException
     * @throws SocketException
     */
    public void drive() throws SocketException, UnknownHostException{

        // Ask the user for a time out.  This time out is used for the client, NOT the connection to the web server
        Scanner reader = new Scanner(System.in);
        System.out.println("Enter a timeout:");
        timeout = reader.nextInt();

        // Establish server connection to localhost port 80, wait for a response.  A time out of 10 seconds is used
        // for browser connection
        connectToServer("127.0.0.1", 80, 10000);

        // As soon as there is a request, send an http 404 to browser
        send404();

        // Retreive the browser's get response
        String browserResponse = getResponse();

        // Close the connection
        closeConnection();

        // Read in ip.txt
        List<String> ipList = getIPList("ip.txt");

        // Print the brower's get request
        System.out.println("Response from browser:\n\n " + browserResponse + "\n");

        // Send requests to external IP addresses in the ipList
        int n = ipList.size();
        List<Thread> threadList = new ArrayList<Thread>();
        for (int i = 1; i <= n; i++) {
            String ipAddress = ipList.get(i - 1);
            String ipRequest = browserResponse.replaceAll("localhost:80", ipAddress).replaceAll("keep-alive", "close") + "\r\n";
            IPThread thread = new IPThread(ipAddress, ipRequest, i);
            threadList.add(thread);
            thread.start();
        }

        // Join the threads
        try {
            for (Thread t : threadList)
            {
              t.join();
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(c650BourqueServer.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Read in the appropriate file and print its contents
        for (int i = 1; i <= n; i++) {
            System.out.println(ipList.get(i - 1));
            System.out.println("DONE");
            String ipFile = readIPFile(i); // one indexed
            System.out.println(ipFile + "\n");
        }

            // Send the file to the client over UDP port 13671
            // so something
        for (int j = 1; j <= n; j++) {
            String ipFile = readIPFile(j);
            sendFileToClient(ipFile, datagramport, timeout, ipList.get(j - 1));
        }
    }


    /**
     * Establish a connection to the server @ ip/port and wait for a response
     *
     * @param ip - The IP address to connect to
     * @param port - the port to connect to
     * @param timeout - The timeout for connecting to the server
     */
    private void connectToServer(String ip, int port, int timeout) {
        System.out.println("\nSetting up Connection");

        try{
            this.server = new ServerSocket();
            SocketAddress socketAddress = new InetSocketAddress(ip,  port);
            this.server.bind(socketAddress);
            this.server.setSoTimeout(timeout);
            this.connection = server.accept();
            System.out.println("Connection received from: " + this.connection.getInetAddress().getHostName());
        } catch (SocketTimeoutException ex){
            Logger.getLogger(c650BourqueServer.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(0);
        } catch (IOException ex){
            Logger.getLogger(c650BourqueServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    /**
     * Send an http 404 error to the localhost
     */
    private void send404(){

        String response = "HTTP/1.1 404 Not Found\r\n" +
            "Content-Length: 22\r\n" +
            "Content-Type: text/html\r\n\r\n" +
            "<h1>404 Not Found</h1>";
        sendRequest(response, this.connection);
    }


    /**
     * Return the response from the client
     * @return - resonseString
     */
    private String getResponse(){

        try{
            this.fromSocket = this.connection.getInputStream();
        } catch (IOException ex){
            Logger.getLogger(c650BourqueServer.class.getName()).log(Level.SEVERE, null, ex);
        }

        StringBuilder sb = new StringBuilder();
        String responseString = null;

        try(BufferedReader br = new BufferedReader(new InputStreamReader(this.fromSocket))) {
            String line = br.readLine();

            while (line.length() != 0) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            responseString = sb.toString();

        } catch (IOException ex) {
            Logger.getLogger(c650BourqueServer.class.getName()).log(Level.SEVERE, null, ex);
        }

        return responseString;
    }


    /**
     * Closes the connection to the socket and server
     */
    private void closeConnection(){

        try{
            this.connection.close();
            this.server.close();
        } catch (IOException ex){
            Logger.getLogger(c650BourqueServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    /**
     * Return the input as a string
     * @param ipFile - The text file that holds the IP addresses
     * @return ipList - A list of IP addresses
     */
    private List<String> getIPList(String ipFile){

        // Initialize list
        List<String> ipList = new ArrayList<String>();

        // Read in the file
        try{
            @SuppressWarnings("resource")
            BufferedReader br = new BufferedReader(new FileReader(ipFile));

            // Read each line
            String line = br.readLine();
            while (line != null) {
                ipList.add(line);
                line = br.readLine();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(c650BourqueServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(c650BourqueServer.class.getName()).log(Level.SEVERE, null, ex);
        }

        return ipList;
    }


    /**
     * Send the given request to the socket
     *
     * @param request - The request to send to the socket
     */
    private void sendRequest(String request, Socket connection){

       System.out.println("Sending Request:\n\n " + request + "\n");

        try{
            OutputStream toSocket = connection.getOutputStream();
            PrintWriter pw = new PrintWriter(toSocket, false);
            pw.print(request);
            pw.flush();
            // ObjectOutputStream toSocket = new ObjectOutputStream(connection.getOutputStream());
            // toSocket.write(request.getBytes());
            // toSocket.flush();
        } catch (IOException ex){
            Logger.getLogger(c650BourqueServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    /**
     * Send the given request to the socket
     *
     * @param fileNumber - The number of the file to read
     * @return ipFile - The contents of the file
     */
    private String readIPFile(int fileNumber) {


        String ipFile = "";

        // Read in the file
        try{
            @SuppressWarnings("resource")
            BufferedReader br = new BufferedReader(new FileReader("c650Bourquefile"+Integer.toString(fileNumber)+".txt"));

            // Read each line
            String line = br.readLine();
            while (line != null) {
                ipFile = ipFile + line + "\r\n";
                line = br.readLine();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(c650BourqueServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(c650BourqueServer.class.getName()).log(Level.SEVERE, null, ex);
        }

        return ipFile;
    }


    /**
     * Send the given file to the client
     *
     * @param fileNumber - The number of the file to read
     * @return ipFile - The contents of the file
     */
    private void sendFileToClient(String ipFile, int port, int timeout, String ipAddress) throws UnknownHostException {

        // Initialize variables
        Integer numberofpacks = 0;
        int remainder = 0;
        String marker = "*";
        String packetinfo ="";

        // Build information packet
        Integer filesize = ipFile.length();
        String strfilesize = filesize.toString();
        numberofpacks = ipFile.length()/1024;
        remainder = ipFile.length() % 1024;
        if (remainder > 0)
        {
            numberofpacks++;
        }
        String numberofpacksstr = numberofpacks.toString();
        InetAddress address = InetAddress.getByName("127.0.0.1");
        packetinfo = ipAddress + marker + numberofpacksstr + marker + strfilesize;

        // Place information into UDP packet
        byte[] buf = new byte[ipAddress.getBytes().length + numberofpacksstr.getBytes().length + strfilesize.getBytes().length + (marker.getBytes().length * 2)];
        System.arraycopy(packetinfo.getBytes(), 0, buf, 0, packetinfo.getBytes().length);
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);

        // Initialize UDP socket
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(timeout);
        } catch (SocketException ex) {
            Logger.getLogger(c650BourqueServer.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Send the information packet
        try {
            socket.send(packet);
        } catch (IOException ex) {
            Logger.getLogger(c650BourqueServer.class.getName()).log(Level.SEVERE, null, ex);
        }

        int sizeholder = filesize;
        byte[] fileholder = ipFile.getBytes();
        // create a new data packet
        buf = new byte[1024];
        packet = new DatagramPacket(buf, buf.length, address, port);
        for(int i = 0; i < numberofpacks; i++)
        {
            // create buffer for packet less then 1024
            if (sizeholder < buf.length)
            {
                buf = new byte[sizeholder];
                System.arraycopy(fileholder, i * buf.length, buf, 0, sizeholder);
                packet = new DatagramPacket(buf, buf.length, address, port);
                try {
                    socket.send(packet);
                } catch (IOException ex) {
                    Logger.getLogger(c650BourqueServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else // create buffer for full packet (size of 1024)
            {
                if (sizeholder < 0)  // should throw an IOException... do not let continue
                {
                    System.out.println("PROBLEM SENDING MORE THAN FILES SIZE");
                }
                buf = new byte[1024];
                System.arraycopy(fileholder, i * buf.length, buf, 0, buf.length);
                packet = new DatagramPacket(buf, buf.length, address, port);

                try {
                    socket.send(packet);
                } catch (IOException ex) {
                    Logger.getLogger(c650BourqueServer.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
            sizeholder = sizeholder - buf.length;
        }

        // Try to receive the ack
        try {
            buf = new byte[ipAddress.getBytes().length];
            packet = new DatagramPacket(buf, buf.length);
            socket = new DatagramSocket(port+1);
            socket.setSoTimeout(timeout);
            socket.receive(packet);
            socket.close();
            String Strpacket = new String(packet.getData(), 0, ipAddress.getBytes().length);
            if(Strpacket.equals(ipAddress))
            {
                System.out.println("ACK: " + ipAddress);
            }
            else
            {
                System.out.println("FAIL: " + ipAddress);
            }

        } catch (IOException ex) {
            System.out.println("FAIL: " + ipAddress);
        }
    }
}



class IPThread extends Thread {

    public String ipAddress;
    public String ipRequest;
    public int count;


    /**
     * Constructor method, set class attributes
     */
    public IPThread(String ipAddress, String ipRequest, int count) {

        this.ipAddress = ipAddress;
        this.ipRequest = ipRequest;
        this.count = count;
    }


    /**
     * Override the Thread.start() method
     */
    public void run() {

        // Send the request to the external IP address
        String ipResponse = sendRequestExternalIP(this.ipAddress, this.ipRequest);
        System.out.println(ipResponse);

        // Save the response to a text file
        saveReponse(ipResponse);
    }

    /**
     * Send the request to the given IP address and return the response as a string
     *
     * @param ipAddress - The IP address that the get request gets sent to.
     * @param ipReqest - The get request to send to the IP address (ipAddress).
     */
    public String sendRequestExternalIP(String ipAddress, String ipRequest){

        String responseString = "";

        try {
            // Open the connection
            Socket s = new Socket(ipAddress, 80);

            // Send the request
            OutputStream output = s.getOutputStream();
            PrintWriter pw = new PrintWriter(output, false);
            pw.print(ipRequest);
            pw.flush();
            System.out.println("Sending: ");
            System.out.println(ipRequest);

            // Get the response
            InputStream in = s.getInputStream();
            InputStreamReader isr = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(isr);
            int c;
            while ((c = br.read()) != -1) {
                responseString = responseString + (char) c;
            }

        } catch (IOException ex){
            Logger.getLogger(c650BourqueServer.class.getName()).log(Level.SEVERE, null, ex);
        }

        return responseString;
    }

    /**
     * Save the message to a text file with the correct name. There should not be an issue of multiple threads writing
     * to the same file because of the different names.
     *
     * @param message - The whole HTML to be saved to the text files. This is basically the whole get request
     */
    private void saveReponse(String message){
        String name = "c650Bourquefile"+Integer.toString(count)+".txt";
        try{
            File outFile = new File(name);

            // If the file exists. delete it
            if (outFile.exists()){
                outFile.delete();
            }

            // Now make the new file for writing
            outFile.createNewFile();

            FileWriter writing = new FileWriter(outFile.getAbsoluteFile());
            BufferedWriter writer = new BufferedWriter(writing);

            writer.write(message);
            writer.close();

        } catch (IOException ex) {
            Logger.getLogger(IPThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

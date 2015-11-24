/*
 * For COSC650 - Networking final project.
 */

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Dave Borncamp
 *
 * This is the main Java class for the server for the main project
 */
public class c650GroupnameServer {


    public static void main (String[] args){

        ServerDriver driver = new ServerDriver();
        driver.drive();
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
    ObjectOutputStream toSocket; // to send data to socket


    /**
     * The main driver
     */
    public void drive(){

        // Ask the user for a time out
        Scanner reader = new Scanner(System.in);
        System.out.println("Enter a timeout: ");
        int timeout = reader.nextInt();

        // Establish server connection to localhost port 80, wait for a request
        connectToServer("127.0.0.1", 1025, 20000);

        // As soon as there is a request, send an http 404 to browser
        send404();

        // Print the browser's get request
        String request = getRequestFromClient();

        // Read in ip.txt
        List<String> ipList = getIPList("ip.txt");
    }


    /**
     * Establish a connection to the server @ ip/port
     *
     * @param ip - The IP address to connect to
     * @param port - the port to connect to
     * @param timeout - The timeout for connecting to the server
     */
    public void connectToServer(String ip, int port, int timeout){
        System.out.println("\nSetting up Connection");

        try{
            server = new ServerSocket();
            SocketAddress socketAddress = new InetSocketAddress(ip,  port);
            this.server.bind(socketAddress);
            this.server.setSoTimeout(timeout);
            this.connection = server.accept();
            System.out.println("Connection received from: " + this.connection.getInetAddress().getHostName());
        } catch (SocketTimeoutException ex){
            Logger.getLogger(c650GroupnameServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex){
            Logger.getLogger(c650GroupnameServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    /**
     * Send an http 404 error to the localhost
     */
    public void send404(){

        String header = "HTTP/1.0 404 Not Found\r\n" +
            "Content-Type: text/html; charset=UTF-8\r\n" +
            "X-Content-Type-Options: nosniff\r\n" +
            "Date: Sun, 22 Nov 2015 20:54:18 GMT\r\n" +
            "Server: sffe\r\n" +
            "Content-Length: 1564\r\n" +
            "X-XSS-Protection: 1; mode=block\r\n";
        String html = "<h1>404 Not Found</h1>";
        String response = header + html;
        System.out.println("\nSending reponse:\n" + response + "\n\n");

        try{
            this.toSocket = new ObjectOutputStream(this.connection.getOutputStream());
            toSocket.write(response.getBytes());
            toSocket.flush();
        } catch (IOException ex){
            Logger.getLogger(c650GroupnameServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    /**
     * Wait for a request from the client, then return the request
     * @return - requestString
     */
    public String getRequestFromClient(){

        try{
            this.fromSocket = this.connection.getInputStream();
        } catch (IOException ex){
            Logger.getLogger(c650GroupnameServer.class.getName()).log(Level.SEVERE, null, ex);
        }

        String requestString = parseRequest(this.fromSocket);
        System.out.println("\nRequest from client:\n\n " + requestString + "\n");

        return requestString;
    }


    /**
     * Return the input as a string
     * @param request - The request to parse
     * @return requestString - The request as a string
     */
    private String parseRequest(InputStream request){

        StringBuilder sb = new StringBuilder();
        String requestString = null;

        try(BufferedReader br = new BufferedReader(new InputStreamReader(request))) {
            String line = br.readLine();

            while (line.length() != 0) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            requestString = sb.toString();

        } catch (IOException ex) {
            Logger.getLogger(c650GroupnameServer.class.getName()).log(Level.SEVERE, null, ex);
        }

        return requestString;
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
            BufferedReader br = new BufferedReader(new FileReader(ipFile));

            // Read each line
            String line = br.readLine();
            while (line != null) {
                ipList.add(line);
                line = br.readLine();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(c650GroupnameServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(c650GroupnameServer.class.getName()).log(Level.SEVERE, null, ex);
        }

        return ipList;
    }
}

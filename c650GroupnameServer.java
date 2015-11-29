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
import java.io.OutputStream;
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
import java.net.URL;

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
        System.out.println("Enter a timeout:");
        int timeout = reader.nextInt();

        // Establish server connection to localhost port 80, wait for a response
        connectToServer("127.0.0.1", 1025, 10000);

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

        // Lets try to do this with just one ip address for now
        String ipAddress = ipList.get(0);

        // This doesn't work
        // String ipRequest = browserResponse.replaceAll("localhost:1025", ipAddress);

        // This one works
        // String ipRequest =
        //     "GET /:80 HTTP/1.0\r\n" +
        //     "Accept: text/plain, text/html, text/*\r\n" +
        //     "\r\n";

        // This doesn't work
        // String ipRequest =
        //     "GET /:80 HTTP/1.1\r\n" +
        //     "Accept: text/plain, text/html, text/*\r\n" +
        //     "\r\n";

        // This works
        // String ipRequest =
        //     "GET / HTTP/1.0\r\n" +
        //     "Accept: text/plain, text/html, text/*\r\n" +
        //     "\r\n";

        // This doesn't work
        // String ipRequest =
        //     "GET / HTTP/1.1\r\n" +
        //     "Accept: text/plain, text/html, text/*\r\n" +
        //     "\r\n";

        // This works
        // String ipRequest =
        //     "GET /:80 HTTP/1.0\r\n" +
        //     "Host: 136.160.171.110\r\n" +
        //     "Connection: keep-alive\r\n" +
        //     "Cache-Control: max-age=0\r\n" +
        //     "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8\r\n" +
        //     "Upgrade-Insecure-Requests: 1\r\n" +
        //     "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36\r\n" +
        //     "Accept-Encoding: gzip, deflate, sdch\r\n" +
        //     "Accept-Language: en-US,en;q=0.8\r\n" +
        //     "\r\n";

        // This works
        // String ipRequest =
        //     "GET / HTTP/1.1\r\n" +
        //     "Host: 136.160.171.110\r\n" +
        //     "Connection: keep-alive\r\n" +
        //     "Cache-Control: max-age=0\r\n" +
        //     "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8\r\n" +
        //     "Upgrade-Insecure-Requests: 1\r\n" +
        //     "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36\r\n" +
        //     "Accept-Encoding: gzip, deflate, sdch\r\n" +
        //     "Accept-Language: en-US,en;q=0.8\r\n" +
        //     "\r\n";

        // This works, but might be a problem because the instructions say to "only replace the IP address"
        String ipRequest = browserResponse.replaceAll("localhost:1025", ipAddress) + "\r\n";

        try {
             Socket s = new Socket("136.160.171.110", 80);
             OutputStream theOutput = s.getOutputStream();
             PrintWriter pw = new PrintWriter(theOutput, false);
             pw.print(ipRequest);
             pw.flush();

             System.out.println("Sending: ");
             System.out.println(ipRequest);

             InputStream in = s.getInputStream();
             InputStreamReader isr = new InputStreamReader(in);
             BufferedReader br = new BufferedReader(isr);
             int c;
             while ((c = br.read()) != -1) {
               System.out.print((char) c);
             }

        } catch (IOException ex){
            Logger.getLogger(c650GroupnameServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    /**
     * Establish a connection to the server @ ip/port and wait for a response
     *
     * @param ip - The IP address to connect to
     * @param port - the port to connect to
     * @param timeout - The timeout for connecting to the server
     */
    private void connectToServer(String ip, int port, int timeout){
        System.out.println("\nSetting up Connection");

        try{
            this.server = new ServerSocket();
            SocketAddress socketAddress = new InetSocketAddress(ip,  port);
            this.server.bind(socketAddress);
            this.server.setSoTimeout(timeout);
            this.connection = server.accept();
            System.out.println("Connection received from: " + this.connection.getInetAddress().getHostName());
        } catch (SocketTimeoutException ex){
            Logger.getLogger(c650GroupnameServer.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(0);
        } catch (IOException ex){
            Logger.getLogger(c650GroupnameServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    /**
     * Send an http 404 error to the localhost
     */
    private void send404(){

        String header = "HTTP/1.0 404 Not Found\r\n" +
            "Content-Type: text/html; charset=UTF-8\r\n" +
            "X-Content-Type-Options: nosniff\r\n" +
            "Date: Sun, 22 Nov 2015 20:54:18 GMT\r\n" +
            "Server: sffe\r\n" +
            "Content-Length: 1564\r\n" +
            "X-XSS-Protection: 1; mode=block\r\n";
        String html = "<h1>404 Not Found</h1>";
        String response = header + html;
        sendRequest(response);
    }


    /**
     * Return the response from the client
     * @return - resonseString
     */
    private String getResponse(){

        try{
            this.fromSocket = this.connection.getInputStream();
        } catch (IOException ex){
            Logger.getLogger(c650GroupnameServer.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(c650GroupnameServer.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(c650GroupnameServer.class.getName()).log(Level.SEVERE, null, ex);
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


    /**
     * Send the given request to the socket
     *
     * @param request - The request to send to the socket
     */
    private void sendRequest(String request){

       System.out.println("Sending Request: \n\n " + request + "\n");

        try{
            this.toSocket = new ObjectOutputStream(this.connection.getOutputStream());
            toSocket.write(request.getBytes());
            toSocket.flush();
        } catch (IOException ex){
            Logger.getLogger(c650GroupnameServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Dave Borncamp
 *
 * This is the main Java class for the server for the main project
 */
public class c650GroupnameServer {

    private static ServerSocket server; // server socket
    private static Socket connection; // connection to client
    private static ObjectOutputStream output; // output stream to client
    private static InputStream input;  // input stream from client

    public static void main (String[] args){

        // Ask the user for a time out
        Scanner reader = new Scanner(System.in);
        System.out.println("Enter a timeout: ");
        int timeout = reader.nextInt();

        // Initialize the browser server
        System.out.println("Initializing Server");
        try {
            ServerSocket server = new ServerSocket();
        } catch( IOException ioException ) {
            ioException.printStackTrace();
        }

        // Establish server connection to localhost port 80, wait for a request
        Socket browserConnection = connectToServer(server, "127.0.0.1", 80, 10000);

        // Parse the request from the browser
        String request = getRequestFromClient(browserConnection);

        // Send 404 message
        try {
        output = new ObjectOutputStream(browserConnection.getOutputStream());
        output.flush(); //flush anything that is already in there
        String response = "HTTP/1.0 404 Not Found\r\n" +
            "Content-Length: 90\r\n" +
            "Content-Type: text/html\r\n\r\n" +
            "<h1>404 Not Found</h1>";
        System.out.println(response);
        output.write(response.getBytes());
        output.flush();
        } catch (IOException ex){
            Logger.getLogger(c650GroupnameServer.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Now read in ip.txt
        String everything = null;
        StringBuilder sb = new StringBuilder();
        try(BufferedReader br = new BufferedReader(new FileReader("ip.txt"))) {
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            everything = sb.toString();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(c650GroupnameServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(c650GroupnameServer.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println(everything);
        System.out.println(sb);

    }

    /**
     * Establish a connection to the server @ ip/port
     * 
     * @param server The server to connect to
     * @param ip - The IP address to connect to
     * @param port - the port to connect to
     * @param timeout - The timeout for connecting to the server
     */
    public static Socket connectToServer(ServerSocket server, String ip, int port, int timeout){
        System.out.println("Setting up Connection");
        SocketAddress socketAddress = new InetSocketAddress(ip,  port);


        try{
            server.bind(socketAddress);
            server.setSoTimeout(timeout);
            while(true){
                connection = server.accept();
                System.out.println("Connection  received from: " + connection.getInetAddress().getHostName());
            }
        } catch (SocketTimeoutException ex){
            Logger.getLogger(c650GroupnameServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex){
            Logger.getLogger(c650GroupnameServer.class.getName()).log(Level.SEVERE, null, ex);
        }
            return connection;
    }

    /**
     * Wait for a request from the client, then return the request
     * @param connection - The connection socket that was made from connect to server.
     * @return - requestString
     */
    public static String getRequestFromClient(Socket connection){

        InputStream request = null;
        try {
            request = connection.getInputStream();
        } catch (IOException ex) {
            Logger.getLogger(c650GroupnameServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        String requestString = parseRequest(request); // Parse request into a string
        System.out.println("Request from client: " + requestString);

        return requestString;
    }

    /**
     * Return the input as a string
     */
    private static String parseRequest(InputStream request){
        StringBuilder sb = new StringBuilder();
        String everything = null;

        try(BufferedReader br = new BufferedReader(new InputStreamReader(request))) {
            String line = br.readLine();

            while (line != null) {
                System.out.println("Line: "+line);
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
                everything = sb.toString();
        } catch (IOException ex) {
            Logger.getLogger(c650GroupnameServer.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("\n Before everything");
        System.out.println(everything);
        return everything;
    }
}



// /**
//  * Class to try to handle the interface with  the Browser. It should return an error 404 when it connects.
//  * The localConnection method will throw a connection exception if something does not connect properly.
//  *
//  * @author Dave
//  */
// class LocalBrowser{


//     /**
//      * This function is supposed to wait until it receives a connection from the browser. The socket class will block until
//      * the connection is made. Once the connection is made it will return an err404.
//      *
//      *  @param timeout - The timeout of the server for the browser to connect to the localhost. This should probably
//      * be set to something very large.
//      *
//      * @return success - returns 1 on success. anything else was a failure.
//      */
//     public void localConnection(int timeout) throws ConnectionException{
//         try{

//                     // Respond with Error 404
//                     System.out.println("Sending: ");

//                     // correct response from here http://stackoverflow.com/questions/12020131/how-to-set-format-http-headers-by-simply-writing-strings-to-a-socket
//                     // also example here: cs.fit.edu/~mmahoney/cse3103/java/Webserver.java
//                     // more on html codes here: http://www.jmarshall.com/easy/http/
//                     //sending a 404 response was much harder then I expected...
//                     //This seems to be malformed
//                     String header = "HTTP/1.1 404 Not Found\r\n" +
//                         "Content-Length: 90\r\n" +
//                         "Content-Type: text/html\r\n\r\n";
// //                        "<h1>404 Not Found</h1>";
// //                    String response = "HTTP/1.0 404 Not Found\r\n"+
// //          "Content-type: text/html\n"+
// //          "Content-Length: 90\r\n\r\n"+
// //          "<html><head></head><body> not found</body></html>\n";

//                     String html = "<html><head> \n <title>404 Not Found</title> \n </head><body> \n <h1>Not Found</h1>"+
//                     "<p>The requested URL"+ip+" was not found on this server.</p> \n <hr><address>"+ip+" Port "+
//                            Integer.toString(localPort)+"</address></body></html>";

//                     String response = header + html;
//                     System.out.println(response);
//                     output.write("HTTP/1.1 200 OK\n\nHello, world!".getBytes()); //just a test, sending everything is ok
//                     //output.write(response.getBytes());
//                     output.flush();
//                     System.out.println("");
//                     break;

//                 } catch (SocketTimeoutException s)  {
//                     System.out.println("Socket timed out!");
//                     throw new ConnectionException("Socket timed out to Browser.");
//                 } catch (Exception e){
//                     throw new ConnectionException(e.fillInStackTrace());
//                 }
// /*                finally{
//                     //closeConnection();
//                 }*/
//             }
//             //response.sendError(HttpServletResponse.SC_NOT_FOUND);
//         }catch( IOException ioException ) {
//             ioException.printStackTrace();
//         } catch( ConnectionException conEx){

//         }
//     }

//     /**
//      * Close the connection variables for this class.
//      */
//     private void closeConnection() {
//         System.out.println("Closing Connection");
//         try {
//             //output.flush();
//             output.close(); // close output stream
//             input.close(); // close input stream
//             connection.close(); // close socket
// //        } catch ( IOException ioException ) {
// //            ioException.printStackTrace();
// //        } catch (NullPointerException nullException){
// //            nullException.printStackTrace();
//         } catch (Exception e){
//             System.out.println("Exception caught on closing:");
//             System.out.println(e.toString());
//         }
//    }

// }

    /**
     * Custom exception for a connection issueDid not fully implement all methods for parent class....
    */
    class ConnectionException extends Exception {

        /**
         * Default constructor
         */
        public ConnectionException(){
        }

        /**
         * Second constructor that passes the message to the parent class.
         * @param message - String that caused the exception.
         */
        public ConnectionException(String message){
            super(message);
        }

        /**
         * Third Constructor that will pass a Throwable to the parent class.
         * @param cause
         */
        public ConnectionException(Throwable cause){
            super(cause);
        }
    }

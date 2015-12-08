import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Matthew Bourque, Dave Borncamp, Dennis Hayden
 *
 * This is the client class for the COSC650 final project.
 */
public class c650BourqueClient {

    /**
     * @throws SocketException
     */
    public static void main(String[] args) throws SocketException {

         ClientDriver driver = new ClientDriver();
         driver.drive();
    }
}



 class ClientDriver {

    DatagramSocket datagram;

    /**
     * The main driver
     */
    public void drive() throws SocketException{

        // Get timeout
        Integer timeout = null;
        Scanner reader = new Scanner(System.in);
        System.out.println("Enter a timeout:");
        timeout = reader.nextInt();

        for(;;){
            try {
                openDatagramsocket(13671, timeout);
            } catch (Exception ex) {
                System.out.println("Program terminated");
                break;
            }
        }
    }


    /**
     * Establish a connection to the server @ ip/port and wait for a response
     *
     * @param port - the port to connect to
     * @param timeout - The timeout for connecting to the server
     * @throws IOException
     */
    private void openDatagramsocket(int port, int timeout) throws IOException {

        // Initialize variables
        String received = null;
        String numberofpackets = null;
        String filesize = null;
        int q = 0;
        InetAddress serveraddress = null;
        String file = new String();

        // Initialize socket
        System.out.println("\nSetting up Connection");
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException ex) {
            Logger.getLogger(c650BourqueClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Initialize empty array to hold data
        byte[] data = new byte[1024];
        DatagramPacket packet = new DatagramPacket(data, data.length);

        // Receive data
        try {
            socket.setSoTimeout(timeout);
            socket.receive(packet);

            // Initilize input streams
            ByteArrayInputStream byteIn = new ByteArrayInputStream (packet.getData (), 0, packet.getLength ());
            DataInputStream dataIn = new DataInputStream (byteIn);

            // See if there are any data in the input stream
            q = dataIn.available();

            // Read the packet into the data array
            data = new byte[packet.getLength()];
            System.arraycopy(packet.getData(), packet.getOffset(), data, 0, packet.getLength());

        } catch (IOException ex) {
            throw ex;
        }

        // Intrepret the data reveived
        try{
            // Convert the data into a string
            String Strpacket = new String(packet.getData(), 0, q);

            // Split the data by the '*' delimiter used by the server
            String[] split = Strpacket.split("\\*");

            // Check to see if the information packet was received
            if (split.length == 3)
            {
                received = split[0];  // IP address
                numberofpackets = split[1];  // numberofpacksstr
                filesize = split[2];  // file size
            }
            else
            {
                throw new Exception();
            }

            // Initiallize some more variables
            int MAXfilesize = Integer.parseInt(filesize.trim()); //the entire length of received file
            int datarev = 0;
            // file = "";
            int buflen = 1024;

            // Receive all the data
            for(int x = 0; x < Integer.parseInt(numberofpackets.trim()); x++ ){
                // reset data array
                // If there is data left put in array, if it's less then 1024, make the array that size
                // otherwise keep it at 1024
                if (MAXfilesize < buflen)
                {
                    data = new byte[MAXfilesize];
                } else {
                    data = new byte[buflen];
                }

                // Create packet to receive the data
                packet = new DatagramPacket(data, data.length);
                // Try to receive the data
                try {
                    socket.setSoTimeout(timeout);
                    socket.receive(packet);

                    // Build the file string with the data
                    file = file + (new String(packet.getData(), 0, packet.getLength()));
                    // How much data has been received
                    datarev += packet.getLength();
                } catch (IOException ex) {
                    Logger.getLogger(c650BourqueClient.class.getName()).log(Level.SEVERE, null, ex);
                }

                // Should be 0 by the end of this loop. If not, not all packets were received
                MAXfilesize -= buflen;
            }

            if (MAXfilesize > 0 ){
                System.out.println(MAXfilesize);
                // throw an exception saying the entire file was not received.
            }
        } catch (Exception ex) {
            System.out.println("FAIL: " + received);
        }

        // Try to send back the ack by sending an IP address
        // Try to find the server address
        try {
            serveraddress = InetAddress.getByName("127.0.0.1");
        } catch (UnknownHostException e1) {
            Logger.getLogger(c650BourqueClient.class.getName()).log(Level.SEVERE, null, e1);
        }

        // Print information
        System.out.println("OK");
        System.out.println("IP Address: " + received);
        System.out.println("File Size: " + filesize);
        System.out.println("Number of Packets: " + numberofpackets);
        System.out.println("File Contents:");
        System.out.println(file);

        // redefine data again, to be the IP address
        data = new byte[received.getBytes().length];
        data = received.getBytes();
        packet = new DatagramPacket(data, data.length, serveraddress, port+1);
        try {
            socket.close();
            socket = new DatagramSocket();
            socket.send(packet);
        } catch (IOException ex) {
            Logger.getLogger(c650BourqueClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
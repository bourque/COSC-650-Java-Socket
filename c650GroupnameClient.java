import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Matthew Bourque, Dave Borncamp
 * 
 * This is the client class for the COSC650 final project.
 */
public class c650GroupnameClient {


    /**
     * @param args the command line arguments
     * @throws SocketException 
     */
    public static void main(String[] args) throws SocketException {
    	 ClientDriver driver = new ClientDriver();
         driver.drive();
     }

        // TODO code application logic here
    }
 
    
    
    

    
    
 class ClientDriver
 {   
	 
		DatagramSocket datagram;
    /**
     * The main driver
     */
    public void drive() throws SocketException{
    	
    	Scanner reader = new Scanner(System.in);
        System.out.println("Enter a timeout:");
        int timeout = reader.nextInt();
        
    	
    	
    	openDatagramsocket(13671, timeout);
    	
    	}
    /**
     * Establish a connection to the server @ ip/port and wait for a response
     *
     * @param ip - The IP address to connect to
     * @param port - the port to connect to
     * @param timeout - The timeout for connecting to the server
     * @throws IOException 
     */
    private void openDatagramsocket(int port, int timeout) {
    	int q = 0;
        System.out.println("\nSetting up Connection");

		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket(13671);
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		byte[] data = new byte[1024];
		DatagramPacket packet = new DatagramPacket(data, data.length);

		//        byte[] buf = new byte[1024];
//        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        try {
			socket.receive(packet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        ByteArrayInputStream byteIn = new ByteArrayInputStream (packet.getData (), 0, packet.getLength ());
        DataInputStream dataIn = new DataInputStream (byteIn);
        System.out.println(dataIn.toString());
		try {
			q = dataIn.available();
			System.out.println(dataIn.available());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        
        data = new byte[packet.getLength()];
     
        System.arraycopy(packet.getData(), packet.getOffset(), data, 0, packet.getLength());
        
        
        String Strpacket = new String(packet.getData(), 0, q);
        String[] split = Strpacket.split("\\*");
        for (int i = 0; i < split.length; i++ )
        {
        	System.out.println(split[i]);
        }
        String received = null;
        String numberofpackets = null;
        String filesize = null;
        if (split.length == 3)
        {
        	received = split[0];
        	numberofpackets = split[1];
        	filesize = split[2];
        }
        else{
        	
        	System.out.println("ERROR");
        }
        int MAXfilesize = Integer.parseInt(filesize.trim());
        int datarev = 0;
        String file = "";
        for(int x = 0; x < Integer.parseInt(numberofpackets.trim()); x++ )
        {
        	data = new byte[1024];
    		packet = new DatagramPacket(data, data.length);
        	try {
				socket.receive(packet);
				file = file +  new String(packet.getData(), 0, packet.getLength());
				datarev += packet.getLength();
				System.out.println(file);
				System.out.println(x);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
        	
        }
        System.out.println(file);





            

    }
}



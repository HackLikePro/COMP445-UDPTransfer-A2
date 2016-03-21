package UDPServer;
import java.io.*; 
import java.net.*;

public class UDPServer {

	final public static int packetSize = 1024;
	final public static int timeout = 500;
	static DatagramSocket serverSocket;
	
	     	     
	
	
	public static void main(String args[]) throws IOException
	{
		// Server side socket set up
		int serverPort = 29723;
		serverSocket = new DatagramSocket(29723);
		
		// Client request packet value
		InetAddress clientIP;
		int clientPort;
		String requestedFileName;
		
		// Run loop for accepting incoming packet
		while(true)
		{		
			// Setup a place to store the in coming packet
			byte[] receivedData = new byte[packetSize];
			DatagramPacket receivedPacket = new DatagramPacket(receivedData, packetSize);
			serverSocket.receive(receivedPacket);
			 
			// Get client's information from in coming packet 
			clientIP = receivedPacket.getAddress();
			clientPort = receivedPacket.getPort();
			
			// Debug:client source value
			System.out.println("Recived request from Client IP:" + clientIP +" Port:"+clientPort);
			
			// Get client requested file name from in coming packet 
			requestedFileName = new String(receivedPacket.getData());		
			String[] filename = requestedFileName.split(" ");
			if (filename[0].equals("GET") )
			{
				requestedFileName = filename[1];	
				System.out.println("Client request to download File: " + requestedFileName);
				stopWait(clientIP, clientPort, requestedFileName);
			}
		}
		
	}




	private static void stopWait(InetAddress clientIP, int clientPort, String requestedFileName) {
		// TODO Auto-generated method stub
		boolean fileExsit = findFile(requestedFileName);
		if (fileExsit)
		{
			byte[][] filePacket = splitFile (requestedFileName);
		}
		else
		{
			//Debug:didnot find the file
			System.out.println("Debug:didnot find the file");
		}
	}




	private static byte[][] splitFile(String requestedFileName) {
		// TODO Auto-generated method stub
		return null;
	}




	private static boolean findFile(String requestedFileName) {
		// TODO Auto-generated method stub
		return false;
	}
}

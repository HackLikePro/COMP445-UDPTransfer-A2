package UDPServer;
import java.io.*; 
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import javafx.scene.shape.Path;

public class UDPServer {

	final public static int packetSize = 1024;
	final public static int headSize = 126;
	final public static int timeout = 500000;
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




	private static void stopWait(InetAddress clientIP, int clientPort, String requestedFileName) throws IOException {
		// TODO Auto-generated method stub
		boolean fileExsit = findFile(requestedFileName);
		if (fileExsit)
		{
			byte[] fileData = splitFile (requestedFileName);
			DatagramPacket datapacket = new DatagramPacket(fileData, fileData.length, clientIP, clientPort);
		    serverSocket.send(datapacket);
		    
		    //set serverSocket time out
		    serverSocket.setSoTimeout(timeout);
		    //set up ACK
		    byte[] ACK = new byte [packetSize];
		    DatagramPacket getAck = new DatagramPacket(ACK, ACK.length);
		    
		    // Waiting for ACK reply 
		    while(true)
		    {
		    	System.out.println("get ACK start");
		    	  try{
		                serverSocket.receive(getAck);
		                String a = new String(getAck.getData());
		                System.out.println("\n-----FROM CLIENT-----\n" + a.trim());
		                break;
		             }
		                catch(SocketTimeoutException e){
		                   continue;
		                }
		    }
		    
		    // Process ACK packet
		    
		    String ack = new String(getAck.getData());
		    if(checkACK(ack))
		    {
		    	//Debug: ACK
		    	System.out.println("got ACK");
		    }
		    
		}
		else
		{
			//Debug:didnot find the file
			System.out.println("Debug:didnot find the file");
		}
	}




	




	private static boolean checkACK(String ack) {
		// TODO Auto-generated method stub
		if(ack.equals("ACK"))
		{
			return true;
		}
		else
		{
			return false;
		}
	}




	private static byte[] splitFile(String requestedFileName) throws IOException {
		// TODO Auto-generated method stub
		// Test: make all data one pack for testing wait and stop
		java.nio.file.Path path =  Paths.get(requestedFileName);
		byte [] data =  Files.readAllBytes(path);		
		
		//Debug: File reading to byte
		return data;
	}




	private static boolean findFile(String requestedFileName) {
		// TODO Auto-generated method stub
		File f = new File(requestedFileName);
		return f.exists();
	}
}
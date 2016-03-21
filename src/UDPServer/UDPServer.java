package UDPServer;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import javafx.scene.shape.Path;

public class UDPServer {

	final public static int packetSize = 1024;
	final public static int headSize = 64;
	final public static int timeout = 1000;
	static DatagramSocket serverSocket;

	public static void main(String args[]) throws IOException {
		// Server side socket set up
		int serverPort = 29723;
		serverSocket = new DatagramSocket(29723);

		// Client request packet value
		InetAddress clientIP;
		int clientPort;
		String requestedFileName;

		// Run loop for accepting incoming packet
		while (true) {
			// Setup a place to store the in coming packet
			byte[] receivedData = new byte[packetSize];
			DatagramPacket receivedPacket = new DatagramPacket(receivedData, packetSize);
			serverSocket.receive(receivedPacket);

			// Get client's information from in coming packet
			clientIP = receivedPacket.getAddress();
			clientPort = receivedPacket.getPort();

			// Debug:client source value
			System.out.println("Recived request from Client IP:" + clientIP + " Port:" + clientPort);

			// Get client requested file name from in coming packet
			requestedFileName = new String(receivedPacket.getData());
			String[] filename = requestedFileName.split(" ");
			if (filename[0].equals("GET")) {
				requestedFileName = filename[1];
				System.out.println("Client request to download File: " + requestedFileName);
				stopWait(clientIP, clientPort, requestedFileName);
			}
		}

	}

	private static void stopWait(InetAddress clientIP, int clientPort, String requestedFileName) throws IOException {
		// TODO Auto-generated method stub
		boolean fileExsit = findFile(requestedFileName);
		if (fileExsit) {
			
			// byte [][] fileData if it is split
			byte[][] fileData = splitFile(requestedFileName);
			DatagramPacket datapacket = null;
			
			// use a loop here to send all the file Data 
			 for (int i = 0 ; i < fileData.length; i++)
			 { 	
				  datapacket = new DatagramPacket(fileData[i], fileData[i].length, clientIP, clientPort);
                  System.out.println("packet created");			 
			 
  
			//DatagramPacket datapacket = new DatagramPacket(fileData, fileData.length, clientIP, clientPort);

			// set serverSocket time out
			serverSocket.setSoTimeout(timeout);
			// Note: set a while true here for resends missing packet
			// set up ACK
			byte[] ACK = new byte[packetSize];
			DatagramPacket getAck = new DatagramPacket(ACK, ACK.length);
			serverSocket.send(datapacket);

			// Send packet and waiting for ACK reply
			System.out.println("Waitting ACK respond starts");
			while (true) {
				try {
					serverSocket.receive(getAck);
					String ack = new String(getAck.getData());
					System.out.println("\n-----FROM CLIENT-----\n" + ack.trim());
					// Process ACK packet

					if (checkACK(ack.trim())) {
						// Debug: ACK
						System.out.println("got ACK");
						break;
					} else {
						System.out.println("Wrong packet : resend");
						serverSocket.send(datapacket);
						continue;
					}
				} catch (SocketTimeoutException e) {
					System.out.println("Time out : resend previous packet");
					serverSocket.send(datapacket);
					continue;

				}

			}
			
		}

		}

		else {
			// Debug:didnot find the file
			System.out.println("Debug:didnot find the file");
		}
	}

	private static boolean checkACK(String ack) {
		// TODO Auto-generated method stub
		if (ack.equals("ACK")) {
			return true;
		} else {
			return false;
		}
	}

	private static byte[][] splitFile(String requestedFileName) throws IOException {
		// TODO Auto-generated method stub
		// Test: make all data one pack for testing wait and stop
		int SequenceNum = 0; 
		int size = 22;
		java.nio.file.Path path = Paths.get(requestedFileName);
		FileInputStream filestream = new FileInputStream(new File(requestedFileName));

		byte[][] data =  new byte[size][1024];
		for (int i = 0; i < data.length; i++) {
			addheader(data, i, SequenceNum);
			for (int j = 64; j < data[i].length; j++) {
				data[i][j] = (byte) filestream.read();
			}
			SequenceNum = (SequenceNum + 1) % 2;
		}
		// Debug: File reading to byte
		return data;
	}

	private static void addheader(byte[][] data, int i, int sequenceNum) {
		// TODO Auto-generated method stub
		int checkSumRan = (int) Math.random();
	    String checkSumResult = "Debug" ;
		String header = " CheckSum: " + checkSumRan + " CheckSumResult: " + checkSumResult + " SequenceNum: " + sequenceNum;
		byte [] temp = header.getBytes();
		byte [] temp2 = new byte [64];
        
        for(int k = 0 ; k<header.getBytes().length; k++)
        {
        	temp2[k] = temp [k];
        }
		
		for(int k = 0 ; k<64 ; k++)
		{
			data [i][k] = temp2[k];
		}

	}

	private static boolean findFile(String requestedFileName) {
		// TODO Auto-generated method stub
		File f = new File(requestedFileName);
		return f.exists();
	}
}
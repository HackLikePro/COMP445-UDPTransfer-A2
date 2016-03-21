package UDPClient;

import java.io.*;
import java.net.*;
import java.util.*;


public class UDPClient {
	static DatagramSocket clientSocket;
	static int packetSize = 1024;
	static int port = 4214;
	
	public static void main(String args[]) throws Exception 
	{
		InetAddress serverIP;
		int serverPort;
		creatConnection();
		InetAddress clientIP = InetAddress.getByName("127.0.0.1");
		clientSocket = new DatagramSocket(port);
		sendRequestPacket(clientIP,"thisname");
		
		
		
		// get data reply test
		while (true)
		{
			byte[] receivedData = new byte[packetSize];
			DatagramPacket receivedPacket = new DatagramPacket(receivedData, packetSize);
			clientSocket.receive(receivedPacket);
			
			serverIP = receivedPacket.getAddress();
			serverPort = receivedPacket.getPort();
			
			System.out.println( new String(receivedPacket.getData()));
			sendACK(serverIP,serverPort);
			
		}
		
	}
	

	private static void sendACK(InetAddress serverIP, int serverPort) throws IOException {
		// TODO Auto-generated method stub
		String ACKrespon = "NCK";
		byte[] ACK = ACKrespon.getBytes();
		DatagramPacket ACKpacket = new DatagramPacket(ACK, ACK.length, serverIP, serverPort);
		clientSocket.send(ACKpacket);
		System.out.println("ACK packet sent" + new String(ACKpacket.getData()));
		
	}

	private static String sendRequestPacket(InetAddress serverIPAddress, String filename) throws Exception {
        String request = ("GET " + filename + " HTTP/1.0\r\n");
        byte[] requestData = new byte[request.length()];
        requestData = request.getBytes();
        DatagramPacket requestPacket = new DatagramPacket(requestData, requestData.length, serverIPAddress, 29723);
        clientSocket.send(requestPacket);
        return request;
     }
	
	private static void creatConnection() {
		// TODO Auto-generated method stub
		
	}
}
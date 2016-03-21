package UDPClient;

import java.io.*;
import java.net.*;
import java.util.*;


public class UDPClient {
	static DatagramSocket clientSocket;
	static int port = 4214;
	
	public static void main(String args[]) throws Exception 
	{
		creatConnection();
		InetAddress clientIP = InetAddress.getByName("127.0.0.1");
		clientSocket = new DatagramSocket(port);
		sendRequestPacket(clientIP,"thisname");
		
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

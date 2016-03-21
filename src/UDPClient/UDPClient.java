package UDPClient;

import java.io.*;
import java.net.*;
import java.util.*;

public class UDPClient {
	static DatagramSocket clientSocket;
	static int packetSize = 1024;
	static int port = 4214;
	static int sequenceNumber = 0;

	public static void main(String args[]) throws Exception {
		InetAddress serverIP;
		int serverPort;
		creatConnection();
		InetAddress clientIP = InetAddress.getByName("127.0.0.1");
		clientSocket = new DatagramSocket(port);
		sendRequestPacket(clientIP, "thisname.jpg");
		byte[][] byteFile = new byte[22][900];

		// client socket timeout -- end downloading or has issue with connection
		clientSocket.setSoTimeout(3000);

		// get data reply test
		while (true) {
			try {
				int packetcount = 0;
				byte[] receivedData = new byte[packetSize];
				DatagramPacket receivedPacket = new DatagramPacket(receivedData, packetSize);
				clientSocket.receive(receivedPacket);
				// Get server information from received packet
				serverIP = receivedPacket.getAddress();
				serverPort = receivedPacket.getPort();

				System.out.println(new String(receivedPacket.getData()));

				// check packet sequence
				// check if packet has error
				if (checkSequence(receivedPacket) && checkSum(receivedPacket)) {
					sendACK(serverIP, serverPort);
					saveData(receivedPacket, byteFile, packetcount);
					packetcount++;
					sequenceNumber = (sequenceNumber+1)%2;
				} else {
					sendNCK(serverIP, serverPort);
				}

			} catch (SocketTimeoutException e) {
				System.out.println("Downloading ends");
				System.exit(0);

			}
		}

	}

	private static void saveFile(byte[][] byteFile, int packetcount) throws IOException {
		// TODO Auto-generated method stub

		String destination = new String("D:\\thisname.jpg");

		FileOutputStream fos = new FileOutputStream(destination, true);
		fos.write(byteFile[packetcount]);
		fos.close();
	}

	private static void saveData(DatagramPacket receivedPacket, byte[][] byteFile, int packetcount) throws IOException {
		// TODO Auto-generated method stub

		byte [] temp = receivedPacket.getData();
		byte [] temp2 = new byte [960];
		for (int i = 0 ; i< 960 ;i++)
		{
			temp2[i] = temp[i+64];
		}
		
		byteFile[packetcount] = temp2;

		saveFile(byteFile, packetcount);

	}

	private static boolean checkSequence(DatagramPacket receivedPacket) {
		// TODO Auto-generated method stub
        byte [] temp = receivedPacket.getData();
		byte [] header = new byte[64];
        for (int i = 0; i<64 ; i++)
        {
		header[i] = temp[i];
        }
		String [] headerSplit = new String(header).split(" ");
		String test1 = headerSplit[6];
	    String test2 = Integer.toString(sequenceNumber);
	    
		if(test1.trim().equals(test2.trim()))
		{
			return true;
		}
		else
		{
			return false;
		}
		
	}

	private static boolean checkSum(DatagramPacket receivedPacket) {
		// TODO Auto-generated method stub
		return true;
	}

	private static void sendNCK(InetAddress serverIP, int serverPort) throws IOException {
		// TODO Auto-generated method stub
		String ACKrespon = "NCK";
		byte[] ACK = ACKrespon.getBytes();
		DatagramPacket ACKpacket = new DatagramPacket(ACK, ACK.length, serverIP, serverPort);
		clientSocket.send(ACKpacket);
		System.out.println("ACK packet sent" + new String(ACKpacket.getData()));
	}

	private static void sendACK(InetAddress serverIP, int serverPort) throws IOException {
		// TODO Auto-generated method stub
		String ACKrespon = "ACK";
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
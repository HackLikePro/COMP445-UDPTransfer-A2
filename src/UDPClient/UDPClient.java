package UDPClient;

import java.io.*;
import java.net.*;
import java.util.*;

public class UDPClient {
	static DatagramSocket clientSocket;
	static int packetSize = 1024;
	static int port = 4214;
	static int sequenceNumber = 0;
	static int packetCount = 0;
	static String fileName;
	static String downorUp;
	static String serverName;
	static int packetdrop  = 4;

	public static void main(String args[]) throws Exception {
		InetAddress serverIP;
		int serverPort;
		creatConnection();
		InetAddress clientIP = InetAddress.getByName(serverName);
		clientSocket = new DatagramSocket(port);
		sendRequestPacket(clientIP, fileName);
		byte[][] byteFile = null ;

		// client socket timeout -- end downloading or has issue with connection
		clientSocket.setSoTimeout(3000);

		// get data reply test
		while (true) {
			try {
				byte[] receivedData = new byte[packetSize];
				DatagramPacket receivedPacket = new DatagramPacket(receivedData, packetSize);
				clientSocket.receive(receivedPacket);
				// Get server information from received packet
				serverIP = receivedPacket.getAddress();
				serverPort = receivedPacket.getPort();

				System.out.println("Receiver: received packet " + sequenceNumber );
				
				packetdrop = (int) (Math.random()*10);
                if (packetdrop == 4)
                {
                	packetDrop(receivedPacket);
                	System.out.println("Warnning: packet Dropped");
                }
				// check packet sequence
				// check if packet has error
				if (checkSequence(receivedPacket) && checkSum(receivedPacket)) {
					sendACK(serverIP, serverPort);
                    System.out.println("Receiver: sent an ACK for packet "+ sequenceNumber);
					saveData(receivedPacket, byteFile, packetCount);
					packetCount++;
					sequenceNumber = (sequenceNumber + 1) % 2;
				} else {
					sendNCK(serverIP, serverPort);
				}

			} catch (SocketTimeoutException e) {
				System.out.println("Receiver: transfer completed");
				System.out.println("Receiver: number of bytes received: " + packetCount);
				System.exit(0);

			}
		}

	}


	private static void packetDrop(DatagramPacket receivedPacket) {
		// TODO Auto-generated method stub
		byte [] dropbyte = new byte [1024];
		Arrays.fill(dropbyte, (byte)'#');
		receivedPacket.setData(dropbyte);
	}


	private static void saveData(DatagramPacket receivedPacket, byte[][] byteFile, int packetcount) throws IOException {
		// TODO Auto-generated method stub

		byte[] temp = receivedPacket.getData();
		byte[] header = new byte[64];
		for (int i = 0; i < 64; i++) {
			header[i] = temp[i];
		}
		String[] headerSplit = new String(header).split(" ");
		int fileSize = Integer.parseInt(headerSplit[8].trim());
		byteFile = new byte[fileSize][1024];

		byte[] temp2 = new byte[960];
		for (int i = 0; i < 960; i++) {
			temp2[i] = temp[i + 64];
		}

		byteFile[packetcount] = temp2;

		saveFile(byteFile, packetcount);

	}
	
	private static void saveFile(byte[][] byteFile, int packetcount) throws IOException {
		// TODO Auto-generated method stub

		String destination = new String("D:\\"+fileName);

		FileOutputStream fos = new FileOutputStream(destination, true);
		fos.write(byteFile[packetcount]);
		fos.close();
	}

	private static boolean checkSequence(DatagramPacket receivedPacket) {
		// TODO Auto-generated method stub
		byte[] temp = receivedPacket.getData();
		byte[] header = new byte[64];
		for (int i = 0; i < 64; i++) {
			header[i] = temp[i];
		}
		String[] headerSplit = new String(header).split(" ");
		if(headerSplit.length<6)
		{
			return false;
		}
		String test1 = headerSplit[6];
		String test2 = Integer.toString(sequenceNumber);

		if (test1.trim().equals(test2.trim())) {
			return true;
		} else {
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
		
	}

	private static void sendACK(InetAddress serverIP, int serverPort) throws IOException {
		// TODO Auto-generated method stub
		String ACKrespon = "ACK";
		byte[] ACK = ACKrespon.getBytes();
		DatagramPacket ACKpacket = new DatagramPacket(ACK, ACK.length, serverIP, serverPort);
		clientSocket.send(ACKpacket);
		
	}

	private static String sendRequestPacket(InetAddress serverIPAddress, String filename) throws Exception {
		String request = (downorUp.trim()+" " + filename + " HTTP/1.0\r\n");
		byte[] requestData = new byte[request.length()];
		requestData = request.getBytes();
		DatagramPacket requestPacket = new DatagramPacket(requestData, requestData.length, serverIPAddress, 29723);
		clientSocket.send(requestPacket);
		return request;
	}

	private static void creatConnection() {
		// TODO Auto-generated method stub
		Scanner kb = new Scanner(System.in);
		System.out.println("  *************************************************");
		System.out.println("  *************************************************");
		System.out.println("  **** Welcome to the UDP Stop and Wait System ****");
		System.out.println("  *************************************************");
		System.out.println("  *************************************************");
		System.out.println("  Input the server name:                           ");
		serverName = kb.nextLine();
		System.out.println("  GET OR PUT ? :                                   ");
		downorUp = kb.nextLine();
		System.out.println("  What is the file's name ? :                      ");
		fileName = kb.nextLine();

	}
}
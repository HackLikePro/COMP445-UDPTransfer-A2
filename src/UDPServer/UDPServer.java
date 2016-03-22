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
	public static int filesize = 0;
	public static int SequenceNum = 0;
	public static int SequenceNumCount = 0;
	public static int packetCount = 0;
	public static int packetArrived = 0;
	public static int effectivebytes = 0;
	public static int totalbytes = 0;
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
			System.out.println("Sender: starting on host " + InetAddress.getLocalHost().getHostName());

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
			for (int i = 0; i < fileData.length; i++) {
				datapacket = new DatagramPacket(fileData[i], fileData[i].length, clientIP, clientPort);
				//System.out.println("packet created");

				// set serverSocket time out
				serverSocket.setSoTimeout(timeout);
				// Note: set a while true here for resends missing packet
				// set up ACK
				byte[] ACK = new byte[packetSize];
				DatagramPacket getAck = new DatagramPacket(ACK, ACK.length);
				serverSocket.send(datapacket);
				packetCount++;
				totalbytes = packetCount * 1024;
				System.out.println("Sender: sent packet " +SequenceNumCount );
				
				// Send packet and waiting for ACK reply
				//System.out.println("Waitting ACK respond starts");
				while (true) {
					try {
						serverSocket.receive(getAck);
						String ack = new String(getAck.getData());
						// Process ACK packet

						if (checkACK(ack.trim())) {
							// Debug: ACK
							System.out.println("Sender: received ACK for packet "+SequenceNumCount );
							SequenceNumCount = (SequenceNumCount + 1)%2;
							packetArrived++;
						    effectivebytes = packetArrived * 1024;
							break;
						} else {
							System.out.println("Sender: Packet error - resend");
							serverSocket.send(datapacket);
							packetCount++;
							continue;
						}
					} catch (SocketTimeoutException e) {
						System.out.println("Sender: Time out - resend previous packet");
						serverSocket.send(datapacket);
						packetCount++;
						continue;

					}

				}

			}
			serverSocket.setSoTimeout(0);
			System.out.println("Sender: file transfer completed");
			System.out.println("Sender: number of effective bytes sent: "+ effectivebytes);
			System.out.println("Sender: number of packets sent: "+ packetCount);
			System.out.println("Sender: number of bytes sent: "+ totalbytes);
			System.exit(0);
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
		File requestedFile = new File(requestedFileName);
		filesize = (int) (requestedFile.length() / 960)+1;
		java.nio.file.Path path = Paths.get(requestedFileName);
		FileInputStream filestream = new FileInputStream(new File(requestedFileName));

		byte[][] data = new byte[filesize][1024];
		for (int i = 0; i < data.length; i++) {
			addheader(data, i);
			for (int j = 64; j < data[i].length; j++) {
				data[i][j] = (byte) filestream.read();
			}
			SequenceNum = (SequenceNum + 1) % 2;
		}
		// Debug: File reading to byte
		return data;
	}

	private static void addheader(byte[][] data, int i) {
		// TODO Auto-generated method stub
		int checkSumRan = (int) Math.random();
		String checkSumResult = "Debug";
		String header = " CheckSum: " + checkSumRan + " CheckSumResult: " + checkSumResult + " SequenceNum: "
				+ SequenceNum + " FileSize: " + filesize;
		byte[] temp = header.getBytes();
		byte[] temp2 = new byte[64];

		for (int k = 0; k < header.getBytes().length; k++) {
			temp2[k] = temp[k];
		}

		for (int k = 0; k < 64; k++) {
			data[i][k] = temp2[k];
		}

	}

	private static boolean findFile(String requestedFileName) {
		// TODO Auto-generated method stub
		File f = new File(requestedFileName);
		return f.exists();
	}
}
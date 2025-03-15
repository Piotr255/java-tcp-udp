package org.lab1.zad4;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Scanner;

public class Server {
    public static void main(String[] args) {
        try {
            Server server = new Server(9876);
            server.start();
        } catch (SocketException e) {
            System.out.println(e.getMessage());
        }

    }

    private final DatagramSocket datagramSocket;
    private InetAddress inetClientAddress;
    private int clientPort;
    private final int buffSize = 1024;
    private final byte[] receiveBuff;
    Scanner scanner;
    private final byte[] testArrayLittleEndian = {44, 1, 0, 0};
    private final byte[] testArrayBigEndian = {0, 0, 1, 44};
    String response = "problem";

    public Server(int port) throws SocketException {
        datagramSocket = new  DatagramSocket(port);
        scanner = new Scanner(System.in);
        receiveBuff = new byte[buffSize];
    }

    public void start() {
        try {
            while (true) {
                receiveMessage();
                sendMessage(response);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            if (!datagramSocket.isClosed()) {
                datagramSocket.close();
            }
            if (scanner != null) {
                scanner.close();
            }
        }
    }




    private void sendMessage(String message) throws IOException {
        byte[] sendBuff = message.getBytes();
        DatagramPacket datagramPacket = new DatagramPacket(sendBuff, sendBuff.length, inetClientAddress, clientPort);
        datagramSocket.send(datagramPacket);
    }

    private String readStdIn() {
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }

    private void receiveMessage() throws IOException {
        DatagramPacket datagramPacket = new DatagramPacket(receiveBuff, buffSize);
        datagramSocket.receive(datagramPacket);
        int receivedValue = ByteBuffer.wrap(receiveBuff).getInt();
        if (receivedValue == 300) response = "Pong Java";
        else response = "Pong Python";
        clientPort = datagramPacket.getPort();
        inetClientAddress = datagramPacket.getAddress();
    }

}

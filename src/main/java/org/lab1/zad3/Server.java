package org.lab1.zad3;

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
    int number;

    public Server(int port) throws SocketException {
        datagramSocket = new  DatagramSocket(port);
        scanner = new Scanner(System.in);
        receiveBuff = new byte[buffSize];
    }

    public void start() {
        try {
            while (true) {
                receiveMessage();
                number++;
                sendMessage(number);
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




    private void sendMessage(int number) throws IOException {
        byte[] sendBuff = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(number).array();
        System.out.println(Arrays.toString(sendBuff));
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
        number = ByteBuffer.wrap(receiveBuff).order(ByteOrder.LITTLE_ENDIAN).getInt();
        System.out.println("client: " + number);
        clientPort = datagramPacket.getPort();
        inetClientAddress = datagramPacket.getAddress();
    }

}

package org.lab1.zad1;

import java.io.IOException;
import java.net.*;
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

    public Server(int port) throws SocketException {
        datagramSocket = new  DatagramSocket(port);
        scanner = new Scanner(System.in);
        receiveBuff = new byte[buffSize];
    }

    public void start() {
        try {
            while (true) {
                receiveMessage();
                String msg = readStdIn();
                sendMessage(msg);
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
        String msg = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
        System.out.println("client: " + msg);
        clientPort = datagramPacket.getPort();
        inetClientAddress = datagramPacket.getAddress();
    }

}

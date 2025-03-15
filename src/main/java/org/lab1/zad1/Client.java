package org.lab1.zad1;


import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try {
            Client client = new Client("localhost", 9876);
            client.start();
        } catch (UnknownHostException | SocketException e) {
            System.out.println(e.getMessage());
        }

    }


    DatagramSocket datagramSocket;
    InetAddress inetServerAddress;
    int serverPort;
    Scanner scanner;
    int buffSize = 1024;
    byte[] receiveBuff;

    public Client(String hostname, int serverPort) throws UnknownHostException, SocketException {
        inetServerAddress = InetAddress.getByName(hostname);
        datagramSocket = new DatagramSocket();
        scanner = new Scanner(System.in);
        receiveBuff = new byte[buffSize];
        this.serverPort = serverPort;
    }

    public void start() {
        try {
            while(true) {
                sendMessage(readStdIn());
                receiveMessage();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            if(datagramSocket != null && !datagramSocket.isClosed()){
                datagramSocket.close();
            }
            if(scanner != null){
                scanner.close();
            }
        }
    }

    private void receiveMessage() throws IOException {
        DatagramPacket datagramPacket = new DatagramPacket(receiveBuff, buffSize);
        datagramSocket.receive(datagramPacket);
        String msg = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
        System.out.println("server: " + msg);
    }

    private void sendMessage(String message) throws IOException {
        byte[] sendBuff = message.getBytes();
        DatagramPacket datagramPacket = new DatagramPacket(sendBuff, sendBuff.length, inetServerAddress, serverPort);
        datagramSocket.send(datagramPacket);
    }

    private String readStdIn(){
        return scanner.nextLine();
    }



}

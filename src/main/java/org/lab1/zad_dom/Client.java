package org.lab1.zad_dom;

import org.w3c.dom.ls.LSOutput;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java Client <clientNickname>");
            System.exit(1);
        }
        Client client = new Client("localhost", 9876, args[0]);
        client.start();
    }
    private Socket socket;
    private DatagramSocket datagramSocket;
    private PrintWriter out;
    private BufferedReader in;
    private String clientNickname;
    private Scanner scanner;
    private volatile boolean isActive;
    private String serverHostname;
    private int serverPort;
    private String asciiArt;

    public Client(String serverHostname, int serverPort, String clientNickname) {
        try {
            this.serverHostname = serverHostname;
            this.serverPort = serverPort;
            socket = new Socket(serverHostname, serverPort);
            datagramSocket = new DatagramSocket();
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            scanner = new Scanner(System.in);
            this.clientNickname = clientNickname;
            isActive = true;
            asciiArt = new String(Files.readAllBytes(Paths.get("./ascii.txt")));
        } catch (IOException e) {
            print("Couldn't create resources");
            closeResources();
        }
    }

    public synchronized void print(String message) {
        System.out.println(message);
    }
    
    private void closeResources() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (scanner != null) {
                scanner.close();
            }
            if (datagramSocket != null && !datagramSocket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            print("Error closing resources!");
            System.exit(1);
        }
    }



    void start() {
        out.println(clientNickname);
        try {
            Thread receiveThread = new Thread(this::receiveMessage);
            receiveThread.start();
            Thread sendThread = new Thread(this::sendMessage);
            sendThread.start();
            Thread receiveUdp = new Thread(this::receiveMessageUdp);
            receiveUdp.start();
            receiveThread.join();
            sendThread.join();
            receiveUdp.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            print("Closed resources");
            closeResources();
        }
    }

    void receiveMessage() {
        try {

            String receiveMessage;
            while (isActive && (receiveMessage = in.readLine()) != null) {
                if (!receiveMessage.isBlank()) {
                    print(receiveMessage);
                }
            }
            if (isActive) {
                synchronized (this) {
                    print("Server disconnected");
                    print("Press any button to end");
                    isActive = false;
                }
            }
        } catch (IOException e) {
            if (isActive) {
                print("Error reading from server" + e.getMessage());
                synchronized (this) {
                    isActive = false;
                }
            }
        }
    }

    void receiveMessageUdp(){
        while(isActive) {
            byte[] buff = new byte[1024];
            DatagramPacket datagramPacket = new DatagramPacket(buff, buff.length);
            String message = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
            print(message);
        }
    }
    
    void sendMessage() {
        try {
            while (isActive) {
                String message = scanner.nextLine();
                if (message.equals("end")) {
                    synchronized (this) {
                        isActive = false;
                        socket.close();
                        break;

                    }
                }
                if (message.equals("U")) {
                    message = scanner.nextLine();
                    if (!message.isBlank()) {
                        byte[] buff = asciiArt.getBytes();
                        DatagramPacket datagramPacket = new DatagramPacket(buff, buff.length, InetAddress.getByName(serverHostname), serverPort);
                        datagramSocket.send(datagramPacket);
                    }
                } else {
                    if (!message.isBlank()) {
                        out.println(message);
                    }
                }
            }
        } catch (IOException e) {
            print("Error closing socket: " + e.getMessage());
        }
    }
}

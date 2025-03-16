package org.lab1.zad_dom;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private MulticastSocket multicastSocket;
    private PrintWriter out;
    private BufferedReader in;
    private String clientNickname;
    private Scanner scanner;
    private volatile boolean isActive;
    private String serverHostname;
    private int serverPort;
    private String asciiArt;
    private ExecutorService executorService;
    private InetAddress group;
    private int multicastPort;
    public Client(String serverHostname, int serverPort, String clientNickname) {
        try {
            this.serverHostname = serverHostname;
            this.serverPort = serverPort;
            socket = new Socket(serverHostname, serverPort);
            datagramSocket = new DatagramSocket(socket.getLocalPort());
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            scanner = new Scanner(System.in);
            this.clientNickname = clientNickname;
            isActive = true;
            asciiArt = new String(Files.readAllBytes(Paths.get("src/main/java/org/lab1/zad_dom/ascii.txt")));
            executorService  = Executors.newFixedThreadPool(4);
            multicastPort = 9999;
            multicastSocket = new MulticastSocket(multicastPort);
            group = Inet4Address.getByName("228.5.6.7");
            multicastSocket.joinGroup(group);
        } catch (IOException e) {
            print("Couldn't create resources. " + e.getMessage());
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
                datagramSocket.close();
            }
            if (executorService != null) {
                executorService.shutdown();
            }
            if (multicastSocket != null && !multicastSocket.isClosed()) {
                multicastSocket.close();
            }
        } catch (IOException e) {
            print("Error closing resources!");
            System.exit(1);
        }
    }



    void start() {
        out.println(clientNickname);
        try {
            executorService.submit(this::receiveMessage);
            executorService.submit(this::sendMessage);
            executorService.submit(this::receiveMessageUdp);
            executorService.submit(this::receiveMessageMulticast);
            while (isActive) Thread.onSpinWait();
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

    void receiveMessageUdp() {
        while(isActive) {
            try {
                byte[] buff = new byte[1024];
                DatagramPacket datagramPacket = new DatagramPacket(buff, buff.length);
                datagramSocket.receive(datagramPacket);
                String message = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
                print("receiveMessageUdp"+ message);
            } catch (IOException e) {
                if (isActive) {
                    print("Problem receiving udp package: " + e.getMessage());
                    closeResources();
                    System.exit(1);
                }

            }
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
                        datagramSocket.close();
                        break;

                    }
                }
                if (message.equals("U")) {
                    byte[] buff = asciiArt.getBytes();
                    DatagramPacket datagramPacket = new DatagramPacket(buff, buff.length, InetAddress.getByName(serverHostname), serverPort);
                    datagramSocket.send(datagramPacket);
                } else if (message.equals("M")) {
                    message = "test multicast";
                    byte[] buff = message.getBytes();
                    DatagramPacket datagramPacket = new DatagramPacket(buff, buff.length, group, multicastPort);
                    datagramSocket.send(datagramPacket);
                } else {
                    if (!message.isBlank()) {
                        out.println(message);
                    }
                }
            }
        } catch (IOException e) {
            print("Error closing socket: " + e.getMessage());
            closeResources();
            System.exit(1);
        }
    }

    void receiveMessageMulticast() {
        while(isActive) {
            try {
                byte[] buff = new byte[1024];
                DatagramPacket datagramPacket = new DatagramPacket(buff, buff.length);
                multicastSocket.receive(datagramPacket);
                String message = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
                print("Receive multi" + message);
            } catch (IOException e) {
                if (isActive) {
                    print("Problem receiving multicast package: " + e.getMessage());
                    closeResources();
                    System.exit(1);
                }

            }
        }
    }

}

package org.lab1.zad_dom;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
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
    private PrintWriter out;
    private BufferedReader in;
    private String clientNickname;
    private Scanner scanner;
    private volatile boolean isActive;

    public Client(String serverHostname, int serverPort, String clientNickname) {
        try {
            socket = new Socket(serverHostname, serverPort);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            scanner = new Scanner(System.in);
            this.clientNickname = clientNickname;
            isActive = true;
        } catch (IOException e) {
            System.err.println("Couldn't create resources");
            closeResources();
        }
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
        } catch (IOException e) {
            System.err.println("Error closing resources!");
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
            receiveThread.join();
            sendThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            System.out.println("Closed resources");
            closeResources();
        }
    }

    void receiveMessage() {
        try {
            String receiveMessage;
            while (isActive && (receiveMessage = in.readLine()) != null) {
                if (!receiveMessage.isBlank()) {
                    System.out.println(receiveMessage);
                }
            }
            if (isActive) {
                synchronized (this) {
                    System.out.println("Server disconnected");
                    System.out.println("Press any button to end");
                    isActive = false;
                }
            }
        } catch (IOException e) {
            if (isActive) {
                System.err.println("Error reading from server" + e.getMessage());
                synchronized (this) {
                    isActive = false;
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
                        break;

                    }
                } else {
                    if (!message.isBlank()) {
                        out.println(message);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error closing socket: " + e.getMessage());
        }
    }
}

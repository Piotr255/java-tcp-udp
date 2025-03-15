package org.lab1.zad_dom;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.Socket;

public class ServerThread implements Runnable {
    private final Socket clientSocket;
    private String clientNickname;
    PrintWriter out;
    BufferedReader in;
    Server server;

    @Override
    public String toString() {
        return "ServerThread{" +
                "clientNickname='" + clientNickname + '\'' +
                '}';
    }

    public ServerThread(Socket clientSocket, Server server) {
        try {
            this.clientSocket = clientSocket;
            this.server = server;
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            closeResources();
            System.err.println("Problem with starting resources");
            throw new RuntimeException(e);
        }
    }

    private void closeResources() {
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            System.err.println("Problem with closing resources");
            throw new RuntimeException(e);
        }
    }




    @Override
    public void run() {
        try {
        String inputLine;
        if ((inputLine = in.readLine()) != null) {
            clientNickname = inputLine;
            System.out.println("New Client:" + clientNickname);
        } else {
            System.err.println("Couldn't get nickname");
            System.exit(-1);
        }
        while ((inputLine = in.readLine()) != null) {
            server.broadcast(inputLine, this);
        }
        } catch (IOException e) {
            System.err.println("Error reading from socket input");
        } finally {
            server.removeCLient(this);
            closeResources();
        }
    }

    public String getClientNickname() {
        return clientNickname;
    }

    public void receiveMessage(String message) {
        out.println(message);
    }
}

package org.lab1.zad_dom;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class ServerThread implements Runnable {
    private final Socket clientSocket;
    private String clientNickname;
    private final InetAddress clientAddress;
    private final int clientPort;
    PrintWriter out;
    BufferedReader in;
    Server server;

    public ServerThread(Socket clientSocket, Server server) throws IOException {
        try {
            this.clientSocket = clientSocket;
            clientAddress = clientSocket.getInetAddress();
            clientPort = clientSocket.getPort();
            this.server = server;
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            closeResources();
            server.print("Problem with starting resources");
            throw e;
        }
    }

    public void closeResources() {
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
            server.print("Closed resources from ServerThread");
        } catch (IOException e) {
            server.print("Problem with closing resources");
            throw new RuntimeException(e);
        }
    }




    @Override
    public void run() {
        try {
        String inputLine;
        if ((inputLine = in.readLine()) != null) {
            clientNickname = inputLine;
            server.print("New Client:" + clientNickname);
        } else {
            server.print("Couldn't get nickname");
        }
        while ((inputLine = in.readLine()) != null) {
            server.broadcast(inputLine, this);
        }
        } catch (IOException e) {
            if (server.isListening()) {
                server.print("Error reading from socket input");
            }
//            closeResources();
        } finally {
            closeResources();
            server.removeClient(this);
        }
    }

    public String getClientNickname() {
        return clientNickname;
    }

    public void receiveMessage(String message) {
        out.println(message);
    }

    public int getClientPort() {
        return clientPort;
    }

    public InetAddress getClientAddress() {
        return clientAddress;
    }

}

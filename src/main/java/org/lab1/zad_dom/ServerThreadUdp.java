package org.lab1.zad_dom;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class ServerThreadUdp implements Runnable {
    private final int buffSize = 1024;
    private final byte[] receiveBuff;
    private final Server server;
    private final DatagramSocket datagramSocket;
    @Override
    public void run() { //
        try {
        while(server.isListening()) {
            receiveMessage();
        }
        } catch (IOException e) {
            if(server.isListening()) {
                server.print("Problem with receiving packets: " + e.getMessage());
            }
        } finally {
            closeResources();
        }

    }

    public ServerThreadUdp(int port, Server server) {
        try {
            datagramSocket = new DatagramSocket(port);
            this.server = server;
            receiveBuff = new byte[buffSize];
        } catch (SocketException e) {
            server.print("Problem creating udp socket: " + e.getMessage());
                throw new RuntimeException();
        }
    }
    private void sendMessage(String message, InetAddress otherClientAddress, int otherClientPort) throws IOException {
        byte[] sendBuff = message.getBytes();
        DatagramPacket datagramPacket = new DatagramPacket(sendBuff, sendBuff.length, otherClientAddress, otherClientPort);
        datagramSocket.send(datagramPacket);
    }

    public void closeResources() {
        if (datagramSocket != null && !datagramSocket.isClosed()) {
            datagramSocket.close();
            server.print("Closed resources from ServerThreadUdp");
        }

    }

    private void receiveMessage() throws IOException {
        DatagramPacket datagramPacket = new DatagramPacket(receiveBuff, buffSize);
        datagramSocket.receive(datagramPacket);
        int currentClientPort = datagramPacket.getPort();
        String msg = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
        for(ServerThread serverThread: server.getServerThreads()) {
            if (serverThread.getClientPort() != currentClientPort) {
                sendMessage(msg , serverThread.getClientAddress(), serverThread.getClientPort());
            }

        }

    }

}

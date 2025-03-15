package org.lab1.zad_dom;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public static void main(String[] args) {
        try {
            Server server = new Server(9876);
            server.start();
        } catch (IOException e) {
            System.out.println("Error starting server occured: " + e.getMessage());
            System.exit(1);
        }
    }
    private final int port;
    private volatile boolean listening;
    private final List<ServerThread>  serverThreads;
    private final ExecutorService threadPool;
    private final ServerSocket serverSocket;

    Server(int port) throws IOException {
        this.port = port;
        listening = true;
        serverThreads = new ArrayList<>();
        //creating new threads is heavy operation, threadPool is more efficient solution
        threadPool = Executors.newFixedThreadPool(6);
        serverSocket = new ServerSocket(port);
    }

    public void start() {
        try {
            ServerThreadUdp serverThreadUdp = new ServerThreadUdp(port, this);
            threadPool.submit(serverThreadUdp);
            threadPool.submit(this::endServer);

            while(listening) {
                ServerThread serverThread = new ServerThread(serverSocket.accept(), this);
                serverThreads.add(serverThread);
                threadPool.submit(serverThread);
            }
        } catch (IOException e) {
            if(listening) {
                System.err.println("Could not listen on port " + port);
                System.exit(1);
            }
            System.exit(0);
        } finally {
            threadPool.shutdownNow();
        }

    }

    public synchronized void broadcast(String message, ServerThread sender){
        message = sender.getClientNickname() + ": " + message;
        for(ServerThread serverThread: serverThreads) {
            if (serverThread.equals(sender)){
                continue;
            }
            serverThread.receiveMessage(message);
        }
    }

    public synchronized void removeCLient(ServerThread serverThread) {
        serverThreads.remove(serverThread);
        System.out.println("Usunięto: " + serverThread.getClientNickname());
        printClients();
    }

    public void printClients(){
        System.out.println("--------------");
        System.out.println("Active users: ");
        serverThreads.forEach(System.out::println);
        System.out.println("--------------");
    }

    public List<ServerThread> getServerThreads() {
        return Collections.unmodifiableList(serverThreads);
    }

    public void endServer() {
        Scanner scanner = new Scanner(System.in);
        while(listening) {
            if (scanner.nextLine().equals("end")) {
                synchronized (this) {
                    listening = false;
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
}

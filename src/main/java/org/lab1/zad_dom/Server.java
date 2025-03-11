package org.lab1.zad_dom;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public static void main(String[] args) {
        try {
            Server server = new Server(9876);
            server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private final int port;
    private final boolean listening;
    private final List<ServerThread>  serverThreads;
    private ExecutorService threadPool;

    Server(int port) throws IOException {
        this.port = port;
        listening = true;
        serverThreads = new ArrayList<>();
        //creating new thread is heavy operation threadPool is more efficient solution
        threadPool = Executors.newFixedThreadPool(6);
    }

    public void start() {
        try(ServerSocket serverSocket = new ServerSocket(port)) {
            while(listening) {
                ServerThread serverThread = new ServerThread(serverSocket.accept(), this);
                serverThreads.add(serverThread);
                threadPool.submit(serverThread);
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + port);
            System.exit(-1);
        } finally {
            threadPool.shutdown();
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
}

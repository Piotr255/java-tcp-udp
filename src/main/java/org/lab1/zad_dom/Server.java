package org.lab1.zad_dom;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
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
    private ServerThreadUdp serverThreadUdp;

    Server(int port) throws IOException {
        this.port = port;
        listening = true;
        serverThreads = new CopyOnWriteArrayList<>();
        //creating new threads is heavy operation, threadPool is more efficient solution
        threadPool = Executors.newFixedThreadPool(6);
        serverSocket = new ServerSocket(port);
    }

    public void start() {
        try {
            serverThreadUdp = new ServerThreadUdp(port, this);
            threadPool.submit(serverThreadUdp);
            threadPool.submit(this::endServer);

            while(listening) {
                ServerThread serverThread = new ServerThread(serverSocket.accept(), this);
                serverThreads.add(serverThread);
                threadPool.submit(serverThread);
            }
        } catch (IOException e) {
            if(listening) {
                System.err.println(e.getMessage());
            }
        } finally {
            closeAllResources();
            threadPool.shutdown();
        }

    }

    private void closeAllResources() {
        serverThreadUdp.closeResources();
        for(ServerThread serverThread : serverThreads) {
            serverThread.closeResources();
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

    public synchronized void removeClient(ServerThread serverThread) {
        serverThreads.remove(serverThread);
        if (serverThread.getClientNickname() != null){
            print("Removed: " + serverThread.getClientNickname());
        }
        printClients();
    }

    public void printClients(){
        print("--------------");
        print("Active users: ");
        serverThreads.forEach((serverThread -> serverThread.server.print(serverThread.getClientNickname())));
        print("--------------");
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
                        if (serverSocket != null && !serverSocket.isClosed()) {
                            serverSocket.close();
                        }
                    } catch (IOException e) {
                        print("Problem with closing socket:" + e.getMessage());
                        System.exit(1);
                    }
                }
            }
        }
    }

    public synchronized void print(String message) {
        System.out.println(message);
    }

    public boolean isListening() {
        return listening;
    }
}

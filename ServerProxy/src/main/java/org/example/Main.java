package org.example;

import org.example.Proxy.ChessServer;
import org.example.Proxy.ChessServerProxy;
import org.example.utils.LocalIPv4;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(9080);
        System.out.println("IP: " + LocalIPv4.get());
        System.out.println("Port: " + serverSocket.getLocalPort());
        ChessServer chessServer = new ChessServer();
        while (true) {
            Socket s = serverSocket.accept();
            ChessServerProxy serverProxy = new ChessServerProxy(s, chessServer);
            Thread t = new Thread(serverProxy);
            t.start();
        }
    }
}
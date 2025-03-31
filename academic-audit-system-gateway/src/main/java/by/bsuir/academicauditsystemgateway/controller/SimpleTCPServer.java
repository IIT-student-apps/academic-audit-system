package by.bsuir.academicauditsystemgateway.controller;

import java.io.*;
import java.net.*;

public class SimpleTCPServer {
    public static void main(String[] args) {
        int port = 8080; // Порт для прослушивания

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен на порту " + port);

            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
                    System.out.println("Новое соединение: " + clientSocket.getInetAddress());

                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        System.out.println(inputLine);
                    }
                } catch (IOException e) {
                    System.out.println("Ошибка при обработке соединения: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Ошибка запуска сервера: " + e.getMessage());
        }
    }
}

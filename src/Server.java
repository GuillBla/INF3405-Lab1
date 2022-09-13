import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Server {
    private static ServerSocket listener;

    public static boolean isIPv4Address(String address) throws UnknownHostException {
        if (address.isEmpty()) {
            return false;
        }
        try {
            Object res = InetAddress.getByName(address);
            return res instanceof Inet4Address;
        }catch(UnknownHostException e) {
            return false;
        }
    }

    public static boolean isPortNumber(int port){
        if (5000 <= port && port <= 5050){
            return true;
        }else{
            return false;
        }
    }

    // Application serveur
    public static void main(String[] args) throws Exception {

        //Compteur du nombre de connexions :
        int clientNumber = 1;

        // On attend qu'une addresse IPv4 correcte soit entrée
        String serverAddress = "";
        while (!isIPv4Address(serverAddress))
        {
            System.out.println("Please enter a correct IP address for the server to create :");
            System.out.println("Expected format : X1.X2.X3.X4 with integer values between 0 and 254");
            Scanner myObj = new Scanner(System.in);
            serverAddress = myObj.nextLine();
        }

        System.out.println("Server address is : " + serverAddress);

        // On attend qu'un numéro de port correct soit entré

        int serverPort = -1;
        while (!isPortNumber(serverPort))
        {
            System.out.println("Please enter a correct port for the server to create :");
            System.out.printf("Expected format : an integer between 5000 and 5050");
            Scanner myObj = new Scanner(System.in);
            serverPort = myObj.nextInt();
        }
        System.out.println("Port number is : " + serverPort);

        // Création d'une connexion avec les clients
        listener = new ServerSocket();
        listener.setReuseAddress(true);
        InetAddress serverIP = InetAddress.getByName(serverAddress);

        // Association de l'addresse et du port
        listener.bind(new InetSocketAddress(serverIP, serverPort));
        System.out.format("The server is running %s:%d\n", serverAddress, serverPort);

        try {
            // Pour chaque connexion d'un client

            while (true) {
                // On attend le prochain client
                // Attention : accept est bloquante
                new ClientHandler(listener.accept(), serverPort,serverAddress).start();
            }

        } finally {
            listener.close();
        }


    }

}

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import static java.lang.System.exit;


public class Client {
    private static Socket socket;


    // Application client
    public static void main(String[] args) throws Exception {

        // Addresse du serveur
        String serverAddress = "";
        System.out.println("Please enter the IP address for the server to communicate with : ");
        System.out.println("Expected format : X1.X2.X3.X4 with integer values between 0 and 254");
        Scanner scan_IP = new Scanner(System.in);
        serverAddress = scan_IP.nextLine();
        while (!isIPv4Address(serverAddress)) {
            System.out.println("Incorrect IP Address !");
            System.out.println("Expected format : X1.X2.X3.X4 with integer values between 0 and 254");
            System.out.println("Please enter the IP address for the server to communicate with  : ");
            serverAddress = scan_IP.nextLine();
        }

        // Port du serveur
        int serverPort = -1;
        System.out.println("Please enter the port to communicate with :");
        System.out.println("Expected format : an integer between 5000 and 5050");
        Scanner scan_Port = new Scanner(System.in);
        serverPort = scan_Port.nextInt();
        while (!isPortNumber(serverPort)) {
            System.out.println("Incorrect Port number !");
            System.out.println("Expected format : an integer between 5000 and 5050");
            System.out.println("Please enter the port to communicate with : ");
            serverPort = scan_Port.nextInt();
        }
        System.out.println("Server address : " + serverAddress);
        System.out.println("Port number : " + serverPort);

        //Création d'une connexion avec le serveur
        try {
            socket = new Socket(serverAddress, serverPort);
            System.out.format("Server %s:%d is running : \n", serverAddress, serverPort);
        } catch (ConnectException e) {
            System.out.format("Server %s:%d is not running : \n", serverAddress, serverPort);
            exit(0);
        }

        // Création d'un canal pour recevoir les messages du serveur
        DataInputStream in = new DataInputStream(socket.getInputStream());
        // Création d'un canal pour envoyer les identifiants au serveur
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

        //Réception du message et impression
        String welcome_message_from_Server = in.readUTF();
        System.out.println(welcome_message_from_Server);

        // Lecture des identifiants
        String userName;
        String password;

        // Demander à l'utilisateur si il a déjà un compte
        boolean has_account = false;
        System.out.println("Do you have an account ? [y/n]");
        Scanner scan_account_answer = new Scanner(System.in);
        String account_answer = scan_account_answer.nextLine();

        // Si il n'a pas de compte, il faut le créer
        while (!has_account) {
            if (account_answer.equals("y")) {
                has_account = true;
                out.writeUTF("true");
            } else if (account_answer.equals("n")) { //
                out.writeUTF("false");
                System.out.println("Please create a new account : ");
                System.out.println("Create a username : (Spaces forbidden)");
                Scanner scan_new_username = new Scanner(System.in);
                String new_username = scan_new_username.nextLine();
                System.out.println("Create a password : (Spaces forbidden)");
                Scanner scan_new_password = new Scanner(System.in);
                String new_password = scan_new_password.nextLine();
                // Envoi des nouveaux identifiants au serveur
                out.writeUTF(new_username + " " + new_password);
                has_account = true;
            } else { // Incorrect entry
                System.out.println("Do you have an account ? [y/n]");
                System.out.println("Please type y or n");
            }
        }

        // Lecture des identifiants déjà créés
        System.out.println("Connect to the server :");
        System.out.println("Please enter your username :");
        Scanner scan_username = new Scanner(System.in);
        userName = scan_username.nextLine();
        System.out.println("Please enter your password : ");
        Scanner scan_password = new Scanner(System.in);
        password = scan_password.nextLine();

        // Envoi des identifiants
        out.writeUTF(userName + " " + password);

        // Lecture du message d'authentification
        boolean connected = false;
        while (!connected) {
            String authentification_message_from_Server = in.readUTF();
            System.out.println(authentification_message_from_Server);
            if (authentification_message_from_Server.equals("Authentification granted")) {
                connected = true;
            }
        }

        // Read the name of the images to send/generate
        System.out.printf("Please enter the name of the image you want to send (PNG format)");
        Scanner scan_image_name = new Scanner(System.in);
        String image_name = scan_image_name.nextLine();
        out.writeUTF(image_name);

        // Send the image to the server
        BufferedImage image_to_send = null;
        try {
            // Read the image from the local path
            image_to_send = ImageIO.read(new File(image_name));
            out.writeUTF("Read image successfully");
        } catch (IOException e) {
            System.out.println("Error : Could not load the image");
            out.writeUTF("Could not read image");
            exit(0);
        }
        System.out.printf("How should the generated image be named ?");
        Scanner scan_generated_image_name = new Scanner(System.in);
        String generated_image_name = scan_generated_image_name.nextLine();

        ImageIO.write(image_to_send, "PNG", socket.getOutputStream());
        System.out.println("Image "+ image_name +" sent to the server");
        // Get the generated image


        BufferedImage generated_image = ImageIO.read(socket.getInputStream());
        File generated_file = new File(generated_image_name);
        ImageIO.write(generated_image, "PNG", generated_file);

        System.out.println("Image received !");
        System.out.println("Path of the image : " + System.getProperty("user.dir") + "/" + generated_image_name);

        // Fermeture de la connexion avec le serveur
        socket.close();
    }


    public static boolean isIPv4Address(String address) throws UnknownHostException {
        if (address.isEmpty()) {
            return false;
        }
        try {
            Object res = InetAddress.getByName(address);
            return res instanceof Inet4Address;
        } catch (UnknownHostException e) {
            return false;
        }
    }

    public static boolean isPortNumber(int port) {
        return 5000 <= port && port <= 5050;
    }


}

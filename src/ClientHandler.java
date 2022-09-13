import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.nio.file.*;
import java.util.Objects;

public class ClientHandler extends Thread {
    private Socket socket;
    private int serverPort;
    private String serverAddress;

    public ClientHandler(Socket socket, int serverPort, String serverAddress) {
        this.socket = socket;
        this.serverPort = serverPort;
        this.serverAddress = serverAddress;
    }

    public void run() {
        try {
            // Création du fichier des identifiants
            String filePath = System.getProperty("user.dir") + "/logins.txt";
            File file = new File("logins.txt");
            if (!file.exists()) {
                file.createNewFile();
            }

            // Création d'un canal pour envoyer des messages au client
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            // Création d'un canal pour recevoir les messages du client
            DataInputStream in = new DataInputStream(socket.getInputStream());

            // Envoi d'un message de bienvenue
            out.writeUTF("Welcome to the server !");

            // Lecture du booleen : l'utilisateur possède un compte
            boolean has_account = Boolean.parseBoolean(in.readUTF());

            // Si nouveau utilisateur : ajout des identifiants dans le fichier login.txt
            if (!has_account) {
                String[] new_identifiants = in.readUTF().split((" "));
                String new_username = new_identifiants[0];
                String new_password = new_identifiants[1];
                FileWriter myWriter = new FileWriter(filePath, true); // true =>  we append data to the file
                myWriter.write(new_username + " : " + new_password + "\n"); //EHBAHOUIIII
                myWriter.close();
            }

            String username = "";
            String password = "";
            boolean connected = false;
            while (!connected) {
                // Lecture des identifiants de l'utilisateur
                String[] identifiants = in.readUTF().split((" "));
                username = identifiants[0];
                password = identifiants[1];

                // Verify the password in the login.txt file
                // Send an authentification message
                BufferedReader reader;
                try {
                    reader = new BufferedReader(new FileReader(filePath));
                    String line = reader.readLine();
                    // Crawl the logins file to check the password
                    while (line != null && connected == false) {
                        if (line.equals(username + " : " + password)) {
                            connected = true;
                            out.writeUTF("Authentification granted");
                        }
                        line = reader.readLine();
                    }
                    reader.close();
                    if (connected == false) {
                        out.writeUTF("Incorrect username/password");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            // Retrieve the image and treatment
            String image_name = in.readUTF();
            String message_read = in.readUTF();
            if (message_read.equals("Read image successfully")) {
                Date now = new Date();
                System.out.println(now);
                System.out.println("[" + username + " - " + serverAddress + ":" + serverPort + " - " + now + "] : Image " + image_name + " pour traitement.");
                BufferedImage image = ImageIO.read(socket.getInputStream());
                BufferedImage generated_image = Sobel.process(image);
                // Send the generated image

                ImageIO.write(generated_image, "PNG", socket.getOutputStream());
            }

        } catch (IOException e) {
            System.out.println("Error handling client " + " : Exception : " + e);
        } finally {
            try {
                // Fermeture de la communication avec le client
                socket.close();
            } catch (IOException e) {
                System.out.println("Could not close a socket");
            }
            System.out.println("Connection with client closed successfully");
        }
    }
}

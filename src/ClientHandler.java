import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.StringTokenizer;

public class ClientHandler extends Thread {

    private final Socket client;
    private final ChatServer chatServer;
    private String name;
    private DataOutputStream dataOut;
    private DataInputStream dataIn;

    public ClientHandler(Socket client, ChatServer chatServer) {
        this.chatServer = chatServer;
        this.client = client;
    }

    @Override
    public void run() {

        try {
            //creating input stream and output stream
            this.dataOut = new DataOutputStream(client.getOutputStream());
            this.dataIn = new DataInputStream(client.getInputStream());

            //send names of all users currently online
            this.printUsers();

            //get user's name
            this.name = dataIn.readUTF();
            chatServer.addUser(name, this);

            //send to all, that there is new user
            chatServer.sendToAll("New user on server: [" + name + "]", this);

            //MAIN
            String clientMessage;

            //while socket is open
            do {
                clientMessage = dataIn.readUTF();
                StringTokenizer stringTokenizer = new StringTokenizer(clientMessage, "#");
                String receiverName = stringTokenizer.nextToken();
                String message = stringTokenizer.nextToken();
                chatServer.sendToUser(message, receiverName, this.name);
            } while (!clientMessage.equalsIgnoreCase("disconnect"));

            //disconnect
            chatServer.removeUser(this.name);
            client.close();
            chatServer.sendToAll("[" + this.name + "] left server", this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printUsers() {
        if (chatServer.hasUsers()) {
            this.sendMessage("Users on server: " + chatServer.getUserNames());
        } else {
            this.sendMessage("No users on server.");
        }
    }

    public void sendMessage(String message) {
        try {
            dataOut.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

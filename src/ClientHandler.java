import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class ClientHandler extends Thread {

    private final Socket client;
    private final ChatServer chatServer;
    private String name;
    private PrintWriter dataOut;
    private BufferedReader dataIn;
    private ClientHandler companion = null;

    public ClientHandler(Socket client, ChatServer chatServer) {
        this.chatServer = chatServer;
        this.client = client;
    }

    @Override
    public void run() {

        try {
            //creating input stream and output stream
            InputStream input = client.getInputStream();
            this.dataIn = new BufferedReader(new InputStreamReader(input));

            OutputStream output = client.getOutputStream();
            this.dataOut = new PrintWriter(output, true);

            //get name of new user
            this.registerUser();

            //Select client for dialog

            this.chooseCompanion();

            //dialog

            this.startDialog();
        } catch (SocketException e) {
            System.out.println("Client disconnected");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registerUser() throws IOException {

        printUsers();

        this.name = dataIn.readLine();
        chatServer.addUser(this.name, this);
        chatServer.addFreeUser(this.name, this);

        chatServer.sendToAll("New user on server: [" + name + "]", this);
    }

    public void chooseCompanion() throws IOException {

        printFreeUsers();

        String name;
        if (chatServer.hasFreeUsers()) {
            sendMessage("Enter name of your companion:");
            name = dataIn.readLine();
            this.setCompanion(chatServer.getFreeUser(name));
            companion.setCompanion(this);
            chatServer.removeFreeUser(companion.getUserName());
            chatServer.removeFreeUser(this.name);
        } else {
            sendMessage("There are no free users. Please wait.");
            //String clientMessage;
            while (this.companion == null) {
                try {
                    this.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            sendMessage("You are invited to chat with " + companion.getUserName());
        }
    }

    public void startDialog() throws IOException {
        String clientMessage;
        //while socket is open
        do {
            clientMessage = dataIn.readLine();
            chatServer.sendToUser(clientMessage, companion, this.name);
        } while (!clientMessage.equalsIgnoreCase(ChatServer.STOP_WORD) || this.companion != null);

        if (clientMessage.equalsIgnoreCase(ChatServer.STOP_WORD)) {
            //disconnect
            chatServer.removeUser(this.name);
            chatServer.addFreeUser(companion.getUserName(), companion);
            companion.sendMessage("Your companion disconnects");
            companion.setCompanion(null);
            chatServer.sendToAll("[" + this.name + "] left server", this);
            System.out.println("Users on server: " + chatServer.getUserNames().size());
            client.close();
        } else {
            chooseCompanion();
            startDialog();
        }
    }

    private void printUsers() {
        if (chatServer.hasUsers()) {
            this.sendMessage("Users on server: " + chatServer.getUserNames());
        } else {
            this.sendMessage("No users on server.");
        }
    }

    private void printFreeUsers() {
        if (chatServer.getFreeUserNames(this.name).size() > 0) {
            this.sendMessage("Free users on server: " + chatServer.getFreeUserNames(this.name));
        } else {
            this.sendMessage("No free users on server.");
        }
    }

    public void sendMessage(String message) {
            dataOut.println(message);
    }

    public String getUserName() {
        return this.name;
    }

    public void setCompanion(ClientHandler companion) {
        this.companion = companion;
    }
}

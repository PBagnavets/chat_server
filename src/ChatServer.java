import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {

    private static int port = 8080;
    private Map<String, ClientHandler> users = new ConcurrentHashMap<>();
    private Map<String, List<ClientHandler>> rooms = new ConcurrentHashMap<>();

    //public void sendToAll
    private ChatServer(int port) {
        ChatServer.port = port;
    }

    public static void main(String[] args) {
        if (args.length == 1) {
            ChatServer.setPort(Integer.parseInt(args[0]));
        }
        ChatServer chatServer = new ChatServer(port);
        System.out.println("Server started. Port : " + port);
        chatServer.runServer();
    }

    private void runServer() {
        try (ServerSocket serverSocket = new ServerSocket(port))
        {
            while (!serverSocket.isClosed()) {

                //close server via server console

                Socket client = serverSocket.accept();
                System.out.println("New client request: " + client);

                ClientHandler newUser = new ClientHandler(client, this);
                newUser.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendToAll(String message, ClientHandler excludeUser) {
        for (ClientHandler user : users.values()) {
            if (user != excludeUser) {
                user.sendMessage(message);
            }
        }
    }

    public void sendToUser(String message, String receiverName, String senderName) {
        ClientHandler receiver = users.get(receiverName);
        if (receiver != null) {
            receiver.sendMessage(senderName + " << " + message);
        } else {
            users.get(senderName).sendMessage("SERVER >> No such user");
        }
    }

    private static void setPort(int port) {
        ChatServer.port = port;
    }

    public void addUser(String name, ClientHandler client) {
        users.put(name, client);
    }

    public void removeUser(String name) {
        users.remove(name);
    }

    public boolean hasUsers() {
        return !this.users.isEmpty();
    }

    public Set<String> getUserNames() {
        return this.users.keySet();
    }

    public boolean hasThisName(String name) {
        return users.containsKey(name);
    }
}

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {

    private static final int port = 8080;
    public static final String STOP_WORD = "bye";
    private final Map<String, ClientHandler> users = new ConcurrentHashMap<>();
    private final Map<String, ClientHandler> freeUsers = new ConcurrentHashMap<>();
//    private final Map<String, List<ClientHandler>> rooms = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer();
        System.out.println("Server started. Port : " + port);
        chatServer.runServer();
    }

    private void runServer() {
        try (ServerSocket serverSocket = new ServerSocket(port))
        {
            while (!serverSocket.isClosed()) {

                //close server via server console

                Socket client = serverSocket.accept();
                //System.out.println("New client request: " + client);

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

    public void sendToUser(String message, ClientHandler receiver, String senderName) {
            receiver.sendMessage(senderName + " << " + message);
    }

    public void addUser(String name, ClientHandler client) {
        users.put(name, client);
    }

    public void addFreeUser(String name, ClientHandler client) {
        freeUsers.put(name, client);
    }

    public void removeUser(String name) {
        users.remove(name);
    }

    public void removeFreeUser(String name) {
        freeUsers.remove(name);
    }

    public boolean hasUsers() {
        return !this.users.isEmpty();
    }

    public boolean hasFreeUsers() {
        return this.freeUsers.size() > 1;
    }

    public Set<String> getUserNames() {
        return this.users.keySet();
    }

    public Set<String> getFreeUserNames(String name) {
        Set<String> set = new HashSet<>(freeUsers.keySet());
        set.remove(name);
        return set;
    }

    public boolean hasThisName(String name) {
        return users.containsKey(name);
    }

    public boolean hasThisFreeName(String name) {
        return freeUsers.containsKey(name);
    }

    public ClientHandler getFreeUser(String name) {
        return freeUsers.get(name);
    }
}

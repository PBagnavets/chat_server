import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ClientHandler extends Thread {

    private final Socket client;
    private final ChatServer chatServer;
    private String name;
    private PrintWriter dataOut;
    private BufferedReader dataIn;
    private ClientHandler companion = null;
    private static Lock lock = new ReentrantLock();
    private static Condition noCompanion = lock.newCondition();

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
            registerUser();

            //Select client for dialog

            chooseCompanion();

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
        lock.lock();
        printFreeUsers();
        try {
            if (chatServer.hasFreeUsers()) {
                inviteCompanion();
            } else {
                waitForInvite();
            }
        } finally {
            lock.unlock();
        }
    }

    private void waitForInvite() {

        sendMessage("There are no free users. Please wait.");
        try {
            while (this.companion == null)
                //synchronized (this) { this.wait(); }
                noCompanion.await();
        } catch (InterruptedException e) {
            System.out.println("SOMETHING WRONG WITH LOCK");
            e.printStackTrace();
        }

        sendMessage("You are invited to chat with " + companion.getUserName());
    }

    private void inviteCompanion() throws IOException {
        String name;
        sendMessage("Enter name of your companion:");
        name = dataIn.readLine();
        this.setCompanion(chatServer.getFreeUser(name));
        companion.setCompanion(this);
        chatServer.removeFreeUser(companion.getUserName());
        chatServer.removeFreeUser(this.name);
        //synchronized (this.companion) { this.companion.notifyAll(); }
        noCompanion.signalAll();

    }

    public void startDialog() throws IOException {
        String clientMessage = null;
        //while socket is open
        while (companion != null) {
            clientMessage = dataIn.readLine();
            if (!clientMessage.equalsIgnoreCase(ChatServer.STOP_WORD))
                chatServer.sendToUser(clientMessage, companion, this.name);
            else break;
        }

        if (clientMessage.equalsIgnoreCase(ChatServer.STOP_WORD)) {
            //disconnect
            chatServer.addFreeUser(companion.getUserName(), companion);
            chatServer.sendToAll("[" + this.name + "] left server", this);
            companion.sendMessage("Your companion disconnects");
            chatServer.removeUser(this.name);
            companion.setCompanion(null);
            System.out.println("Users on server: " + chatServer.getUserNames().size());
            client.close();
        }
        chooseCompanion();
        startDialog();
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

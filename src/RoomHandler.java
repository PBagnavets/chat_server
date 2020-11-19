import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RoomHandler extends Thread {

    private final String roomName;
    private final int capacity;
    private List<ClientHandler> roomUsers;

    public RoomHandler(String name, int capacity, ClientHandler user) throws IOException{
        this.roomName = name;
        this.capacity = capacity;
        this.roomUsers = new ArrayList<>(capacity);
        this.addUser(user);
    }

    @Override
    public void run() {
        super.run();
    }

    public void addUser(ClientHandler user) throws IOException {
        if (this.capacity < this.roomUsers.size()) {
            this.roomUsers.add(user);
        } else {
            user.sendMessage("Room is full");
        }
    }

}

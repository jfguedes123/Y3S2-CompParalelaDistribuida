package Server;

import java.util.ArrayList;


public class RoomManager {

    static ArrayList<Room> rooms;
    static Integer idGenerator;

    public RoomManager() {
        this.rooms = new ArrayList<>();
        this.idGenerator = 0;
    }

    public static Room createRRoom(ArrayList<ServerClient> players) {
        Room room = new Room(idGenerator);

        System.out.println("Room created with id: " + idGenerator);
        rooms.add(room);
        for (ServerClient p : players){
            room.addClient(p);
        }
        idGenerator++;
        return room;
    }
}

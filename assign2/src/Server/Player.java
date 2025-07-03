package Server;

import java.util.UUID;
import java.util.Comparator;

public class Player implements Comparable<Player> {

    String id;
    String email;
    String password;
    String name;
    Room room;
    int score;
    int rank;
    int interval = 0;
    int timeInQueue;
    boolean online = true;


    public void Player(){
        this.id = null;
        this.email = "";
        this.password = "";
        this.name = "";
        this.interval = 0;
        this.score = 0;
        this.rank = 0;
        this.timeInQueue = 0;
        this.online = true;
        this.room = null;
    }

    public void Player(String id, String email, String password, String name, int rank, int interval){
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.rank = rank;
        this.interval = interval;
        this.online = true;
    }

    public synchronized void expandInterval(){
        this.interval++;
        System.out.println("Interval expanded to: " + this.interval);
    }

    public boolean calculateRange(int rankk){
        return rankk <= this.rank + this.interval && rankk >= this.rank - this.interval;
    }

    public void setRoomNull(){
        this.room = null;
    }

    public void setRoom(Room room){
        this.room = room;
    }

    public Room getRoom(){
        return this.room;
    }

    public synchronized void setOnline(boolean online){
        this.online = online;
    }

    public synchronized boolean isOnline(){
        return this.online;
    }

    public int getRank(){
        return this.rank;
    }

    public void setRank(int rank){
        this.rank = rank;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public String getId(){
        return this.id;
    }

    public void setId(String id){
        this.id = id;
    }

    public int getTimeInQueue(){
        return this.timeInQueue;
    }

    public void setTimeInQueue(int timeInQueue){
        this.timeInQueue = timeInQueue;
    }


    @Override
    public int compareTo(Player other) {
        int rankComparison = Integer.compare(this.rank, other.rank);
        if (rankComparison!= 0) {
            return rankComparison;
        } else {
            return Long.compare(this.timeInQueue, other.timeInQueue);
        }
    }
}

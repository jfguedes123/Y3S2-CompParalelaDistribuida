package Server;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.Queue;
import java.util.LinkedList;

public class PlayerWaitingQueue {
    private final Queue<ServerClient> queue = new LinkedList<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public void addPlayer(ServerClient player) {
        lock.writeLock().lock();
        try {
            queue.add(player);
            System.out.println("added someone to the queue");
            System.out.println(queue.size());
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Queue<ServerClient> getQueue() {
        return queue;
    }

    public int getQueueSize() {
        lock.readLock().lock();
        try {
            return queue.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean contains(ServerClient player) {
        lock.readLock().lock();
        try {
            return queue.contains(player);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void removePlayer(ServerClient player) {
        lock.writeLock().lock();
        try {
            queue.remove(player);
            System.out.println("removed from the queue");
            System.out.println(queue.size());
        } finally {
            lock.writeLock().unlock();
        }
    }
}


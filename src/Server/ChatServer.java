package Server;

import Interfaces.BulletinBoard;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;

public class ChatServer extends UnicastRemoteObject implements BulletinBoard {
    private final int size;
    private final ArrayList<HashMap<Integer, byte[]>> board;

    public ChatServer(int size) throws RemoteException {
        super();
        this.size = size;
        this.board = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            this.board.add(new HashMap<>());
        }
    }

    public synchronized void add(byte[] message, int index, int tag) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index out of bounds");
        }
        board.get(index).put(tag, message);
        System.out.println("Added a message to index " + index + " with tag " + tag);
    }

    public synchronized byte[] get(int index, int tag) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index out of bounds");
        }
        if (board.get(index).containsKey(tag)) {
            byte[] value = board.get(index).get(tag);
            board.get(index).remove(tag);
            System.out.println("Retrieved a message from index " + index + " with tag " + tag);
            return value;
        }
        return null;
    }
}

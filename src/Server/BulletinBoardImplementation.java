package Server;

import java.util.ArrayList;
import java.util.HashMap;

public class BulletinBoardImplementation {
    private final int size;
    private final ArrayList<HashMap<Integer, String>> board;

    public BulletinBoardImplementation(int size) {
        this.size = size;
        this.board = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            this.board.add(new HashMap<>());
        }
    }

    public synchronized void add(int index, String value, int tag) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index out of bounds");
        }
        board.get(index).put(tag, value);
        System.out.println("Added " + value + " to index " + index + " with tag " + tag);
    }

    public synchronized String get(int index, int tag) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index out of bounds");
        }
        if (board.get(index).containsKey(tag)) {
            String value = board.get(index).get(tag);
            System.out.println("Got " + value + " from index " + index + " with tag " + tag);
            return value;
        }
        System.out.println("Got null from index " + index + " with tag " + tag);
        return null;
    }
}

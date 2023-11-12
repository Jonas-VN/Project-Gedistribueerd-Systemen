package Server;

import Shared.BulletinBoard;
import Shared.Utils;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

public class ChatServer extends UnicastRemoteObject implements BulletinBoard {
    private final int SIZE;
    private final ArrayList<HashMap<String, byte[]>> board;

    public ChatServer(int size) throws RemoteException {
        super();
        this.SIZE = size;
        this.board = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            this.board.add(new HashMap<>());
        }
    }

    public int getSize() {
        return SIZE;
    }

    public synchronized void add(byte[] message, int index, byte[] tag) throws NoSuchAlgorithmException {
        if (index < 0 || index >= this.SIZE) {
            throw new IndexOutOfBoundsException("Index out of bounds");
        }

        byte[] hashedTag = hash(tag);
        // de hashedTag als key werkt niet omdat er geen equals methode is voor byte arrays? dus kijkt ie naar het object zelf ipv de inhoud
        String tagString = Utils.tagToBase64(hashedTag);
        System.out.println("[+] Added a message to index " + index + " with hashed tag " + tagString);
        board.get(index).put(tagString, message);
    }

    public synchronized byte[] get(int index, byte[] tag) throws NoSuchAlgorithmException {
        if (index < 0 || index >= this.SIZE) {
            throw new IndexOutOfBoundsException("Index out of bounds");
        }

        byte[] hashedTag = hash(tag);
        String tagString = Utils.tagToBase64(hashedTag);
        System.out.println("[*] Attempting to retrieve a message from index " + index + " with hashed tag " + tagString);
        if (board.get(index).containsKey(tagString)) {
            byte[] value = board.get(index).get(tagString);
            board.get(index).remove(tagString);
            System.out.println("[-] Retrieved a message from index " + index);
            return value;
        }
        System.out.println("[!] Failed to retrieve a message from index " + index);
        return null;
    }

    private byte[] hash(byte[] message) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(message);
    }
}

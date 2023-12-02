package Server;

import Shared.BulletinBoard;
import Shared.Utils;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

public class ChatServer extends UnicastRemoteObject implements BulletinBoard {
    private final int SIZE;
    private final ArrayList<ConcurrentHashMap<String, byte[]>> board;
    private final ArrayList<ConcurrentHashMap<String, Semaphore>> mutexPerTag = new ArrayList<>();
    private final Semaphore tagsToIgnoreMutex = new Semaphore(1);
    private final ArrayList<String> tagsToIgnore = new ArrayList<>();


    public ChatServer(int size) throws RemoteException {
        super();
        System.out.println("[*] Initializing ChatServer");
        this.SIZE = size;
        this.board = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            this.board.add(new ConcurrentHashMap<>());
            this.mutexPerTag.add(new ConcurrentHashMap<>());
        }
        System.out.println("[+] Initialized ChatServer");
    }

    public int getSize() {
        return SIZE;
    }

    public void add(byte[] message, int index, byte[] tag) throws NoSuchAlgorithmException {
        if (index < 0 || index >= this.SIZE) {
            throw new IndexOutOfBoundsException("Index out of bounds");
        }
        byte[] hashedTag = hash(tag);
        String tagString = Utils.bytesToBase64(hashedTag);

        board.get(index).put(tagString, message);
        System.out.println("[+] Added a message to index " + index + " with hashed tag " + tagString);
        if (mutexPerTag.get(index).containsKey(tagString)) {
            // A thread was already waiting → release de semaphore
            mutexPerTag.get(index).get(tagString).release();
        }
        else {
            // No thread was waiting → make a new semaphore for the receiving thread
            mutexPerTag.get(index).put(tagString, new Semaphore(1));
        }
    }

    public byte[] get(int index, byte[] tag) throws NoSuchAlgorithmException, InterruptedException {
        if (index < 0 || index >= this.SIZE) {
            throw new IndexOutOfBoundsException("Index out of bounds");
        }
        byte[] hashedTag = hash(tag);
        String tagString = Utils.tagToBase64(hashedTag);

        if (!this.mutexPerTag.get(index).containsKey(tagString)) {
            // The add method didn't add the message yet -> make a new semaphore
            this.mutexPerTag.get(index).put(tagString, new Semaphore(0));
        }

        // Wait for this tag specific semaphore to be released
        System.out.println("[*] Waiting for a message to be added to index " + index + " with tag " + tagString);
        this.mutexPerTag.get(index).get(tagString).acquire();

        this.tagsToIgnoreMutex.acquire();
        if (this.tagsToIgnore.contains(tagString)) {
            System.out.println("[!] Ignoring a message with tag " + tagString);
            this.tagsToIgnore.remove(tagString);
            this.tagsToIgnoreMutex.release();

            // Release own permit and the permit for the thread that needs this message
            this.mutexPerTag.get(index).get(tagString).release(2);
            return null;
        }
        this.tagsToIgnoreMutex.release();
        this.mutexPerTag.get(index).get(tagString).release();
        this.mutexPerTag.get(index).remove(tagString);

        byte[] value = board.get(index).get(tagString);
        board.get(index).remove(tagString);
        System.out.println("[-] Retrieved a message from index " + index + " with tag " + tagString);
        return value;
    }

    public void ignoreTag(byte[] tag) throws NoSuchAlgorithmException, InterruptedException {
        byte[] hashedTag = hash(tag);
        String tagString = Utils.tagToBase64(hashedTag);
        System.out.println("[*] Ignoring a tag " + tagString);
        this.tagsToIgnoreMutex.acquire();
        this.tagsToIgnore.add(tagString);
        this.tagsToIgnoreMutex.release();
    }

    private byte[] hash(byte[] message) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(message);
    }
}

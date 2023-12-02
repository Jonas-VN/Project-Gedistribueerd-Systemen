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
import java.util.logging.Logger;

public class ChatServer extends UnicastRemoteObject implements BulletinBoard {
    private final Logger logger = Logger.getLogger(ChatServer.class.getName());
    private final int SIZE;
    private final ArrayList<ConcurrentHashMap<String, byte[]>> board;
    private final Semaphore[] semaphores;
    private final int[] numberOfWaitingThreads;
    private final Semaphore[] indexMutexes;
    private final Semaphore mutex = new Semaphore(1);
    private final ArrayList<String> tagsToIgnore = new ArrayList<>();


    public ChatServer(int size) throws RemoteException {
        super();
        this.logger.info("[*] Initializing ChatServer");
        this.SIZE = size;
        this.board = new ArrayList<>(size);
        this.semaphores = new Semaphore[size];
        this.numberOfWaitingThreads = new int[size];
        this.indexMutexes = new Semaphore[size];
        for (int i = 0; i < size; i++) {
            this.board.add(new ConcurrentHashMap<>());
            this.semaphores[i] = new Semaphore(0, true);
            this.numberOfWaitingThreads[i] = 0;
            this.indexMutexes[i] = new Semaphore(1);
        }
        this.logger.info("[+] Initialized ChatServer");
    }

    public int getSize() {
        return SIZE;
    }

    public void add(byte[] message, int index, byte[] tag) throws NoSuchAlgorithmException, InterruptedException {
        if (index < 0 || index >= this.SIZE) {
            throw new IndexOutOfBoundsException("Index out of bounds");
        }

        mutex.acquire();
        byte[] hashedTag = hash(tag);
        // de hashedTag als key werkt niet omdat er geen equals methode is voor byte arrays? dus kijkt ie naar het object zelf ipv de inhoud
        String tagString = Utils.tagToBase64(hashedTag);
        this.logger.info("[+] Added a message to index " + index + " with hashed tag " + tagString);
        board.get(index).put(tagString, message);

        this.indexMutexes[index].acquire();
        this.semaphores[index].release(this.numberOfWaitingThreads[index]);
        this.indexMutexes[index].release();

        mutex.release();
    }

    public byte[] get(int index, byte[] tag) throws NoSuchAlgorithmException, InterruptedException {
        if (index < 0 || index >= this.SIZE) {
            throw new IndexOutOfBoundsException("Index out of bounds");
        }
        byte[] hashedTag = hash(tag);
        String tagString = Utils.tagToBase64(hashedTag);

        this.indexMutexes[index].acquire();
        this.numberOfWaitingThreads[index]++;
        this.indexMutexes[index].release();

        while (!this.containsKey(index, tagString)) {
            this.logger.info("[*] Waiting for a message to be added to index " + index + " with tag " + tagString);
            this.semaphores[index].acquire();
        }

        this.indexMutexes[index].acquire();
        this.numberOfWaitingThreads[index]--;
        this.indexMutexes[index].release();

        mutex.acquire();
        if (this.tagsToIgnore.contains(Utils.tagToBase64(hashedTag))) {
            // Sort of interrupt the thread, so it can quit when the GUI is closed
            this.logger.info("[!] Ignoring a message with tag " + tagString);
            tagsToIgnore.remove(Utils.tagToBase64(hashedTag));
            mutex.release();
            return null;
        }
        byte[] value = board.get(index).get(tagString);
        board.get(index).remove(tagString);
        this.logger.info("[-] Retrieved a message from index " + index + " with tag " + tagString);
        mutex.release();
        return value;
    }

    public void ignoreTag(byte[] tag) throws NoSuchAlgorithmException, InterruptedException {
        byte[] hashedTag = hash(tag);
        String tagString = Utils.tagToBase64(hashedTag);
        this.logger.info("[*] Ignoring a tag " + tagString);
        mutex.acquire();
        this.tagsToIgnore.add(tagString);
        mutex.release();
    }

    private byte[] hash(byte[] message) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(message);
    }

    private synchronized boolean containsKey(int index, String tag) throws InterruptedException {
        this.indexMutexes[index].acquire();
        boolean containsKey = this.board.get(index).containsKey(tag);
        this.indexMutexes[index].release();
        return containsKey;
    }
}

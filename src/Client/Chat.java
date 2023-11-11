package Client;

import Client.Message.Message;
import Interfaces.BulletinBoard;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.*;
import java.util.ArrayList;


public class Chat {
    private final BulletinBoard chatServer;
    private final ArrayList<Message> messages = new ArrayList<>();
    private final String name;
    private PrivateKey privateKey;
    private PublicKey otherPublicKey;

    public Chat(String name) throws RemoteException, NotBoundException {
        this.name = name;
        Registry registry = LocateRegistry.getRegistry("localhost", 1099);
        this.chatServer = (BulletinBoard) registry.lookup("BulletinBoard");
    }

    public void mainLoop() throws NoSuchAlgorithmException {
        System.out.println(generateKeyPair());
    }

    // Returns the public key and keeps the private key
    private PublicKey generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        privateKey = keyPair.getPrivate();
        return keyPair.getPublic();
    }
}

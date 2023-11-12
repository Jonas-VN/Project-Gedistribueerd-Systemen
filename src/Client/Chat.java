package Client;

import Shared.BulletinBoard;
import Shared.Utils;

import javax.crypto.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.*;
import java.util.ArrayList;
import java.util.Scanner;


public class Chat {
    private final BulletinBoard chatServer;
    private final ArrayList<Message> messages = new ArrayList<>();
    private final String userName;
    private final UserInfo AB = new UserInfo();
    private final UserInfo BA = new UserInfo();

    public Chat(String userName) throws RemoteException, NotBoundException, NoSuchAlgorithmException {
        this.userName = userName;
        Registry registry = LocateRegistry.getRegistry("localhost", 1099);
        this.chatServer = (BulletinBoard) registry.lookup("ChatServer");

        this.AB.setSecretKey(UserInfo.generateKey());
        this.AB.setIndex(UserInfo.generateIndex(this.chatServer.getSize()));
        this.AB.setTag(UserInfo.generateTag());
    }

    public void setup(SecretKey secretKey, int index, byte[] tag) {
        this.BA.setSecretKey(secretKey);
        this.BA.setIndex(index);
        this.BA.setTag(tag);
    }

    public String getUserName() {
        return this.userName;
    }

    public ArrayList<Message> getMessages() {
        return this.messages;
    }

    public void sendMessage(String message) throws RemoteException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Message messageObject = new Message(message);
        int nextIndex = UserInfo.generateIndex(chatServer.getSize());
        messageObject.setIndex(nextIndex);

        byte[] nextTag = UserInfo.generateTag();
        messageObject.setTag(nextTag);

        byte[] encryptedMessage = messageObject.encrypt(this.AB.getSecretKey());
        this.chatServer.add(encryptedMessage, this.AB.getIndex(), this.AB.getTag());
        this.messages.add(messageObject);

        this.AB.deriveNewKey();
        this.AB.setIndex(nextIndex);
        this.AB.setTag(nextTag);
    }

    public void receiveMessage() throws RemoteException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        byte[] encryptedMessage = this.chatServer.get(this.BA.getIndex(), this.BA.getTag());
        Message message = Message.decrypt(encryptedMessage, this.BA.getSecretKey());
        this.messages.add(message);

        this.BA.deriveNewKey();
        this.BA.setIndex(message.getIndex());
        this.BA.setTag(message.getTag());
    }

    public void loop() {
        Scanner scanner = new Scanner(System.in);
        System.out.println(AB);
        System.out.println("Key: ");
        SecretKey secretKey = Utils.base64ToKey(scanner.nextLine());
        System.out.println("Index: ");
        int index = scanner.nextInt();
        scanner.nextLine(); // Consumes the \n
        System.out.println("Tag: ");
        byte[] tag = Utils.base64ToTag(scanner.nextLine());
        this.setup(secretKey, index, tag);
        System.out.println("Completed setup, let's chat!");

        while (true) {
            try {
                System.out.println("Enter message: ");
                String message = scanner.nextLine();
                this.sendMessage(message);
            } catch (Exception e) {
                System.out.println("Failed to send message: " + e.getMessage());
            }

            try {
                this.receiveMessage();
                System.out.println("Received message: " + this.messages.getLast().getMessage());
            } catch (Exception e) {
                System.out.println("Failed to receive message: " + e.getMessage());
            }
        }

    }
}

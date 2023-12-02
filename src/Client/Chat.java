package Client;

import Shared.BulletinBoard;

import javax.crypto.*;
import java.io.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Objects;


public class Chat implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final BulletinBoard chatServer;
    private final ArrayList<Message> messages = new ArrayList<>();
    private String userName;
    private final UserInfo AB = new UserInfo();
    private UserInfo BA = new UserInfo();

    public Chat() throws RemoteException, NotBoundException, NoSuchAlgorithmException {
        Registry registry = LocateRegistry.getRegistry("localhost", 1099);
        this.chatServer = (BulletinBoard) registry.lookup("ChatServer");

        this.AB.setSecretKey(UserInfo.generateKey());
        this.AB.setIndex(UserInfo.generateIndex(this.chatServer.getSize()));
        this.AB.setTag(UserInfo.generateTag());
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setup(UserInfo userInfo) {
        this.BA = userInfo;
    }

    public String getUserName() {
        return this.userName;
    }

    public ArrayList<Message> getMessages() {
        return this.messages;
    }

    public void ignoreBATag() throws RemoteException, NoSuchAlgorithmException, InterruptedException {
        this.chatServer.ignoreTag(this.BA.getTag());
    }

    public void sendMessage(Message message) throws RemoteException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException, InterruptedException {
        int nextIndex = UserInfo.generateIndex(chatServer.getSize());
        message.setIndex(nextIndex);

        byte[] nextTag = UserInfo.generateTag();
        message.setTag(nextTag);

        byte[] encryptedMessage = message.encrypt(this.AB.getSecretKey());
        this.chatServer.add(encryptedMessage, this.AB.getIndex(), this.AB.getTag());
        message.clearIndexAndTag();
        this.messages.add(message);

        this.AB.deriveNewKey(this.AB.getTag());
        this.AB.setIndex(nextIndex);
        this.AB.setTag(nextTag);
    }

    public Message receiveMessage() throws RemoteException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException, InterruptedException {
        byte[] encryptedMessage = this.chatServer.get(this.BA.getIndex(), this.BA.getTag());
        if (encryptedMessage == null) {
            return null;
        }
        Message message = Message.decrypt(encryptedMessage, this.BA.getSecretKey());
        message.setSentByMe(false);
        this.messages.add(message);

        this.BA.deriveNewKey(this.BA.getTag());
        this.BA.setIndex(message.getIndex());
        this.BA.setTag(message.getTag());
        message.clearIndexAndTag();
        return message;
    }

    @Override
    public String toString() {
        return this.userName;
    }

    public UserInfo getAB() {
        return AB;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Chat chat = (Chat) o;
        return Objects.equals(messages, chat.messages) && Objects.equals(userName, chat.userName) && Objects.equals(AB, chat.AB) && Objects.equals(BA, chat.BA);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messages, userName, AB, BA);
    }

    public static Chat readFromFile(String fileName) throws IOException {
        FileInputStream fileIn = new FileInputStream(fileName);
        byte[] serializedChat = fileIn.readAllBytes();
        return Chat.deserialize(serializedChat);
    }

    public void writeToFile(String fileName) throws IOException {
        FileOutputStream fileOut = new FileOutputStream(fileName);
        byte[] serializedChat = this.serialize();
        fileOut.write(serializedChat);
        fileOut.close();
    }

    public byte[] serialize(){
        try{
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(this);
            return byteArrayOutputStream.toByteArray();
        }catch (IOException ioe){
            System.err.println(ioe.getLocalizedMessage());
            return null;
        }
    }

    public static Chat deserialize(byte[] sChat){
        try{
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(sChat);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            Object o = objectInputStream.readObject();
            if(o instanceof Chat)
                return (Chat) o;
            else return null;
        }catch  (IOException | ClassNotFoundException ioe){
            System.err.println(ioe.getLocalizedMessage());
            return null;
        }
    }
}

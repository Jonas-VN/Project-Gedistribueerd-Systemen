package Client;

import Client.Messages.BulletinBoardMessage;
import Client.Messages.Message;
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
    private final CryptoMetaData AB = new CryptoMetaData();
    private CryptoMetaData BA = new CryptoMetaData();

    public Chat(BulletinBoard chatServer) throws RemoteException, NoSuchAlgorithmException {
        this.chatServer = chatServer;

        this.AB.setSecretKey(CryptoMetaData.generateKey());
        this.AB.setIndex(CryptoMetaData.generateIndex(this.chatServer.getSize()));
        this.AB.setTag(CryptoMetaData.generateTag());
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setup(CryptoMetaData userInfo) {
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
        int nextIndex = CryptoMetaData.generateIndex(chatServer.getSize());
        byte[] nextTag = CryptoMetaData.generateTag();
        BulletinBoardMessage bulletinBoardMessage = new BulletinBoardMessage(message, nextIndex, nextTag);

        byte[] encryptedMessage = bulletinBoardMessage.encrypt(this.AB.getSecretKey());
        this.chatServer.add(encryptedMessage, this.AB.getIndex(), this.AB.getTag());
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
        BulletinBoardMessage bulletinBoardMessage = BulletinBoardMessage.decrypt(encryptedMessage, this.BA.getSecretKey());
        Message message = bulletinBoardMessage.message();
        message.setSentByMe(false);
        this.messages.add(message);

        this.BA.deriveNewKey(this.BA.getTag());
        this.BA.setIndex(bulletinBoardMessage.index());
        this.BA.setTag(bulletinBoardMessage.tag());
        return message;
    }

    @Override
    public String toString() {
        return this.userName;
    }

    public CryptoMetaData getAB() {
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

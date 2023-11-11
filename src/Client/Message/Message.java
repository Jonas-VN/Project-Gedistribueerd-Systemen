package Client.Message;

import java.io.*;
import java.security.PublicKey;
import java.util.Objects;

public class Message implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String message;
    private final MessageMetaData metaData;

    public Message(String message, MessageMetaData metaData) {
        this.message = message;
        this.metaData = metaData;
    }

    public Message(String message, PublicKey publicKey, int index, int tag, boolean seen) {
        this.message = message;
        this.metaData = new MessageMetaData(publicKey, index, tag, seen);
    }

    public String getMessage() {
        return message;
    }

    public MessageMetaData getMetaData() {
        return metaData;
    }

    public byte[] serialize() {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(this);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        }
    }

    public static Message deserialize(byte[] encryptedMessage) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(encryptedMessage);
             ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
            Object o = objectInputStream.readObject();
            if (o instanceof Message) return (Message) o;
            else return null;
        } catch (IOException | ClassNotFoundException ioe) {
            ioe.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        return "Message{" +
                "message='" + message + '\'' +
                ", metaData=" + metaData +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message1 = (Message) o;
        return Objects.equals(message, message1.message) && Objects.equals(metaData, message1.metaData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, metaData);
    }
}

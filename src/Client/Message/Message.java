package Client.Message;

import java.io.*;
import java.util.Objects;

public class Message implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String message;
    private int index;
    private int tag;


    public Message(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public int getIndex() {
        return index;
    }

    public int getTag() {
        return tag;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setTag(int tag) {
        this.tag = tag;
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
                ", index=" + index +
                ", tag=" + tag +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message1 = (Message) o;
        return index == message1.index && tag == message1.tag && Objects.equals(message, message1.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, index, tag);
    }
}

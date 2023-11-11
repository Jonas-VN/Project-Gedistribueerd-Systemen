package Client.Message;

import java.io.*;
import java.security.PublicKey;
import java.util.Objects;

public class MessageMetaData implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final PublicKey publicKey;
    private final int index;
    private final int tag;
    private boolean seen;

    public MessageMetaData(PublicKey publicKey, int index, int tag, boolean seen) {
        this.publicKey = publicKey;
        this.index = index;
        this.tag = tag;
        this.seen = seen;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public int getIndex() {
        return index;
    }

    public int getTag() {
        return tag;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
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

    public static MessageMetaData deserialize(byte[] encryptedMessage) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(encryptedMessage);
             ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
            Object o = objectInputStream.readObject();
            if (o instanceof Message) return (MessageMetaData) o;
            else return null;
        } catch (IOException | ClassNotFoundException ioe) {
            ioe.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        return "MessageMetaData{" +
                "index=" + index +
                ", tag=" + tag +
                ", seen=" + seen +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageMetaData that = (MessageMetaData) o;
        return index == that.index && tag == that.tag && seen == that.seen && Objects.equals(publicKey, that.publicKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(publicKey, index, tag, seen);
    }
}

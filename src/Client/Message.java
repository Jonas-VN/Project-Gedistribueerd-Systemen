package Client;

import Shared.Utils;

import javax.crypto.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;

public class Message {
    private static final String SEPARATOR = "§";

    private final String message;
    private int index;
    private byte[] tag;


    public Message(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public int getIndex() {
        return index;
    }

    public byte[] getTag() {
        return tag;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setTag(byte[] tag) {
        this.tag = tag;
    }

    public String getString() {
        return this.message + SEPARATOR + this.index + SEPARATOR + Utils.tagToBase64(this.tag);
    }

    public static Message fromString(String message) {
        String[] split = message.split(SEPARATOR);
        Message m = new Message(split[0]);
        m.setIndex(Integer.parseInt(split[1]));
        m.setTag(Utils.base64ToTag(split[2]));
        return m;
    }

    public byte[] encrypt(SecretKey secretKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(this.getString().getBytes());
    }

    public static Message decrypt(byte[] encryptedMessage, SecretKey secretKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return Message.fromString(new String(cipher.doFinal(encryptedMessage)));
    }

    @Override
    public String toString() {
        return "Message{" +
                "message='" + message + '\'' +
                ", index=" + index +
                ", tag=" + new String(tag) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message1 = (Message) o;
        return index == message1.index && Objects.equals(message, message1.message) && Arrays.equals(tag, message1.tag);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(message, index);
        result = 31 * result + Arrays.hashCode(tag);
        return result;
    }
}

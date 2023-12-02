package Client.Messages;

import Shared.Utils;

import javax.crypto.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public record BulletinBoardMessage(Message message, int index, byte[] tag) {
    private static final String SEPARATOR = "§";

    public String getString() {
        return this.message.getMessage() + SEPARATOR + this.index + SEPARATOR + Utils.tagToBase64(this.tag);
    }

    public static BulletinBoardMessage fromString(String string) {
        String[] parts = string.split(SEPARATOR);
        String message = parts[0];
        int index = Integer.parseInt(parts[1]);
        byte[] tag = Utils.base64ToTag(parts[2]);
        return new BulletinBoardMessage(new Message(message), index, tag);
    }

    public byte[] encrypt(SecretKey secretKey) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(this.getString().getBytes());
    }

    public static BulletinBoardMessage decrypt(byte[] encryptedMessage, SecretKey secretKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return BulletinBoardMessage.fromString(new String(cipher.doFinal(encryptedMessage)));
    }

    @Override
    public String toString() {
        return "BulletinBoardMessage{" +
                "message='" + message + '\'' +
                ", index=" + index +
                ", tag='" + Utils.bytesToBase64(tag) + '\'' +
                '}';
    }
}

package Client.Messages;

import Client.CryptoMetaData;
import org.junit.Test;

import javax.crypto.*;
import java.security.*;

import static org.junit.Assert.assertEquals;

public class MessageTest {
    @Test
    public void testGetStringAndFromString() {
        Message message = new Message("Hello");
        BulletinBoardMessage bulletinBoardMessage = new BulletinBoardMessage(message, CryptoMetaData.generateIndex(100), CryptoMetaData.generateTag());
        String serializedMessage = bulletinBoardMessage.getString();
        BulletinBoardMessage deserializedBulletinBoardMessage = BulletinBoardMessage.fromString(serializedMessage);
        Message deserializedMessage = deserializedBulletinBoardMessage.message();

        assertEquals(message, deserializedMessage);
        assertEquals(message.getMessage(), deserializedMessage.getMessage());
    }
    @Test
    public void testEncryption() throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        SecretKey secretKey = CryptoMetaData.generateKey();

        Message message = new Message("Hello");
        BulletinBoardMessage bulletinBoardMessage = new BulletinBoardMessage(message, CryptoMetaData.generateIndex(100), CryptoMetaData.generateTag());
        byte[] encryptedMessage = bulletinBoardMessage.encrypt(secretKey);
        BulletinBoardMessage decryptedBulletinBoardMessage = BulletinBoardMessage.decrypt(encryptedMessage, secretKey);
        Message decryptedMessage = decryptedBulletinBoardMessage.message();

        assertEquals(message, decryptedMessage);
        assertEquals(message.getMessage(), decryptedMessage.getMessage());
    }
}

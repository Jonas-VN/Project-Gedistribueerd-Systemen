package Client;


import Shared.Utils;
import org.junit.Test;

import javax.crypto.*;
import java.security.*;

import static org.junit.Assert.assertEquals;

public class MessageTest {
    @Test
    public void testGetStringAndFromString() {
        Message message = new Message("Hello");
        message.setIndex(UserInfo.generateIndex(100));
        message.setTag(UserInfo.generateTag());
        String serializedMessage = message.getString();
        Message deserializedMessage = Message.fromString(serializedMessage);

        assertEquals(message, deserializedMessage);
        assertEquals(message.getMessage(), deserializedMessage.getMessage());
    }
    @Test
    public void testEncryption() throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        SecretKey secretKey = UserInfo.generateKey();

        Message message = new Message("Hello");
        message.setIndex(UserInfo.generateIndex(100));
        message.setTag(UserInfo.generateTag());
        byte[] encryptedMessage = message.encrypt(secretKey);
        Message decryptedMessage = Message.decrypt(encryptedMessage, secretKey);

        assertEquals(message, decryptedMessage);
        assertEquals(message.getMessage(), decryptedMessage.getMessage());
    }
}

package Client.Message;


import org.junit.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;

import static org.junit.Assert.assertEquals;

public class MessageTest {
    @Test
    public void testSerialization() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen2 = KeyPairGenerator.getInstance("RSA");
        keyGen2.initialize(2048);
        KeyPair keyPair = keyGen2.generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();

        Message message = new Message("Hello", publicKey, 0, 0, false);
        byte[] serializedMessage = message.serialize();
        Message deserializedMessage = Message.deserialize(serializedMessage);

        assert deserializedMessage != null;
        assertEquals(message, deserializedMessage);
        assertEquals(message.getMessage(), deserializedMessage.getMessage());
    }
    @Test
    public void testEncryption() throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        KeyPairGenerator keyGen2 = KeyPairGenerator.getInstance("RSA");
        keyGen2.initialize(2048);
        KeyPair keyPair = keyGen2.generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        Message message = new Message("Hello", publicKey, 0, 0, false);
        byte[] encryptedMessage = MessageEncryptor.encrypt(message, publicKey);
        Message decryptedMessage = MessageEncryptor.decrypt(encryptedMessage, privateKey);

        assertEquals(message, decryptedMessage);
        assertEquals(message.getMessage(), decryptedMessage.getMessage());
    }
}

package Client.Message;


import java.security.*;
import java.util.Base64;

public class MessageTest {
    public static void main(String[] args) throws NoSuchAlgorithmException {
        testSerialization();
    }

    public static void testSerialization() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen2 = KeyPairGenerator.getInstance("RSA");
        keyGen2.initialize(2048);
        KeyPair keyPair = keyGen2.generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();

        Message message = new Message("Hello", publicKey, 0, 0, false);
        byte[] serializedMessage = message.serialize();
        System.out.println("Serialized bytes: \n" + Base64.getEncoder().encodeToString(serializedMessage));
        Message deserializedMessage = Message.deserialize(serializedMessage);
        assert deserializedMessage != null;
        System.out.println("Deserialized: " + deserializedMessage);
    }
}

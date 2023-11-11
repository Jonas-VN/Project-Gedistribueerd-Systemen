package Client;

import Client.Message.Message;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.util.ArrayList;


public class Chat {
    private final ArrayList<Message> messages = new ArrayList<>();
    private final String name;
    private PrivateKey privateKey;
    private PublicKey otherPublicKey;

    public Chat(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    // Returns the public key and keeps the private key
    public PublicKey generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        privateKey = keyPair.getPrivate();
        return keyPair.getPublic();
    }

    public void bump(PublicKey otherPublicKey, int index, int tag)  {
        this.messages.add(new Message("Welcome to this new chatroom"));
    }

    // Encrypt a message
    public byte[] encrypt(String message) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, otherPublicKey);
        return cipher.doFinal(message.getBytes());
    }
}

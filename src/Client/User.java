package Client;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class User {
    private String key;
    private String tag;
    private int index;

    public User(String key, String tag, int index) {
        this.key = key;
        this.tag = tag;
        this.index = index;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String password) {
        // Example: PBKDF2 with SHA-256
        try {
            int iterations = 10000;
            int keyLength = 256; // in bits

            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), tag.getBytes(), iterations, keyLength);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hashedKey = skf.generateSecret(spec).getEncoded();

            this.key = Base64.getEncoder().encodeToString(hashedKey);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace(); // Handle the exception appropriately in your code
        }
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}

package Client;

import Shared.Utils;
import jdk.jshell.execution.Util;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public class UserInfo {
    private SecretKey secretKey;
    private int index;
    private byte[] tag;

    public SecretKey getSecretKey() {
        return secretKey;
    }

    public int getIndex() {
        return index;
    }

    public byte[] getTag() {
        return tag;
    }

    public void setSecretKey(SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setTag(byte[] tag) {
        this.tag = tag;
    }
    public void deriveNewKey(byte[] salt) throws InvalidKeySpecException, NoSuchAlgorithmException {
        int iterations = 1000;
        KeySpec keySpec = new PBEKeySpec(Utils.keyToBase64(this.secretKey).toCharArray(), salt, iterations, 256);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] derivedKeyBytes = keyFactory.generateSecret(keySpec).getEncoded();
        this.secretKey = Utils.bytesToKey(derivedKeyBytes);
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "secretKey=" + Utils.keyToBase64(secretKey) +
                ", index=" + index +
                ", tag=" + Utils.tagToBase64(tag) +
                '}';
    }

    public static SecretKey generateKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128, new SecureRandom());
        return keyGenerator.generateKey();
    }

    public static byte[] generateTag() {
        return new SecureRandom().generateSeed(16);
    }

    public static int generateIndex(int size) {
        return new SecureRandom().nextInt(size);
    }
}

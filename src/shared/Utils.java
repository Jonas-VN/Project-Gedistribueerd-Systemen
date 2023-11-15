package shared;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Utility class for cryptographic operations.
 */
public class Utils {

    /**
     * Converts a SecretKey to its Base64 representation.
     *
     * @param key The SecretKey to be converted.
     * @return The Base64 representation of the key.
     */
    public static String keyToBase64(SecretKey key) {
        return bytesToBase64(key.getEncoded());
    }

    /**
     * Converts a Base64-encoded string to a SecretKey.
     *
     * @param key The Base64 representation of the key.
     * @return The SecretKey.
     */
    public static SecretKey base64ToKey(String key) {
        byte[] keyBytes = base64ToBytes(key);
        return bytesToKey(keyBytes);
    }

    /**
     * Converts a byte array to a SecretKey.
     *
     * @param keyBytes The byte array representing the key.
     * @return The SecretKey.
     */
    public static SecretKey bytesToKey(byte[] keyBytes) {
        return new SecretKeySpec(keyBytes, 0, keyBytes.length, "AES");
    }

    /**
     * Converts a byte array to its Base64 representation.
     *
     * @param //bytes The byte array to be converted.
     * @return The Base64 representation of the byte array.
     */
    public static String tagToBase64(byte[] tag) {
        return bytesToBase64(tag);
    }

    /**
     * Converts a Base64-encoded string to a byte array.
     *
     * @param base64 The Base64 representation to be converted.
     * @return The byte array.
     */
    public static byte[] base64ToTag(String base64) {
        return base64ToBytes(base64);
    }

    private static String bytesToBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    private static byte[] base64ToBytes(String base64) {
        return Base64.getDecoder().decode(base64);
    }
    public static String encrypt(String plainText, String key) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            SecretKey secretKey = base64ToKey(key);

            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());

            return bytesToBase64(encryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
}

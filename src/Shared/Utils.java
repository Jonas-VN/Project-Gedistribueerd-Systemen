package Shared;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class Utils {
    public static String keyToBase64(SecretKey key) {
        return bytesToBase64(key.getEncoded());
    }

    public static SecretKey base64ToKey(String key) {
        byte[] keyBytes = base64ToBytes(key);
        return new SecretKeySpec(keyBytes, 0, keyBytes.length, "AES");
    }

    public static String tagToBase64(byte[] tag) {
        return bytesToBase64(tag);
    }

    public static byte[] base64ToTag(String tag) {
        return base64ToBytes(tag);
    }

    private static String bytesToBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    private static byte[] base64ToBytes(String base64) {
        return Base64.getDecoder().decode(base64);
    }
}

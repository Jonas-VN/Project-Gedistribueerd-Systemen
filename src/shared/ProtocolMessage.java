package shared;

import java.util.Random;

public class ProtocolMessage {
    private String message; // Encrypted message
    private int index; // Next index to store the message
    private String tag; // Next tag

    public ProtocolMessage(String message, int index, String tag) {
        this.message = message;
        this.index = index;
        this.tag = tag;
    }

    // Copy constructor
    public ProtocolMessage(ProtocolMessage message) {
        this.message = message.getMessage();
        this.index = message.getIndex();
        this.tag = message.getTag();
    }

    public String getMessage() {
        return message;
    }

    public int getIndex() {
        return index;
    }

    public String getTag() {
        return tag;
    }

    public ProtocolMessage createMessage(String plainMessage, String key, String currentTag) {
        this.message = Utils.encrypt(plainMessage, key);

        // Generate a random index
        Random random = new Random();
        this.index = random.nextInt(1000);
        this.tag = generateNextTag(currentTag);

        return new ProtocolMessage(message, index, tag);
    }

    public String generateNextTag(String currentTag) {
        byte[] tagBytes = currentTag.getBytes(); // Convert the String to byte[]
        return Utils.tagToBase64(tagBytes);
    }

}

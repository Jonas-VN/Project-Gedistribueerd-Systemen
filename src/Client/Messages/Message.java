package Client.Messages;

import java.io.*;
import java.util.Objects;

public class Message implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final String message;
    private boolean sentByMe = true;

    public Message(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSentByMe() {
        return sentByMe;
    }

    public void setSentByMe(boolean sentByMe) {
        this.sentByMe = sentByMe;
    }

    @Override
    public String toString() {
        return "Message{" +
                "message='" + message + '\'' +
                "sentByMe='" + sentByMe + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message1 = (Message) o;
        return sentByMe == message1.sentByMe && Objects.equals(message, message1.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, sentByMe);
    }
}

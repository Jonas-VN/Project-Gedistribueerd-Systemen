package Server;

import shared.ProtocolMessage;

import java.util.HashMap;
import java.util.Map;

public class BulletinBoardEntry {
    private Map<String, ProtocolMessage> messages = new HashMap<>();

    public void addMessage(String tag, ProtocolMessage message) {
        messages.put(tag, new ProtocolMessage(message));
    }

    public ProtocolMessage getMessage(String tag) {
        return messages.get(tag);
    }

    public boolean containsMessage(String tag) {
        return messages.containsKey(tag);
    }
    public void removeMessage(String tag) {
        messages.remove(tag);
    }
}

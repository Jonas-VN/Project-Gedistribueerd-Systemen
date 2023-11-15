package Server;

import shared.ProtocolMessage;

public class BulletinBoard {
    private BulletinBoardEntry[] entries = new BulletinBoardEntry[1000];

    public void addMessage(int ix, String hashedTag, ProtocolMessage message) {
        if (entries[ix] == null) {
            entries[ix] = new BulletinBoardEntry();
        }
        entries[ix].addMessage(hashedTag, message);
    }

    public ProtocolMessage getMessage(int ix, String hashedTag) {
        BulletinBoardEntry entry = entries[ix];
        if (entry != null) {
            ProtocolMessage message = entry.getMessage(hashedTag);
            if (message != null) {
                // Message found, remove it from the BulletinBoardEntry
                entry.removeMessage(hashedTag);
            }
            return message;
        }
        return null; // Entry not found
    }
}

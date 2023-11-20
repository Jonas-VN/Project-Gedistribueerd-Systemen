package Client;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ChatGUI chatGUI = null;
            try {
                chatGUI = new ChatGUI();
            } catch (NotBoundException | NoSuchAlgorithmException | RemoteException | NoSuchPaddingException |
                     InvalidKeyException | InvalidKeySpecException | IllegalBlockSizeException | BadPaddingException e) {
                throw new RuntimeException(e);
            }
            chatGUI.setVisible(true);
        });

    }
}

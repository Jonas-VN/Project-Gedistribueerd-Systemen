package Client;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.*;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ChatGUI chatGUI;
            try {
                chatGUI = new ChatGUI();
            } catch (NotBoundException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidKeySpecException | IllegalBlockSizeException | BadPaddingException | IOException e) {
                throw new RuntimeException(e);
            }
            chatGUI.setVisible(true);
        });
    }
}

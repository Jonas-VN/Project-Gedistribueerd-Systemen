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
        // Gil: Username als startup argument meegeven zodat duidelijk is wie met wie chat. Ook als default gebruiken om settings op te slaan of te laden
        String userName = "?";
        if (args.length > 0 && !args[0].isEmpty()){
            userName = args[0];
        }

        String finalUserName = userName;
        SwingUtilities.invokeLater(() -> {
            ChatGUI chatGUI;
            try {
                chatGUI = new ChatGUI(finalUserName);
            } catch (NotBoundException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidKeySpecException | IllegalBlockSizeException | BadPaddingException | IOException e) {
                throw new RuntimeException(e);
            }
            chatGUI.setVisible(true);
        });
    }
}

package Client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;

public class Main {
    public static void main(String[] args) throws RemoteException, NotBoundException, NoSuchAlgorithmException {
        Chat chatClient = new Chat("Hallo");
        chatClient.mainLoop();
    }
}

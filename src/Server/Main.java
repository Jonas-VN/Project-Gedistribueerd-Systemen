package Server;

public class Main {
    public static void main(String[] args) {
        try {
            ChatServer chatServer = new ChatServer(10);
            java.rmi.registry.LocateRegistry.createRegistry(1099);
            java.rmi.Naming.rebind("ChatServer", chatServer);
            System.out.println("Server started");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package Client;

public class ReceiveThread extends Thread {
    private final ChatGUI chatGUI;
    private final Chat chat;
    private boolean running = true;

    public ReceiveThread(ChatGUI chatGUI, Chat chat) {
        this.chatGUI = chatGUI;
        this.chat = chat;
    }

    public void stopThread() {
        this.running = false;
    }

    @Override
    public void run() {
        while (this.running) {
            try {
                chatGUI.receiveMessage(chat);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                this.stopThread();
            }
        }
    }
}

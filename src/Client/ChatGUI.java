package Client;

import Client.Messages.Message;
import Shared.BulletinBoard;
import Shared.Utils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMISocketFactory;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

public class ChatGUI extends JFrame {
    private final BulletinBoard chatServer;
    private JTextArea chatArea;
    private JTextField messageInput;
    private final DefaultListModel<Chat> chatListModel = new DefaultListModel<>();
    private final JList<Chat> chatList = new JList<>(chatListModel);
    private final ArrayList<ReceiveThread> threads = new ArrayList<>();

    public ChatGUI() throws NotBoundException, NoSuchAlgorithmException, IOException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException, InvalidKeyException {
//        System.setProperty("proxySet", "true");
//        System.setProperty("socksProxyHost", "localhost");
//        System.setProperty("socksProxyPort", "9050");

        Registry registry = LocateRegistry.getRegistry("localhost", 1099);
        this.chatServer = (BulletinBoard) registry.lookup("ChatServer");

        setTitle("Chat Application");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(800, 600);

        chatList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        chatList.addListSelectionListener(e -> {
            try {
                initChatArea();
            } catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException |
                     BadPaddingException | InvalidKeySpecException | RemoteException | InvalidKeyException ex) {
                throw new RuntimeException(ex);
            }
        });

        JScrollPane chatListScrollPane = new JScrollPane(chatList);
        chatListScrollPane.setPreferredSize(new Dimension(100, 0));

        JButton bumpButton = new JButton("Bump");
        bumpButton.addActionListener(e -> {
            try {
                openBumpWindow();
            } catch (NotBoundException | NoSuchAlgorithmException | RemoteException ex) {
                throw new RuntimeException(ex);
            }
        });
        JPanel chatListPanel = new JPanel(new BorderLayout());
        chatListPanel.add(chatListScrollPane, BorderLayout.CENTER);
        chatListPanel.add(bumpButton, BorderLayout.SOUTH);

        // Create a split pane to separate the chat list and chat area
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chatListPanel, createChatAreaPanel());
        splitPane.setResizeWeight(0.1); // Adjust the initial size of the chat list

        add(splitPane, BorderLayout.CENTER);

        // Add a window listener to prompt the user when closing the main window
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int option = JOptionPane.showConfirmDialog(ChatGUI.this,
                        "Do you want to save the chat data?", "Save Data", JOptionPane.YES_NO_OPTION);
                if (option == JOptionPane.YES_OPTION) {
                    askForFileNameAndSave();
                }
                dispose(); // Close the window
                for (int i = 0; i < threads.size(); i++) {
                    threads.get(i).stopThread();
                    Chat chat = chatListModel.get(i);
                    try {
                        chat.sendDummyMessage();
                    } catch (RemoteException | NoSuchAlgorithmException | InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });

        int startupOption = JOptionPane.showConfirmDialog(ChatGUI.this,
                "Do you want to load chat data from a file?", "Load Data", JOptionPane.YES_NO_OPTION);
        if (startupOption == JOptionPane.YES_OPTION) {
            askForFileNameAndLoad();
        }
    }

    private void askForFileNameAndLoad() {
        JTextField fileNameField = new JTextField();

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Enter the filename to load chat data from:"), BorderLayout.NORTH);
        panel.add(fileNameField, BorderLayout.CENTER);

        int result = JOptionPane.showOptionDialog(ChatGUI.this, panel, "Load from File",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);

        if (result == JOptionPane.OK_OPTION) {
            String fileName = fileNameField.getText().trim();
            if (!fileName.isEmpty()) {
                setTitle("Chat Application - " + fileName);
                File directory = new File("Chats/" + fileName);
                if (directory.exists() && directory.isDirectory()) {
                    File[] files = directory.listFiles((dir, name) -> name.endsWith(".chat"));
                    if (files != null) {
                        for (File file : files) {
                            try {
                                Chat loadedChat = Chat.readFromFile(file.getAbsolutePath());
                                addChat(loadedChat);
                            } catch (IOException | NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException | InvalidKeySpecException | InvalidKeyException ignored) {}
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(ChatGUI.this,
                            "Please enter a valid directory name.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(ChatGUI.this,
                        "Please enter a valid directory name.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void askForFileNameAndSave() {
        JTextField fileNameField = new JTextField();

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Enter a filename to save:"), BorderLayout.NORTH);
        panel.add(fileNameField, BorderLayout.CENTER);

        int result = JOptionPane.showOptionDialog(ChatGUI.this, panel, "Save to File",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);

        if (result == JOptionPane.OK_OPTION) {
            String fileName = fileNameField.getText().trim();
            if (!fileName.isEmpty()) {
                File directory = new File("Chats/" + fileName);
                if (!directory.exists()) {
                    directory.mkdir();
                }
                for (Object object : chatListModel.toArray()) {
                    Chat chat = (Chat) object;
                    try {
                        chat.writeToFile("Chats/" + fileName + "/" + chat.getUserName() + ".chat");
                    } catch (IOException ignored) {}
                }
            } else {
                JOptionPane.showMessageDialog(ChatGUI.this, "Please enter a valid filename.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel createChatAreaPanel() {
        JPanel chatAreaPanel = new JPanel(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane chatAreaScrollPane = new JScrollPane(chatArea);
        chatAreaPanel.add(chatAreaScrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        messageInput = new JTextField();
        messageInput.addActionListener(e -> sendMessage());

        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(e -> sendMessage());

        bottomPanel.add(messageInput, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);
        chatAreaPanel.add(bottomPanel, BorderLayout.SOUTH);

        return chatAreaPanel;
    }

    private void initChatArea() throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeySpecException, RemoteException, InvalidKeyException {
        Chat selectedChat = chatList.getSelectedValue();
        if (selectedChat != null) {
            StringBuilder chatText = new StringBuilder();
            for (Message message : selectedChat.getMessages()) {
                if (message.isSentByMe()) chatText.append("You: ").append(message.getMessage()).append("\n");
                else chatText.append(selectedChat.getUserName()).append(": ").append(message.getMessage()).append("\n");
            }
            chatArea.setText(chatText.toString());
        }
    }
    public void receiveMessage(Chat chat) {
        Message receivedMessage = null;
        try {
            receivedMessage = chat.receiveMessage();
        }
        catch (Exception ignored){}
        if (receivedMessage == null) return;
        if (chatList.getSelectedValue() == chat) {
            chatArea.append(chat.getUserName() + ": " + receivedMessage.getMessage() + "\n");

            // Scroll to the bottom of the chat area
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        }
        // Else: the chat is not selected, so we don't need to update the chat area (the message already has been added to the list)
    }

    private void sendMessage() {
        Chat selectedChat = chatList.getSelectedValue();
        if (selectedChat != null && !messageInput.getText().isEmpty()) {
            Message message = new Message(messageInput.getText());
            try {
                selectedChat.sendMessage(message);
                chatArea.append("You: " + message.getMessage() + "\n");
                messageInput.setText("");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Failed to send message: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openBumpWindow() throws NotBoundException, NoSuchAlgorithmException, RemoteException {
        Chat newChat = new Chat(this.chatServer);
        JFrame bumpFrame = new JFrame("Bump Window");
        bumpFrame.setSize(500, 400);

        // Create components for the Bump window
        JTextField userNameInput = new JTextField();
        JButton copyKeyButton = new JButton("Copy");
        JButton copyIndexButton = new JButton("Copy");
        JButton copyTagButton = new JButton("Copy");
        JButton copyCSVButton = new JButton("Copy");
        JTextField keyInput = new JTextField();
        JTextField indexInput = new JTextField();
        JTextField tagInput = new JTextField();
        JTextField CSVInput = new JTextField();
        JButton okButton = new JButton("OK");

        // Set layout for the Bump window
        bumpFrame.setLayout(new GridLayout(10, 2, 15, 15));

        // Add components to the Bump window
        bumpFrame.add(new JLabel("Enter UserName:"));
        bumpFrame.add(userNameInput);
        bumpFrame.add(new JLabel("Key: " + Utils.keyToBase64(newChat.getAB().getSecretKey())));
        bumpFrame.add(copyKeyButton);
        bumpFrame.add(new JLabel("Index: " + newChat.getAB().getIndex()));
        bumpFrame.add(copyIndexButton);
        bumpFrame.add(new JLabel("Tag: " + Utils.tagToBase64(newChat.getAB().getTag())));
        bumpFrame.add(copyTagButton);
        bumpFrame.add(new JLabel("CSV: " + newChat.getAB().toCSV()));
        bumpFrame.add(copyCSVButton);
        bumpFrame.add(new JLabel("Enter Key"));
        bumpFrame.add(keyInput);
        bumpFrame.add(new JLabel("Enter Index"));
        bumpFrame.add(indexInput);
        bumpFrame.add(new JLabel("Enter Tag"));
        bumpFrame.add(tagInput);
        bumpFrame.add(new JLabel("Enter CSV"));
        bumpFrame.add(CSVInput);
        bumpFrame.add(okButton);

        // Add action listeners for copy buttons
        copyKeyButton.addActionListener(createCopyButtonListener(Utils.keyToBase64(newChat.getAB().getSecretKey())));
        copyIndexButton.addActionListener(createCopyButtonListener(String.valueOf(newChat.getAB().getIndex())));
        copyTagButton.addActionListener(createCopyButtonListener(Utils.tagToBase64(newChat.getAB().getTag())));
        copyCSVButton.addActionListener(createCopyButtonListener(newChat.getAB().toCSV()));

        // Add action listener for the "OK" button
        okButton.addActionListener(e -> {
            CryptoMetaData cryptoMetaData;
            if (CSVInput.getText() != null && !CSVInput.getText().isEmpty()) {
                cryptoMetaData = CryptoMetaData.fromCSV(CSVInput.getText());
            }
            else {
                cryptoMetaData = new CryptoMetaData();
                cryptoMetaData.setSecretKey(Utils.base64ToKey(keyInput.getText()));
                cryptoMetaData.setIndex(Integer.parseInt(indexInput.getText()));
                cryptoMetaData.setTag(Utils.base64ToTag(tagInput.getText()));
            }
            newChat.setUserName(userNameInput.getText());
            newChat.setup(cryptoMetaData);
            try {
                addChat(newChat);
            } catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException |
                     BadPaddingException | InvalidKeySpecException | RemoteException | InvalidKeyException ex) {
                throw new RuntimeException(ex);
            }
            bumpFrame.dispose();
        });

        // Set window properties
        bumpFrame.setLocationRelativeTo(chatArea);
        bumpFrame.setVisible(true);
        bumpFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void addChat(Chat chat) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeySpecException, RemoteException, InvalidKeyException {
        chatListModel.addElement(chat);
        chatList.setSelectedValue(chat, true);
        ReceiveThread thread = new ReceiveThread(this, chat);
        thread.start();
        threads.add(thread);
    }


    private ActionListener createCopyButtonListener(String text) {
        return e -> {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(new StringSelection(text), null);
        };
    }
}

package Client;

import Shared.Utils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

public class ChatGUI extends JFrame {
    private JTextArea chatArea;
    private JTextField messageInput;
    private DefaultListModel<Chat> chatListModel = new DefaultListModel<>();
    private final JList<Chat> chatList = new JList<>(chatListModel);

    public ChatGUI() throws NotBoundException, NoSuchAlgorithmException, RemoteException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException, InvalidKeyException {
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
        bumpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    openBumpWindow();
                } catch (NotBoundException | NoSuchAlgorithmException | RemoteException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        JPanel chatListPanel = new JPanel(new BorderLayout());
        chatListPanel.add(chatListScrollPane, BorderLayout.CENTER);
        chatListPanel.add(bumpButton, BorderLayout.SOUTH);

        // Create a split pane to separate the chat list and chat area
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chatListPanel, createChatAreaPanel());
        splitPane.setResizeWeight(0.1); // Adjust the initial size of the chat list

        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createChatAreaPanel() {
        JPanel chatAreaPanel = new JPanel(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane chatAreaScrollPane = new JScrollPane(chatArea);
        chatAreaPanel.add(chatAreaScrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        messageInput = new JTextField();
        messageInput.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

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
    private void recieveMessage(Chat chat) {
        Message receivedMessage = null;
        try {
            receivedMessage = chat.receiveMessage();
        }
        catch (Exception ignored){}
        assert receivedMessage != null;
        if (chatList.getSelectedValue() == chat) {
            chatArea.append(chat.getUserName() + ": " + receivedMessage.getMessage() + "\n");
        }
        // Else: the chat is not selected, so we don't need to update the chat area
    }

    private void sendMessage() {
        Chat selectedChat = chatList.getSelectedValue();
        if (selectedChat != null) {
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
        Chat newChat = new Chat();
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
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                UserInfo userInfo;
                if (CSVInput.getText() != null && !CSVInput.getText().isEmpty()) {
                    userInfo = UserInfo.fromCSV(CSVInput.getText());
                }
                else {
                    userInfo = new UserInfo();
                    userInfo.setSecretKey(Utils.base64ToKey(keyInput.getText()));
                    userInfo.setIndex(Integer.parseInt(indexInput.getText()));
                    userInfo.setTag(Utils.base64ToTag(tagInput.getText()));
                }
                newChat.setUserName(userNameInput.getText());
                newChat.setup(userInfo);
                try {
                    addChat(newChat);
                    new Thread(() -> {
                        while (true) {
                            try {
                                recieveMessage(newChat);
                            } catch (Exception ignored) {}
                        }
                    }).start();
                } catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException |
                         BadPaddingException | InvalidKeySpecException | RemoteException | InvalidKeyException ex) {
                    throw new RuntimeException(ex);
                }
                bumpFrame.dispose();
            }
        });

        // Set window properties
        bumpFrame.setLocationRelativeTo(chatArea);
        bumpFrame.setVisible(true);
        bumpFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void addChat(Chat chat) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeySpecException, RemoteException, InvalidKeyException {
        chatListModel.addElement(chat);
        chatList.setSelectedValue(chat, true);
    }


    private ActionListener createCopyButtonListener(String text) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(new StringSelection(text), null);
                // JOptionPane.showMessageDialog(null, "Copied to clipboard!");
            }
        };
    }
}

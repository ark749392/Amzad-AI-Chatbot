import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ChatGUI {

    private JFrame frame;
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private JButton clearButton;

    private NLPProcessor nlp;
    private Chatbot chatbot;

    public ChatGUI() {

        nlp = new NLPProcessor();
        chatbot = new Chatbot();

        // =========================
        // MAIN WINDOW
        // =========================

        frame = new JFrame("AMZAD AI");
        frame.setSize(700, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // =========================
        // HEADER
        // =========================

        JPanel header = new JPanel();
        header.setLayout(new BorderLayout());
        header.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel title = new JLabel("AMZAD AI");
        title.setFont(new Font("Arial", Font.BOLD, 24));

        JLabel status = new JLabel("● Online");
        status.setFont(new Font("Arial", Font.PLAIN, 14));

        header.add(title, BorderLayout.WEST);
        header.add(status, BorderLayout.EAST);

        // =========================
        // CHAT AREA
        // =========================

        chatArea = new JTextArea();

        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("Arial", Font.PLAIN, 16));
        chatArea.setBorder(new EmptyBorder(15, 15, 15, 15));

        JScrollPane scrollPane = new JScrollPane(chatArea);

        // =========================
        // INPUT AREA
        // =========================

        inputField = new JTextField();
        inputField.setFont(new Font("Arial", Font.PLAIN, 16));
        inputField.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.GRAY),
                        new EmptyBorder(8, 10, 8, 10)
                )
        );

        sendButton = new JButton("Send");
        sendButton.setFont(new Font("Arial", Font.BOLD, 15));

        clearButton = new JButton("Clear");
        clearButton.setFont(new Font("Arial", Font.PLAIN, 14));

        JPanel buttonPanel = new JPanel(new BorderLayout(5, 0));

        buttonPanel.add(clearButton, BorderLayout.WEST);
        buttonPanel.add(sendButton, BorderLayout.EAST);

        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.EAST);

        // =========================
        // ADD COMPONENTS
        // =========================

        frame.add(header, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(inputPanel, BorderLayout.SOUTH);

        // =========================
        // WELCOME MESSAGE
        // =========================

        chatArea.append( 
             "Amzad: Hello! I am Amzad AI. How can I help you today?\n"
        );

        chatArea.append(
                "Amzad: Ask me about anything.\n\n"
        );

        // =========================
        // SEND BUTTON
        // =========================

        sendButton.addActionListener(e -> sendMessage());

        // =========================
        // ENTER KEY
        // =========================

        inputField.addActionListener(e -> sendMessage());

        // =========================
        // CLEAR BUTTON
        // =========================

        clearButton.addActionListener(e -> {

            chatArea.setText("");

            chatArea.append(
                    "Amzad: Chat cleared. How can I help you?\n\n"
            );

            inputField.requestFocus();
        });

        // =========================
        // SHOW WINDOW
        // =========================

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        inputField.requestFocus();
    }

    // =========================
    // SEND MESSAGE FUNCTION
    // =========================

    private void sendMessage() {

        String userInput = inputField.getText().trim();

        // Don't send empty messages
        if (userInput.isEmpty()) {
            return;
        }

        // Display user message
        chatArea.append(
                "You: " + userInput + "\n"
        );

        // NLP processing
        String processedInput = nlp.process(userInput);

        // Get chatbot response
        String response = chatbot.getResponse(processedInput);

        // Display bot response
        chatArea.append(
                "Amzad: " + response + "\n\n"
        );

        // Clear input
        inputField.setText("");

        // Focus input
        inputField.requestFocus();

        // Scroll to bottom
        chatArea.setCaretPosition(
                chatArea.getDocument().getLength()
        );
    }
}
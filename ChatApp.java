package module4.loggingAPI.second;

import java.io.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.*;

public class ChatApp {
    private static final String USER_DATA_FILE = "userdata.txt";
    private static final Map<String, UserData> userMap = new HashMap<>();
    private static final Scanner scanner = new Scanner(System.in);
    private static final Logger LOGGER = Logger.getLogger(ChatApp.class.getName());

    public static void main(String[] args) {
        configureLogger();

        loadUserDataFromFile();

        LOGGER.info("Welcome to the Chat App!");

        while (true) {
            LOGGER.info("1. Register\n2. Login\n3. Exit");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume the newline

            switch (choice) {
                case 1:
                    registerUser();
                    break;
                case 2:
                    loginUser();
                    break;
                case 3:
                    saveUserDataToFile();
                    LOGGER.info("Goodbye!");
                    System.exit(0);
                    break;
                default:
                    LOGGER.warning("Invalid choice. Please try again.");
            }
        }
    }

    private static void configureLogger() {
        LogManager.getLogManager().reset();
        LOGGER.setLevel(Level.ALL);

        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);
        LOGGER.addHandler(consoleHandler);

        try {
            FileHandler fileHandler = new FileHandler("chatapp.log");
            fileHandler.setLevel(Level.ALL);
            LOGGER.addHandler(fileHandler);
        } catch (IOException e) {
            LOGGER.warning("Failed to create FileHandler: " + e.getMessage());
        }
    }

    private static void registerUser() {
        LOGGER.info("Enter your email address:");
        String email = scanner.nextLine();

        if (!validateEmail(email)) {
            LOGGER.warning("Invalid email format. Please enter a valid email address.");
            return;
        }

        if (userMap.containsKey(email)) {
            LOGGER.warning("User with this email already exists. Please choose another email.");
            return;
        }

        LOGGER.info("Enter your username:");
        String username = scanner.nextLine();

        if (!validateUsername(username)) {
            LOGGER.warning("Invalid username format. Please enter a valid username.");
            return;
        }

        LocalDateTime registrationTime = LocalDateTime.now();
        userMap.put(email, new UserData(username, registrationTime));

        LOGGER.info("Registration successful!");
        LOGGER.info("You can now log in.");
    }

    private static void loginUser() {
        LOGGER.info("Enter your email address:");
        String email = scanner.nextLine();

        if (!validateEmail(email)) {
            LOGGER.warning("Invalid email format. Please enter a valid email address.");
            return;
        }

        if (!userMap.containsKey(email)) {
            LOGGER.warning("User with this email does not exist. Please register first.");
            return;
        }

        UserData userData = userMap.get(email);

        LOGGER.info("Enter your username:");
        String enteredUsername = scanner.nextLine();

        if (!enteredUsername.equals(userData.getUsername())) {
            LOGGER.warning("Incorrect username. Please try again.");
            return;
        }

        LOGGER.info("Login successful!");
        LOGGER.info("Welcome back, " + enteredUsername + "!");

        // Implement chat functionality here
        chat(userData);
    }

    private static void chat(UserData userData) {
        LOGGER.info("Chatting...");

        while (true) {
            LOGGER.info("Enter your message (type 'exit' to exit chat):");
            String message = scanner.nextLine();

            if (message.equalsIgnoreCase("exit")) {
                break;
            }

            LocalDateTime timestamp = LocalDateTime.now();
            userData.addChatMessage(new ChatMessage(timestamp, userData.getUsername(), message));

            LOGGER.info("Message sent at " + timestamp);
        }
    }

    private static boolean validateEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private static boolean validateUsername(String username) {
        // Add any username validation rules as needed
        return username.matches("[a-zA-Z0-9]+");
    }

    private static void loadUserDataFromFile() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(USER_DATA_FILE))) {
            userMap.clear();
            userMap.putAll((Map<String, UserData>) ois.readObject());
        } catch (FileNotFoundException e) {
            LOGGER.warning("User data file not found. Starting with an empty user list.");
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.warning("Failed to load user data from file: " + e.getMessage());
        }
    }

    private static void saveUserDataToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USER_DATA_FILE))) {
            oos.writeObject(userMap);
        } catch (IOException e) {
            LOGGER.warning("Failed to save user data to file: " + e.getMessage());
        }
    }

    private static class UserData implements Serializable {
        private final String username;
        private final LocalDateTime registrationTime;
        private final Map<LocalDateTime, ChatMessage> chatMessages;

        public UserData(String username, LocalDateTime registrationTime) {
            this.username = username;
            this.registrationTime = registrationTime;
            this.chatMessages = new HashMap<>();
        }

        public String getUsername() {
            return username;
        }

        public LocalDateTime getRegistrationTime() {
            return registrationTime;
        }

        public Map<LocalDateTime, ChatMessage> getChatMessages() {
            return chatMessages;
        }

        public void addChatMessage(ChatMessage message) {
            chatMessages.put(message.getTimestamp(), message);
        }
    }

    private static class ChatMessage implements Serializable {
        private final LocalDateTime timestamp;
        private final String sender;
        private final String message;

        public ChatMessage(LocalDateTime timestamp, String sender, String message) {
            this.timestamp = timestamp;
            this.sender = sender;
            this.message = message;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public String getSender() {
            return sender;
        }

        public String getMessage() {
            return message;
        }
    }
}


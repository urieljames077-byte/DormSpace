package dao;

import model.User;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class UserDao {
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    public UserDao() {
        createDefaultAdmin();
    }

    private void createDefaultAdmin() {
        String sql = """
                INSERT OR IGNORE INTO users (username, password, role, status)
                VALUES (?, ?, 'ADMIN', 'ACTIVE')
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "admin");
            statement.setString(2, hashPassword("Admin@123"));
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Could not create default admin: " + e.getMessage());
        }
    }

    public boolean register(String username, String password, String role) {
        String sql = """
                INSERT INTO users (username, password, role, status)
                VALUES (?, ?, ?, 'PENDING')
                """;

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            System.out.println("Username and password are required.");
            return false;
        }

        if (!List.of("ADMIN", "STAFF", "TENANT", "BILLING_OFFICER").contains(role)) {
            System.out.println("Invalid role.");
            return false;
        }

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username.trim());
            statement.setString(2, hashPassword(password));
            statement.setString(3, role);
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("unique")) {
                System.out.println("That username is already taken.");
            } else {
                System.out.println("Registration failed: " + e.getMessage());
            }
            return false;
        }
    }

    public User login(String username, String password) {
        String sql = "SELECT id, username, password, role, status FROM users WHERE username = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username.trim());
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return null;
                }

                String storedPassword = result.getString("password");
                String status = result.getString("status");
                if (!verifyPassword(password, storedPassword)) {
                    return null;
                }
                if (!"ACTIVE".equalsIgnoreCase(status)) {
                    if ("PENDING".equalsIgnoreCase(status)) {
                        System.out.println("Your account is waiting for admin approval.");
                    } else {
                        System.out.println("Your account is inactive.");
                    }
                    return null;
                }

                return new User(
                        result.getInt("id"),
                        result.getString("username"),
                        storedPassword,
                        result.getString("role"),
                        status
                );
            }
        } catch (SQLException e) {
            System.err.println("Login failed: " + e.getMessage());
            return null;
        }
    }

    public List<User> getPendingUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, username, password, role, status FROM users WHERE status = 'PENDING' ORDER BY id";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                users.add(new User(
                        result.getInt("id"),
                        result.getString("username"),
                        result.getString("password"),
                        result.getString("role"),
                        result.getString("status")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Could not retrieve pending accounts: " + e.getMessage());
        }
        return users;
    }

    public boolean approveUser(int userId) {
        String sql = "UPDATE users SET status = 'ACTIVE' WHERE id = ? AND status = 'PENDING'";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Approval failed: " + e.getMessage());
            return false;
        }
    }

    private String hashPassword(String password) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        byte[] hash = deriveKey(password.toCharArray(), salt);
        return Base64.getEncoder().encodeToString(salt) + ":"
                + Base64.getEncoder().encodeToString(hash);
    }

    private boolean verifyPassword(String password, String stored) {
        if (password == null || stored == null) return false;
        try {
            String[] parts = stored.split(":", -1);
            if (parts.length != 2) return false;
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expected = Base64.getDecoder().decode(parts[1]);
            byte[] actual = deriveKey(password.toCharArray(), salt);
            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private byte[] deriveKey(char[] password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Password hashing is unavailable.", e);
        } finally {
            spec.clearPassword();
        }
    }
}

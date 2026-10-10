package ui;

import dao.UserDao;
import model.User;

import java.util.List;
import java.util.Scanner;

public class Menu {
    private final Scanner scanner;
    private final UserDao userDao;

    public Menu(Scanner scanner, UserDao userDao) {
        this.scanner = scanner;
        this.userDao = userDao;
    }

    public void showMainMenu() {
        System.out.println("\\n================================");
        System.out.println("       DORMSPACED SYSTEM");
        System.out.println(" Boarding House Management");
        System.out.println("================================");
        System.out.println("1. Register Account");
        System.out.println("2. Login");
        System.out.println("3. Exit");
        System.out.print("Choose an option: ");
    }

    public void registerAccount() {
        System.out.println("\\n--- ACCOUNT REGISTRATION ---");
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        if (username.isBlank() || password.isBlank()) {
            System.out.println("Username and password are required.");
            return;
        }

        System.out.println("\\nSelect your role:");
        System.out.println("1. ADMIN");
        System.out.println("2. STAFF");
        System.out.println("3. TENANT");
        System.out.println("4. BILLING_OFFICER");
        System.out.print("Enter role number: ");

        String role = switch (scanner.nextLine().trim()) {
            case "1" -> "ADMIN";
            case "2" -> "STAFF";
            case "3" -> "TENANT";
            case "4" -> "BILLING_OFFICER";
            default -> null;
        };

        if (role == null) {
            System.out.println("Invalid role selection.");
            return;
        }

        if (userDao.register(username, password, role)) {
            System.out.println("Registration successful.");
            System.out.println("Role: " + role);
            System.out.println("Account status: PENDING");
            System.out.println("An administrator must approve your account before login.");
        }
    }

    public User loginAccount() {
        System.out.println("\\n--- LOGIN ---");
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        User user = userDao.login(username, password);
        if (user != null) {
            System.out.println("Login successful. Welcome, " + user.username() + "!");
            System.out.println("Role: " + user.role());
        } else {
            System.out.println("Login failed. Check your credentials and account status.");
        }
        return user;
    }

    public void showUserMenu(User user) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\\n================================");
            System.out.println("DORMSPACED - " + user.role());
            System.out.println("Logged in as: " + user.username());
            System.out.println("================================");

            switch (user.role()) {
                case "ADMIN" -> {
                    System.out.println("1. Approve Pending Accounts");
                    System.out.println("2. Room management (not implemented yet)");
                    System.out.println("3. Reports (not implemented yet)");
                }
                case "STAFF" -> System.out.println("Room status and tenant records (not implemented yet)");
                case "TENANT" -> System.out.println("Room booking and booking history (not implemented yet)");
                case "BILLING_OFFICER" -> System.out.println("Payments and income reports (not implemented yet)");
                default -> System.out.println("Unknown role.");
            }

            System.out.println("0. Logout");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            if ("0".equals(choice)) {
                loggedIn = false;
            } else if ("ADMIN".equals(user.role()) && "1".equals(choice)) {
                approveAccounts();
            } else {
                System.out.println("That feature has not been implemented yet.");
            }
        }
        System.out.println("You have logged out.");
    }

    private void approveAccounts() {
        List<User> pending = userDao.getPendingUsers();
        System.out.println("\\n--- PENDING ACCOUNTS ---");
        if (pending.isEmpty()) {
            System.out.println("There are no pending accounts.");
            return;
        }

        for (User user : pending) {
            System.out.printf("ID: %d | Username: %s | Role: %s | Status: %s%n",
                    user.id(), user.username(), user.role(), user.status());
        }

        System.out.print("Enter account ID to approve (0 to cancel): ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            if (id == 0) return;
            System.out.println(userDao.approveUser(id)
                    ? "Account approved successfully."
                    : "Account not approved. Check the ID and status.");
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid numeric ID.");
        }
    }
}

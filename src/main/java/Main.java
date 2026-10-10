import dao.DatabaseConnection;
import dao.UserDao;
import model.User;
import ui.Menu;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        DatabaseConnection.initializeDatabase();

        try (Scanner scanner = new Scanner(System.in)) {
            UserDao userDao = new UserDao();
            Menu menu = new Menu(scanner, userDao);
            boolean running = true;

            while (running) {
                menu.showMainMenu();
                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1" -> menu.registerAccount();
                    case "2" -> {
                        User user = menu.loginAccount();
                        if (user != null) {
                            menu.showUserMenu(user);
                        }
                    }
                    case "3" -> {
                        running = false;
                        System.out.println("Thank you for using DormSpaced!");
                    }
                    default -> System.out.println("Invalid option. Try again.");
                }
            }
        }
    }
}

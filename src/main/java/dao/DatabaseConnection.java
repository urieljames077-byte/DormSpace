package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String URL = "jdbc:sqlite:dormspaced.db";

    public static Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(URL);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }

    public static void initializeDatabase() {
        String[] tables = {
            """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL,
                role TEXT NOT NULL CHECK(role IN ('ADMIN','STAFF','TENANT','BILLING_OFFICER')),
                status TEXT NOT NULL DEFAULT 'PENDING'
                    CHECK(status IN ('PENDING','ACTIVE','INACTIVE'))
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS tenant (
                tenant_id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER UNIQUE,
                fullname TEXT NOT NULL,
                contact_number TEXT,
                email TEXT,
                move_in TEXT,
                status TEXT NOT NULL DEFAULT 'Active'
                    CHECK(status IN ('Active','Inactive')),
                FOREIGN KEY (user_id) REFERENCES users(id)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS room (
                room_id INTEGER PRIMARY KEY AUTOINCREMENT,
                room_num TEXT NOT NULL UNIQUE,
                room_type TEXT NOT NULL,
                rate REAL NOT NULL CHECK(rate >= 0),
                status TEXT NOT NULL DEFAULT 'Vacant'
                    CHECK(status IN ('Vacant','Occupied','Maintenance'))
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS booking (
                book_id INTEGER PRIMARY KEY AUTOINCREMENT,
                tenant_id INTEGER NOT NULL,
                room_id INTEGER NOT NULL,
                start_date TEXT NOT NULL,
                end_date TEXT,
                booking_status TEXT NOT NULL DEFAULT 'Pending'
                    CHECK(booking_status IN ('Pending','Approved','Canceled')),
                FOREIGN KEY (tenant_id) REFERENCES tenant(tenant_id),
                FOREIGN KEY (room_id) REFERENCES room(room_id)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS payment (
                payment_id INTEGER PRIMARY KEY AUTOINCREMENT,
                booking_id INTEGER NOT NULL,
                amount_paid REAL NOT NULL CHECK(amount_paid >= 0),
                payment_method TEXT,
                payment_date TEXT NOT NULL,
                payment_status TEXT NOT NULL DEFAULT 'Paid'
                    CHECK(payment_status IN ('Paid','Pending','Overdue')),
                FOREIGN KEY (booking_id) REFERENCES booking(book_id)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS audit_log (
                log_id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                action TEXT NOT NULL,
                details TEXT,
                created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id)
            )
            """
        };

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            for (String table : tables) {
                statement.execute(table);
            }
            System.out.println("Database initialized successfully.");
            System.out.println("Six database tables are ready.");
        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
        }
    }
}

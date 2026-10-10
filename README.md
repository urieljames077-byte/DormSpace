# DormSpaced (Java 17 + Maven + SQLite)

## Open in IntelliJ IDEA
1. Extract `DormSpaced.zip` to a folder.
2. In IntelliJ, choose **File > Open**, then select the extracted `DormSpaced` folder (the folder containing `pom.xml`).
3. If asked, choose **Trust Project** and **Load Maven Project**.
4. Wait for Maven to download SQLite JDBC.
5. Open `src/main/java/Main.java` and click the green Run icon.

## Database
On first run, SQLite creates `dormspaced.db` in the application's working directory and initializes:
- `users`
- `tenant`
- `room`
- `booking`
- `payment`
- `audit_log`

The working directory is usually the project root. If you cannot find the file, check **Run > Edit Configurations > Working directory**.

## First login
- Username: `admin`
- Password: `Admin@123`

Change the default admin password before using this beyond a classroom demo. This starter automatically creates the admin account if it does not already exist.

## Current features
- Account registration with ADMIN, STAFF, TENANT, or BILLING_OFFICER role.
- New accounts start as PENDING.
- Login requires an ACTIVE account.
- Admin can approve pending accounts.
- Role-based menus, logout, and exit.
- Passwords are stored as salted PBKDF2 hashes.

## Important limitations
The room, booking, payment, and audit tables are created, but the complete CRUD operations and monthly income report are not implemented in this starter yet. Role selection is open for a school demo; in a real system, privileged roles must be assigned by an administrator, not freely selected during public registration.

# Smart Parking System

A Java-based Smart Parking System with a separated Frontend and Backend architecture, utilizing an SQLite database for persistent storage.

## Features
- **Frontend Dashboard**: User-friendly UI to manage parking slots.
- **Backend Logic**: Handles vehicle entry, parking fees, and checkout processes.
- **Database Integration**: Persistent history of parked vehicles (`parking_data.db`) using SQLite.
- **View History**: Allows searching and filtering past records.

## Project Structure
- `SmartParkingFrontend.java`: The Graphical User Interface (GUI).
- `SmartParkingBackend.java`: The core logic for parking operations.
- `DatabaseManager.java`: Handles all interactions with the SQLite database.
- `parking_data.db`: The local SQLite database storing the state and history.
- `lib/`: Contains the SQLite JDBC driver needed to connect to the database.

## How to Run
1. Make sure you have the Java Development Kit (JDK) installed.
2. Compile the Java files with the SQLite driver in your classpath.
3. Run the `SmartParkingFrontend` to launch the application.

package com.ucinema.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseInitializer {
    public static void main(String[] args) {
        try {
            // Load the SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");

            // Connect to the database (this will create it if it doesn't exist)
            Connection connection = DriverManager.getConnection("jdbc:sqlite:university_cinema.db");

            // Create tables
            Statement statement = connection.createStatement();

            // Create students table
            statement.execute(
                    "CREATE TABLE IF NOT EXISTS students (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "name TEXT NOT NULL," +
                            "email TEXT NOT NULL UNIQUE," +
                            "password TEXT NOT NULL," +
                            "student_id TEXT NOT NULL UNIQUE," +
                            "created_at TIMESTAMP," +
                            "updated_at TIMESTAMP" +
                            ")"
            );

            // Create movies table
            statement.execute(
                    "CREATE TABLE IF NOT EXISTS movies (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "title TEXT NOT NULL," +
                            "description TEXT," +
                            "duration INTEGER NOT NULL," +
                            "release_date DATE," +
                            "genre TEXT," +
                            "director TEXT," +
                            "poster_url TEXT," +
                            "rating TEXT," +
                            "created_at TIMESTAMP," +
                            "updated_at TIMESTAMP" +
                            ")"
            );

            // Create halls table
            statement.execute(
                    "CREATE TABLE IF NOT EXISTS halls (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "name TEXT NOT NULL," +
                            "capacity INTEGER NOT NULL," +
                            "location TEXT," +
                            "type TEXT," +
                            "seating_layout TEXT," +
                            "created_at TIMESTAMP," +
                            "updated_at TIMESTAMP" +
                            ")"
            );

            // Create movie_schedules table
            statement.execute(
                    "CREATE TABLE IF NOT EXISTS movie_schedules (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "movie_id INTEGER NOT NULL," +
                            "hall_id INTEGER NOT NULL," +
                            "start_time TIMESTAMP NOT NULL," +
                            "end_time TIMESTAMP NOT NULL," +
                            "price REAL NOT NULL," +
                            "is_active BOOLEAN DEFAULT 1," +
                            "created_at TIMESTAMP," +
                            "updated_at TIMESTAMP," +
                            "FOREIGN KEY (movie_id) REFERENCES movies (id)," +
                            "FOREIGN KEY (hall_id) REFERENCES halls (id)" +
                            ")"
            );

            // Create reservations table
            statement.execute(
                    "CREATE TABLE IF NOT EXISTS reservations (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "student_id INTEGER NOT NULL," +
                            "schedule_id INTEGER NOT NULL," +
                            "seat_id TEXT NOT NULL," +
                            "reservation_time TIMESTAMP NOT NULL," +
                            "price REAL NOT NULL," +
                            "status TEXT DEFAULT 'confirmed'," +
                            "created_at TIMESTAMP," +
                            "updated_at TIMESTAMP," +
                            "FOREIGN KEY (student_id) REFERENCES students (id)," +
                            "FOREIGN KEY (schedule_id) REFERENCES movie_schedules (id)" +
                            ")"
            );

            // Insert admin user
            statement.execute(
                    "INSERT OR IGNORE INTO students (name, email, password, student_id, created_at, updated_at) " +
                            "VALUES ('Admin User', 'admin@university.edu', 'admin123', 'ADMIN001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)"
            );

            statement.close();
            connection.close();

            System.out.println("Database and tables created successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
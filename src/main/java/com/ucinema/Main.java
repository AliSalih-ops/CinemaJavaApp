package com.ucinema;

import com.ucinema.util.HibernateUtil;
import com.ucinema.util.SampleDataInitializer;
import com.ucinema.view.LoginScreen;

import javafx.application.Application;
import javafx.stage.Stage;
import java.io.File;

/**
 * Main application class for the University Cinema Hall Reservation System.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("Starting application...");

            // Set the application title
            primaryStage.setTitle("University Cinema Hall Reservation System");

            // Always initialize the database connection first
            HibernateUtil.getSessionFactory();

            // Check if database file exists, if not, initialize sample data
            boolean initializeData = false;
            File dbFile = new File("university_cinema.db");

            if (!dbFile.exists()) {
                System.out.println("Database file not found, will initialize sample data");
                initializeData = true;
            } else {
                System.out.println("Database file found, checking if it has data");
                // Even if the file exists, check if it has data
                try {
                    SampleDataInitializer testInitializer = new SampleDataInitializer();

                    // If there are no movies, we should initialize data
                    if (testInitializer.getMovieCount() == 0) {
                        System.out.println("No movies found in database, will initialize sample data");
                        initializeData = true;
                    } else {
                        System.out.println("Found " + testInitializer.getMovieCount() + " movies in database");
                    }
                } catch (Exception e) {
                    System.out.println("Error checking database: " + e.getMessage());
                    initializeData = true;
                }
            }

            if (initializeData) {
                System.out.println("Initializing sample data...");
                initializeSampleData();
            }

            System.out.println("Creating login screen...");
            // Show the login screen
            LoginScreen loginScreen = new LoginScreen(primaryStage);
            System.out.println("Showing login screen...");
            loginScreen.show();
            System.out.println("Login screen should be visible now.");

        } catch (Exception e) {
            System.err.println("Error starting application:");
            e.printStackTrace();
        }
    }

    /**
     * Initialize sample data for demonstration purposes
     */
    private void initializeSampleData() {
        System.out.println("Starting sample data initialization...");
        try {
            SampleDataInitializer initializer = new SampleDataInitializer();
            initializer.initialize();
            System.out.println("Sample data initialization completed.");
        } catch (Exception e) {
            System.err.println("Error initializing sample data:");
            e.printStackTrace();
        }
    }

    /**
     * Cleanup resources when application closes
     */
    @Override
    public void stop() {
        // Close Hibernate session factory
        System.out.println("Shutting down application...");
        HibernateUtil.shutdown();
        System.out.println("Application shutdown complete.");
    }

    /**
     * Main method to launch the application
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
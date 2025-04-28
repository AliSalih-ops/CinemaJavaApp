package com.ucinema.view;

import com.ucinema.model.entities.Student;
import com.ucinema.service.StudentService;
import com.ucinema.view.admin.AdminDashboard;
import com.ucinema.view.student.StudentDashboard;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Login screen for the application.
 */
public class LoginScreen {

    private final Stage stage;
    private final StudentService studentService;

    /**
     * Constructor
     * @param stage The primary stage
     */
    public LoginScreen(Stage stage) {
        this.stage = stage;
        this.studentService = new StudentService();
    }

    /**
     * Display the login screen
     */
    public void show() {
        // Create the root layout with a BorderPane for better organization
        BorderPane root = new BorderPane();

        // Create a VBox for the header with logo
        VBox header = new VBox(15);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(20, 0, 30, 0));
        header.setStyle("-fx-background-color: #f5f5f5;");

        // Add the logo
        ImageView logoView = new ImageView(new Image(getClass().getResourceAsStream("/images/bau_logo.png")));
        logoView.setFitWidth(150);
        logoView.setPreserveRatio(true);

        // Add the title
        Text sceneTitle = new Text("University Cinema Hall Reservation");
        sceneTitle.setFont(Font.font("Tahoma", FontWeight.BOLD, 22));
        sceneTitle.setFill(Color.web("#00309c")); // BAU blue color

        header.getChildren().addAll(logoView, sceneTitle);

        // Create the login form
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(25, 40, 25, 40));
        grid.setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 0);");

        // Email field
        Label emailLabel = new Label("Email:");
        emailLabel.setFont(Font.font("Tahoma", FontWeight.NORMAL, 14));
        grid.add(emailLabel, 0, 0);

        TextField emailField = new TextField();
        emailField.setPromptText("Enter your email");
        emailField.setPrefWidth(250);
        emailField.setPrefHeight(35);
        grid.add(emailField, 0, 1);

        // Password field
        Label passwordLabel = new Label("Password:");
        passwordLabel.setFont(Font.font("Tahoma", FontWeight.NORMAL, 14));
        grid.add(passwordLabel, 0, 2);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setPrefHeight(35);
        grid.add(passwordField, 0, 3);

        // Login button
        Button loginButton = new Button("Login");
        loginButton.setDefaultButton(true);
        loginButton.setPrefWidth(250);
        loginButton.setPrefHeight(40);
        loginButton.setStyle("-fx-background-color: #00309c; -fx-text-fill: white; -fx-font-size: 14px;");

        grid.add(loginButton, 0, 4);

        // Links
        HBox linksBox = new HBox(20);
        linksBox.setAlignment(Pos.CENTER);
        linksBox.setPadding(new Insets(10, 0, 0, 0));

        Hyperlink registerLink = new Hyperlink("Register New Account");
        Hyperlink adminLink = new Hyperlink("Admin Login (Demo)");

        linksBox.getChildren().addAll(registerLink, adminLink);
        grid.add(linksBox, 0, 5);

        // Message area
        final Text actionTarget = new Text();
        actionTarget.setFill(Color.FIREBRICK);
        grid.add(actionTarget, 0, 6);

        // Add the footer
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(20, 0, 20, 0));

        Label footerText = new Label("© 2025 Bahçeşehir University Cinema Hall Reservation System");
        footerText.setStyle("-fx-text-fill: #666666;");
        footer.getChildren().add(footerText);

        // Add everything to the root layout
        root.setTop(header);
        root.setCenter(grid);
        root.setBottom(footer);

        // Create the scene with larger dimensions
        Scene scene = new Scene(root, 900, 700);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        // Set the scene
        stage.setScene(scene);
        stage.setMinWidth(800);
        stage.setMinHeight(600);

        // Set actions
        loginButton.setOnAction(e -> handleLogin(emailField.getText(), passwordField.getText(), actionTarget));

        registerLink.setOnAction(e -> {
            RegisterScreen registerScreen = new RegisterScreen(stage);
            registerScreen.show();
        });

        adminLink.setOnAction(e -> {
            try {
                System.out.println("Admin link clicked");
                AdminDashboard adminDashboard = new AdminDashboard(stage);
                System.out.println("Admin dashboard created");
                adminDashboard.show();
                System.out.println("Admin dashboard shown");
            } catch (Exception ex) {
                System.err.println("Error loading admin dashboard:");
                ex.printStackTrace();
                showErrorAlert("Error", "Could not load admin dashboard", ex.getMessage());
            }
        });

        // Show the stage
        stage.show();
    }

    /**
     * Handle login button click
     * @param email User email
     * @param password User password
     * @param actionTarget Text field for displaying messages
     */
    private void handleLogin(String email, String password, Text actionTarget) {
        try {
            if (email.isEmpty() || password.isEmpty()) {
                actionTarget.setText("Please enter both email and password");
                return;
            }

            // Check if this is admin login
            if (email.equals("admin@university.edu") && password.equals("admin123")) {
                System.out.println("Admin credentials detected, redirecting to admin dashboard");
                AdminDashboard adminDashboard = new AdminDashboard(stage);
                adminDashboard.show();
                return;
            }

            // If not admin, try student login
            Student student = studentService.login(email, password);

            if (student != null) {
                // Login successful, show student dashboard
                StudentDashboard dashboard = new StudentDashboard(stage, student);
                dashboard.show();
            } else {
                // Login failed
                actionTarget.setText("Invalid email or password");
            }
        } catch (Exception e) {
            showErrorAlert("Login Error", "An error occurred during login", e.getMessage());
        }
    }

    /**
     * Show an error alert
     * @param title Alert title
     * @param header Alert header
     * @param content Alert content
     */
    private void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
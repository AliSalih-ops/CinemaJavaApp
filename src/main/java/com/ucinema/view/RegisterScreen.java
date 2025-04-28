package com.ucinema.view;

import com.ucinema.model.entities.Student;
import com.ucinema.service.StudentService;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Registration screen for new students.
 */
public class RegisterScreen {

    private final Stage stage;
    private final StudentService studentService;

    /**
     * Constructor
     * @param stage The primary stage
     */
    public RegisterScreen(Stage stage) {
        this.stage = stage;
        this.studentService = new StudentService();
    }

    /**
     * Display the registration screen
     */
    public void show() {
        // Create the root layout
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        // Create the scene
        Scene scene = new Scene(grid, 500, 600);
        stage.setScene(scene);

        // Add CSS
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        // Add components
        Text sceneTitle = new Text("Create New Account");
        sceneTitle.setFont(Font.font("Tahoma", FontWeight.BOLD, 20));
        grid.add(sceneTitle, 0, 0, 2, 1);

        Label subtitle = new Label("Please fill in your details");
        grid.add(subtitle, 0, 1, 2, 1);

        // Name field
        Label nameLabel = new Label("Full Name:");
        grid.add(nameLabel, 0, 2);

        TextField nameField = new TextField();
        nameField.setPromptText("Enter your full name");
        grid.add(nameField, 1, 2);

        // Email field
        Label emailLabel = new Label("Email:");
        grid.add(emailLabel, 0, 3);

        TextField emailField = new TextField();
        emailField.setPromptText("Enter your email");
        grid.add(emailField, 1, 3);

        // Student number field
        Label studentNumberLabel = new Label("Student Number:");
        grid.add(studentNumberLabel, 0, 4);

        TextField studentNumberField = new TextField();
        studentNumberField.setPromptText("Enter your student number");
        grid.add(studentNumberField, 1, 4);

        // Password field
        Label passwordLabel = new Label("Password:");
        grid.add(passwordLabel, 0, 5);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        grid.add(passwordField, 1, 5);

        // Confirm password field
        Label confirmPasswordLabel = new Label("Confirm Password:");
        grid.add(confirmPasswordLabel, 0, 6);

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm your password");
        grid.add(confirmPasswordField, 1, 6);

        // Register button
        Button registerButton = new Button("Register");
        registerButton.setDefaultButton(true);
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(registerButton);
        grid.add(hbBtn, 1, 7);

        // Back to login link
        Hyperlink loginLink = new Hyperlink("Already have an account? Login here");
        grid.add(loginLink, 0, 8, 2, 1);

        // Message area
        final Text actionTarget = new Text();
        grid.add(actionTarget, 1, 9);

        // Set actions
        registerButton.setOnAction(e -> handleRegistration(
                nameField.getText(),
                emailField.getText(),
                studentNumberField.getText(),
                passwordField.getText(),
                confirmPasswordField.getText(),
                actionTarget));

        loginLink.setOnAction(e -> {
            LoginScreen loginScreen = new LoginScreen(stage);
            loginScreen.show();
        });

        // Show the stage
        stage.show();
    }

    /**
     * Handle registration button click
     * @param name User name
     * @param email User email
     * @param studentNumber Student number
     * @param password User password
     * @param confirmPassword Password confirmation
     * @param actionTarget Text field for displaying messages
     */
    private void handleRegistration(String name, String email, String studentNumber,
                                    String password, String confirmPassword, Text actionTarget) {
        try {
            // Validate input
            if (name.isEmpty() || email.isEmpty() || studentNumber.isEmpty() ||
                    password.isEmpty() || confirmPassword.isEmpty()) {
                actionTarget.setText("Please fill in all fields");
                return;
            }

            // Validate email format
            if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                actionTarget.setText("Please enter a valid email address");
                return;
            }

            // Validate password match
            if (!password.equals(confirmPassword)) {
                actionTarget.setText("Passwords do not match");
                return;
            }

            // Validate password strength
            if (password.length() < 6) {
                actionTarget.setText("Password must be at least 6 characters long");
                return;
            }

            // Register the student
            Student student = studentService.registerStudent(name, email, password, studentNumber);

            if (student != null) {
                // Registration successful, show success message and switch to login
                showSuccessAlert("Registration Successful",
                        "Your account has been created",
                        "You can now log in with your email and password");

                LoginScreen loginScreen = new LoginScreen(stage);
                loginScreen.show();
            } else {
                System.out.println("Registration failed - student object is null");
                actionTarget.setText("Registration failed. Please try again.");
            }
        } catch (Exception e) {
            System.out.println("Registration error: " + e.getMessage());
            e.printStackTrace();
            actionTarget.setText("Error: " + e.getMessage());
        }
    }

    /**
     * Show a success alert
     * @param title Alert title
     * @param header Alert header
     * @param content Alert content
     */
    private void showSuccessAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
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
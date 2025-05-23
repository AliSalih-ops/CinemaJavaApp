package com.ucinema.view.admin;

import com.ucinema.model.entities.Hall;
import com.ucinema.service.HallService;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Form screen for adding and editing cinema halls.
 */
public class HallFormScreen {

    private final Stage stage;
    private final Hall hallToEdit;
    private final AdminDashboard dashboard;
    private final HallService hallService;

    // Standard hall capacities - added 200-seat option
    private final Integer[] STANDARD_CAPACITIES = {25, 50, 75, 100, 150, 200};

    /**
     * Constructor
     * @param parentStage The parent stage
     * @param hallToEdit The hall to edit (null for adding new hall)
     * @param dashboard The admin dashboard
     */
    public HallFormScreen(Stage parentStage, Hall hallToEdit, AdminDashboard dashboard) {
        this.hallToEdit = hallToEdit;
        this.dashboard = dashboard;
        this.hallService = new HallService();

        // Create a new modal stage
        this.stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(parentStage);

        // Set title based on mode
        if (hallToEdit == null) {
            stage.setTitle("Add New Hall");
        } else {
            stage.setTitle("Edit Hall: " + hallToEdit.getName());
        }
    }

    /**
     * Display the hall form screen
     */
    public void show() {
        // Create the form grid
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        // Page title
        Text sceneTitle;
        if (hallToEdit == null) {
            sceneTitle = new Text("Add New Hall");
        } else {
            sceneTitle = new Text("Edit Hall");
        }
        sceneTitle.setFont(Font.font("Tahoma", FontWeight.BOLD, 20));
        grid.add(sceneTitle, 0, 0, 2, 1);

        // Name
        Label nameLabel = new Label("Hall Name:");
        grid.add(nameLabel, 0, 1);

        TextField nameField = new TextField();
        grid.add(nameField, 1, 1);

        // Capacity - changed from TextField to ComboBox
        Label capacityLabel = new Label("Capacity:");
        grid.add(capacityLabel, 0, 2);

        ComboBox<Integer> capacityComboBox = new ComboBox<>();
        capacityComboBox.getItems().addAll(STANDARD_CAPACITIES);
        capacityComboBox.setPromptText("Select capacity");
        grid.add(capacityComboBox, 1, 2);

        // Location
        Label locationLabel = new Label("Location:");
        grid.add(locationLabel, 0, 3);

        TextField locationField = new TextField();
        grid.add(locationField, 1, 3);

        // Type
        Label typeLabel = new Label("Hall Type:");
        grid.add(typeLabel, 0, 4);

        ComboBox<String> typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll("standard", "VIP", "3D");
        typeComboBox.setPromptText("Select hall type");
        grid.add(typeComboBox, 1, 4);

        // Seating layout (simplified for demo)
        Label seatsLabel = new Label("Seating Layout:");
        grid.add(seatsLabel, 0, 5);

        Label seatsNote = new Label("Default layout will be created based on capacity");
        seatsNote.setStyle("-fx-font-style: italic;");
        grid.add(seatsNote, 1, 5);

        // Help text for capacity options
        Label capacityHelpLabel = new Label("Standard sizes ensure optimal seating arrangements");
        capacityHelpLabel.setStyle("-fx-font-style: italic; -fx-font-size: 10px;");
        grid.add(capacityHelpLabel, 1, 6);

        // Buttons
        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
        buttonBox.getChildren().addAll(saveButton, cancelButton);
        grid.add(buttonBox, 1, 7);

        // Populate fields if editing
        if (hallToEdit != null) {
            nameField.setText(hallToEdit.getName());

            // Find closest standard capacity
            int currentCapacity = hallToEdit.getCapacity();
            Integer closestCapacity = findClosestStandardCapacity(currentCapacity);
            capacityComboBox.setValue(closestCapacity);

            locationField.setText(hallToEdit.getLocation());
            typeComboBox.setValue(hallToEdit.getType());
        }

        // Set actions
        saveButton.setOnAction(e -> {
            if (validateInput(nameField, capacityComboBox, locationField, typeComboBox)) {
                saveHall(
                        nameField.getText(),
                        capacityComboBox.getValue(),
                        locationField.getText(),
                        typeComboBox.getValue()
                );
            }
        });

        cancelButton.setOnAction(e -> stage.close());

        // Create the scene
        Scene scene = new Scene(grid, 450, 420);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Find the closest standard capacity to the given value
     * @param currentCapacity The current capacity value
     * @return The closest standard capacity
     */
    private Integer findClosestStandardCapacity(int currentCapacity) {
        Integer closest = STANDARD_CAPACITIES[0];
        int minDiff = Math.abs(currentCapacity - closest);

        for (Integer capacity : STANDARD_CAPACITIES) {
            int diff = Math.abs(currentCapacity - capacity);
            if (diff < minDiff) {
                minDiff = diff;
                closest = capacity;
            }
        }

        return closest;
    }

    /**
     * Validate form input
     * @param nameField Name field
     * @param capacityComboBox Capacity combo box
     * @param locationField Location field
     * @param typeComboBox Type combo box
     * @return True if input is valid
     */
    private boolean validateInput(TextField nameField, ComboBox<Integer> capacityComboBox,
                                  TextField locationField, ComboBox<String> typeComboBox) {
        StringBuilder errorMessage = new StringBuilder();

        // Check name
        if (nameField.getText().trim().isEmpty()) {
            errorMessage.append("- Hall name is required\n");
        }

        // Check capacity
        if (capacityComboBox.getValue() == null) {
            errorMessage.append("- You must select a capacity\n");
        }

        // Check location
        if (locationField.getText().trim().isEmpty()) {
            errorMessage.append("- Location is required\n");
        }

        // Check hall type
        if (typeComboBox.getValue() == null || typeComboBox.getValue().trim().isEmpty()) {
            errorMessage.append("- Hall type is required\n");
        }

        // Show error message if validation failed
        if (errorMessage.length() > 0) {
            showErrorAlert("Validation Error", "Please correct the following errors:", errorMessage.toString());
            return false;
        }

        return true;
    }

    /**
     * Save hall to database
     * @param name Hall name
     * @param capacity Hall capacity
     * @param location Hall location
     * @param type Hall type
     */
    private void saveHall(String name, Integer capacity, String location, String type) {
        try {
            if (hallToEdit == null) {
                // Add new hall
                Hall newHall = hallService.addHall(name, capacity, location, type);

                if (newHall != null) {
                    showInfoAlert("Success", "Hall added successfully");
                    stage.close();

                    // Refresh dashboard
                    dashboard.show();
                } else {
                    showErrorAlert("Error", "Failed to add hall");
                }
            } else {
                // Update existing hall
                hallToEdit.setName(name);
                hallToEdit.setCapacity(capacity);
                hallToEdit.setLocation(location);
                hallToEdit.setType(type);

                Hall updatedHall = hallService.updateHall(hallToEdit);

                if (updatedHall != null) {
                    showInfoAlert("Success", "Hall updated successfully");
                    stage.close();

                    // Refresh dashboard
                    dashboard.show();
                } else {
                    showErrorAlert("Error", "Failed to update hall");
                }
            }
        } catch (Exception e) {
            showErrorAlert("Error", "An error occurred: " + e.getMessage());
        }
    }

    /**
     * Show an information alert
     * @param title Alert title
     * @param content Alert content
     */
    private void showInfoAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
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

    /**
     * Show an error alert
     * @param title Alert title
     * @param content Alert content
     */
    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
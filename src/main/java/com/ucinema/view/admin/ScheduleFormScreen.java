package com.ucinema.view.admin;

import com.ucinema.model.entities.Hall;
import com.ucinema.model.entities.Movie;
import com.ucinema.model.entities.MovieSchedule;
import com.ucinema.service.HallService;
import com.ucinema.service.MovieScheduleService;
import com.ucinema.service.MovieService;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Form screen for adding and editing movie schedules.
 */
public class ScheduleFormScreen {

    private final Stage stage;
    private final MovieSchedule scheduleToEdit;
    private final AdminDashboard dashboard;
    private final MovieScheduleService scheduleService;
    private final MovieService movieService;
    private final HallService hallService;

    /**
     * Constructor
     * @param parentStage The parent stage
     * @param scheduleToEdit The schedule to edit (null for adding new schedule)
     * @param dashboard The admin dashboard
     */
    public ScheduleFormScreen(Stage parentStage, MovieSchedule scheduleToEdit, AdminDashboard dashboard) {
        this.scheduleToEdit = scheduleToEdit;
        this.dashboard = dashboard;
        this.scheduleService = new MovieScheduleService();
        this.movieService = new MovieService();
        this.hallService = new HallService();

        // Create a new modal stage
        this.stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(parentStage);

        // Set title based on mode
        if (scheduleToEdit == null) {
            stage.setTitle("Add New Schedule");
        } else {
            stage.setTitle("Edit Schedule");
        }
    }

    /**
     * Display the schedule form screen
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
        if (scheduleToEdit == null) {
            sceneTitle = new Text("Add New Schedule");
        } else {
            sceneTitle = new Text("Edit Schedule");
        }
        sceneTitle.setFont(Font.font("Tahoma", FontWeight.BOLD, 20));
        grid.add(sceneTitle, 0, 0, 2, 1);

        // Movie selection
        Label movieLabel = new Label("Movie:");
        grid.add(movieLabel, 0, 1);

        // Get all movies
        List<Movie> movies = movieService.getAllMovies();
        List<String> movieTitles = movies.stream()
                .map(movie -> movie.getId() + ": " + movie.getTitle())
                .collect(Collectors.toList());

        ComboBox<String> movieComboBox = new ComboBox<>(FXCollections.observableArrayList(movieTitles));
        grid.add(movieComboBox, 1, 1);

        // Hall selection
        Label hallLabel = new Label("Hall:");
        grid.add(hallLabel, 0, 2);

        // Get all halls
        List<Hall> halls = hallService.getAllHalls();
        List<String> hallNames = halls.stream()
                .map(hall -> hall.getId() + ": " + hall.getName())
                .collect(Collectors.toList());

        ComboBox<String> hallComboBox = new ComboBox<>(FXCollections.observableArrayList(hallNames));
        grid.add(hallComboBox, 1, 2);

        // Date selection
        Label dateLabel = new Label("Date:");
        grid.add(dateLabel, 0, 3);

        DatePicker datePicker = new DatePicker(LocalDate.now());
        grid.add(datePicker, 1, 3);

        // Time selection
        Label timeLabel = new Label("Time:");
        grid.add(timeLabel, 0, 4);

        HBox timeBox = new HBox(5);

        // Hour spinner (0-23)
        Spinner<Integer> hourSpinner = new Spinner<>(0, 23, 12);
        hourSpinner.setEditable(true);
        hourSpinner.setPrefWidth(70);

        // Minute spinner (0-59)
        Spinner<Integer> minuteSpinner = new Spinner<>(0, 59, 0);
        minuteSpinner.setEditable(true);
        minuteSpinner.setPrefWidth(70);

        timeBox.getChildren().addAll(hourSpinner, new Label(":"), minuteSpinner);
        grid.add(timeBox, 1, 4);

        // Price
        Label priceLabel = new Label("Price ($):");
        grid.add(priceLabel, 0, 5);

        TextField priceField = new TextField("10.00");
        grid.add(priceField, 1, 5);

        // Active status
        Label activeLabel = new Label("Active:");
        grid.add(activeLabel, 0, 6);

        ComboBox<String> activeComboBox = new ComboBox<>();
        activeComboBox.getItems().addAll("Yes", "No");
        activeComboBox.setValue("Yes");
        grid.add(activeComboBox, 1, 6);

        // Buttons
        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
        buttonBox.getChildren().addAll(saveButton, cancelButton);
        grid.add(buttonBox, 1, 7);

        // Populate fields if editing
        if (scheduleToEdit != null) {
            // Find the movie in the list
            for (int i = 0; i < movies.size(); i++) {
                if (movies.get(i).getId() == scheduleToEdit.getMovieId()) {
                    movieComboBox.getSelectionModel().select(i);
                    break;
                }
            }

            // Find the hall in the list
            for (int i = 0; i < halls.size(); i++) {
                if (halls.get(i).getId() == scheduleToEdit.getHallId()) {
                    hallComboBox.getSelectionModel().select(i);
                    break;
                }
            }

            // Set date and time
            datePicker.setValue(scheduleToEdit.getStartTime().toLocalDate());
            hourSpinner.getValueFactory().setValue(scheduleToEdit.getStartTime().getHour());
            minuteSpinner.getValueFactory().setValue(scheduleToEdit.getStartTime().getMinute());

            // Set price
            priceField.setText(String.format("%.2f", scheduleToEdit.getPrice()));

            // Set active status
            activeComboBox.setValue(scheduleToEdit.isActive() ? "Yes" : "No");
        }

        // Set actions
        saveButton.setOnAction(e -> {
            if (validateInput(movieComboBox, hallComboBox, datePicker, priceField)) {
                saveSchedule(
                        getSelectedMovieId(movieComboBox, movies),
                        getSelectedHallId(hallComboBox, halls),
                        datePicker.getValue(),
                        hourSpinner.getValue(),
                        minuteSpinner.getValue(),
                        priceField.getText(),
                        activeComboBox.getValue().equals("Yes")
                );
            }
        });

        cancelButton.setOnAction(e -> stage.close());

        // Create the scene
        Scene scene = new Scene(grid, 450, 400);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Get the selected movie ID from the combo box
     * @param comboBox The movie combo box
     * @param movies The list of movies
     * @return The selected movie ID
     */
    private int getSelectedMovieId(ComboBox<String> comboBox, List<Movie> movies) {
        String selected = comboBox.getValue();
        if (selected == null) {
            return -1;
        }

        // Parse ID from the string (format: "id: title")
        String idStr = selected.split(":")[0].trim();
        return Integer.parseInt(idStr);
    }

    /**
     * Get the selected hall ID from the combo box
     * @param comboBox The hall combo box
     * @param halls The list of halls
     * @return The selected hall ID
     */
    private int getSelectedHallId(ComboBox<String> comboBox, List<Hall> halls) {
        String selected = comboBox.getValue();
        if (selected == null) {
            return -1;
        }

        // Parse ID from the string (format: "id: name")
        String idStr = selected.split(":")[0].trim();
        return Integer.parseInt(idStr);
    }

    /**
     * Validate form input
     * @param movieComboBox Movie combo box
     * @param hallComboBox Hall combo box
     * @param datePicker Date picker
     * @param priceField Price field
     * @return True if input is valid
     */
    private boolean validateInput(ComboBox<String> movieComboBox, ComboBox<String> hallComboBox,
                                  DatePicker datePicker, TextField priceField) {
        StringBuilder errorMessage = new StringBuilder();

        // Check movie selection
        if (movieComboBox.getValue() == null) {
            errorMessage.append("- Please select a movie\n");
        }

        // Check hall selection
        if (hallComboBox.getValue() == null) {
            errorMessage.append("- Please select a hall\n");
        }

        // Check date
        if (datePicker.getValue() == null) {
            errorMessage.append("- Please select a date\n");
        } else if (datePicker.getValue().isBefore(LocalDate.now())) {
            errorMessage.append("- Schedule date cannot be in the past\n");
        }

        // Check price
        try {
            double price = Double.parseDouble(priceField.getText().trim());
            if (price <= 0) {
                errorMessage.append("- Price must be a positive number\n");
            }
        } catch (NumberFormatException e) {
            errorMessage.append("- Price must be a valid number\n");
        }

        // Show error message if validation failed
        if (errorMessage.length() > 0) {
            showErrorAlert("Validation Error", "Please correct the following errors:", errorMessage.toString());
            return false;
        }

        return true;
    }

    /**
     * Save schedule to database
     * @param movieId Movie ID
     * @param hallId Hall ID
     * @param date Schedule date
     * @param hour Hour of the schedule
     * @param minute Minute of the schedule
     * @param priceStr Schedule price as string
     * @param isActive Active status
     */
    private void saveSchedule(int movieId, int hallId, LocalDate date,
                              int hour, int minute, String priceStr, boolean isActive) {
        try {
            // Create date time object
            LocalDateTime startTime = LocalDateTime.of(date, LocalTime.of(hour, minute));
            double price = Double.parseDouble(priceStr.trim());

            // Calculate end time based on movie duration
            Movie movie = movieService.findMovieById(movieId);
            if (movie == null) {
                showErrorAlert("Error", "Selected movie not found");
                return;
            }

            LocalDateTime endTime = startTime.plusMinutes(movie.getDuration());

            if (scheduleToEdit == null) {
                // Add new schedule
                MovieSchedule newSchedule = scheduleService.addSchedule(movieId, hallId, startTime, price);

                if (newSchedule != null) {
                    // Set active status (not included in the constructor)
                    newSchedule.setActive(isActive);
                    newSchedule.setEndTime(endTime);
                    scheduleService.updateSchedule(newSchedule);

                    showInfoAlert("Success", "Schedule added successfully");
                    stage.close();

                    // Refresh dashboard
                    dashboard.show();
                } else {
                    showErrorAlert("Error", "Failed to add schedule");
                }
            } else {
                // Update existing schedule
                scheduleToEdit.setMovieId(movieId);
                scheduleToEdit.setHallId(hallId);
                scheduleToEdit.setStartTime(startTime);
                scheduleToEdit.setEndTime(endTime);
                scheduleToEdit.setPrice(price);
                scheduleToEdit.setActive(isActive);

                MovieSchedule updatedSchedule = scheduleService.updateSchedule(scheduleToEdit);

                if (updatedSchedule != null) {
                    showInfoAlert("Success", "Schedule updated successfully");
                    stage.close();

                    // Refresh dashboard
                    dashboard.show();
                } else {
                    showErrorAlert("Error", "Failed to update schedule");
                }
            }
        } catch (NumberFormatException e) {
            showErrorAlert("Error", "Invalid price value");
        } catch (IllegalArgumentException e) {
            showErrorAlert("Error", e.getMessage());
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
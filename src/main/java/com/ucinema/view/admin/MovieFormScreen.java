package com.ucinema.view.admin;

import com.ucinema.model.entities.Movie;
import com.ucinema.service.MovieService;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;

/**
 * Form screen for adding and editing movies.
 */
public class MovieFormScreen {

    private final Stage stage;
    private final Movie movieToEdit;
    private final AdminDashboard dashboard;
    private final MovieService movieService;

    /**
     * Constructor
     * @param parentStage The parent stage
     * @param movieToEdit The movie to edit (null for adding new movie)
     * @param dashboard The admin dashboard
     */
    public MovieFormScreen(Stage parentStage, Movie movieToEdit, AdminDashboard dashboard) {
        this.movieToEdit = movieToEdit;
        this.dashboard = dashboard;
        this.movieService = new MovieService();

        // Create a new modal stage
        this.stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(parentStage);

        // Set title based on mode
        if (movieToEdit == null) {
            stage.setTitle("Add New Movie");
        } else {
            stage.setTitle("Edit Movie: " + movieToEdit.getTitle());
        }
    }

    /**
     * Display the movie form screen
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
        if (movieToEdit == null) {
            sceneTitle = new Text("Add New Movie");
        } else {
            sceneTitle = new Text("Edit Movie");
        }
        sceneTitle.setFont(Font.font("Tahoma", FontWeight.BOLD, 20));
        grid.add(sceneTitle, 0, 0, 2, 1);

        // Title
        Label titleLabel = new Label("Title:");
        grid.add(titleLabel, 0, 1);

        TextField titleField = new TextField();
        grid.add(titleField, 1, 1);

        // Description
        Label descriptionLabel = new Label("Description:");
        grid.add(descriptionLabel, 0, 2);

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPrefRowCount(3);
        descriptionArea.setWrapText(true);
        grid.add(descriptionArea, 1, 2);

        // Duration
        Label durationLabel = new Label("Duration (min):");
        grid.add(durationLabel, 0, 3);

        TextField durationField = new TextField();
        grid.add(durationField, 1, 3);

        // Release Date
        Label releaseDateLabel = new Label("Release Date:");
        grid.add(releaseDateLabel, 0, 4);

        DatePicker releaseDatePicker = new DatePicker();
        grid.add(releaseDatePicker, 1, 4);

        // Genre
        Label genreLabel = new Label("Genre:");
        grid.add(genreLabel, 0, 5);

        ComboBox<String> genreComboBox = new ComboBox<>();
        genreComboBox.getItems().addAll(
                "Action", "Comedy", "Drama", "Horror", "Sci-Fi",
                "Adventure", "Thriller", "Romance", "Animation", "Documentary"
        );
        genreComboBox.setEditable(true);
        grid.add(genreComboBox, 1, 5);

        // Director
        Label directorLabel = new Label("Director:");
        grid.add(directorLabel, 0, 6);

        TextField directorField = new TextField();
        grid.add(directorField, 1, 6);

        // Rating
        Label ratingLabel = new Label("Rating:");
        grid.add(ratingLabel, 0, 7);

        ComboBox<String> ratingComboBox = new ComboBox<>();
        ratingComboBox.getItems().addAll("G", "PG", "PG-13", "R", "NC-17");
        grid.add(ratingComboBox, 1, 7);

        // Poster URL
        Label posterUrlLabel = new Label("Poster URL:");
        grid.add(posterUrlLabel, 0, 8);

        TextField posterUrlField = new TextField();
        grid.add(posterUrlField, 1, 8);

        // Buttons
        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
        buttonBox.getChildren().addAll(saveButton, cancelButton);
        grid.add(buttonBox, 1, 9);

        // Populate fields if editing
        if (movieToEdit != null) {
            titleField.setText(movieToEdit.getTitle());
            descriptionArea.setText(movieToEdit.getDescription());
            durationField.setText(String.valueOf(movieToEdit.getDuration()));
            releaseDatePicker.setValue(movieToEdit.getReleaseDate());
            genreComboBox.setValue(movieToEdit.getGenre());
            directorField.setText(movieToEdit.getDirector());
            ratingComboBox.setValue(movieToEdit.getRating());
            posterUrlField.setText(movieToEdit.getPosterUrl());
        }

        // Set actions
        saveButton.setOnAction(e -> {
            if (validateInput(titleField, durationField, releaseDatePicker)) {
                saveMovie(
                        titleField.getText(),
                        descriptionArea.getText(),
                        durationField.getText(),
                        releaseDatePicker.getValue(),
                        genreComboBox.getValue(),
                        directorField.getText(),
                        ratingComboBox.getValue(),
                        posterUrlField.getText()
                );
            }
        });

        cancelButton.setOnAction(e -> stage.close());

        // Create the scene
        Scene scene = new Scene(grid, 500, 550);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Validate form input
     * @param titleField Title field
     * @param durationField Duration field
     * @param releaseDatePicker Release date picker
     * @return True if input is valid
     */
    private boolean validateInput(TextField titleField, TextField durationField, DatePicker releaseDatePicker) {
        StringBuilder errorMessage = new StringBuilder();

        // Check title
        if (titleField.getText().trim().isEmpty()) {
            errorMessage.append("- Title is required\n");
        }

        // Check duration
        try {
            int duration = Integer.parseInt(durationField.getText().trim());
            if (duration <= 0) {
                errorMessage.append("- Duration must be a positive number\n");
            }
        } catch (NumberFormatException e) {
            errorMessage.append("- Duration must be a valid number\n");
        }

        // Check release date
        if (releaseDatePicker.getValue() == null) {
            errorMessage.append("- Release date is required\n");
        }

        // Show error message if validation failed
        if (errorMessage.length() > 0) {
            showErrorAlert("Validation Error", "Please correct the following errors:", errorMessage.toString());
            return false;
        }

        return true;
    }

    /**
     * Save movie to database
     * @param title Movie title
     * @param description Movie description
     * @param durationStr Movie duration as string
     * @param releaseDate Movie release date
     * @param genre Movie genre
     * @param director Movie director
     * @param rating Movie rating
     * @param posterUrl Movie poster URL
     */
    private void saveMovie(String title, String description, String durationStr,
                           LocalDate releaseDate, String genre, String director,
                           String rating, String posterUrl) {
        try {
            int duration = Integer.parseInt(durationStr.trim());

            if (movieToEdit == null) {
                // Add new movie
                Movie newMovie = movieService.addMovie(
                        title, description, duration, releaseDate, genre, director, rating);

                if (newMovie != null) {
                    // Set poster URL (not included in the constructor)
                    newMovie.setPosterUrl(posterUrl);
                    movieService.updateMovie(newMovie);

                    showInfoAlert("Success", "Movie added successfully");
                    stage.close();

                    // Refresh dashboard
                    dashboard.show();
                } else {
                    showErrorAlert("Error", "Failed to add movie");
                }
            } else {
                // Update existing movie
                movieToEdit.setTitle(title);
                movieToEdit.setDescription(description);
                movieToEdit.setDuration(duration);
                movieToEdit.setReleaseDate(releaseDate);
                movieToEdit.setGenre(genre);
                movieToEdit.setDirector(director);
                movieToEdit.setRating(rating);
                movieToEdit.setPosterUrl(posterUrl);

                Movie updatedMovie = movieService.updateMovie(movieToEdit);

                if (updatedMovie != null) {
                    showInfoAlert("Success", "Movie updated successfully");
                    stage.close();

                    // Refresh dashboard
                    dashboard.show();
                } else {
                    showErrorAlert("Error", "Failed to update movie");
                }
            }
        } catch (NumberFormatException e) {
            showErrorAlert("Error", "Invalid duration value");
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
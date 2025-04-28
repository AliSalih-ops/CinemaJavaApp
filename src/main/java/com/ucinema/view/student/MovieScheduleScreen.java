package com.ucinema.view.student;

import com.ucinema.model.entities.Movie;
import com.ucinema.model.entities.MovieSchedule;
import com.ucinema.model.entities.Student;
import com.ucinema.model.entities.Hall;
import com.ucinema.service.MovieScheduleService;
import com.ucinema.service.ReservationService;
import com.ucinema.service.HallService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Screen for viewing movie schedules and booking seats.
 */
public class MovieScheduleScreen {

    private final Stage stage;
    private final Student student;
    private final Movie movie;
    private final Stage parentStage;
    private final MovieScheduleService scheduleService;
    private final ReservationService reservationService;
    private final HallService hallService;

    /**
     * Constructor
     * @param parentStage The parent stage
     * @param student The logged-in student
     * @param movie The selected movie
     */
    public MovieScheduleScreen(Stage parentStage, Student student, Movie movie) {
        this.parentStage = parentStage;
        this.student = student;
        this.movie = movie;
        this.stage = parentStage; // Use the parentStage instead of creating a new one
        this.scheduleService = new MovieScheduleService();
        this.reservationService = new ReservationService();
        this.hallService = new HallService();
    }

    /**
     * Display the movie schedule screen
     */
    public void show() {
        // Set the stage title
        stage.setTitle("Schedules for " + movie.getTitle());

        // Create a border pane as the root
        BorderPane root = new BorderPane();

        // Create the top header
        VBox header = createHeader();

        // Create main content
        ScrollPane content = createContent();

        // Add components to root
        root.setTop(header);
        root.setCenter(content);

        // Create the scene
        Scene scene = new Scene(root, 700, 500); // Made the window slightly wider

        // Add CSS
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        // Set the scene
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Create the header
     * @return VBox containing the header elements
     */
    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(15, 12, 15, 12));
        header.setStyle("-fx-background-color: #336699;");

        // Title
        Label title = new Label(movie.getTitle());
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setStyle("-fx-text-fill: white;");

        // Movie details
        Label details = new Label(String.format(
                "Genre: %s | Director: %s | Duration: %d min | Rating: %s",
                movie.getGenre(),
                movie.getDirector(),
                movie.getDuration(),
                movie.getRating()
        ));
        details.setStyle("-fx-text-fill: white;");

        header.getChildren().addAll(title, details);

        return header;
    }

    /**
     * Create the main content
     * @return ScrollPane containing the main content
     */
    private ScrollPane createContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(15));

        // Title
        Text contentTitle = new Text("Available Schedules");
        contentTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        // Get schedules for this movie
        List<MovieSchedule> schedules = scheduleService.findSchedulesByMovie(movie.getId());

        if (schedules.isEmpty()) {
            Label noSchedules = new Label("No schedules available for this movie");
            content.getChildren().addAll(contentTitle, noSchedules);

            // Wrap in ScrollPane and return
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            return scrollPane;
        }

        // Format date/time for display
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        // Convert to observable list with formatted strings
        ObservableList<String> formattedSchedules = FXCollections.observableArrayList();

        for (MovieSchedule schedule : schedules) {
            // Get the Hall name instead of just displaying the ID
            Hall hall = hallService.findHallById(schedule.getHallId());
            String hallName = (hall != null) ? hall.getName() : "Unknown Hall";
            String hallType = (hall != null) ? hall.getType() : "";
            String hallLocation = (hall != null) ? hall.getLocation() : "";

            String formattedSchedule = String.format(
                    "Date: %s | Hall: %s (%s) | Location: %s | Price: $%.2f",
                    schedule.getStartTime().format(formatter),
                    hallName,
                    hallType,
                    hallLocation,
                    schedule.getPrice()
            );
            formattedSchedules.add(formattedSchedule);
        }

        // Create list view
        ListView<String> scheduleListView = new ListView<>(formattedSchedules);
        scheduleListView.setPrefHeight(300); // Ensure the list has enough space

        // Book button
        Button bookButton = new Button("Book Selected Schedule");
        bookButton.setDisable(true);

        // Back button
        Button backButton = new Button("Back to Movies");

        // Set actions
        scheduleListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> bookButton.setDisable(newValue == null));

        bookButton.setOnAction(e -> {
            int selectedIndex = scheduleListView.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0 && selectedIndex < schedules.size()) {
                MovieSchedule selectedSchedule = schedules.get(selectedIndex);
                // Create a NEW stage for the seat selection screen
                Stage seatStage = new Stage();
                SeatSelectionScreen seatScreen = new SeatSelectionScreen(seatStage, student, selectedSchedule);
                seatScreen.show();
            }
        });

        backButton.setOnAction(e -> {
            // Don't create a new dashboard, just return to the existing one
            StudentDashboard dashboard = new StudentDashboard(parentStage, student);
            dashboard.show();
        });

        // Button container
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(bookButton, backButton);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        content.getChildren().addAll(contentTitle, scheduleListView, buttonBox);

        // Create scroll pane
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        return scrollPane;
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
}
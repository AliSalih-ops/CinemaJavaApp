package com.ucinema.view.student;

import com.ucinema.model.datastructures.HallGraph;
import com.ucinema.model.entities.Hall;
import com.ucinema.model.entities.Movie;
import com.ucinema.model.entities.MovieSchedule;
import com.ucinema.model.entities.Reservation;
import com.ucinema.model.entities.Student;
import com.ucinema.service.HallService;
import com.ucinema.service.MovieService;
import com.ucinema.service.ReservationService;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Screen for selecting seats for a movie reservation.
 */
public class SeatSelectionScreen {

    private final Stage stage;
    private final Student student;
    private final MovieSchedule schedule;
    private final Stage parentStage;
    private final HallService hallService;
    private final MovieService movieService;
    private final ReservationService reservationService;
    private String selectedSeatId = null;

    /**
     * Constructor
     * @param parentStage The parent stage
     * @param student The logged-in student
     * @param schedule The selected movie schedule
     */
    public SeatSelectionScreen(Stage parentStage, Student student, MovieSchedule schedule) {
        this.parentStage = parentStage;
        this.student = student;
        this.schedule = schedule;
        this.stage = new Stage();
        this.hallService = new HallService();
        this.movieService = new MovieService();
        this.reservationService = new ReservationService();
    }

    /**
     * Display the seat selection screen
     */
    public void show() {
        // Get movie and hall information
        Movie movie = movieService.findMovieById(schedule.getMovieId());
        Hall hall = hallService.findHallById(schedule.getHallId());

        if (movie == null || hall == null) {
            showErrorAlert("Error", "Movie or hall information not found");
            return;
        }

        // Set the stage title
        stage.setTitle("Select Seat for " + movie.getTitle());

        // Create a border pane as the root
        BorderPane root = new BorderPane();

        // Create the top header
        VBox header = createHeader(movie, hall);

        // Create main content with seating chart
        ScrollPane content = createSeatingChart(hall);

        // Create bottom panel with legend and buttons
        VBox bottomPanel = createBottomPanel();

        // Add components to root
        root.setTop(header);
        root.setCenter(content);
        root.setBottom(bottomPanel);

        // Create the scene
        Scene scene = new Scene(root, 700, 600);

        // Add CSS
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        // Set the scene
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Create the header
     * @param movie The movie
     * @param hall The hall
     * @return VBox containing the header elements
     */
    private VBox createHeader(Movie movie, Hall hall) {
        VBox header = new VBox(10);
        header.setPadding(new Insets(15, 12, 15, 12));
        header.setStyle("-fx-background-color: #336699;");

        // Title
        Label title = new Label(movie.getTitle());
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setStyle("-fx-text-fill: white;");

        // Movie details
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        Label details = new Label(String.format(
                "Date: %s | Hall: %s | Price: $%.2f",
                schedule.getStartTime().format(formatter),
                hall.getName(),
                schedule.getPrice()
        ));
        details.setStyle("-fx-text-fill: white;");

        header.getChildren().addAll(title, details);

        return header;
    }

    /**
     * Create the seating chart
     * @param hall The hall
     * @return ScrollPane containing the seating chart
     */
    private ScrollPane createSeatingChart(Hall hall) {
        // Get all seats in the hall
        List<HallGraph.Seat> seats = hallService.getSeatsInHall(hall.getId());

        // Get reserved seats for this schedule
        List<String> reservedSeats = reservationService.getReservedSeats(schedule.getId());

        // Create grid pane for seats
        GridPane seatingGrid = new GridPane();
        seatingGrid.setAlignment(Pos.CENTER);
        seatingGrid.setHgap(10);
        seatingGrid.setVgap(10);
        seatingGrid.setPadding(new Insets(25));

        // Add screen at the top
        Rectangle screen = new Rectangle(400, 20);
        screen.setFill(Color.LIGHTGRAY);
        screen.setStroke(Color.BLACK);

        Label screenLabel = new Label("SCREEN");
        screenLabel.setAlignment(Pos.CENTER);

        VBox screenBox = new VBox(5);
        screenBox.setAlignment(Pos.CENTER);
        screenBox.getChildren().addAll(screen, screenLabel);

        seatingGrid.add(screenBox, 0, 0, 10, 1); // Span across 10 columns

        // Group seats by row
        int maxRow = 0;
        int maxCol = 0;

        for (HallGraph.Seat seat : seats) {
            maxRow = Math.max(maxRow, seat.getRow());
            maxCol = Math.max(maxCol, seat.getColumn());
        }

        // Add row labels
        for (int i = 0; i <= maxRow; i++) {
            char rowLabel = (char) ('A' + i);
            Label label = new Label(String.valueOf(rowLabel));
            label.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            seatingGrid.add(label, 0, i + 2); // +2 to account for the screen and gap
        }

        // Create seat buttons
        for (HallGraph.Seat seat : seats) {
            Button seatButton = new Button(seat.getId());
            seatButton.setPrefSize(40, 40);

            // Check if seat is reserved
            boolean isReserved = reservedSeats != null && reservedSeats.contains(seat.getId());

            if (isReserved) {
                seatButton.setStyle("-fx-background-color: #ff6666;"); // Red for reserved
                seatButton.setDisable(true);
            } else {
                // Set color based on seat type
                switch (seat.getType()) {
                    case "premium":
                        seatButton.setStyle("-fx-background-color: #ffcc66;"); // Gold for premium
                        break;
                    case "accessible":
                        seatButton.setStyle("-fx-background-color: #99ccff;"); // Blue for accessible
                        break;
                    default:
                        seatButton.setStyle("-fx-background-color: #99cc99;"); // Green for standard
                }

                // Add click event
                final String seatId = seat.getId();
                seatButton.setOnAction(e -> handleSeatSelection(seatButton, seatId));
            }

            seatingGrid.add(seatButton, seat.getColumn() + 1, seat.getRow() + 2); // +1 for row label, +2 for screen
        }

        // Create scroll pane
        ScrollPane scrollPane = new ScrollPane(seatingGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);

        return scrollPane;
    }

    /**
     * Create the bottom panel with legend and buttons
     * @return VBox containing the bottom panel elements
     */
    private VBox createBottomPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(15));
        panel.setAlignment(Pos.CENTER);

        // Create legend
        HBox legend = new HBox(20);
        legend.setAlignment(Pos.CENTER);

        createLegendItem(legend, "#99cc99", "Standard");
        createLegendItem(legend, "#ffcc66", "Premium");
        createLegendItem(legend, "#99ccff", "Accessible");
        createLegendItem(legend, "#ff6666", "Reserved");

        // Selected seat info
        HBox selectionBox = new HBox(10);
        selectionBox.setAlignment(Pos.CENTER);

        Label selectionLabel = new Label("Selected Seat:");
        Text selectedSeatText = new Text("None");
        selectedSeatText.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        selectionBox.getChildren().addAll(selectionLabel, selectedSeatText);

        // Buttons
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);

        Button confirmButton = new Button("Confirm Reservation");
        confirmButton.setDisable(true);

        Button cancelButton = new Button("Cancel");

        buttonBox.getChildren().addAll(confirmButton, cancelButton);

        // Add everything to the panel
        panel.getChildren().addAll(legend, selectionBox, buttonBox);

        // Set actions
        confirmButton.setOnAction(e -> {
            if (selectedSeatId != null) {
                makeReservation(selectedSeatId);
            }
        });

        cancelButton.setOnAction(e -> {
            stage.close();
        });

        // Update button state when seat is selected
        updateSelectedSeat(selectedSeatText, confirmButton);

        return panel;
    }

    /**
     * Create a legend item
     * @param container The container to add the legend item to
     * @param color The color of the legend item
     * @param text The text for the legend item
     */
    private void createLegendItem(HBox container, String color, String text) {
        Rectangle rect = new Rectangle(20, 20);
        rect.setStyle("-fx-fill: " + color + "; -fx-stroke: black;");

        Label label = new Label(text);

        HBox item = new HBox(5);
        item.setAlignment(Pos.CENTER);
        item.getChildren().addAll(rect, label);

        container.getChildren().add(item);
    }

    /**
     * Handle seat selection
     * @param seatButton The clicked seat button
     * @param seatId The ID of the selected seat
     */
    private void handleSeatSelection(Button seatButton, String seatId) {
        // Clear previous selection if any
        if (selectedSeatId != null) {
            // Reset style for all seat buttons
            for (javafx.scene.Node node : ((GridPane) ((ScrollPane) stage.getScene().getRoot().lookup(".scroll-pane")).getContent()).getChildren()) {
                if (node instanceof Button && !node.isDisabled()) {
                    Button btn = (Button) node;
                    if (btn.getText().equals(selectedSeatId)) {
                        // Reset to original color based on seat type
                        HallGraph.Seat seat = hallService.getSeat(selectedSeatId);
                        if (seat != null) {
                            switch (seat.getType()) {
                                case "premium":
                                    btn.setStyle("-fx-background-color: #ffcc66;");
                                    break;
                                case "accessible":
                                    btn.setStyle("-fx-background-color: #99ccff;");
                                    break;
                                default:
                                    btn.setStyle("-fx-background-color: #99cc99;");
                            }
                        }
                    }
                }
            }
        }

        // Set new selection
        selectedSeatId = seatId;
        seatButton.setStyle("-fx-background-color: #6666ff;"); // Blue for selected

        // Update selected seat text and confirm button
        updateSelectedSeat(
                (Text) ((HBox) ((VBox) stage.getScene().getRoot().lookup(".vbox")).getChildren().get(1)).getChildren().get(1),
                (Button) ((HBox) ((VBox) stage.getScene().getRoot().lookup(".vbox")).getChildren().get(2)).getChildren().get(0)
        );
    }

    /**
     * Update the selected seat text and confirm button state
     * @param selectedSeatText The text to update
     * @param confirmButton The button to update
     */
    private void updateSelectedSeat(Text selectedSeatText, Button confirmButton) {
        if (selectedSeatId != null) {
            selectedSeatText.setText(selectedSeatId);
            confirmButton.setDisable(false);
        } else {
            selectedSeatText.setText("None");
            confirmButton.setDisable(true);
        }
    }

    /**
     * Make a reservation
     * @param seatId The ID of the selected seat
     */
    private void makeReservation(String seatId) {
        try {
            Reservation reservation = reservationService.makeReservation(student.getId(), schedule.getId(), seatId);

            if (reservation != null) {
                showSuccessAlert("Reservation Successful", "Your seat has been reserved successfully");
                stage.close();
                parentStage.close();

                // Refresh student dashboard
                StudentDashboard dashboard = new StudentDashboard(parentStage, student);
                dashboard.show();
            } else {
                showErrorAlert("Reservation Failed", "Failed to make reservation");
            }
        } catch (Exception e) {
            showErrorAlert("Reservation Error", e.getMessage());
        }
    }

    /**
     * Show a success alert
     * @param title Alert title
     * @param content Alert content
     */
    private void showSuccessAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
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
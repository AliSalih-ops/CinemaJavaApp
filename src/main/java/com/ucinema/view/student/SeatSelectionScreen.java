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
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
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
    private Text selectedSeatText; // Add this field to track the selected seat text element
    private Button confirmButton; // Add this field to track the confirm button

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
        // Force hall refresh to ensure we have all seats
        hallService.refreshAllHallSeats();

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

        // Create the scene - make it bigger to accommodate larger seat charts
        Scene scene = new Scene(root, 900, 700);

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
                "Date: %s | Hall: %s (%s) | Capacity: %d | Price: $%.2f",
                schedule.getStartTime().format(formatter),
                hall.getName(),
                hall.getType(),
                hall.getCapacity(),
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
        // Debug information
        System.out.println("DEBUG: Hall capacity: " + hall.getCapacity());
        System.out.println("DEBUG: Hall type: " + hall.getType());
        System.out.println("DEBUG: Hall name: " + hall.getName());
        System.out.println("DEBUG: Hall ID: " + hall.getId());

        // Get all seats in the hall
        List<HallGraph.Seat> seats = hallService.getSeatsInHall(hall.getId());
        System.out.println("Found " + seats.size() + " seats for hall ID " + hall.getId());

        // Get reserved seats for this schedule
        List<String> reservedSeats = reservationService.getReservedSeats(schedule.getId());
        System.out.println("Found " + (reservedSeats != null ? reservedSeats.size() : 0) + " reserved seats for schedule ID " + schedule.getId());

        // Create grid pane for seats
        GridPane seatingGrid = new GridPane();
        seatingGrid.setAlignment(Pos.CENTER);
        seatingGrid.setHgap(10);
        seatingGrid.setVgap(10);
        seatingGrid.setPadding(new Insets(25));

        // If there are no seats, add a message and generate them
        if (seats.isEmpty()) {
            Label noSeatsLabel = new Label("No seats found for this hall. Generating seats...");
            noSeatsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #cc0000;");
            seatingGrid.add(noSeatsLabel, 0, 1, 10, 1);

            // Add a progress indicator
            ProgressIndicator progress = new ProgressIndicator();
            progress.setPrefSize(50, 50);
            seatingGrid.add(progress, 0, 2, 10, 1);

            // Force generation of seats
            hallService.refreshAllHallSeats();

            // Get seats again after generation
            seats = hallService.getSeatsInHall(hall.getId());
            System.out.println("After generation: Found " + seats.size() + " seats for hall ID " + hall.getId());

            if (seats.isEmpty()) {
                Label errorLabel = new Label("Could not generate seats. Please check hall configuration.");
                errorLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #cc0000;");
                seatingGrid.add(errorLabel, 0, 3, 10, 1);
            }
        }

        // Group seats by row
        int maxRow = 0;
        int maxCol = 0;

        for (HallGraph.Seat seat : seats) {
            maxRow = Math.max(maxRow, seat.getRow());
            maxCol = Math.max(maxCol, seat.getColumn());
        }

        System.out.println("DEBUG: Max row: " + maxRow + ", Max column: " + maxCol);

        // Calculate dimensions for the seating grid
        double seatWidth = 40; // Width of each seat button
        double rowLabelWidth = 30; // Width for row labels
        double screenWidth = Math.max(400, (maxCol + 1) * seatWidth); // Screen width based on max columns

        // Add screen at the top with proper width
        Rectangle screen = new Rectangle(screenWidth, 20);
        screen.setFill(Color.LIGHTGRAY);
        screen.setStroke(Color.BLACK);

        Label screenLabel = new Label("SCREEN");
        screenLabel.setAlignment(Pos.CENTER);

        VBox screenBox = new VBox(5);
        screenBox.setAlignment(Pos.CENTER);
        screenBox.getChildren().addAll(screen, screenLabel);
        screenBox.setPrefWidth(screenWidth);

        // Add screen spanning across all columns plus row label column
        seatingGrid.add(screenBox, 0, 0, maxCol + 2, 1);

        // Add row labels
        for (int i = 0; i <= maxRow; i++) {
            char rowLabel = (char) ('A' + i);
            Label label = new Label(String.valueOf(rowLabel));
            label.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            label.setPrefWidth(rowLabelWidth);
            label.setAlignment(Pos.CENTER);
            seatingGrid.add(label, 0, i + 2); // +2 to account for the screen and gap
        }

        // Create seat buttons
        for (HallGraph.Seat seat : seats) {
            Button seatButton = new Button(seat.getId());
            seatButton.setPrefSize(seatWidth, seatWidth);

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

            // Add seat button to grid - ensure column index is correct
            seatingGrid.add(seatButton, seat.getColumn() + 1, seat.getRow() + 2); // +1 for row label, +2 for screen
        }

        // Create scroll pane with appropriate size
        ScrollPane scrollPane = new ScrollPane(seatingGrid);
        scrollPane.setFitToWidth(false); // Don't fit to width to prevent distortion
        scrollPane.setFitToHeight(false); // Don't fit to height to prevent distortion
        scrollPane.setPannable(true);
        scrollPane.setPrefViewportWidth(Math.min(800, (maxCol + 2) * (seatWidth + 10))); // Limit width to 800px
        scrollPane.setPrefViewportHeight(Math.min(500, (maxRow + 3) * (seatWidth + 10))); // Limit height to 500px

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
        createLegendItem(legend, "#6666ff", "Selected");

        // Selected seat info
        HBox selectionBox = new HBox(10);
        selectionBox.setAlignment(Pos.CENTER);
        selectionBox.setPadding(new Insets(10, 0, 10, 0));

        Label selectionLabel = new Label("Selected Seat:");
        selectionLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        // Store this reference so we can update it later
        selectedSeatText = new Text("None");
        selectedSeatText.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        selectedSeatText.setFill(Color.web("#00309c"));

        selectionBox.getChildren().addAll(selectionLabel, selectedSeatText);

        // Buttons
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);

        // Store this reference so we can enable/disable it later
        confirmButton = new Button("Confirm Reservation");
        confirmButton.setStyle("-fx-background-color: #00309c; -fx-text-fill: white; -fx-font-size: 14px;");
        confirmButton.setDisable(true);
        confirmButton.setOnAction(e -> {
            if (selectedSeatId != null) {
                makeReservation(selectedSeatId);
            }
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> stage.close());

        buttonBox.getChildren().addAll(confirmButton, cancelButton);

        // Add everything to the panel
        panel.getChildren().addAll(legend, selectionBox, buttonBox);

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
        // Reset previously selected seat if any
        if (selectedSeatId != null) {
            resetSeatButton(selectedSeatId);
        }

        // Set new selection
        selectedSeatId = seatId;
        seatButton.setStyle("-fx-background-color: #6666ff;"); // Blue for selected

        // Update the selected seat text
        if (selectedSeatText != null) {
            selectedSeatText.setText(seatId);
        }

        // Enable the confirm button
        if (confirmButton != null) {
            confirmButton.setDisable(false);
        }

        System.out.println("Selected seat: " + seatId);
    }

    /**
     * Reset a seat button to its original style
     * @param seatId The seat ID to reset
     */
    private void resetSeatButton(String seatId) {
        // Find the button in the grid
        ScrollPane scrollPane = (ScrollPane) ((BorderPane) stage.getScene().getRoot()).getCenter();
        GridPane grid = (GridPane) scrollPane.getContent();

        for (javafx.scene.Node node : grid.getChildren()) {
            if (node instanceof Button) {
                Button button = (Button) node;
                if (button.getText().equals(seatId)) {
                    // Set color based on seat type
                    HallGraph.Seat seat = hallService.getSeat(seatId);
                    if (seat != null) {
                        switch (seat.getType()) {
                            case "premium":
                                button.setStyle("-fx-background-color: #ffcc66;"); // Gold for premium
                                break;
                            case "accessible":
                                button.setStyle("-fx-background-color: #99ccff;"); // Blue for accessible
                                break;
                            default:
                                button.setStyle("-fx-background-color: #99cc99;"); // Green for standard
                        }
                    }
                    break;
                }
            }
        }
    }

    /**
     * Make a reservation
     * @param seatId The ID of the selected seat
     */
    private void makeReservation(String seatId) {
        try {
            System.out.println("Attempting to make reservation for seat: " + seatId);

            // Show a loading indicator
            BorderPane root = (BorderPane) stage.getScene().getRoot();

            StackPane loadingPane = new StackPane();
            loadingPane.setStyle("-fx-background-color: rgba(0,0,0,0.5);");

            VBox loadingBox = new VBox(10);
            loadingBox.setAlignment(Pos.CENTER);

            ProgressIndicator progress = new ProgressIndicator();
            progress.setPrefSize(60, 60);

            Label loadingLabel = new Label("Processing reservation...");
            loadingLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

            loadingBox.getChildren().addAll(progress, loadingLabel);
            loadingPane.getChildren().add(loadingBox);

            // Add loading overlay
            root.setCenter(loadingPane);

            // Create the reservation
            Reservation reservation = reservationService.makeReservation(student.getId(), schedule.getId(), seatId);

            if (reservation != null) {
                System.out.println("Reservation created successfully");
                showSuccessAlert("Reservation Successful", "Your seat has been reserved successfully");

                // Close this stage only
                stage.close();
            } else {
                // If reservation failed, restore the seating chart
                ScrollPane content = createSeatingChart(hallService.findHallById(schedule.getHallId()));
                root.setCenter(content);

                showErrorAlert("Reservation Failed", "Failed to make reservation. Please try again.");
            }
        } catch (Exception e) {
            e.printStackTrace();
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
package com.ucinema.view.admin;

import com.ucinema.model.datastructures.HallGraph;
import com.ucinema.model.entities.Hall;
import com.ucinema.service.HallService;

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
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

/**
 * Screen for viewing the seating chart of a hall.
 */
public class SeatingChartScreen {

    private final Stage stage;
    private final Hall hall;
    private final HallService hallService;

    /**
     * Constructor
     * @param parentStage The parent stage
     * @param hall The hall to display
     */
    public SeatingChartScreen(Stage parentStage, Hall hall) {
        this.hall = hall;
        this.hallService = new HallService();

        // Create a new modal stage
        this.stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(parentStage);

        // Set title
        stage.setTitle("Seating Chart: " + hall.getName());
    }

    /**
     * Display the seating chart screen
     */
    public void show() {
        // Create a border pane as the root
        BorderPane root = new BorderPane();

        // Create the top header
        VBox header = createHeader();

        // Create main content with seating chart
        ScrollPane content = createSeatingChart();

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
     * @return VBox containing the header elements
     */
    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(15, 12, 15, 12));
        header.setStyle("-fx-background-color: #336699;");

        // Title
        Label title = new Label("Seating Chart");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setStyle("-fx-text-fill: white;");

        // Hall details
        Label details = new Label(String.format(
                "Hall: %s | Capacity: %d | Type: %s | Location: %s",
                hall.getName(),
                hall.getCapacity(),
                hall.getType(),
                hall.getLocation()
        ));
        details.setStyle("-fx-text-fill: white;");

        header.getChildren().addAll(title, details);

        return header;
    }

    /**
     * Create the seating chart
     * @return ScrollPane containing the seating chart
     */
    private ScrollPane createSeatingChart() {
        // Get all seats in the hall
        List<HallGraph.Seat> seats = hallService.getSeatsInHall(hall.getId());

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

        // Create seat boxes
        for (HallGraph.Seat seat : seats) {
            Button seatButton = new Button(seat.getId());
            seatButton.setPrefSize(40, 40);

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

            // Add reserved status indicator
            if (seat.isReserved()) {
                seatButton.setStyle(seatButton.getStyle() + "; -fx-opacity: 0.5;");
                seatButton.setText(seat.getId() + " (X)");
            }

            seatButton.setOnAction(e -> showSeatDetails(seat));

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

        // Buttons
        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> stage.close());

        // Add everything to the panel
        panel.getChildren().addAll(legend, closeButton);

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
     * Show seat details
     * @param seat The seat
     */
    private void showSeatDetails(HallGraph.Seat seat) {
        String status = seat.isReserved() ? "Reserved" : "Available";

        showInfoAlert("Seat Details",
                "Seat ID: " + seat.getId() + "\n" +
                        "Type: " + seat.getType() + "\n" +
                        "Row: " + (seat.getRow() + 1) + "\n" +
                        "Column: " + (seat.getColumn() + 1) + "\n" +
                        "Status: " + status);
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
package com.ucinema.view.student;

import com.ucinema.model.entities.Movie;
import com.ucinema.model.entities.MovieSchedule;
import com.ucinema.model.entities.Reservation;
import com.ucinema.model.entities.Student;
import com.ucinema.service.MovieService;
import com.ucinema.service.MovieScheduleService;
import com.ucinema.service.ReservationService;
import com.ucinema.service.HallService;
import com.ucinema.view.LoginScreen;

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
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Dashboard for student users.
 */
public class StudentDashboard {

    private final Stage stage;
    private final Student student;
    private final MovieService movieService;
    private final ReservationService reservationService;
    private final HallService hallService;
    private final MovieScheduleService scheduleService;
    private ListView<Reservation> reservationListView;
    private ObservableList<Reservation> reservationList;

    /**
     * Constructor
     * @param stage The primary stage
     * @param student The logged-in student
     */
    public StudentDashboard(Stage stage, Student student) {
        this.stage = stage;
        this.student = student;
        this.movieService = new MovieService();
        this.reservationService = new ReservationService();
        this.hallService = new HallService();
        this.scheduleService = new MovieScheduleService();
    }

    /**
     * Display the student dashboard
     */
    public void show() {
        // Set the stage title with student name
        stage.setTitle("University Cinema - Welcome, " + student.getName());

        // Create a border pane as the root
        BorderPane root = new BorderPane();

        // Create the top header
        HBox header = createHeader();

        // Create a tab pane for different sections
        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add("dashboard-tabs");

        // Create tabs
        Tab movieTab = createMovieTab();
        Tab reservationTab = createReservationTab();
        Tab profileTab = createProfileTab();

        tabPane.getTabs().addAll(movieTab, reservationTab, profileTab);

        // Create footer
        HBox footer = createFooter();

        // Add components to root
        root.setTop(header);
        root.setCenter(tabPane);
        root.setBottom(footer);

        // Create the scene
        Scene scene = new Scene(root, 900, 700);

        // Add CSS
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/css/student-dashboard.css").toExternalForm());

        // Set the scene
        stage.setScene(scene);
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.show();
    }

    /**
     * Create the header bar
     * @return HBox containing the header elements
     */
    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setPadding(new Insets(12, 20, 12, 20));
        header.getStyleClass().add("dashboard-header");

        // Logo
        ImageView logoView = new ImageView(new Image(getClass().getResourceAsStream("/images/bau-logo1.jpg")));
        logoView.setFitHeight(40);
        logoView.setPreserveRatio(true);

        // Title
        Label title = new Label("University Cinema");
        title.getStyleClass().add("dashboard-title");

        // Student info
        HBox studentInfoBox = new HBox(5);
        studentInfoBox.setAlignment(Pos.CENTER);
        Label loggedInLabel = new Label("Logged in as:");
        loggedInLabel.getStyleClass().add("logged-in-label");

        Label studentNameLabel = new Label(student.getName());
        studentNameLabel.getStyleClass().add("student-name");

        studentInfoBox.getChildren().addAll(loggedInLabel, studentNameLabel);

        // Logout button
        Button logoutButton = new Button("Logout");
        logoutButton.getStyleClass().add("white-logout-button");
        logoutButton.setOnAction(e -> handleLogout());

        // Add spacing to push logout to the right
        HBox.setHgrow(studentInfoBox, Priority.ALWAYS);

        header.getChildren().addAll(logoView, title, studentInfoBox, logoutButton);
        header.setAlignment(Pos.CENTER_LEFT);

        return header;
    }

    /**
     * Create the footer bar
     * @return HBox containing the footer elements
     */
    private HBox createFooter() {
        HBox footer = new HBox();
        footer.setPadding(new Insets(10, 20, 10, 20));
        footer.setAlignment(Pos.CENTER);
        footer.getStyleClass().add("dashboard-footer");

        Label footerText = new Label("© 2025 Bahçeşehir University Cinema Hall Reservation System");
        footer.getChildren().add(footerText);

        return footer;
    }

    /**
     * Create the movie browsing tab
     * @return Tab for movie browsing
     */
    private Tab createMovieTab() {
        Tab tab = new Tab("Movies");
        tab.setClosable(false);

        BorderPane contentPane = new BorderPane();

        // Title and search area
        VBox topContent = new VBox(15);
        topContent.setPadding(new Insets(20, 20, 10, 20));

        Text title = new Text("Browse Movies");
        title.getStyleClass().add("content-title");

        // Search box
        HBox searchBox = new HBox(10);
        searchBox.getStyleClass().add("search-box");

        TextField searchField = new TextField();
        searchField.setPromptText("Search movies...");
        searchField.setPrefWidth(300);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        Button searchButton = new Button("Search");
        searchButton.getStyleClass().add("search-button");

        searchBox.getChildren().addAll(searchField, searchButton);

        topContent.getChildren().addAll(title, searchBox);

        // Movie list
        VBox centerContent = new VBox(15);
        centerContent.setPadding(new Insets(0, 20, 20, 20));

        ListView<Movie> movieListView = new ListView<>();
        movieListView.getStyleClass().add("movie-list");
        VBox.setVgrow(movieListView, Priority.ALWAYS);

        movieListView.setCellFactory(param -> new javafx.scene.control.ListCell<Movie>() {
            @Override
            protected void updateItem(Movie movie, boolean empty) {
                super.updateItem(movie, empty);

                if (empty || movie == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox cellContent = new VBox(5);
                    cellContent.setPadding(new Insets(5, 0, 5, 0));

                    Text title = new Text(movie.getTitle());
                    title.setFont(Font.font("System", FontWeight.BOLD, 14));
                    title.setFill(Color.web("#00309c"));

                    Text details = new Text(movie.getReleaseDate().getYear() + " | " +
                            movie.getDuration() + " min | " +
                            movie.getGenre());
                    details.setFill(Color.GRAY);

                    cellContent.getChildren().addAll(title, details);
                    setGraphic(cellContent);
                    setText(null);
                }
            }
        });

        // Load movies
        List<Movie> movies = movieService.getAllMovies();
        ObservableList<Movie> movieList = FXCollections.observableArrayList(movies);
        movieListView.setItems(movieList);

        // View schedules button
        Button viewSchedulesButton = new Button("View Schedules");
        viewSchedulesButton.getStyleClass().add("action-button");
        viewSchedulesButton.setDisable(true);
        viewSchedulesButton.setMaxWidth(Double.MAX_VALUE);

        centerContent.getChildren().addAll(movieListView, viewSchedulesButton);

        // Set actions
        movieListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> viewSchedulesButton.setDisable(newValue == null));

        searchButton.setOnAction(e -> {
            String searchText = searchField.getText().trim();
            if (!searchText.isEmpty()) {
                List<Movie> searchResults = movieService.searchMoviesByTitle(searchText);
                movieListView.setItems(FXCollections.observableArrayList(searchResults));
            } else {
                movieListView.setItems(movieList);
            }
        });

        viewSchedulesButton.setOnAction(e -> {
            Movie selectedMovie = movieListView.getSelectionModel().getSelectedItem();
            if (selectedMovie != null) {
                MovieScheduleScreen scheduleScreen = new MovieScheduleScreen(stage, student, selectedMovie);
                scheduleScreen.show();
            }
        });

        contentPane.setTop(topContent);
        contentPane.setCenter(centerContent);

        tab.setContent(contentPane);
        return tab;
    }

    /**
     * Create the reservations tab
     * @return Tab for reservations
     */
    private Tab createReservationTab() {
        Tab tab = new Tab("My Reservations");
        tab.setClosable(false);

        BorderPane contentPane = new BorderPane();

        // Title area
        VBox topContent = new VBox(15);
        topContent.setPadding(new Insets(20, 20, 10, 20));

        Text title = new Text("My Reservations");
        title.getStyleClass().add("content-title");

        // Refresh button
        Button refreshButton = new Button("Refresh Reservations");
        refreshButton.getStyleClass().add("refresh-button");
        refreshButton.setOnAction(e -> loadReservations());

        HBox titleBar = new HBox(20);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(titleBar, Priority.ALWAYS);
        titleBar.getChildren().addAll(title, refreshButton);

        topContent.getChildren().add(titleBar);

        // Reservations list
        VBox centerContent = new VBox(15);
        centerContent.setPadding(new Insets(0, 20, 20, 20));

        reservationListView = new ListView<>();
        reservationListView.getStyleClass().add("reservation-list");
        VBox.setVgrow(reservationListView, Priority.ALWAYS);

        // Custom cell factory to display reservation details
        reservationListView.setCellFactory(param -> new javafx.scene.control.ListCell<Reservation>() {
            @Override
            protected void updateItem(Reservation reservation, boolean empty) {
                super.updateItem(reservation, empty);

                if (empty || reservation == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox cellContent = new VBox(8);
                    cellContent.setPadding(new Insets(10, 5, 10, 5));
                    cellContent.getStyleClass().add("reservation-cell");

                    try {
                        // Get schedule information
                        MovieSchedule schedule = scheduleService.findScheduleById(reservation.getScheduleId());

                        // Get movie details
                        String movieTitle = "Unknown Movie";
                        if (schedule != null) {
                            Movie movie = movieService.findMovieById(schedule.getMovieId());
                            if (movie != null) {
                                movieTitle = movie.getTitle();
                            }
                        }

                        // Get hall details
                        String hallName = "Unknown Hall";
                        if (schedule != null) {
                            try {
                                hallName = hallService.findHallById(schedule.getHallId()).getName();
                            } catch (Exception ex) {
                                System.out.println("Could not find hall: " + ex.getMessage());
                            }
                        }

                        // Format date/time
                        String dateTime = "Unknown Time";
                        if (schedule != null && schedule.getStartTime() != null) {
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                            dateTime = schedule.getStartTime().format(formatter);
                        }

                        // Create labels
                        Label movieLabel = new Label(movieTitle);
                        movieLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
                        movieLabel.setTextFill(Color.web("#00309c"));

                        Label detailsLabel = new Label(String.format(
                                "Date/Time: %s | Hall: %s | Seat: %s",
                                dateTime, hallName, reservation.getSeatId()));
                        detailsLabel.setTextFill(Color.GRAY);

                        Label priceLabel = new Label(String.format("Price: $%.2f", reservation.getPrice()));

                        cellContent.getChildren().addAll(movieLabel, detailsLabel, priceLabel);
                    } catch (Exception e) {
                        // Fallback simple display if we can't get all details
                        Label simpleLabel = new Label(String.format(
                                "Reservation #%d - Seat: %s - $%.2f",
                                reservation.getId(), reservation.getSeatId(), reservation.getPrice()));
                        simpleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

                        Label timeLabel = new Label("Reserved on: " +
                                reservation.getReservationTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

                        cellContent.getChildren().addAll(simpleLabel, timeLabel);
                    }

                    setGraphic(cellContent);
                    setText(null);
                }
            }
        });

        // Load user's reservations
        loadReservations();

        // Cancel reservation button
        Button cancelButton = new Button("Cancel Selected Reservation");
        cancelButton.getStyleClass().add("action-button");
        cancelButton.setDisable(true);
        cancelButton.setMaxWidth(Double.MAX_VALUE);

        centerContent.getChildren().addAll(reservationListView, cancelButton);

        // Set actions
        reservationListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> cancelButton.setDisable(newValue == null));

        cancelButton.setOnAction(e -> {
            Reservation selectedReservation = reservationListView.getSelectionModel().getSelectedItem();
            if (selectedReservation != null) {
                // Show confirmation dialog
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Cancel Reservation");
                alert.setHeaderText("Cancel Reservation");
                alert.setContentText("Are you sure you want to cancel this reservation?");

                alert.showAndWait().ifPresent(response -> {
                    if (response == javafx.scene.control.ButtonType.OK) {
                        // Cancel the reservation
                        boolean success = reservationService.cancelReservation(selectedReservation.getId());
                        if (success) {
                            // Reload reservations
                            loadReservations();
                            showInfoAlert("Reservation Cancelled", "Your reservation has been cancelled successfully");
                        } else {
                            showErrorAlert("Error", "Failed to cancel reservation");
                        }
                    }
                });
            }
        });

        contentPane.setTop(topContent);
        contentPane.setCenter(centerContent);

        tab.setContent(contentPane);

        // Add a listener to refresh reservations when tab is selected
        tab.setOnSelectionChanged(event -> {
            if (tab.isSelected()) {
                loadReservations();
            }
        });

        return tab;
    }

    /**
     * Load the student's reservations
     */
    private void loadReservations() {
        List<Reservation> reservations = reservationService.getReservationsByStudent(student.getId());
        reservationList = FXCollections.observableArrayList(reservations);

        if (reservationListView != null) {
            reservationListView.setItems(reservationList);

            if (reservations.isEmpty()) {
                Label noReservationsLabel = new Label("You have no reservations");
                noReservationsLabel.setStyle("-fx-padding: 20px; -fx-font-style: italic;");
                reservationListView.setPlaceholder(noReservationsLabel);
            }
        }

        System.out.println("Loaded " + reservations.size() + " reservations for student " + student.getId());
    }

    /**
     * Create the profile tab
     * @return Tab for user profile
     */
    private Tab createProfileTab() {
        Tab tab = new Tab("My Profile");
        tab.setClosable(false);

        BorderPane contentPane = new BorderPane();

        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setAlignment(Pos.TOP_CENTER);

        // Title
        Text title = new Text("My Profile");
        title.getStyleClass().add("content-title");

        // Profile card
        VBox profileCard = new VBox(20);
        profileCard.getStyleClass().add("profile-card");
        profileCard.setPadding(new Insets(30));
        profileCard.setMaxWidth(500);

        // Profile icon
        ImageView profileIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/user_icon.png")));
        profileIcon.setFitHeight(80);
        profileIcon.setPreserveRatio(true);

        HBox iconBox = new HBox();
        iconBox.setAlignment(Pos.CENTER);
        iconBox.getChildren().add(profileIcon);

        // Profile information
        GridPane profileGrid = new GridPane();
        profileGrid.setHgap(20);
        profileGrid.setVgap(15);
        profileGrid.setAlignment(Pos.CENTER);

        // Name
        Label nameLabel = new Label("Name:");
        nameLabel.getStyleClass().add("profile-label");

        Label nameValue = new Label(student.getName());
        nameValue.getStyleClass().add("profile-value");

        // Email
        Label emailLabel = new Label("Email:");
        emailLabel.getStyleClass().add("profile-label");

        Label emailValue = new Label(student.getEmail());
        emailValue.getStyleClass().add("profile-value");

        // Student Number
        Label studentNumberLabel = new Label("Student Number:");
        studentNumberLabel.getStyleClass().add("profile-label");

        Label studentNumberValue = new Label(student.getStudentNumber());
        studentNumberValue.getStyleClass().add("profile-value");

        // Add to grid
        profileGrid.add(nameLabel, 0, 0);
        profileGrid.add(nameValue, 1, 0);
        profileGrid.add(emailLabel, 0, 1);
        profileGrid.add(emailValue, 1, 1);
        profileGrid.add(studentNumberLabel, 0, 2);
        profileGrid.add(studentNumberValue, 1, 2);

        profileCard.getChildren().addAll(iconBox, profileGrid);

        content.getChildren().addAll(title, profileCard);

        contentPane.setCenter(content);

        tab.setContent(contentPane);
        return tab;
    }

    /**
     * Handle logout button click
     */
    private void handleLogout() {
        // Show confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("Logout Confirmation");
        alert.setContentText("Are you sure you want to logout?");

        alert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                // Return to login screen
                LoginScreen loginScreen = new LoginScreen(stage);
                loginScreen.show();
            }
        });
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
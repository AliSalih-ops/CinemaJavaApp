package com.ucinema.view.admin;

import com.ucinema.model.entities.Hall;
import com.ucinema.model.entities.Movie;
import com.ucinema.model.entities.MovieSchedule;
import com.ucinema.model.entities.Student;
import com.ucinema.service.HallService;
import com.ucinema.service.MovieScheduleService;
import com.ucinema.service.MovieService;
import com.ucinema.service.StudentService;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.List;

/**
 * Dashboard for admin users.
 */
public class AdminDashboard {

    private final Stage stage;
    private final StudentService studentService;
    private final MovieService movieService;
    private final HallService hallService;
    private final MovieScheduleService scheduleService;

    /**
     * Constructor
     * @param stage The primary stage
     */
    public AdminDashboard(Stage stage) {
        this.stage = stage;
        this.studentService = new StudentService();
        this.movieService = new MovieService();
        this.hallService = new HallService();
        this.scheduleService = new MovieScheduleService();
    }

    /**
     * Display the admin dashboard
     */
    public void show() {
        // Set the stage title
        stage.setTitle("University Cinema - Admin Dashboard");

        // Create a border pane as the root
        BorderPane root = new BorderPane();

        // Create a tab pane for different sections
        TabPane tabPane = new TabPane();

        // Create tabs
        Tab moviesTab = createMoviesTab();
        Tab hallsTab = createHallsTab();
        Tab schedulesTab = createSchedulesTab();
        Tab studentsTab = createStudentsTab();

        tabPane.getTabs().addAll(moviesTab, hallsTab, schedulesTab, studentsTab);

        // Create the top header
        HBox header = createHeader();

        // Add components to root
        root.setTop(header);
        root.setCenter(tabPane);

        // Create the scene
        Scene scene = new Scene(root, 900, 700);

        // Add CSS
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        // Set the scene
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Create the header bar
     * @return HBox containing the header elements
     */
    private HBox createHeader() {
        HBox header = new HBox(10);
        header.setPadding(new Insets(15, 12, 15, 12));
        header.setStyle("-fx-background-color: #336699;");

        // Title
        Label title = new Label("University Cinema - Admin Panel");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setStyle("-fx-text-fill: white;");

        // Admin info
        Label adminInfo = new Label("Logged in as: Administrator");
        adminInfo.setStyle("-fx-text-fill: white;");

        // Logout button
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(e -> handleLogout());

        // Add spacing to push logout to the right
        HBox.setHgrow(adminInfo, Priority.ALWAYS);

        header.getChildren().addAll(title, adminInfo, logoutButton);
        header.setAlignment(Pos.CENTER_LEFT);

        return header;
    }

    /**
     * Create the movies management tab
     * @return Tab for movies management
     */
    private Tab createMoviesTab() {
        Tab tab = new Tab("Movies");
        tab.setClosable(false);

        VBox contentBox = new VBox(10);
        contentBox.setPadding(new Insets(10));

        // Title
        Text title = new Text("Movies Management");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        // Search box
        HBox searchBox = new HBox(10);
        TextField searchField = new TextField();
        searchField.setPromptText("Search movies...");
        searchField.setPrefWidth(300);
        Button searchButton = new Button("Search");

        searchBox.getChildren().addAll(searchField, searchButton);

        // Movies list
        ListView<Movie> movieListView = new ListView<>();
        VBox.setVgrow(movieListView, Priority.ALWAYS);

        // Action buttons
        HBox buttonBox = new HBox(10);
        Button addButton = new Button("Add Movie");
        Button editButton = new Button("Edit Movie");
        Button deleteButton = new Button("Delete Movie");

        // Disable edit/delete until movie is selected
        editButton.setDisable(true);
        deleteButton.setDisable(true);

        buttonBox.getChildren().addAll(addButton, editButton, deleteButton);

        // Load movies
        List<Movie> movies = movieService.getAllMovies();
        ObservableList<Movie> movieList = FXCollections.observableArrayList(movies);
        movieListView.setItems(movieList);

        // Set actions
        movieListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    editButton.setDisable(newValue == null);
                    deleteButton.setDisable(newValue == null);
                });

        searchButton.setOnAction(e -> {
            String searchText = searchField.getText().trim();
            if (!searchText.isEmpty()) {
                List<Movie> searchResults = movieService.searchMoviesByTitle(searchText);
                movieListView.setItems(FXCollections.observableArrayList(searchResults));
            } else {
                movieListView.setItems(movieList);
            }
        });

        addButton.setOnAction(e -> {
            MovieFormScreen formScreen = new MovieFormScreen(stage, null, this);
            formScreen.show();
        });

        editButton.setOnAction(e -> {
            Movie selectedMovie = movieListView.getSelectionModel().getSelectedItem();
            if (selectedMovie != null) {
                MovieFormScreen formScreen = new MovieFormScreen(stage, selectedMovie, this);
                formScreen.show();
            }
        });

        deleteButton.setOnAction(e -> {
            Movie selectedMovie = movieListView.getSelectionModel().getSelectedItem();
            if (selectedMovie != null) {
                deleteMovie(selectedMovie, movieList);
            }
        });

        contentBox.getChildren().addAll(title, searchBox, movieListView, buttonBox);

        // Create scroll pane
        ScrollPane scrollPane = new ScrollPane(contentBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        tab.setContent(scrollPane);
        return tab;
    }

    /**
     * Create the halls management tab
     * @return Tab for halls management
     */
    private Tab createHallsTab() {
        Tab tab = new Tab("Halls");
        tab.setClosable(false);

        VBox contentBox = new VBox(10);
        contentBox.setPadding(new Insets(10));

        // Title
        Text title = new Text("Halls Management");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        // Halls list
        ListView<Hall> hallListView = new ListView<>();
        VBox.setVgrow(hallListView, Priority.ALWAYS);

        // Action buttons
        HBox buttonBox = new HBox(10);
        Button addButton = new Button("Add Hall");
        Button editButton = new Button("Edit Hall");
        Button deleteButton = new Button("Delete Hall");
        Button viewSeatingButton = new Button("View Seating");

        // Disable buttons until hall is selected
        editButton.setDisable(true);
        deleteButton.setDisable(true);
        viewSeatingButton.setDisable(true);

        buttonBox.getChildren().addAll(addButton, editButton, deleteButton, viewSeatingButton);

        // Load halls
        List<Hall> halls = hallService.getAllHalls();
        ObservableList<Hall> hallList = FXCollections.observableArrayList(halls);
        hallListView.setItems(hallList);

        // Set actions
        hallListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    boolean disabled = newValue == null;
                    editButton.setDisable(disabled);
                    deleteButton.setDisable(disabled);
                    viewSeatingButton.setDisable(disabled);
                });

        addButton.setOnAction(e -> {
            HallFormScreen formScreen = new HallFormScreen(stage, null, this);
            formScreen.show();
        });

        editButton.setOnAction(e -> {
            Hall selectedHall = hallListView.getSelectionModel().getSelectedItem();
            if (selectedHall != null) {
                HallFormScreen formScreen = new HallFormScreen(stage, selectedHall, this);
                formScreen.show();
            }
        });

        deleteButton.setOnAction(e -> {
            Hall selectedHall = hallListView.getSelectionModel().getSelectedItem();
            if (selectedHall != null) {
                deleteHall(selectedHall, hallList);
            }
        });

        viewSeatingButton.setOnAction(e -> {
            Hall selectedHall = hallListView.getSelectionModel().getSelectedItem();
            if (selectedHall != null) {
                SeatingChartScreen seatingScreen = new SeatingChartScreen(stage, selectedHall);
                seatingScreen.show();
            }
        });

        contentBox.getChildren().addAll(title, hallListView, buttonBox);

        // Create scroll pane
        ScrollPane scrollPane = new ScrollPane(contentBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        tab.setContent(scrollPane);
        return tab;
    }

    /**
     * Create the schedules management tab
     * @return Tab for schedules management
     */
    private Tab createSchedulesTab() {
        Tab tab = new Tab("Schedules");
        tab.setClosable(false);

        VBox contentBox = new VBox(10);
        contentBox.setPadding(new Insets(10));

        // Title
        Text title = new Text("Movie Schedules Management");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        // Schedules list
        ListView<MovieSchedule> scheduleListView = new ListView<>();
        VBox.setVgrow(scheduleListView, Priority.ALWAYS);

        // Action buttons
        HBox buttonBox = new HBox(10);
        Button addButton = new Button("Add Schedule");
        Button editButton = new Button("Edit Schedule");
        Button deleteButton = new Button("Delete Schedule");
        Button viewBookingsButton = new Button("View Bookings");

        // Disable buttons until schedule is selected
        editButton.setDisable(true);
        deleteButton.setDisable(true);
        viewBookingsButton.setDisable(true);

        buttonBox.getChildren().addAll(addButton, editButton, deleteButton, viewBookingsButton);

        // Load schedules
        List<MovieSchedule> schedules = scheduleService.getAllSchedules();
        ObservableList<MovieSchedule> scheduleList = FXCollections.observableArrayList(schedules);
        scheduleListView.setItems(scheduleList);

        // Set actions
        scheduleListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    boolean disabled = newValue == null;
                    editButton.setDisable(disabled);
                    deleteButton.setDisable(disabled);
                    viewBookingsButton.setDisable(disabled);
                });

        addButton.setOnAction(e -> {
            ScheduleFormScreen formScreen = new ScheduleFormScreen(stage, null, this);
            formScreen.show();
        });

        editButton.setOnAction(e -> {
            MovieSchedule selectedSchedule = scheduleListView.getSelectionModel().getSelectedItem();
            if (selectedSchedule != null) {
                ScheduleFormScreen formScreen = new ScheduleFormScreen(stage, selectedSchedule, this);
                formScreen.show();
            }
        });

        deleteButton.setOnAction(e -> {
            MovieSchedule selectedSchedule = scheduleListView.getSelectionModel().getSelectedItem();
            if (selectedSchedule != null) {
                deleteSchedule(selectedSchedule, scheduleList);
            }
        });

        viewBookingsButton.setOnAction(e -> {
            MovieSchedule selectedSchedule = scheduleListView.getSelectionModel().getSelectedItem();
            if (selectedSchedule != null) {
                // Implement viewing bookings for a schedule
                showInfoAlert("Not Implemented", "Viewing bookings is not implemented in this demo");
            }
        });

        contentBox.getChildren().addAll(title, scheduleListView, buttonBox);

        // Create scroll pane
        ScrollPane scrollPane = new ScrollPane(contentBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        tab.setContent(scrollPane);
        return tab;
    }

    /**
     * Create the students management tab
     * @return Tab for students management
     */
    private Tab createStudentsTab() {
        Tab tab = new Tab("Students");
        tab.setClosable(false);

        VBox contentBox = new VBox(10);
        contentBox.setPadding(new Insets(10));

        // Title
        Text title = new Text("Students Management");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        // Search box
        HBox searchBox = new HBox(10);
        TextField searchField = new TextField();
        searchField.setPromptText("Search students by name...");
        searchField.setPrefWidth(300);
        Button searchButton = new Button("Search");

        searchBox.getChildren().addAll(searchField, searchButton);

        // Students list
        ListView<Student> studentListView = new ListView<>();
        VBox.setVgrow(studentListView, Priority.ALWAYS);

        // Action buttons
        HBox buttonBox = new HBox(10);
        Button viewButton = new Button("View Details");
        Button viewReservationsButton = new Button("View Reservations");

        // Disable buttons until student is selected
        viewButton.setDisable(true);
        viewReservationsButton.setDisable(true);

        buttonBox.getChildren().addAll(viewButton, viewReservationsButton);

        // Load students
        List<Student> students = studentService.getAllStudents();
        ObservableList<Student> studentList = FXCollections.observableArrayList(students);
        studentListView.setItems(studentList);

        // Set actions
        studentListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    boolean disabled = newValue == null;
                    viewButton.setDisable(disabled);
                    viewReservationsButton.setDisable(disabled);
                });

        searchButton.setOnAction(e -> {
            String searchText = searchField.getText().trim();
            if (!searchText.isEmpty()) {
                List<Student> searchResults = studentService.searchStudentsByName(searchText);
                studentListView.setItems(FXCollections.observableArrayList(searchResults));
            } else {
                studentListView.setItems(studentList);
            }
        });

        viewButton.setOnAction(e -> {
            Student selectedStudent = studentListView.getSelectionModel().getSelectedItem();
            if (selectedStudent != null) {
                // Implement viewing student details
                showInfoAlert("Student Details",
                        "Name: " + selectedStudent.getName() + "\n" +
                                "Email: " + selectedStudent.getEmail() + "\n" +
                                "Student Number: " + selectedStudent.getStudentNumber());
            }
        });

        viewReservationsButton.setOnAction(e -> {
            Student selectedStudent = studentListView.getSelectionModel().getSelectedItem();
            if (selectedStudent != null) {
                // Implement viewing student reservations
                showInfoAlert("Not Implemented", "Viewing student reservations is not implemented in this demo");
            }
        });

        contentBox.getChildren().addAll(title, searchBox, studentListView, buttonBox);

        // Create scroll pane
        ScrollPane scrollPane = new ScrollPane(contentBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        tab.setContent(scrollPane);
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
     * Delete a movie
     * @param movie The movie to delete
     * @param movieList The observable list to update
     */
    private void deleteMovie(Movie movie, ObservableList<Movie> movieList) {
        // Show confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Movie");
        alert.setHeaderText("Delete " + movie.getTitle());
        alert.setContentText("Are you sure you want to delete this movie? This cannot be undone.");

        alert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                boolean deleted = movieService.deleteMovie(movie);

                if (deleted) {
                    movieList.remove(movie);
                    showInfoAlert("Movie Deleted", "Movie has been deleted successfully");
                } else {
                    showErrorAlert("Delete Failed", "Failed to delete movie");
                }
            }
        });
    }

    /**
     * Delete a hall
     * @param hall The hall to delete
     * @param hallList The observable list to update
     */
    private void deleteHall(Hall hall, ObservableList<Hall> hallList) {
        // Show confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Hall");
        alert.setHeaderText("Delete " + hall.getName());
        alert.setContentText("Are you sure you want to delete this hall? This cannot be undone.");

        alert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                boolean deleted = hallService.deleteHall(hall);

                if (deleted) {
                    hallList.remove(hall);
                    showInfoAlert("Hall Deleted", "Hall has been deleted successfully");
                } else {
                    showErrorAlert("Delete Failed", "Failed to delete hall");
                }
            }
        });
    }

    /**
     * Delete a schedule
     * @param schedule The schedule to delete
     * @param scheduleList The observable list to update
     */
    private void deleteSchedule(MovieSchedule schedule, ObservableList<MovieSchedule> scheduleList) {
        // Show confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Schedule");
        alert.setHeaderText("Delete Schedule");
        alert.setContentText("Are you sure you want to delete this schedule? This cannot be undone.");

        alert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                boolean deleted = scheduleService.deleteSchedule(schedule);

                if (deleted) {
                    scheduleList.remove(schedule);
                    showInfoAlert("Schedule Deleted", "Schedule has been deleted successfully");
                } else {
                    showErrorAlert("Delete Failed", "Failed to delete schedule");
                }
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
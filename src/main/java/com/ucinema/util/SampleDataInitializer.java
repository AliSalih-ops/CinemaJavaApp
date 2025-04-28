package com.ucinema.util;

import com.ucinema.model.entities.Hall;
import com.ucinema.model.entities.Movie;
import com.ucinema.model.entities.MovieSchedule;
import com.ucinema.model.entities.Student;
import com.ucinema.service.HallService;
import com.ucinema.service.MovieScheduleService;
import com.ucinema.service.MovieService;
import com.ucinema.service.StudentService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Utility class for initializing sample data for the application.
 */
public class SampleDataInitializer {

    private final StudentService studentService;
    private final MovieService movieService;
    private final HallService hallService;
    private final MovieScheduleService scheduleService;

    /**
     * Constructor
     */
    public SampleDataInitializer() {
        this.studentService = new StudentService();
        this.movieService = new MovieService();
        this.hallService = new HallService();
        this.scheduleService = new MovieScheduleService();
    }

    /**
     * Initialize sample data
     */
    public void initialize() {
        System.out.println("Starting sample data initialization...");
        createSampleStudents();

        // Check if movies already exist before creating new ones
        List<Movie> existingMovies = movieService.getAllMovies();
        if (existingMovies == null || existingMovies.isEmpty()) {
            System.out.println("No existing movies found, creating sample movies...");
            createSampleMovies();
        } else {
            System.out.println("Found " + existingMovies.size() + " existing movies, skipping movie creation.");
        }

        // Check if halls already exist before creating new ones
        List<Hall> existingHalls = hallService.getAllHalls();
        if (existingHalls == null || existingHalls.isEmpty()) {
            System.out.println("No existing halls found, creating sample halls...");
            createSampleHalls();
        } else {
            System.out.println("Found " + existingHalls.size() + " existing halls, skipping hall creation.");
        }

        // Force reload movies and halls before creating schedules
        List<Movie> movies = movieService.getAllMovies();
        List<Hall> halls = hallService.getAllHalls();

        System.out.println("After initialization: " + movies.size() + " movies and " + halls.size() + " halls available");

        if ((movies == null || movies.isEmpty()) || (halls == null || halls.isEmpty())) {
            System.out.println("ERROR: Could not create movies or halls. Schedules cannot be created.");
        } else {
            createSampleSchedules();
        }

        System.out.println("Sample data initialization completed.");
    }

    /**
     * Create sample students
     */
    private void createSampleStudents() {
        System.out.println("Creating sample students...");

        // Create admin student
        try {
            Student admin = studentService.registerStudent(
                    "Admin User",
                    "admin@university.edu",
                    "admin123",
                    "ADMIN001");
            if (admin != null) {
                System.out.println("Created admin user: " + admin.getName());
            }
        } catch (Exception e) {
            System.out.println("Admin already exists: " + e.getMessage());
        }

        // Create regular students
        try {
            Student student1 = studentService.registerStudent(
                    "John Smith",
                    "john.smith@university.edu",
                    "password123",
                    "S12345");
            if (student1 != null) {
                System.out.println("Created student: " + student1.getName());
            }
        } catch (Exception e) {
            System.out.println("Student already exists: " + e.getMessage());
        }

        try {
            Student student2 = studentService.registerStudent(
                    "Emma Johnson",
                    "emma.johnson@university.edu",
                    "password123",
                    "S12346");
            if (student2 != null) {
                System.out.println("Created student: " + student2.getName());
            }
        } catch (Exception e) {
            System.out.println("Student already exists: " + e.getMessage());
        }

        try {
            Student student3 = studentService.registerStudent(
                    "Michael Brown",
                    "michael.brown@university.edu",
                    "password123",
                    "S12347");
            if (student3 != null) {
                System.out.println("Created student: " + student3.getName());
            }
        } catch (Exception e) {
            System.out.println("Student already exists: " + e.getMessage());
        }
    }

    /**
     * Create sample movies
     */
    private void createSampleMovies() {
        System.out.println("Creating sample movies...");

        // Create movies
        Movie movie1 = createMovie(
                "The Matrix",
                "A computer hacker learns from mysterious rebels about the true nature of his reality and his role in the war against its controllers.",
                136,
                LocalDate.of(1999, 3, 31),
                "Science Fiction",
                "Lana Wachowski, Lilly Wachowski",
                "R");

        Movie movie2 = createMovie(
                "Inception",
                "A thief who steals corporate secrets through the use of dream-sharing technology is given the inverse task of planting an idea into the mind of a C.E.O.",
                148,
                LocalDate.of(2010, 7, 16),
                "Science Fiction",
                "Christopher Nolan",
                "PG-13");

        Movie movie3 = createMovie(
                "The Shawshank Redemption",
                "Two imprisoned men bond over a number of years, finding solace and eventual redemption through acts of common decency.",
                142,
                LocalDate.of(1994, 9, 23),
                "Drama",
                "Frank Darabont",
                "R");

        Movie movie4 = createMovie(
                "The Dark Knight",
                "When the menace known as the Joker wreaks havoc and chaos on the people of Gotham, Batman must accept one of the greatest psychological and physical tests of his ability to fight injustice.",
                152,
                LocalDate.of(2008, 7, 18),
                "Action",
                "Christopher Nolan",
                "PG-13");

        Movie movie5 = createMovie(
                "Pulp Fiction",
                "The lives of two mob hitmen, a boxer, a gangster and his wife, and a pair of diner bandits intertwine in four tales of violence and redemption.",
                154,
                LocalDate.of(1994, 10, 14),
                "Crime",
                "Quentin Tarantino",
                "R");

        // Check if movies were created successfully
        List<Movie> createdMovies = movieService.getAllMovies();
        System.out.println("After movie creation: " + (createdMovies != null ? createdMovies.size() : 0) + " movies in database");
    }

    /**
     * Helper method to create a movie
     * @return The created movie or null if failed
     */
    private Movie createMovie(String title, String description, int duration,
                              LocalDate releaseDate, String genre, String director, String rating) {
        try {
            Movie movie = movieService.addMovie(
                    title, description, duration, releaseDate, genre, director, rating);

            if (movie != null) {
                System.out.println("Created movie: " + title + " (ID: " + movie.getId() + ")");
                return movie;
            } else {
                System.out.println("Failed to create movie: " + title);
            }
        } catch (Exception e) {
            System.out.println("Error creating movie " + title + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Create sample halls
     */
    private void createSampleHalls() {
        System.out.println("Creating sample halls...");

        // Create halls
        Hall hall1 = createHall("Main Hall", 200, "Main Building", "standard");
        Hall hall2 = createHall("IMAX Hall", 150, "Science Building", "IMAX");
        Hall hall3 = createHall("VIP Hall", 50, "Arts Building", "VIP");

        // Check if halls were created successfully
        List<Hall> createdHalls = hallService.getAllHalls();
        System.out.println("After hall creation: " + (createdHalls != null ? createdHalls.size() : 0) + " halls in database");
    }

    /**
     * Helper method to create a hall
     * @return The created hall or null if failed
     */
    private Hall createHall(String name, int capacity, String location, String type) {
        try {
            Hall hall = hallService.addHall(name, capacity, location, type);

            if (hall != null) {
                System.out.println("Created hall: " + name + " (ID: " + hall.getId() + ")");
                return hall;
            } else {
                System.out.println("Failed to create hall: " + name);
            }
        } catch (Exception e) {
            System.out.println("Error creating hall " + name + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Create sample schedules
     */
    private void createSampleSchedules() {
        System.out.println("Creating sample schedules...");

        // Get movies
        List<Movie> movies = movieService.getAllMovies();

        // Get halls
        List<Hall> halls = hallService.getAllHalls();

        if (movies.isEmpty() || halls.isEmpty()) {
            System.out.println("No movies or halls available. Cannot create schedules.");
            return;
        }

        System.out.println("Found " + movies.size() + " movies and " + halls.size() + " halls for scheduling");

        // Convert to arrays for easier indexing
        Movie[] movieArray = movies.toArray(new Movie[0]);
        Hall[] hallArray = halls.toArray(new Hall[0]);

        // Create schedules for the next 7 days
        LocalDate today = LocalDate.now();

        for (int day = 0; day < 7; day++) {
            LocalDate scheduleDate = today.plusDays(day);

            int movieIndex1 = day % movieArray.length;
            int movieIndex2 = (day + 1) % movieArray.length;
            int movieIndex3 = (day + 2) % movieArray.length;

            // Morning show (10:00 AM)
            if (hallArray.length > 0 && movieArray.length > movieIndex1) {
                createSchedule(
                        movieArray[movieIndex1].getId(),
                        hallArray[0].getId(),
                        scheduleDate,
                        LocalTime.of(10, 0),
                        10.00);
            }

            // Afternoon show (2:00 PM)
            if (hallArray.length > 1 && movieArray.length > movieIndex2) {
                createSchedule(
                        movieArray[movieIndex2].getId(),
                        hallArray[1].getId(),
                        scheduleDate,
                        LocalTime.of(14, 0),
                        12.00);
            }

            // Evening show (7:00 PM)
            if (hallArray.length > 2 && movieArray.length > movieIndex3) {
                createSchedule(
                        movieArray[movieIndex3].getId(),
                        hallArray[2].getId(),
                        scheduleDate,
                        LocalTime.of(19, 0),
                        15.00);
            }
        }

        // Check if schedules were created successfully
        List<MovieSchedule> createdSchedules = scheduleService.getAllSchedules();
        System.out.println("After schedule creation: " +
                (createdSchedules != null ? createdSchedules.size() : 0) + " schedules in database");
    }

    /**
     * Helper method to create a schedule
     */
    private void createSchedule(int movieId, int hallId, LocalDate date,
                                LocalTime time, double price) {
        try {
            LocalDateTime startTime = LocalDateTime.of(date, time);
            MovieSchedule schedule = scheduleService.addSchedule(movieId, hallId, startTime, price);

            if (schedule != null) {
                System.out.println("Created schedule: Movie ID " + movieId +
                        " at " + startTime + " in Hall ID " + hallId + " (ID: " + schedule.getId() + ")");
            } else {
                System.out.println("Failed to create schedule for Movie ID " + movieId +
                        " at " + startTime + " in Hall ID " + hallId);
            }
        } catch (Exception e) {
            System.out.println("Error creating schedule for Movie ID " + movieId +
                    " at " + LocalDateTime.of(date, time) + " in Hall ID " + hallId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    /**
     * Get the number of movies in the database
     * @return The number of movies
     */
    public int getMovieCount() {
        List<Movie> movies = movieService.getAllMovies();
        return movies != null ? movies.size() : 0;
    }

    /**
     * Get the number of halls in the database
     * @return The number of halls
     */
    public int getHallCount() {
        List<Hall> halls = hallService.getAllHalls();
        return halls != null ? halls.size() : 0;
    }

    /**
     * Get the number of schedules in the database
     * @return The number of schedules
     */
    public int getScheduleCount() {
        List<MovieSchedule> schedules = scheduleService.getAllSchedules();
        return schedules != null ? schedules.size() : 0;
    }
}
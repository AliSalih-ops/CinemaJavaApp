package com.ucinema.service;

import com.ucinema.dao.MovieDAO;
import com.ucinema.model.entities.Movie;

import java.time.LocalDate;
import java.util.List;

/**
 * Service class for movie-related operations.
 */
public class MovieService {
    private final MovieDAO movieDAO;

    public MovieService() {
        this.movieDAO = new MovieDAO();
    }

    /**
     * Add a new movie
     * @param title Movie title
     * @param description Movie description
     * @param duration Movie duration in minutes
     * @param releaseDate Movie release date
     * @param genre Movie genre
     * @param director Movie director
     * @param rating Movie rating
     * @return The added movie or null if operation failed
     */
    public Movie addMovie(String title, String description, int duration,
                          LocalDate releaseDate, String genre, String director, String rating) {
        Movie movie = new Movie(title, description, duration, releaseDate, genre, director, rating);
        return movieDAO.save(movie);
    }

    /**
     * Find a movie by ID
     * @param movieId The movie ID
     * @return The movie or null if not found
     */
    public Movie findMovieById(int movieId) {
        return movieDAO.findById(movieId);
    }

    /**
     * Update movie information
     * @param movie The movie to update
     * @return The updated movie
     */
    public Movie updateMovie(Movie movie) {
        return movieDAO.update(movie);
    }

    /**
     * Delete a movie
     * @param movie The movie to delete
     * @return True if successful
     */
    public boolean deleteMovie(Movie movie) {
        return movieDAO.delete(movie);
    }

    /**
     * Get all movies
     * @return List of all movies
     */
    public List<Movie> getAllMovies() {
        return movieDAO.findAll();
    }

    /**
     * Search movies by title
     * @param title The title to search for
     * @return List of matching movies
     */
    public List<Movie> searchMoviesByTitle(String title) {
        return movieDAO.searchByTitle(title);
    }

    /**
     * Find movies by genre
     * @param genre The genre to search for
     * @return List of movies in the specified genre
     */
    public List<Movie> findMoviesByGenre(String genre) {
        return movieDAO.findByGenre(genre);
    }

    /**
     * Find recently released movies
     * @param days Number of days to look back
     * @return List of recent movies
     */
    public List<Movie> findRecentMovies(int days) {
        return movieDAO.findRecentMovies(days);
    }

    /**
     * Find upcoming movies
     * @return List of upcoming movies
     */
    public List<Movie> findUpcomingMovies() {
        return movieDAO.findUpcomingMovies();
    }
}
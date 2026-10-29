public class Movie {
    private final int movieId;
    private final String movieName;
    private final String genre;
    private final int durationMinutes;

    public Movie(int movieId, String movieName, String genre, int durationMinutes) {
        this.movieId = movieId;
        this.movieName = movieName;
        this.genre = genre;
        this.durationMinutes = durationMinutes;
    }

    public int getMovieId() {
        return movieId;
    }

    public String getMovieName() {
        return movieName;
    }

    public String getGenre() {
        return genre;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }
}
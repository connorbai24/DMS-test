import java.io.IOException;
import java.util.List;

// Persistence port for high scores (Repository/DAO pattern)
public interface ScoreRepository {
    // Load all available scores (may be empty). Implementations should not return null.
    List<Integer> read() throws IOException;
    // Persist the provided list of scores (non-null). Implementations may overwrite the entire store.
    void write(List<Integer> scores) throws IOException;
}

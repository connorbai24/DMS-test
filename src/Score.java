import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Score {
	// keep only the top N scores
	private static final int TOP_N = 10;

	// list of scores (mutable, internal)
	private final List<Integer> highs;
	private final ScoreRepository repository;

    // constructor creates list of integers from a file
    public Score(String filename) {
        this(createFileRepository(filename));
    }

	// Alternate constructor for dependency injection/testing
    public Score(ScoreRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("repository must not be null");
        }
        this.repository = repository;
        this.highs = new ArrayList<Integer>();
		// Read existing high scores; tolerate missing/corrupt lines and IO failures
		try {
			List<Integer> loaded = repository.read();
			if (loaded != null) {
				for (Integer v : loaded) {
					if (highs.size() >= TOP_N) break;
					highs.add(v == null ? 0 : v);
				}
			}
		} catch (IOException e) {
			// ignore; start with defaults below
		}
		// Ensure we always have TOP_N entries to match UI expectations
		while (highs.size() < TOP_N) {
			highs.add(0);
		}
	}

	// adds a new score into the list, sorts the list,
	// and updates the .txt file
    public void addHighScore(int score) throws IOException {
        highs.add(score);
		// sort descending in-place
		highs.sort(Collections.reverseOrder());
		// trim to TOP_N
		while (highs.size() > TOP_N) {
			highs.remove(highs.size() - 1);
		}

		// persist via repository
		repository.write(highs);
	}

	// returns a defensive copy of the list of high scores
	public List<Integer> getHighScores() {
		return new ArrayList<Integer>(highs);
	}

    // helper to validate and create repository while keeping 'this(...)' first in ctor
    private static ScoreRepository createFileRepository(String filename) {
        if (filename == null) {
            throw new IllegalArgumentException("filename must not be null");
        }
        return new FileScoreRepository(filename);
    }
}

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Score {
	// keep only the top N scores
	private static final int TOP_N = 10;

	// list of scores (mutable, internal)
	private final List<Integer> highs;
	private final String file;

	// constructor creates list of integers from .txt file
	public Score(String filename) {
		this.file = filename;
		this.highs = new ArrayList<Integer>();

		// Read existing high scores; tolerate missing/corrupt lines
		try (BufferedReader in = Files.newBufferedReader(Paths.get(filename), StandardCharsets.UTF_8)) {
			String r;
			int j = 0;
			while ((r = in.readLine()) != null && j < TOP_N) {
				r = r.trim();
				if (r.isEmpty()) {
					continue;
				}
				try {
					highs.add(Integer.parseInt(r));
					j++;
				} catch (NumberFormatException ignored) {
					// skip non-integer lines
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
	public void addHighScore(int i) throws IOException {
		highs.add(i);
		// sort descending in-place
		highs.sort(Collections.reverseOrder());
		// trim to TOP_N
		while (highs.size() > TOP_N) {
			highs.remove(highs.size() - 1);
		}

		// persist with explicit charset and platform line separator
		try (BufferedWriter out = Files.newBufferedWriter(Paths.get(file), StandardCharsets.UTF_8)) {
			for (int j = 0; j < highs.size(); j++) {
				out.write(Integer.toString(highs.get(j)));
				if (j != highs.size() - 1) {
					out.write(System.lineSeparator());
				}
			}
		}
	}

	// returns a defensive copy of the list of high scores
	public List<Integer> getHighScores() {
		return new ArrayList<Integer>(highs);
	}
}

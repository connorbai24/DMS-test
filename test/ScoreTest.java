import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScoreTest {

	private static Path writeTempFile(String... lines) throws IOException {
		Path tmp = Files.createTempFile("ScoreTest-", ".txt");
		tmp.toFile().deleteOnExit();
		Files.write(tmp, Arrays.asList(lines), StandardCharsets.UTF_8);
		return tmp;
	}

	@Test
	public void loadsAndPadsWhenEmpty() throws IOException {
		Path p = writeTempFile();
		Score s = new Score(p.toString());
		List<Integer> highs = s.getHighScores();
		assertEquals(10, highs.size());
		for (Integer v : highs) {
			assertEquals(Integer.valueOf(0), v);
		}
	}

	@Test
	public void parsesAndSkipsInvalidLines() throws IOException {
		Path p = writeTempFile("100", "", "abc", " 200 ", "  ", "300x", "400");
		Score s = new Score(p.toString());
		List<Integer> highs = s.getHighScores();
		// Expect the valid integers in order of appearance, then zeros padded to 10
		assertEquals(10, highs.size());
		assertEquals(Integer.valueOf(100), highs.get(0));
		assertEquals(Integer.valueOf(200), highs.get(1));
		assertEquals(Integer.valueOf(400), highs.get(2));
		for (int i = 3; i < 10; i++) {
			assertEquals(Integer.valueOf(0), highs.get(i));
		}
	}

	@Test
	public void addHighScoreSortsTrimsAndPersists() throws IOException {
		Path p = writeTempFile("10", "20", "30", "40", "50", "60", "70", "80");
		Score s = new Score(p.toString());
		s.addHighScore(25); // should be inserted and list sorted desc
		s.addHighScore(90); // becomes top
		List<Integer> highs = s.getHighScores();
		assertEquals(10, highs.size());
		// assert descending
		for (int i = 1; i < highs.size(); i++) {
			assertTrue(highs.get(i - 1) >= highs.get(i));
		}
		assertEquals(Integer.valueOf(90), highs.get(0));
		assertTrue(highs.contains(25));

		// persisted values match the in-memory list
		List<String> persisted = Files.readAllLines(p, StandardCharsets.UTF_8);
		List<String> expected = new ArrayList<String>();
		for (Integer v : highs) expected.add(Integer.toString(v));
		assertEquals(expected, persisted);
	}

	@Test
	public void getHighScoresReturnsDefensiveCopy() throws IOException {
		Path p = writeTempFile("1", "2", "3");
		Score s = new Score(p.toString());
		List<Integer> a = s.getHighScores();
		a.clear();
		List<Integer> b = s.getHighScores();
		assertEquals(10, b.size()); // internal state unchanged (still padded)
	}

	@Test(expected = IOException.class)
	public void addHighScorePropagatesIOException() throws IOException {
		// Create a directory and pass its path as if it were a file; writing will fail
		Path dir = Files.createTempDirectory("ScoreTestDir-");
		dir.toFile().deleteOnExit();
		Score s = new Score(dir.toString());
		s.addHighScore(123); // should throw when trying to write into a directory
	}

	@Test
	public void readsOnlyTopN() throws IOException {
		// 12 lines; only 10 should be loaded, then potentially trimmed/sorted on add
		String[] lines = new String[]{"1","2","3","4","5","6","7","8","9","10","11","12"};
		Path p = writeTempFile(lines);
		Score s = new Score(p.toString());
		List<Integer> highs = s.getHighScores();
		assertEquals(10, highs.size());
		assertEquals(Integer.valueOf(1), highs.get(0));
		assertEquals(Integer.valueOf(10), highs.get(9));
	}
}


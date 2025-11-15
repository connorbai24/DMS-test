import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScoreRepositoryTest {

    // Simple in-memory implementation to validate the interface contract.
    static class InMemoryScoreRepository implements ScoreRepository {
        private final List<Integer> store = new ArrayList<Integer>();

        public List<Integer> read() {
            return new ArrayList<Integer>(store); // defensive copy
        }

        public void write(List<Integer> scores) {
            if (scores == null) throw new IllegalArgumentException("scores");
            store.clear();
            store.addAll(scores);
        }
    }

    @Test
    public void roundTripPersistsValues() throws IOException {
        InMemoryScoreRepository repo = new InMemoryScoreRepository();
        assertTrue(repo.read().isEmpty());
        repo.write(Arrays.asList(5, 1, 3));
        assertEquals(Arrays.asList(5, 1, 3), repo.read());
    }

    @Test(expected = IllegalArgumentException.class)
    public void writeRejectsNull() throws IOException {
        InMemoryScoreRepository repo = new InMemoryScoreRepository();
        repo.write(null);
    }

    @Test
    public void readReturnsDefensiveCopy() throws IOException {
        InMemoryScoreRepository repo = new InMemoryScoreRepository();
        repo.write(Arrays.asList(1,2,3));
        List<Integer> a = repo.read();
        a.clear();
        assertEquals(Arrays.asList(1,2,3), repo.read());
    }
}


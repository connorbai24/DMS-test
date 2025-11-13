import java.io.IOException;
import java.util.List;

// Persistence port for high scores (Repository/DAO pattern)
public interface ScoreRepository {
	List<Integer> read() throws IOException;
	void write(List<Integer> scores) throws IOException;
}


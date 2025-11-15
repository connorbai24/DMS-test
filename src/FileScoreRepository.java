import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

// File-backed repository for high scores
public class FileScoreRepository implements ScoreRepository {

    private final String file;

    public FileScoreRepository(String file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("file path must not be null/empty");
        }
        this.file = file;
    }

    @Override
    public List<Integer> read() throws IOException {
        List<Integer> out = new ArrayList<Integer>();
        try (BufferedReader in = Files.newBufferedReader(Paths.get(file), StandardCharsets.UTF_8)) {
            String line;
            while ((line = in.readLine()) != null) {
                String r = line.trim();
                if (r.isEmpty()) continue;
                try {
                    out.add(Integer.parseInt(r));
                } catch (NumberFormatException ignored) {
                    // skip non-integer lines
                }
            }
        }
        return out;
    }

    @Override
    public void write(List<Integer> scores) throws IOException {
        if (scores == null) {
            throw new IllegalArgumentException("scores must not be null");
        }
        try (BufferedWriter out = Files.newBufferedWriter(Paths.get(file), StandardCharsets.UTF_8)) {
            for (int i = 0; i < scores.size(); i++) {
                out.write(Integer.toString(scores.get(i)));
                if (i != scores.size() - 1) {
                    out.write(System.lineSeparator());
                }
            }
        }
    }
}

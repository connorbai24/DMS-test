import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class FileScoreRepositoryTest {

    private static Path tempFile(String... lines) throws IOException {
        Path p = Files.createTempFile("fsr-", ".txt");
        p.toFile().deleteOnExit();
        if (lines != null && lines.length > 0) {
            Files.write(p, Arrays.asList(lines), StandardCharsets.UTF_8);
        }
        return p;
    }

    @Test(expected = IllegalArgumentException.class)
    public void ctorRejectsNullPath() {
        new FileScoreRepository(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ctorRejectsEmptyPath() {
        new FileScoreRepository("");
    }

    @Test
    public void readSkipsInvalidAndTrims() throws IOException {
        Path p = tempFile(" 10 ", "", "bogus", "20", "30x", "40");
        FileScoreRepository repo = new FileScoreRepository(p.toString());
        List<Integer> list = repo.read();
        assertEquals(Arrays.asList(10, 20, 40), list);
    }

    @Test
    public void writePersistsExactly() throws IOException {
        Path p = tempFile();
        FileScoreRepository repo = new FileScoreRepository(p.toString());
        List<Integer> src = Arrays.asList(90, 50, 10);
        repo.write(src);
        List<String> lines = Files.readAllLines(p, StandardCharsets.UTF_8);
        assertEquals(Arrays.asList("90", "50", "10"), lines);
    }

    @Test(expected = IllegalArgumentException.class)
    public void writeRejectsNullList() throws IOException {
        Path p = tempFile();
        FileScoreRepository repo = new FileScoreRepository(p.toString());
        repo.write(null);
    }

    @Test(expected = IOException.class)
    public void readThrowsOnMissingFile() throws IOException {
        FileScoreRepository repo = new FileScoreRepository("non-existent-" + System.nanoTime());
        repo.read();
    }

    @Test(expected = IOException.class)
    public void readThrowsOnDirectory() throws IOException {
        Path dir = Files.createTempDirectory("fsr-dir-");
        dir.toFile().deleteOnExit();
        FileScoreRepository repo = new FileScoreRepository(dir.toString());
        repo.read();
    }
}


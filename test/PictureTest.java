import org.junit.Test;
import static org.junit.Assert.*;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import javax.imageio.ImageIO;

public class PictureTest {
    static {
        // Ensure tests can run in environments without an X server
        System.setProperty("java.awt.headless", "true");
    }

    @Test
    public void classIsFinalAndHasPrivateCtor() {
        // class is final
        assertTrue(Modifier.isFinal(Picture.class.getModifiers()));
        // single private constructor
        Constructor<?>[] ctors = Picture.class.getDeclaredConstructors();
        assertEquals(1, ctors.length);
        assertTrue(Modifier.isPrivate(ctors[0].getModifiers()));
    }

    @Test
    public void drawRejectsNullGraphics() {
        try {
            Picture.draw(null, "over.png", 0, 0);
            fail("Expected IllegalArgumentException when Graphics is null");
        } catch (IllegalArgumentException e) {
            assertEquals("Graphics must not be null", e.getMessage());
        }
    }

    @Test
    public void drawRejectsNullFilepath() {
        BufferedImage canvas = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        Graphics g = canvas.getGraphics();
        try {
            Picture.draw(g, null, 0, 0);
            fail("Expected IllegalArgumentException when filepath is null");
        } catch (IllegalArgumentException e) {
            assertEquals("filepath must not be null or empty", e.getMessage());
        }
    }

    @Test
    public void drawRejectsEmptyFilepath() {
        BufferedImage canvas = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        Graphics g = canvas.getGraphics();
        try {
            Picture.draw(g, "", 0, 0);
            fail("Expected IllegalArgumentException when filepath is empty");
        } catch (IllegalArgumentException e) {
            assertEquals("filepath must not be null or empty", e.getMessage());
        }
    }

    @Test
    public void drawLoadsFromClasspathImages() throws Exception {
        // Load a known image from classpath to size the canvas
        try (InputStream is = Picture.class.getResourceAsStream("/images/over.png")) {
            assertNotNull("Test precondition: /images/over.png should exist on classpath", is);
            BufferedImage src = ImageIO.read(is);
            assertNotNull(src);

            // Start with a blank canvas
            BufferedImage canvas = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
            int before = countNonZeroPixels(canvas);
            assertEquals(0, before);

            // Draw by logical name (BASE_PATH should be applied internally)
            Graphics g = canvas.getGraphics();
            Picture.draw(g, "over.png", 0, 0);

            int after = countNonZeroPixels(canvas);
            assertTrue("Expected some pixels to change after drawing", after > 0);
        }
    }

    @Test
    public void internalsExposeExpectedConstants() throws Exception {
        // BASE_PATH should be a private static final String set to "/images/"
        Field basePath = Picture.class.getDeclaredField("BASE_PATH");
        assertTrue(Modifier.isPrivate(basePath.getModifiers()));
        assertTrue(Modifier.isStatic(basePath.getModifiers()));
        assertTrue(Modifier.isFinal(basePath.getModifiers()));
        basePath.setAccessible(true);
        assertEquals("/images/", basePath.get(null));

        // cache should be private static final
        Field cache = Picture.class.getDeclaredField("cache");
        assertTrue(Modifier.isPrivate(cache.getModifiers()));
        assertTrue(Modifier.isStatic(cache.getModifiers()));
        assertTrue(Modifier.isFinal(cache.getModifiers()));
    }

    @Test
    public void drawMissingResourceDoesNothing() {
        // a name that certainly does not exist in resources
        String missing = "missing-" + System.nanoTime() + ".png";
        BufferedImage canvas = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        int before = countNonZeroPixels(canvas);
        Graphics g = canvas.getGraphics();
        // should not throw and should not modify canvas
        Picture.draw(g, missing, 0, 0);
        int after = countNonZeroPixels(canvas);
        assertEquals(before, after);
    }

    @Test
    public void drawIgnoresExistingDiskFile() throws Exception {
        // Create a real image on disk that is NOT on classpath under /images.
        // With the previous fallback-to-file behavior this would draw something;
        // after the change (classpath-only), it must not draw.
        File tmp = File.createTempFile("PictureTest-", ".png");
        tmp.deleteOnExit();
        BufferedImage src = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
        src.setRGB(0, 0, 0xFF00FF00); // one visible pixel
        javax.imageio.ImageIO.write(src, "PNG", tmp);

        BufferedImage canvas = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
        int before = countNonZeroPixels(canvas);
        Graphics g = canvas.getGraphics();
        // Pass just the filename. If file-fallback existed, this would draw.
        Picture.draw(g, tmp.getName(), 0, 0);
        int after = countNonZeroPixels(canvas);
        assertEquals("Classpath-only loader should ignore disk files not on classpath", before, after);
    }

    private static int countNonZeroPixels(BufferedImage img) {
        int count = 0;
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                if ((img.getRGB(x, y) & 0xFFFFFFFF) != 0x00000000) {
                    count++;
                }
            }
        }
        return count;
    }
}

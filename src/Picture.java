
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * Provides methods to facilitate drawing images.
 */
public final class Picture {

	// Base classpath location for game images
	private static final String BASE_PATH = "/images/";

	// Utility class; prevent instantiation
	private Picture() {
		// no instances
	}
	
	/**
	 * Keep track of pictures that have already been drawn so that we don't have to load them every time.
	 */
	private static final Map<String, BufferedImage> cache = new HashMap<String, BufferedImage>();

	/**
	 * Draw an image.
	 *
	 * @param g The graphics context in which to draw the image.
	 * @param filepath The location of the image file.
	 * @param x The x-coordinate of where the upper-left corner of the image should be drawn.
	 * @param y The y-coordinate of where the upper-left corner of the image should be drawn.
	 */
	public static void draw(Graphics g, String filepath, int x, int y) {
		// Basic argument validation for clearer failures
		if (g == null) {
			throw new IllegalArgumentException("Graphics must not be null");
		}
		if (filepath == null || filepath.isEmpty()) {
			throw new IllegalArgumentException("filepath must not be null or empty");
		}

		try {
			BufferedImage img = cache.get(filepath); // rely on null to mean "not cached"
			if (img == null) {
				// Not cached yet; attempt to load from classpath
				InputStream is = Picture.class.getResourceAsStream(BASE_PATH + filepath);
				if (is != null) {
					// Ensure the stream is properly closed
					try (InputStream in = is) {
						img = ImageIO.read(in);
					}
					// Cache only successfully decoded images (non-null)
					if (img != null) {
						cache.put(filepath, img);
					}
				}
			}
			// If image couldn't be loaded from classpath, do not draw
			if (img == null) return;
			g.drawImage(img, x, y, null);
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}
	
	/**
	 * Clears the in-memory image cache. Primarily for tests or when assets change at runtime.
	 */
	public static void clearCache() {
		cache.clear();
	}
	
}

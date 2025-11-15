
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * Provides methods to facilitate drawing images.
 * Non-instantiable utility with a small LRU cache and safe I/O.
 */
public final class Picture {

	// Prevent instantiation
	private Picture() {}

	// Classpath base for images
	private static final String CLASSPATH_IMAGES = "/images/";

	// Bound the cache to avoid unbounded memory growth
	private static final int CACHE_CAPACITY = 64;

	// Simple LRU cache; synchronized wrapper for basic thread-safety
	private static final Map<String, BufferedImage> cache =
		Collections.synchronizedMap(new LinkedHashMap<String, BufferedImage>(16, 0.75f, true) {
			private static final long serialVersionUID = 1L;
			@Override
			protected boolean removeEldestEntry(Map.Entry<String, BufferedImage> eldest) {
				return size() > CACHE_CAPACITY;
			}
		});

	/**
	 * Draw an image.
	 *
	 * Note: First draw may perform I/O; subsequent draws hit the in-memory cache.
	 *
	 * @param g The graphics context in which to draw the image (ignored if null).
	 * @param filepath The image path (classpath relative file name under /images, or filesystem fallback).
	 * @param x The x-coordinate of where the upper-left corner of the image should be drawn.
	 * @param y The y-coordinate of where the upper-left corner of the image should be drawn.
	 */
	public static void draw(Graphics g, String filepath, int x, int y) {
		if (g == null || filepath == null || filepath.isEmpty()) {
			return; // avoid NPEs during painting
		}

		BufferedImage img = cache.get(filepath);
		if (img == null) {
			img = loadImage(filepath);
			if (img != null) {
				cache.put(filepath, img);
			} else {
				// Could not load; skip drawing quietly to avoid disrupting UI
				return;
			}
		}
		g.drawImage(img, x, y, null);
	}

	// Load from classpath (/images/...) first, then filesystem fallback
	private static BufferedImage loadImage(String filepath) {
		// Try classpath
		try (InputStream is = Picture.class.getResourceAsStream(CLASSPATH_IMAGES + filepath)) {
			if (is != null) {
				return ImageIO.read(is);
			}
		} catch (IOException e) {
			// Log succinctly; continue to filesystem fallback
			System.err.println("Failed to load image from classpath: " + filepath + " (" + e + ")");
		}
		// Filesystem fallback (kept for compatibility)
		try {
			File f = new File(filepath);
			if (f.exists()) {
				return ImageIO.read(f);
			}
		} catch (IOException e) {
			System.err.println("Failed to load image from file: " + filepath + " (" + e + ")");
		}
		return null;
	}
}


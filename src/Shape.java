
import java.awt.Graphics;
/**
 * Interface for shapes drawn on the court.
 */
public interface Shape {

	/**
	 * Checks if the shape is vertical.
	 * 
	 * @return true if the shape is vertical
	 */
	public boolean isVertical();
	
	/**
	 * Returns the starting X position of the shape.
	 * 
	 * @return the starting X coordinate
	 */
	public int getStartX();
	
	/**
	 * Returns the starting Y position of the shape.
	 * 
	 * @return the starting Y coordinate
	 */
	public int getStartY();
	
	/**
	 * Returns the ending X position of the shape.
	 * 
	 * @return the ending X coordinate
	 */
	public int getEndX();
	
	/**
	 * Returns the ending Y position of the shape.
	 * 
	 * @return the ending Y coordinate
	 */
	public int getEndY();

}

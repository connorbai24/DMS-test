/**
 * Represents a line segment of trails drawn on the court.
 * This class implements the Shape interface and is immutable.
 */
public class Line implements Shape {
	
	private final int x;
	private final int y;
	private final int x2;
	private final int y2;
	
	/**
	 * Creates a new Line segment with the specified start and end coordinates.
	 * 
	 * @param x the starting X coordinate
	 * @param y the starting Y coordinate
	 * @param x2 the ending X coordinate
	 * @param y2 the ending Y coordinate
	 */
	public Line(int x, int y, int x2, int y2) {
		this.x = x;
		this.y = y;
		this.x2 = x2;
		this.y2 = y2;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isVertical() {
		return (x == x2);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getStartX() {
		return x;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getStartY() {
		return y;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getEndX() {
		return x2;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getEndY() {
		return y2;
	}
}

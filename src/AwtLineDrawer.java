import java.awt.Graphics;

public final class AwtLineDrawer {
    private AwtLineDrawer() {}

    public static void draw(Line line, Graphics g) {
        g.drawLine(line.getStartX(), line.getStartY(), line.getEndX(), line.getEndY());
    }
}



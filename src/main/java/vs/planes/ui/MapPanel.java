package vs.planes.ui;

import vs.planes.model.Airport;
import vs.planes.model.Route;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.QuadCurve2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

public class MapPanel extends JPanel {
    private BufferedImage mapImage;
    private List<Route> selectedRoute;
    private final int WIDTH = 600;
    private final int HEIGHT = 600;
    private final List<Airport> airports;

    public MapPanel(List<Airport> airports) {
        this.airports = airports;
        loadMapImage();
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
    }

    private void loadMapImage() {
        try {
            // Load a simple world map image (you'll need to provide this)
            mapImage = ImageIO.read(getClass().getResourceAsStream("/worldmap.jpeg"));
        } catch (IOException e) {
            // If image loading fails, create a blank image
            mapImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = mapImage.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, WIDTH, HEIGHT);
            g.dispose();
        }
    }

    public void setSelectedRoute(List<Route> route) {
        this.selectedRoute = route;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw map
        g2.drawImage(mapImage, 0, 0, WIDTH, HEIGHT, null);

        // Draw all airports
        for (Airport airport : airports) {
            Point p = convertCoordinatesToPixels(airport.getLatitude(), airport.getLongitude());
            drawAirport(g2, p, airport, false);
        }

        // Draw selected route
        if (selectedRoute != null && !selectedRoute.isEmpty()) {
            g2.setStroke(new BasicStroke(2f));

            // Draw route lines
            for (int i = 0; i < selectedRoute.size(); i++) {
                Route route = selectedRoute.get(i);
                Point p1 = convertCoordinatesToPixels(
                        route.getOrigin().getLatitude(),
                        route.getOrigin().getLongitude()
                );
                Point p2 = convertCoordinatesToPixels(
                        route.getDestination().getLatitude(),
                        route.getDestination().getLongitude()
                );

                // Create gradient for route line
                GradientPaint gradient = new GradientPaint(
                        p1.x, p1.y, new Color(0, 150, 255, 180),
                        p2.x, p2.y, new Color(0, 0, 255, 180)
                );
                g2.setPaint(gradient);

                // Draw curved line
                drawCurvedLine(g2, p1, p2);
            }

            // Highlight airports in the route
            for (Route route : selectedRoute) {
                Point p1 = convertCoordinatesToPixels(
                        route.getOrigin().getLatitude(),
                        route.getOrigin().getLongitude()
                );
                Point p2 = convertCoordinatesToPixels(
                        route.getDestination().getLatitude(),
                        route.getDestination().getLongitude()
                );

                drawAirport(g2, p1, route.getOrigin(), true);
                drawAirport(g2, p2, route.getDestination(), true);
            }
        }
    }

    private void drawAirport(Graphics2D g2, Point p, Airport airport, boolean highlighted) {
        int size = highlighted ? 8 : 6;

        if (highlighted) {
            g2.setColor(new Color(255, 100, 100));
            g2.fill(new Ellipse2D.Double(p.x - size, p.y - size, size * 2, size * 2));
        } else {
            g2.setColor(new Color(100, 200, 200, 180));
            g2.fill(new Ellipse2D.Double(p.x - size / 2, p.y - size / 2, size, size));
        }

        // Draw airport code for highlighted airports
        if (highlighted) {
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            g2.drawString(airport.getCode(), p.x + 10, p.y + 4);
        }
    }

    private void drawCurvedLine(Graphics2D g2, Point p1, Point p2) {
        // Calculate control point for quadratic curve
        int controlX = (p1.x + p2.x) / 2;
        int controlY = Math.min(p1.y, p2.y) - 30; // Curve upward

        // Draw curved line
        g2.draw(new QuadCurve2D.Float(p1.x, p1.y, controlX, controlY, p2.x, p2.y));

        // Draw direction arrow
        drawArrow(g2, p1, p2);
    }

    private void drawArrow(Graphics2D g2, Point start, Point end) {
        double angle = Math.atan2(end.y - start.y, end.x - start.x);
        int arrowLength = 10;
        int arrowAngle = 25;

        // Calculate arrow points
        Point midPoint = new Point(
                (start.x + end.x) / 2,
                (start.y + end.y) / 2
        );

        int[] xPoints = new int[3];
        int[] yPoints = new int[3];

        xPoints[0] = midPoint.x + (int) (arrowLength * Math.cos(angle));
        yPoints[0] = midPoint.y + (int) (arrowLength * Math.sin(angle));

        xPoints[1] = midPoint.x + (int) (arrowLength * Math.cos(angle - Math.toRadians(arrowAngle)));
        yPoints[1] = midPoint.y + (int) (arrowLength * Math.sin(angle - Math.toRadians(arrowAngle)));

        xPoints[2] = midPoint.x + (int) (arrowLength * Math.cos(angle + Math.toRadians(arrowAngle)));
        yPoints[2] = midPoint.y + (int) (arrowLength * Math.sin(angle + Math.toRadians(arrowAngle)));

        g2.fillPolygon(xPoints, yPoints, 3);
    }

    private Point convertCoordinatesToPixels(double lat, double lon) {
        // Mercator projection compensation for latitude
        double latRad = Math.toRadians(lat);
        double mercatorY = Math.log(Math.tan((Math.PI / 4) + (latRad / 2)));

        // Calculate the pixel coordinates
        int x = (int) ((lon + 180) * (WIDTH / 360.0));
        int y = (int) ((HEIGHT / 2) - (HEIGHT * mercatorY / (2 * Math.PI)));

        return new Point(x, y);
    }

}

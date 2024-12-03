package vs.planes.utils;

import vs.planes.model.Airport;
import java.time.OffsetTime;

public class DistanceCalculator {
    private static final double EARTH_RADIUS_KM = 6371.0; // Earth's radius in kilometers
    private static final double AVG_SPEED_KM_H = 800.0;   // Average commercial aircraft speed

    /**
     * Calculates the distance between two airports using the Haversine formula
     * @param origin Origin airport
     * @param destination Destination airport
     * @return Distance in kilometers
     */
    public static double calculateDistance(Airport origin, Airport destination) {
        double dLat = Math.toRadians(destination.getLatitude() - origin.getLatitude());
        double dLon = Math.toRadians(destination.getLongitude() - origin.getLongitude());

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(origin.getLatitude())) *
                        Math.cos(Math.toRadians(destination.getLatitude())) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    /**
     * Calculates estimated flight duration in hours
     * @param distance Distance in kilometers
     * @return Duration in hours (includes 30 minutes for takeoff and landing)
     */
    public static double calculateFlightDuration(double distance) {
        // Add 0.5 hours (30 minutes) for takeoff and landing procedures
        return (distance / AVG_SPEED_KM_H) + 0.5;
    }

    /**
     * Calculates arrival time based on departure time and flight duration
     * @param departureTime Departure time
     * @param distance Distance in kilometers
     * @return Calculated arrival time
     */
    public static OffsetTime calculateArrivalTime(OffsetTime departureTime, double distance) {
        double flightDuration = calculateFlightDuration(distance);
        int hours = (int) flightDuration;
        int minutes = (int) ((flightDuration - hours) * 60);

        return departureTime.plusHours(hours).plusMinutes(minutes);
    }
}
package vs.planes.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Duration;
import java.time.OffsetTime;

@AllArgsConstructor
@Data
public class Route {
    private Airport origin;
    private Airport destination;
    private double cost;
    private OffsetTime departureTime;
    private OffsetTime arrivalTime;
    private PlaneType planeType;

    public Duration getDuration() {
        Duration duration = Duration.between(departureTime, arrivalTime);
        // If duration is negative, it means the flight arrives the next day
        if (duration.isNegative()) {
            duration = duration.plus(Duration.ofHours(24));
        }
        return duration;
    }
}

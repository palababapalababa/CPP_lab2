package vs.planes.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Airport {
    private String code;
    private String name;
    private double latitude;   // Positive for North, negative for South
    private double longitude;  // Positive for East, negative for West
}

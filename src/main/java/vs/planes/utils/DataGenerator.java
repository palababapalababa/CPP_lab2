package vs.planes.utils;

import vs.planes.model.Airport;
import vs.planes.model.PlaneType;
import vs.planes.model.Route;
import lombok.Getter;
import lombok.ToString;

import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Getter
@ToString
public class DataGenerator {

    @Getter
    private static final List<Airport> airports = new ArrayList<>();
    @Getter
    private static final List<Route> routes = new ArrayList<>();

    public DataGenerator() {
        generateAirports();
        generateRoutes();
    }

    public void generateRoutes() {
        for (int i = 0; i < 2000; i++) {
            Airport origin = airports.get((int) (Math.random() * airports.size()));
            Airport destination = airports.get((int) (Math.random() * airports.size()));

            while (origin.equals(destination)) {
                destination = airports.get((int) (Math.random() * airports.size()));
            }

            // Calculate distance between airports
            double distance = DistanceCalculator.calculateDistance(origin, destination);

            // Generate random departure time
            OffsetTime departureTime = OffsetTime.of(
                    (int) (Math.random() * 24),
                    (int) (Math.random() * 60),
                    0,
                    0,
                    ZoneOffset.of("+2")
            );

            // Calculate arrival time based on distance
            OffsetTime arrivalTime = DistanceCalculator.calculateArrivalTime(
                    departureTime,
                    distance
            );

            routes.add(new Route(
                    origin,
                    destination,
                    distance,
                    departureTime,
                    arrivalTime,
                    PlaneType.values()[(int) (Math.random() * PlaneType.values().length)]
            ));
        }
    }

    private void generateAirports() {
        airports.add(new Airport("PRG", "Prague Václav Havel", 50.1008, 14.2600));
        airports.add(new Airport("LHR", "London Heathrow", 51.4700, -0.4543));
        airports.add(new Airport("LGW", "London Gatwick", 51.1537, -0.1821));
        airports.add(new Airport("LCY", "London City", 51.5048, 0.0495));
        airports.add(new Airport("LTN", "London Luton", 51.8763, -0.3717));
        airports.add(new Airport("STN", "London Stansted", 51.8860, 0.2389));
        airports.add(new Airport("SEN", "London Southend", 51.5714, 0.6956));
        airports.add(new Airport("LWO", "Lviv Danylo Halytskyi", 49.8125, 23.9561));
        airports.add(new Airport("KBP", "Kyiv Boryspil", 50.3450, 30.8947));
        airports.add(new Airport("IEV", "Kyiv Zhuliany", 50.4016, 30.4492));
        airports.add(new Airport("ODS", "Odesa Central", 46.4268, 30.6764));
        airports.add(new Airport("DOK", "Donetsk Sergey Prokofiev", 48.0738, 37.7397));
        airports.add(new Airport("SIP", "Simferopol International", 45.0522, 33.9751));
        airports.add(new Airport("KIV", "Chișinău International", 46.9277, 28.9313));
        airports.add(new Airport("BUD", "Budapest Ferenc Liszt", 47.4298, 19.2611));
        airports.add(new Airport("DEB", "Debrecen International", 47.4889, 21.6154));
        airports.add(new Airport("KEF", "Keflavík International", 63.9850, -22.6056));
        airports.add(new Airport("RKV", "Reykjavík Domestic", 64.1300, -21.9406));
        airports.add(new Airport("BGO", "Bergen Flesland", 60.2934, 5.2180));
        airports.add(new Airport("OSL", "Oslo Gardermoen", 60.1975, 11.1004));
        airports.add(new Airport("SVG", "Stavanger Sola", 58.8767, 5.6378));
        airports.add(new Airport("TRD", "Trondheim Værnes", 63.4578, 10.9240));
        airports.add(new Airport("TOS", "Tromsø Langnes", 69.6832, 18.9189));
        airports.add(new Airport("HEL", "Helsinki Vantaa", 60.3172, 24.9633));
        airports.add(new Airport("TLL", "Tallinn Lennart Meri", 59.4133, 24.8328));
        airports.add(new Airport("RIX", "Riga International", 56.9235, 23.9711));
        airports.add(new Airport("VNO", "Vilnius International", 54.6341, 25.2858));
        airports.add(new Airport("CPH", "Copenhagen Kastrup", 55.6180, 12.6508));
        airports.add(new Airport("AAL", "Aalborg", 57.0928, 9.8492));
        airports.add(new Airport("AAR", "Aarhus", 56.3088, 10.6268));
        airports.add(new Airport("SYD", "Sydney Kingsford Smith", -33.9461, 151.1772));
        airports.add(new Airport("HND", "Tokyo Haneda", 35.5494, 139.7798));
        airports.add(new Airport("NRT", "Tokyo Narita", 35.7653, 140.3854));
        airports.add(new Airport("KIX", "Osaka Kansai", 34.4347, 135.2441));
        airports.add(new Airport("NGO", "Nagoya Chubu Centrair", 34.8583, 136.8053));
        airports.add(new Airport("JFK", "New York John F. Kennedy", 40.6413, -73.7781));
        airports.add(new Airport("LGA", "New York LaGuardia", 40.7769, -73.8740));
        airports.add(new Airport("EWR", "Newark Liberty", 40.6895, -74.1745));
        airports.add(new Airport("BOS", "Boston Logan", 42.3656, -71.0096));
        airports.add(new Airport("MIA", "Miami International", 25.7959, -80.2870));
        airports.add(new Airport("FLL", "Fort Lauderdale Hollywood", 26.0722, -80.1528));
        airports.add(new Airport("MCO", "Orlando International", 28.4312, -81.3081));
        airports.add(new Airport("TPA", "Tampa International", 27.9756, -82.5333));
        airports.add(new Airport("ATL", "Atlanta Hartsfield Jackson", 33.6407, -84.4277));
        airports.add(new Airport("ORD", "Chicago O'Hare", 41.9742, -87.9073));
        airports.add(new Airport("JNB", "Johannesburg O.R. Tambo", -26.1392, 28.2460));
        airports.add(new Airport("CPT", "Cape Town International", -33.9715, 18.6021));
        airports.add(new Airport("GRU", "São Paulo Guarulhos", -23.4356, -46.4731));
        airports.add(new Airport("GIG", "Rio de Janeiro Galeão", -22.8126, -43.2486));
        airports.add(new Airport("EZE", "Buenos Aires Ezeiza", -34.8220, -58.5358));
        airports.add(new Airport("AEP", "Buenos Aires Aeroparque", -34.5592, -58.4156));
        airports.add(new Airport("SCL", "Santiago Comodoro Arturo Merino Benítez", -33.3930, -70.7858));
    }
}

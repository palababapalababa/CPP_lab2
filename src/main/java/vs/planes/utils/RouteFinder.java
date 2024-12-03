package vs.planes.utils;

import vs.planes.model.Airport;
import vs.planes.model.Route;
import vs.planes.model.RouteCriteria;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.*;

public class RouteFinder {
    private static final int MAX_ROUTES = 10;
    private static final int MAX_TRANSFERS = 3;  // Maximum number of transfers allowed
    private static final Duration MIN_TRANSFER_TIME = Duration.ofMinutes(30);
    private static final Duration MAX_TRANSFER_TIME = Duration.ofHours(4);

    public static List<List<Route>> findRoutes(
            List<Route> routes,
            Airport origin,
            Airport destination,
            RouteCriteria criteria,
            OffsetDateTime departureTime) {

        // Create adjacency list for faster route lookup
        Map<Airport, List<Route>> routeMap = new HashMap<>();
        for (Route route : routes) {
            routeMap.computeIfAbsent(route.getOrigin(), k -> new ArrayList<>()).add(route);
        }

        // Priority queue to store partial paths
        PriorityQueue<PartialPath> queue = new PriorityQueue<>((p1, p2) -> {
            if (criteria == RouteCriteria.COST) {
                return Double.compare(p1.totalCost, p2.totalCost);
            } else {
                return p1.totalDuration.compareTo(p2.totalDuration);
            }
        });

        // Initialize with routes from origin
        List<Route> initialRoutes = routeMap.getOrDefault(origin, Collections.emptyList());
        for (Route route : initialRoutes) {
            queue.offer(new PartialPath(Collections.singletonList(route), departureTime.toOffsetTime()));

        }

        // Store found complete paths
        List<List<Route>> completePaths = new ArrayList<>();

        // Process queue
        while (!queue.isEmpty() && completePaths.size() < MAX_ROUTES) {
            PartialPath currentPath = queue.poll();
            Route lastRoute = currentPath.routes.get(currentPath.routes.size() - 1);

            // If we reached destination, add to complete paths
            if (lastRoute.getDestination().equals(destination)) {
                completePaths.add(new ArrayList<>(currentPath.routes));
                continue;
            }

            // If we haven't reached max transfers, explore more routes
            if (currentPath.routes.size() < MAX_TRANSFERS + 1) {
                List<Route> nextRoutes = routeMap.getOrDefault(lastRoute.getDestination(), Collections.emptyList());
                for (Route nextRoute : nextRoutes) {
                    if (isValidConnection(lastRoute, nextRoute)) {
                        List<Route> newRoutes = new ArrayList<>(currentPath.routes);
                        newRoutes.add(nextRoute);
                        queue.offer(new PartialPath(newRoutes, departureTime.toOffsetTime()));
                    }
                }
            }
        }

        return completePaths;
    }

    private static boolean isValidConnection(Route first, Route second) {
        // Prevent cycles
        if (second.getDestination().equals(first.getOrigin())) {
            return false;
        }

        // Check if transfer time is within acceptable range
        Duration transferTime = Duration.between(first.getArrivalTime(), second.getDepartureTime());
        if (transferTime.isNegative()) {
            // If negative, it means the flight is on the next day
            transferTime = transferTime.plus(Duration.ofHours(24));
        }

        return transferTime.compareTo(MIN_TRANSFER_TIME) >= 0 &&
                transferTime.compareTo(MAX_TRANSFER_TIME) <= 0;
    }

    private static class PartialPath {
        final OffsetTime departureTime;
        final List<Route> routes;
        final double totalCost;
        final Duration totalDuration;

        PartialPath(List<Route> routes, OffsetTime departureTime) {
            this.departureTime = departureTime;
            this.routes = routes;
            this.totalCost = calculateTotalCost();
            this.totalDuration = calculateTotalDuration();
        }

        private double calculateTotalCost() {
            return routes.stream().mapToDouble(Route::getCost).sum();
        }

        private Duration calculateTotalDuration() {
            Duration duration = Duration.ZERO;

            // Add waiting for the first flight
            Route firstRoute = routes.get(0);
            Duration waitingTime = Duration.between(departureTime, firstRoute.getDepartureTime());
            if (waitingTime.isNegative()) {
                waitingTime = waitingTime.plus(Duration.ofHours(24));
            }
            duration = duration.plus(waitingTime);

            // Add flight durations
            for (Route route : routes) {
                duration = duration.plus(route.getDuration());
            }

            // Add transfer times
            for (int i = 0; i < routes.size() - 1; i++) {
                Route current = routes.get(i);
                Route next = routes.get(i + 1);
                Duration transferTime = Duration.between(current.getArrivalTime(), next.getDepartureTime());
                if (transferTime.isNegative()) {
                    transferTime = transferTime.plus(Duration.ofHours(24));
                }
                duration = duration.plus(transferTime);
            }

            return duration;
        }
    }
}
package vs.planes.ui;

import vs.planes.model.Airport;
import vs.planes.model.Route;
import vs.planes.model.RouteCriteria;
import vs.planes.utils.DataGenerator;
import vs.planes.utils.RouteFinder;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Vector;

public class RouteFinderGUI extends JFrame {
    private final DataGenerator dataGenerator;
    private final JComboBox<Airport> originAirportCombo;
    private final JComboBox<Airport> destAirportCombo;
    private final JComboBox<RouteCriteria> criteriaCombo;
    private final JSpinner timeSpinner;
    private final JTable resultsTable;
    private final DefaultTableModel tableModel;
    private List<List<Route>> currentResults;
    private final MapPanel mapPanel;

    private OffsetDateTime departureTime;

    public RouteFinderGUI() {
        setTitle("Route Finder");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        Color mountainMist = new Color(152, 151, 156); // Темно-сірий
        Color cavernPink = new Color(217, 187, 189); // Рожевий
        Color oysterPink = new Color(234, 208, 209); // Світло-рожевий
        Color fairPink = new Color(246, 232, 231);   // Дуже світло-рожевий

        // Initialize data
        dataGenerator = new DataGenerator();

        // Create map panel
        mapPanel = new MapPanel(dataGenerator.getAirports());
        mapPanel.setBackground(fairPink);

        // Create panels
        JPanel topPanel = new JPanel(new GridLayout(2, 4, 10, 10));
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        topPanel.setBackground(oysterPink);
        buttonPanel.setBackground(fairPink);

        // Create components
        originAirportCombo = new JComboBox<>(dataGenerator.getAirports().toArray(new Airport[0]));
        destAirportCombo = new JComboBox<>(dataGenerator.getAirports().toArray(new Airport[0]));
        criteriaCombo = new JComboBox<>(RouteCriteria.values());

        SpinnerDateModel timeModel = new SpinnerDateModel();
        timeSpinner = new JSpinner(timeModel);
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm");
        timeSpinner.setEditor(timeEditor);

        JButton findRoutesButton = new JButton("Find Routes");
        JButton sortByCostButton = new JButton("Sort by Cost");
        JButton sortByDurationButton = new JButton("Sort by Duration");

        // Create table
        String[] columnNames = {"Route", "Total Cost", "Total Duration", "Transfers"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        resultsTable = new JTable(tableModel);
        resultsTable.setDefaultRenderer(Object.class, new MultiLineCellRenderer());
        resultsTable.setRowHeight(300);

        // Create split pane for map and results
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(mapPanel);
        splitPane.setRightComponent(new JScrollPane(resultsTable));
        splitPane.setResizeWeight(0.2);

        // Add selection listener to the table
        resultsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = resultsTable.getSelectedRow();
                if (selectedRow >= 0 && currentResults != null) {
                    mapPanel.setSelectedRoute(currentResults.get(selectedRow));
                }
            }
        });

        // Add components to panels
        topPanel.add(new JLabel("Origin Airport:"));
        topPanel.add(new JLabel("Destination Airport:"));
        topPanel.add(new JLabel("Criteria:"));
        topPanel.add(new JLabel("Departure Time:"));

        topPanel.add(originAirportCombo);
        topPanel.add(destAirportCombo);
        topPanel.add(criteriaCombo);
        topPanel.add(timeSpinner);

        buttonPanel.add(findRoutesButton);
        buttonPanel.add(sortByCostButton);
        buttonPanel.add(sortByDurationButton);

        // Add panels to frame
        add(topPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(splitPane, BorderLayout.SOUTH);

        // Add listeners
        findRoutesButton.addActionListener(e -> findRoutes());
        sortByCostButton.addActionListener(e -> sortResults(RouteCriteria.COST));
        sortByDurationButton.addActionListener(e -> sortResults(RouteCriteria.DURATION));

        // Set frame properties
        setSize(1200, 800);
        setLocationRelativeTo(null);
    }

    private void findRoutes() {
        Airport origin = (Airport) originAirportCombo.getSelectedItem();
        Airport destination = (Airport) destAirportCombo.getSelectedItem();
        RouteCriteria criteria = (RouteCriteria) criteriaCombo.getSelectedItem();

        if (origin.equals(destination)) {
            JOptionPane.showMessageDialog(this,
                    "Origin and destination airports must be different!",
                    "Invalid Selection",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Convert spinner time to OffsetDateTime
        departureTime = OffsetDateTime.now()
                .withHour(((java.util.Date) timeSpinner.getValue()).getHours())
                .withMinute(((java.util.Date) timeSpinner.getValue()).getMinutes())
                .withSecond(0)
                .withNano(0);

        currentResults = RouteFinder.findRoutes(
                dataGenerator.getRoutes(),
                origin,
                destination,
                criteria,
                departureTime
        );

        displayResults(currentResults);
    }

    private void sortResults(RouteCriteria criteria) {
        if (currentResults == null || currentResults.isEmpty()) {
            return;
        }

        if (criteria == RouteCriteria.COST) {
            currentResults.sort((r1, r2) -> Double.compare(
                    calculateTotalCost(r1),
                    calculateTotalCost(r2)
            ));
        } else {
            currentResults.sort((r1, r2) -> calculateTotalDuration(r1).compareTo(
                    calculateTotalDuration(r2)
            ));
        }

        displayResults(currentResults);
    }

    private void displayResults(List<List<Route>> routes) {
        tableModel.setRowCount(0);

        if (routes == null || routes.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No routes found!",
                    "Search Results",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        for (List<Route> route : routes) {
            Vector<Object> row = new Vector<>();

            // Format route description
            StringBuilder routeDesc = new StringBuilder("<html>");
            for (int i = 0; i < route.size(); i++) {
                Route segment = route.get(i);
                routeDesc.append(segment.getOrigin().getCode())
                        .append(" (")
                        .append(segment.getDepartureTime().format(timeFormatter))
                        .append(") → ")
                        .append(segment.getDestination().getCode())
                        .append(" (")
                        .append(segment.getArrivalTime().format(timeFormatter))
                        .append(")")
                        .append("<br>")
                        .append("Flight duration: ")
                        .append(formatDuration(segment.getDuration()))
                        .append("<br>")
                        .append("Plane type: ")
                        .append(segment.getPlaneType())
                        .append("<br>")
                        .append("Cost: ")
                        .append(String.format("%.2f", segment.getCost()));

                if (i < route.size() - 1) {
                    Duration layover = calculateLayover(segment, route.get(i + 1));
                    routeDesc.append("<br>Layover: ")
                            .append(formatDuration(layover))
                            .append("<br>---<br>");
                }
            }
            routeDesc.append("</html>");

            row.add(routeDesc.toString());
            row.add(String.format("%.2f", calculateTotalCost(route)));
            row.add(formatDuration(calculateTotalDuration(route)));
            row.add(route.size() - 1);

            tableModel.addRow(row);
        }
    }

    private double calculateTotalCost(List<Route> route) {
        return route.stream().mapToDouble(Route::getCost).sum();
    }

    private Duration calculateTotalDuration(List<Route> route) {
        Duration total = Duration.ZERO;

        // Add waiting for first flight
        Route first = route.get(0);
        Duration waiting = Duration.between(departureTime.toOffsetTime(), first.getDepartureTime());
        if (waiting.isNegative()) {
            waiting = waiting.plus(Duration.ofHours(24));
        }
        total = total.plus(waiting);

        // Add flight durations
        for (Route r : route) {
            total = total.plus(r.getDuration());
        }

        // Add layover times
        for (int i = 0; i < route.size() - 1; i++) {
            total = total.plus(calculateLayover(route.get(i), route.get(i + 1)));
        }

        return total;
    }

    private Duration calculateLayover(Route first, Route second) {
        Duration layover = Duration.between(first.getArrivalTime(), second.getDepartureTime());
        if (layover.isNegative()) {
            layover = layover.plus(Duration.ofHours(24));
        }
        return layover;
    }

    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        return String.format("%dh %dm", hours, minutes);
    }

    private static class MultiLineCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            label.setVerticalAlignment(JLabel.TOP);
            return label;
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            RouteFinderGUI gui = new RouteFinderGUI();
            gui.setVisible(true);
        });
    }
}